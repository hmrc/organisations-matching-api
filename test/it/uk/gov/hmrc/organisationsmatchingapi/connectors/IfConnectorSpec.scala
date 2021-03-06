/*
 * Copyright 2021 HM Revenue & Customs
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
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, InternalServerException, NotFoundException}
import uk.gov.hmrc.organisationsmatchingapi.audit.AuditHelper
import uk.gov.hmrc.organisationsmatchingapi.connectors.IfConnector
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

  val stubPort = sys.env.getOrElse("WIREMOCK", "11122").toInt
  val stubHost = "localhost"
  val wireMockServer = new WireMockServer(wireMockConfig().port(stubPort))
  val integrationFrameworkAuthorizationToken = "IF_TOKEN"
  val integrationFrameworkEnvironment = "IF_ENVIRONMENT"

  def externalServices: Seq[String] = Seq.empty

  override lazy val fakeApplication = new GuiceApplicationBuilder()
    .bindings(bindModules: _*)
    .configure(
      "cache.enabled"  -> false,
      "microservice.services.integration-framework.host" -> "localhost",
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
    val sampleCorrelationIdHeader: (String, String) = ("CorrelationId" -> sampleCorrelationId)

    implicit val hc: HeaderCarrier = HeaderCarrier()

    val config: ServicesConfig = fakeApplication.injector.instanceOf[ServicesConfig]
    val httpClient: HttpClient = fakeApplication.injector.instanceOf[HttpClient]
    val auditHelper: AuditHelper = mock[AuditHelper]

    val underTest = new IfConnector(config, httpClient, auditHelper)
  }

  override def beforeEach() {
    wireMockServer.start()
    configureFor(stubHost, stubPort)
  }

  override def afterEach() {
    wireMockServer.stop()
  }

  "fetch foo" should {
    val crn = "123456789"

    "Fail when IF returns an error" in new Setup {

      Mockito.reset(underTest.auditHelper)

      stubFor(
        get(urlPathMatching(s"/organisations/corporation-tax/$crn/company/details"))
          .willReturn(aResponse().withStatus(500)))

      intercept[InternalServerException] {
        await(
          underTest.fetchFoo(UUID.randomUUID().toString, "123456789")(
            hc,
            FakeRequest().withHeaders(sampleCorrelationIdHeader),
            ec
          )
        )
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
        await(
          underTest.fetchFoo(UUID.randomUUID().toString, "123456789")(
            hc,
            FakeRequest().withHeaders(sampleCorrelationIdHeader),
            ec
          )
        )
      }

      verify(underTest.auditHelper,
        times(1)).auditIfApiFailure(any(), any(), any(), any(), any())(any())
    }

    "Fail when IF returns a NOT_FOUND" in new Setup {

      Mockito.reset(underTest.auditHelper)

      stubFor(
        get(urlPathMatching(s"/organisations/corporation-tax/$crn/company/details"))
          .willReturn(aResponse().withStatus(404).withBody("NOT_FOUND")))

      intercept[NotFoundException] {
        await(
          underTest.fetchFoo(UUID.randomUUID().toString, "123456789")(
            hc,
            FakeRequest().withHeaders(sampleCorrelationIdHeader),
            ec
          )
        )
      }
      verify(underTest.auditHelper,
        times(1)).auditIfApiFailure(any(), any(), any(), any(), any())(any())
    }

    "for standard response" in new Setup {

      Mockito.reset(underTest.auditHelper)

      stubFor(
        get(urlPathMatching(s"/organisations/corporation-tax/$crn/company/details"))
          .withHeader(
            "Authorization",
            equalTo(s"Bearer $integrationFrameworkAuthorizationToken"))
          .withHeader("Environment", equalTo(integrationFrameworkEnvironment))
          .willReturn(aResponse()
            .withStatus(200)
            .withBody(Json.toJson("bar").toString())))

      val result = await(
        underTest.fetchFoo(UUID.randomUUID().toString, "123456789")(
          hc,
          FakeRequest().withHeaders(sampleCorrelationIdHeader),
          ec
        )
      )

      result shouldBe "bar"

      verify(underTest.auditHelper,
        times(1)).auditIfApiResponse(any(), any(), any(), any(), any())(any())

    }
  }
}
