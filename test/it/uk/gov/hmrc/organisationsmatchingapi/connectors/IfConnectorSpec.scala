/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.uk.gov.hmrc.organisationsmatchingapi.connectors

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.{times, verify}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, HttpClient, InternalServerException}
import uk.gov.hmrc.organisationsmatchingapi.audit.AuditHelper
import uk.gov.hmrc.organisationsmatchingapi.connectors.IfConnector
import uk.gov.hmrc.organisationsmatchingapi.domain.integrationframework.common.IfAddress
import uk.gov.hmrc.organisationsmatchingapi.domain.integrationframework.ct.{IfCorpTaxCompanyDetails, IfNameAndAddressDetails, IfNameDetails}
import uk.gov.hmrc.organisationsmatchingapi.domain.integrationframework.sa.{IfSaTaxpayerDetails, IfSaTaxpayerNameAddress}
import uk.gov.hmrc.organisationsmatchingapi.domain.integrationframework.vat.{IfPPOB, IfVatApprovedInformation, IfVatCustomerAddress, IfVatCustomerDetails, IfVatCustomerInformation}
import uk.gov.hmrc.organisationsmatchingapi.domain.models.MatchingException
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import util.UnitSpec

import java.util.UUID
import scala.concurrent.ExecutionContext


class IfConnectorSpec
  extends AnyWordSpec
    with BeforeAndAfterEach
    with UnitSpec
    with MockitoSugar
    with Matchers
    with GuiceOneAppPerSuite {

  val stubPort: Int = sys.env.getOrElse("WIREMOCK", "11122").toInt
  val stubHost = "127.0.0.1"
  val wireMockServer = new WireMockServer(wireMockConfig().port(stubPort))
  val integrationFrameworkAuthorizationToken = "IF_TOKEN"
  val integrationFrameworkEnvironment = "IF_ENVIRONMENT"

  def externalServices: Seq[String] = Seq.empty

  override lazy val fakeApplication = new GuiceApplicationBuilder()
    .bindings(bindModules: _*)
    .configure(
      "auditing.enabled" -> false,
      "cache.enabled"  -> false,
      "microservice.services.integration-framework.host" -> "127.0.0.1",
      "microservice.services.integration-framework.port" -> "11122",
      "microservice.services.integration-framework.authorization-token" -> integrationFrameworkAuthorizationToken,
      "microservice.services.integration-framework.environment" -> integrationFrameworkEnvironment
    )
    .build()

  implicit val ec: ExecutionContext =
    fakeApplication.injector.instanceOf[ExecutionContext]

  trait Setup {
    val matchId = "80a6bb14-d888-436e-a541-4000674c60aa"
    val sampleCorrelationId = "188e9400-b636-4a3b-80ba-230a8c72b92a"
    val sampleCorrelationIdHeader: (String, String) = "CorrelationId" -> sampleCorrelationId

    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val request = FakeRequest().withHeaders(sampleCorrelationIdHeader)

    val config: ServicesConfig = fakeApplication.injector.instanceOf[ServicesConfig]
    val httpClient: HttpClient = fakeApplication.injector.instanceOf[HttpClient]
    val auditHelper: AuditHelper = mock[AuditHelper]

    val underTest = new IfConnector(config, httpClient, auditHelper)
  }

  override def beforeEach(): Unit = {
    wireMockServer.start()
    configureFor(stubHost, stubPort)
  }

  override def afterEach(): Unit = {
    wireMockServer.stop()
  }

  "fetch Corporation Tax" should {
    val crn = "12345678"
    val utr = "1234567890"

    "return data on successful call" in new Setup {

      val registeredDetails: IfNameAndAddressDetails = IfNameAndAddressDetails(
        Some(IfNameDetails(
          Some("Waitrose"),
          Some("And Partners")
        )),
        Some(IfAddress(
          Some("Alfie House"),
          Some("Main Street"),
          Some("Manchester"),
          Some("Londonberry"),
          Some("LN1 1AG")
        ))
      )

      val communicationDetails: IfNameAndAddressDetails = IfNameAndAddressDetails(
        Some(IfNameDetails(
          Some("Waitrose"),
          Some("And Partners")
        )),
        Some(IfAddress(
          Some("Orange House"),
          Some("Corporation Street"),
          Some("London"),
          Some("Londonberry"),
          Some("LN1 1AG")
        ))
      )

      Mockito.reset(underTest.auditHelper)

      stubFor(
        get(urlPathMatching(s"/organisations/corporation-tax/$crn/company/details"))
          .withHeader(HeaderNames.authorisation, equalTo(s"Bearer $integrationFrameworkAuthorizationToken"))
          .withHeader("Environment", equalTo(integrationFrameworkEnvironment))
          .withHeader("CorrelationId", equalTo(sampleCorrelationId))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withBody(
                Json.toJson(
                  IfCorpTaxCompanyDetails(
                    Some(utr),
                    Some(crn),
                    Some(registeredDetails),
                    Some(communicationDetails)
                  )).toString()
              )
          )
      )

      val result: IfCorpTaxCompanyDetails = await(underTest.fetchCorporationTax(matchId, crn))

      verify(underTest.auditHelper, times(1))
        .auditIfApiResponse(any(), any(), any(), any(), any())(any())

      result shouldBe IfCorpTaxCompanyDetails(
        Some(utr),
        Some(crn),
        Some(registeredDetails),
        Some(communicationDetails)
      )

    }

    "Fail when IF returns an error" in new Setup {

      Mockito.reset(underTest.auditHelper)

      stubFor(
        get(urlPathMatching(s"/organisations/corporation-tax/$crn/company/details"))
          .willReturn(aResponse().withStatus(500)))

      intercept[InternalServerException] {
        await(underTest.fetchCorporationTax(UUID.randomUUID().toString, "12345678"))
      }

      verify(underTest.auditHelper,
        times(1)).auditIfApiFailure(any(), any(), any(), any(), any())(any())

    }

    "Fail when IF returns a bad request" in new Setup {

      Mockito.reset(underTest.auditHelper)

      stubFor(
        get(urlPathMatching(s"/organisations/corporation-tax/$crn/company/details"))
          .willReturn(aResponse().withStatus(400).withBody("BAD_REQUEST")))

      intercept[InternalServerException] {
        await(underTest.fetchCorporationTax(UUID.randomUUID().toString, "12345678"))
      }

      verify(underTest.auditHelper,
        times(1)).auditIfApiFailure(any(), any(), any(), any(), any())(any())
    }

    "Fail when IF returns a NOT_FOUND" in new Setup {

      Mockito.reset(underTest.auditHelper)

      stubFor(
        get(urlPathMatching(s"/organisations/corporation-tax/$crn/company/details"))
          .willReturn(aResponse().withStatus(404).withBody("NOT_FOUND")))

      intercept[MatchingException] {
        await(underTest.fetchCorporationTax(UUID.randomUUID().toString, "12345678"))
      }
      verify(underTest.auditHelper, times(1))
        .auditIfApiFailure(any(), any(), any(), any(), any())(any())
    }
  }

  "fetch Self Assessment" should {
    val utr = "1234567890"

    "return data on successful request" in new Setup {

      val taxpayerJohnNameAddress: IfSaTaxpayerNameAddress = IfSaTaxpayerNameAddress(
        Some("John Smith II"),
        Some("Base"),
        Some(IfAddress(
          Some("Alfie House"),
          Some("Main Street"),
          Some("Birmingham"),
          Some("West Midlands"),
          Some("B14 6JH"),
        ))
      )

      val taxpayerJoanneNameAddress: IfSaTaxpayerNameAddress = IfSaTaxpayerNameAddress(
        Some("Joanne Smith"),
        Some("Correspondence"),
        Some(IfAddress(
          Some("Alfie House"),
          Some("Main Street"),
          Some("Birmingham"),
          Some("West Midlands"),
          Some("MC1 4AA"),
        ))
      )

      Mockito.reset(underTest.auditHelper)

      stubFor(
        get(urlPathMatching(s"/organisations/self-assessment/$utr/taxpayer/details"))
          .withHeader(
            HeaderNames.authorisation,
            equalTo(s"Bearer $integrationFrameworkAuthorizationToken"))
          .withHeader("Environment", equalTo(integrationFrameworkEnvironment))
          .withHeader("CorrelationId", equalTo(sampleCorrelationId))
          .willReturn(aResponse()
            .withStatus(200)
            .withBody(Json.toJson(IfSaTaxpayerDetails(
              Some(utr),
              Some("Individual"),
              Some(Seq(taxpayerJohnNameAddress, taxpayerJoanneNameAddress))
            )).toString())))

      val result: IfSaTaxpayerDetails = await(
        underTest.fetchSelfAssessment(UUID.randomUUID().toString, "1234567890")
      )

      verify(underTest.auditHelper, times(1))
        .auditIfApiResponse(any(), any(), any(), any(), any())(any())

      result shouldBe IfSaTaxpayerDetails(
        Some(utr),
        Some("Individual"),
        Some(Seq(taxpayerJohnNameAddress, taxpayerJoanneNameAddress))
      )

    }

    "Fail when IF returns an error" in new Setup {

      Mockito.reset(underTest.auditHelper)

      stubFor(
        get(urlPathMatching(s"/organisations/self-assessment/$utr/taxpayer/details"))
          .willReturn(aResponse().withStatus(500)))

      intercept[InternalServerException] {
        await(underTest.fetchSelfAssessment(UUID.randomUUID().toString, "1234567890"))
      }

      verify(underTest.auditHelper,
        times(1)).auditIfApiFailure(any(), any(), any(), any(), any())(any())

    }

    "Fail when IF returns a bad request" in new Setup {

      Mockito.reset(underTest.auditHelper)

      stubFor(
        get(urlPathMatching(s"/organisations/self-assessment/$utr/taxpayer/details"))
          .willReturn(aResponse().withStatus(400).withBody("BAD_REQUEST")))

      intercept[InternalServerException] {
        await(underTest.fetchSelfAssessment(UUID.randomUUID().toString, "1234567890"))
      }

      verify(underTest.auditHelper, times(1))
        .auditIfApiFailure(any(), any(), any(), any(), any())(any())
    }

    "Fail when IF returns a NOT_FOUND" in new Setup {

      Mockito.reset(underTest.auditHelper)

      stubFor(
        get(urlPathMatching(s"/organisations/self-assessment/$utr/taxpayer/details"))
          .willReturn(aResponse().withStatus(404).withBody("NOT_FOUND")))

      intercept[MatchingException] {
        await(underTest.fetchSelfAssessment(UUID.randomUUID().toString, "1234567890"))
      }
      verify(underTest.auditHelper,
        times(1)).auditIfApiFailure(any(), any(), any(), any(), any())(any())
    }
  }

  "fetch VAT" should {
    val vrn = "123456789"
    val vatUrl = s"/vat/customer/vrn/$vrn/information"

    "return data on successful request" in new Setup {
      Mockito.reset(underTest.auditHelper)

      val ifResponse = IfVatCustomerInformation(
        IfVatApprovedInformation(
          IfVatCustomerDetails(Some("orgName")),
          IfPPOB(Some(IfVatCustomerAddress(Some("line1"), Some("postcode"))))
        )
      )

      stubFor(
        get(urlPathMatching(vatUrl))
          .withHeader(
            HeaderNames.authorisation,
            equalTo(s"Bearer $integrationFrameworkAuthorizationToken"))
          .withHeader("Environment", equalTo(integrationFrameworkEnvironment))
          .withHeader("CorrelationId", equalTo(sampleCorrelationId))
          .willReturn(aResponse()
            .withStatus(200)
            .withBody(Json.toJson(ifResponse).toString())
          )
      )

      val result: IfVatCustomerInformation = await(underTest.fetchVat(UUID.randomUUID().toString, vrn))

      verify(underTest.auditHelper, times(1))
        .auditIfApiResponse(any(), any(), any(), any(), any())(any())

      result shouldBe ifResponse
    }

    "fail with InternalServerException when IF returns 500" in new Setup {
      Mockito.reset(underTest.auditHelper)

      stubFor(
        get(urlPathMatching(vatUrl))
          .willReturn(aResponse().withStatus(500)))

      intercept[InternalServerException] {
        await(underTest.fetchVat(UUID.randomUUID().toString, vrn))
      }

      verify(underTest.auditHelper, times(1))
        .auditIfApiFailure(any(), any(), any(), any(), any())(any())

    }

    "fail with InternalServerException when IF returns a bad request" in new Setup {
      Mockito.reset(underTest.auditHelper)

      stubFor(
        get(urlPathMatching(vatUrl))
          .willReturn(aResponse().withStatus(400).withBody("BAD_REQUEST")))

      intercept[InternalServerException] {
        await(underTest.fetchVat(UUID.randomUUID().toString, vrn))
      }

      verify(underTest.auditHelper,
        times(1)).auditIfApiFailure(any(), any(), any(), any(), any())(any())
    }

    "fail with MatchingException when IF returns a NOT_FOUND" in new Setup {
      Mockito.reset(underTest.auditHelper)

      stubFor(
        get(urlPathMatching(vatUrl))
          .willReturn(aResponse().withStatus(404).withBody("NOT_FOUND")))

      intercept[MatchingException] {
        await(underTest.fetchVat(UUID.randomUUID().toString, vrn))
      }
      verify(underTest.auditHelper,
        times(1)).auditIfApiFailure(any(), any(), any(), any(), any())(any())
    }

  }
}
