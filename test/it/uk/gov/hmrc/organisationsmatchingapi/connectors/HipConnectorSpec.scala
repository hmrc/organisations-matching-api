/*
 * Copyright 2026 HM Revenue & Customs
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

import com.github.tomakehurst.wiremock.client.WireMock.*
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.test.WireMockSupport
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.organisationsmatchingapi.audit.AuditHelper
import uk.gov.hmrc.organisationsmatchingapi.connectors.HipConnector
import uk.gov.hmrc.organisationsmatchingapi.domain.integrationframework.vat.*
import uk.gov.hmrc.organisationsmatchingapi.domain.models.MatchingException
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.util.{Base64, UUID}
import scala.concurrent.ExecutionContext.Implicits.global

class HipConnectorSpec
    extends AnyWordSpec with WireMockSupport with MockitoSugar with Matchers with GuiceOneAppPerSuite {

  val hipClientId = "testClientId"
  val hipSecret = "testSecret"
  val expectedAuthHeader = s"Basic ${Base64.getEncoder.encodeToString(s"$hipClientId:$hipSecret".getBytes("UTF-8"))}"

  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .configure(
      "microservice.services.hip.port"     -> wireMockPort,
      "microservice.services.hip.clientId" -> hipClientId,
      "microservice.services.hip.secret"   -> hipSecret
    )
    .build()

  trait Setup {
    val matchId = "80a6bb14-d888-436e-a541-4000674c60aa"
    val sampleCorrelationId = "188e9400-b636-4a3b-80ba-230a8c72b92a"
    val sampleCorrelationIdHeader: (String, String) = "CorrelationId" -> sampleCorrelationId

    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withHeaders(sampleCorrelationIdHeader)

    val config: ServicesConfig = app.injector.instanceOf[ServicesConfig]
    private val httpClient = app.injector.instanceOf[HttpClientV2]
    val auditHelper: AuditHelper = mock[AuditHelper]

    val underTest = new HipConnector(config, httpClient, auditHelper)
  }

  "fetch VAT" should {
    val vrn = "123456789"
    val vatUrl = s"/etmp/RestAdapter/vat/subscription/vrn/$vrn"

    "return data on successful request" in new Setup {
      val ifResponse = IfVatCustomerInformation(
        IfVatApprovedInformation(
          IfVatCustomerDetails(Some("orgName")),
          IfPPOB(Some(IfVatCustomerAddress(Some("line1"), Some("postcode"))))
        )
      )

      val mockHipSuccessWrapperJson = Json.obj(
        "success" -> Json.toJson(ifResponse)
      )

      stubFor(
        get(urlPathMatching(vatUrl))
          .withHeader("correlationid", equalTo(sampleCorrelationId))
          .withHeader("X-Originating-System", equalTo("MDTP"))
          .withHeader("X-Transmitting-System", equalTo("HIP"))
          .withHeader("Authorization", equalTo(expectedAuthHeader))
          .withHeader("X-Receipt-Date", matching(".*")) // Matches ISO_INSTANT truncated format
          .willReturn(
            aResponse()
              .withStatus(200)
              .withBody(mockHipSuccessWrapperJson.toString())
          )
      )

      val result: IfVatCustomerInformation = await(underTest.fetchVat(UUID.randomUUID().toString, vrn))

      verify(auditHelper, times(1))
        .auditIfApiResponse(any(), any(), any(), any(), any())(using any())

      result shouldBe ifResponse
    }

    "fail with InternalServerException when HIP returns 500" in new Setup {
      stubFor(
        get(urlPathMatching(vatUrl))
          .willReturn(aResponse().withStatus(500))
      )

      intercept[InternalServerException] {
        await(underTest.fetchVat(UUID.randomUUID().toString, vrn))
      }

      verify(auditHelper, times(1))
        .auditIfApiFailure(any(), any(), any(), any(), any())(using any())
    }

    "fail with InternalServerException when HIP returns a bad request (400)" in new Setup {
      stubFor(
        get(urlPathMatching(vatUrl))
          .willReturn(aResponse().withStatus(400).withBody("BAD_REQUEST"))
      )

      intercept[InternalServerException] {
        await(underTest.fetchVat(UUID.randomUUID().toString, vrn))
      }

      verify(auditHelper, times(1))
        .auditIfApiFailure(any(), any(), any(), any(), any())(using any())
    }

    "fail with MatchingException when HIP returns a NOT_FOUND (404)" in new Setup {
      stubFor(
        get(urlPathMatching(vatUrl))
          .willReturn(aResponse().withStatus(404).withBody("NOT_FOUND"))
      )

      intercept[MatchingException] {
        await(underTest.fetchVat(UUID.randomUUID().toString, vrn))
      }

      verify(auditHelper, times(1))
        .auditIfApiFailure(any(), any(), any(), any(), any())(using any())
    }

    "fail with MatchingException when HIP returns 422 with error code '002' (HIP specific NotFound)" in new Setup {
      val hipNotFoundResponseBody = Json.obj(
        "error" -> Json.obj(
          "code"    -> "002",
          "processingDate" -> "2026-03-09T12:34:46Z",
          "text" -> "ID not found"
        )
      )

      stubFor(
        get(urlPathMatching(vatUrl))
          .willReturn(
            aResponse()
              .withStatus(422)
              .withBody(hipNotFoundResponseBody.toString())
          )
      )

      intercept[MatchingException] {
        await(underTest.fetchVat(UUID.randomUUID().toString, vrn))
      }

      verify(auditHelper, times(1))
        .auditIfApiFailure(any(), any(), any(), any(), any())(using any())
    }

    "fail with InternalServerException when HIP returns 422 with a non '002' error code" in new Setup {
      val genericErrorResponseBody = Json.obj(
        "error" -> Json.obj(
          "code"    -> "003",
          "processingDate" -> "2026-03-09T12:34:46Z",
          "text" -> "Request could not be processed"
        )
      )

      stubFor(
        get(urlPathMatching(vatUrl))
          .willReturn(
            aResponse()
              .withStatus(422)
              .withBody(genericErrorResponseBody.toString())
          )
      )

      intercept[InternalServerException] {
        await(underTest.fetchVat(UUID.randomUUID().toString, vrn))
      }

      verify(auditHelper, times(1))
        .auditIfApiFailure(any(), any(), any(), any(), any())(using any())
    }
  }
}
