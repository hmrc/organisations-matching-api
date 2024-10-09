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

import com.github.tomakehurst.wiremock.client.WireMock._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.test.WireMockSupport
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.organisationsmatchingapi.audit.AuditHelper
import uk.gov.hmrc.organisationsmatchingapi.connectors.OrganisationsMatchingConnector
import uk.gov.hmrc.organisationsmatchingapi.domain.integrationframework.common.IfAddress
import uk.gov.hmrc.organisationsmatchingapi.domain.integrationframework.ct.{IfCorpTaxCompanyDetails, IfNameAndAddressDetails, IfNameDetails}
import uk.gov.hmrc.organisationsmatchingapi.domain.integrationframework.sa.{IfSaTaxpayerDetails, IfSaTaxpayerNameAddress}
import uk.gov.hmrc.organisationsmatchingapi.domain.integrationframework.vat.IfVatCustomerInformationSimplified
import uk.gov.hmrc.organisationsmatchingapi.domain.models.MatchingException
import uk.gov.hmrc.organisationsmatchingapi.domain.organisationsmatching._
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global

class OrganisationsMatchingConnectorSpec
  extends AnyWordSpec
    with WireMockSupport
    with MockitoSugar
    with Matchers
    with GuiceOneAppPerSuite {

  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .configure(
      "microservice.services.organisations-matching.port" -> wireMockPort,
    )
    .build()

  trait Setup {
    val matchId = UUID.randomUUID()
    val matchIdStr = matchId.toString
    val correlationId = UUID.randomUUID()
    val correlationIdStr = correlationId.toString
    val correlationIdHeader: (String, String) = "CorrelationId" -> correlationIdStr
    val applicationIdHeader: (String, String) = "X-Application-ID" -> "12345"
    val ctKnownFacts: CtKnownFacts = CtKnownFacts("test", "test", "test", "test")
    val name: IfNameDetails = IfNameDetails(Some("test"), Some("test"))
    val address: IfAddress = IfAddress(Some("test"), None, None, None, Some("test"))
    val details: IfNameAndAddressDetails = IfNameAndAddressDetails(Some(name), Some(address))
    val ctIfData: IfCorpTaxCompanyDetails = IfCorpTaxCompanyDetails(Some("test"), Some("test"), Some(details), Some(details))
    val ctPostData: CtOrganisationsMatchingRequest = CtOrganisationsMatchingRequest(ctKnownFacts, ctIfData)

    val saKnownFacts: SaKnownFacts = SaKnownFacts("test", "Individual", "test", "test", "test")
    val saDetails: IfSaTaxpayerNameAddress = IfSaTaxpayerNameAddress(Some("test"), None, Some(address))
    val taxpayerDetails: Option[Seq[IfSaTaxpayerNameAddress]] = Some(Seq(saDetails))
    val saIfData: IfSaTaxpayerDetails = IfSaTaxpayerDetails(Some("test"), Some("Individual"), taxpayerDetails)
    val saPostData: SaOrganisationsMatchingRequest = SaOrganisationsMatchingRequest(saKnownFacts, saIfData)

    implicit val hc: HeaderCarrier = HeaderCarrier(otherHeaders = Seq(correlationIdHeader, applicationIdHeader))
    implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

    val config: ServicesConfig = app.injector.instanceOf[ServicesConfig]
    private val httpClient = app.injector.instanceOf[HttpClientV2]
    val auditHelper: AuditHelper = mock[AuditHelper]

    val underTest = new OrganisationsMatchingConnector(config, httpClient, auditHelper)
  }

  "matchCycleCotax" should {
    "Fail when organisations matching returns an error CT" in new Setup {
      stubFor(
        post(urlMatching(s"/organisations-matching/perform-match/cotax\\?matchId=$matchId&correlationId=$correlationId"))
          .withRequestBody(equalToJson(
            """
              |{
              |    "knownFacts": {
              |        "crn": "test",
              |        "name": "test",
              |        "line1": "test",
              |        "postcode": "test"
              |    },
              |    "ifData": {
              |        "utr": "test",
              |        "crn": "test",
              |        "registeredDetails": {
              |          "name": {
              |            "name1": "test",
              |            "name2": "test"
              |          },
              |          "address": {
              |            "line1": "test",
              |            "postcode": "test"
              |          }
              |        },
              |        "communicationDetails": {
              |          "name": {
              |            "name1": "test",
              |            "name2": "test"
              |          },
              |          "address": {
              |            "line1": "test",
              |            "postcode": "test"
              |          }
              |        }
              |     }
              |}
            """.stripMargin
          ))
          .willReturn(aResponse().withStatus(500)))

      intercept[InternalServerException] {
        await(underTest.matchCycleCotax(matchIdStr, correlationIdStr, ctPostData))
      }

      verify(auditHelper, times(1))
        .auditOrganisationsMatchingFailure(any(), any(), any(), any(), any())(any())
    }

    "Fail when organisations matching returns a bad request" in new Setup {
      stubFor(
        post(urlMatching(s"/organisations-matching/perform-match/cotax\\?matchId=$matchId&correlationId=$correlationId"))
          .withRequestBody(equalToJson(
            """
              |{
              |    "knownFacts": {
              |        "crn": "test",
              |        "name": "test",
              |        "line1": "test",
              |        "postcode": "test"
              |    },
              |    "ifData": {
              |        "utr": "test",
              |        "crn": "test",
              |        "registeredDetails": {
              |          "name": {
              |            "name1": "test",
              |            "name2": "test"
              |          },
              |          "address": {
              |            "line1": "test",
              |            "postcode": "test"
              |          }
              |        },
              |        "communicationDetails": {
              |          "name": {
              |            "name1": "test",
              |            "name2": "test"
              |          },
              |          "address": {
              |            "line1": "test",
              |            "postcode": "test"
              |          }
              |        }
              |     }
              |}
            """.stripMargin
          ))
          .willReturn(aResponse().withStatus(400).withBody("BAD_REQUEST")))

      intercept[InternalServerException] {
        await(underTest.matchCycleCotax(matchIdStr, correlationIdStr, ctPostData))
      }

      verify(auditHelper,
        times(1)).auditOrganisationsMatchingFailure(any(), any(), any(), any(), any())(any())
    }

    "return a match for a matched request CT" in new Setup {
      stubFor(
        post(urlMatching(s"/organisations-matching/perform-match/cotax\\?matchId=$matchId&correlationId=$correlationId"))
          .withRequestBody(equalToJson(
            """
              |{
              |    "knownFacts": {
              |        "crn": "test",
              |        "name": "test",
              |        "line1": "test",
              |        "postcode": "test"
              |    },
              |    "ifData": {
              |        "utr": "test",
              |        "crn": "test",
              |        "registeredDetails": {
              |          "name": {
              |            "name1": "test",
              |            "name2": "test"
              |          },
              |          "address": {
              |            "line1": "test",
              |            "postcode": "test"
              |          }
              |        },
              |        "communicationDetails": {
              |          "name": {
              |            "name1": "test",
              |            "name2": "test"
              |          },
              |          "address": {
              |            "line1": "test",
              |            "postcode": "test"
              |          }
              |        }
              |     }
              |}
            """.stripMargin
          ))
          .willReturn(aResponse()
            .withStatus(200)
            .withBody(Json.toJson("match").toString())))

      val result: JsValue = await(
        underTest.matchCycleCotax(matchIdStr, correlationIdStr, ctPostData)
      )

      result shouldBe Json.toJson("match")

      verify(auditHelper, times(1))
        .auditOrganisationsMatchingResponse(any(), any(), any(), any(), any())(any())
    }

    "return NOT_FOUND for a non matched request CT" in new Setup {
      stubFor(
        post(urlMatching(s"/organisations-matching/perform-match/cotax\\?matchId=$matchId&correlationId=$correlationId"))
          .withRequestBody(equalToJson(
            """
              |{
              |    "knownFacts": {
              |        "crn": "test",
              |        "name": "test",
              |        "line1": "test",
              |        "postcode": "test"
              |    },
              |    "ifData": {
              |        "utr": "test",
              |        "crn": "test",
              |        "registeredDetails": {
              |          "name": {
              |            "name1": "test",
              |            "name2": "test"
              |          },
              |          "address": {
              |            "line1": "test",
              |            "postcode": "test"
              |          }
              |        },
              |        "communicationDetails": {
              |          "name": {
              |            "name1": "test",
              |            "name2": "test"
              |          },
              |          "address": {
              |            "line1": "test",
              |            "postcode": "test"
              |          }
              |        }
              |     }
              |}
            """.stripMargin
          ))
          .willReturn(aResponse()
            .withStatus(404).withBody("{ \"code\" : \"NOT_FOUND\"}")))

      intercept[MatchingException] {
        await(underTest.matchCycleCotax(matchIdStr, correlationIdStr, ctPostData))
      }

      verify(auditHelper, times(1))
        .auditOrganisationsMatchingResponse(any(), any(), any(), any(), any())(any())
    }
  }

  "matchCycleSelfAssessment" should {
    "Fail when organisations matching returns an error SA" in new Setup {
      stubFor(
        post(urlMatching(s"/organisations-matching/perform-match/self-assessment\\?matchId=$matchId&correlationId=$correlationId"))
          .withRequestBody(equalToJson(
            """
              |{
              |    "knownFacts": {
              |        "utr": "test",
              |        "taxpayerType": "Individual",
              |        "name": "test",
              |        "line1": "test",
              |        "postcode": "test"
              |    },
              |    "ifData": {
              |        "utr": "test",
              |        "taxpayerType": "Individual",
              |        "taxpayerDetails": [{
              |          "name": "test",
              |          "address": {
              |            "line1": "test",
              |            "postcode": "test"
              |          }
              |        }]
              |     }
              |}
            """.stripMargin
          ))
          .willReturn(aResponse().withStatus(500)))

      intercept[InternalServerException] {
        await(underTest.matchCycleSelfAssessment(matchIdStr, correlationIdStr, saPostData))
      }

      verify(auditHelper, times(1))
        .auditOrganisationsMatchingFailure(any(), any(), any(), any(), any())(any())
    }

    "Fail when organisations matching returns a bad request" in new Setup {
      stubFor(
        post(urlMatching(s"/organisations-matching/perform-match/self-assessment\\?matchId=$matchId&correlationId=$correlationId"))
          .withRequestBody(equalToJson(
            """
              |{
              |    "knownFacts": {
              |        "utr": "test",
              |        "taxpayerType": "Individual",
              |        "name": "test",
              |        "line1": "test",
              |        "postcode": "test"
              |    },
              |    "ifData": {
              |        "utr": "test",
              |        "taxpayerType": "Individual",
              |        "taxpayerDetails": [{
              |          "name": "test",
              |          "address": {
              |            "line1": "test",
              |            "postcode": "test"
              |          }
              |        }]
              |     }
              |}
            """.stripMargin
          ))
          .willReturn(aResponse().withStatus(400).withBody("BAD_REQUEST")))

      intercept[InternalServerException] {
        await(underTest.matchCycleSelfAssessment(matchIdStr, correlationIdStr, saPostData))
      }

      verify(auditHelper, times(1))
        .auditOrganisationsMatchingFailure(any(), any(), any(), any(), any())(any())
    }

    "return a match for a matched request" in new Setup {
      stubFor(
        post(urlMatching(s"/organisations-matching/perform-match/self-assessment\\?matchId=$matchId&correlationId=$correlationId"))
          .withRequestBody(equalToJson(
            """
              |{
              |    "knownFacts": {
              |        "utr": "test",
              |        "taxpayerType": "Individual",
              |        "name": "test",
              |        "line1": "test",
              |        "postcode": "test"
              |    },
              |    "ifData": {
              |        "utr": "test",
              |        "taxpayerType": "Individual",
              |        "taxpayerDetails": [{
              |          "name": "test",
              |          "address": {
              |            "line1": "test",
              |            "postcode": "test"
              |          }
              |        }]
              |     }
              |}
            """.stripMargin
          ))
          .willReturn(aResponse()
            .withStatus(200)
            .withBody(Json.toJson("match").toString())))

      val result = await(underTest.matchCycleSelfAssessment(matchIdStr, correlationIdStr, saPostData))

      result shouldBe Json.toJson("match")

      verify(auditHelper, times(1))
        .auditOrganisationsMatchingResponse(any(), any(), any(), any(), any())(any())
    }

    "return NOT_FOUND for a non matched request" in new Setup {
      stubFor(
        post(urlMatching(s"/organisations-matching/perform-match/self-assessment\\?matchId=$matchId&correlationId=$correlationId"))
          .withRequestBody(equalToJson(
            """
              |{
              |    "knownFacts": {
              |        "utr": "test",
              |        "taxpayerType": "Individual",
              |        "name": "test",
              |        "line1": "test",
              |        "postcode": "test"
              |    },
              |    "ifData": {
              |        "utr": "test",
              |        "taxpayerType": "Individual",
              |        "taxpayerDetails": [{
              |          "name": "test",
              |          "address": {
              |            "line1": "test",
              |            "postcode": "test"
              |          }
              |        }]
              |     }
              |}
            """.stripMargin
          ))
          .willReturn(aResponse()
            .withStatus(404).withBody("{ \"code\" : \"NOT_FOUND\"}")))

      intercept[MatchingException] {
        await(underTest.matchCycleCotax(matchIdStr, correlationIdStr, ctPostData))
      }

      verify(auditHelper, times(1))
        .auditOrganisationsMatchingResponse(any(), any(), any(), any(), any())(any())
    }

    "return appropriate header carrier" in new Setup {
      hc.headers(Seq("X-Application-ID", "CorrelationId")) shouldBe Seq("CorrelationId" -> correlationIdStr, "X-Application-ID" -> "12345")
    }
  }

  "matchCycleVat" should {
    val matchingRequest = VatOrganisationsMatchingRequest(
      VatKnownFacts("vrn", "org", "line1", "postcode"),
      IfVatCustomerInformationSimplified("vrn", "org", Some("line1"), Some("postcode"))
    )

    def orgsMatchingVatUrl(matchId: UUID, correlationId: UUID) =
      s"/organisations-matching/perform-match/vat\\?matchId=$matchId&correlationId=$correlationId"

    "fail with InternalServerException when organisations matching vat returns 500" in new Setup {
      stubFor(
        post(urlMatching(orgsMatchingVatUrl(matchId, correlationId)))
          .withRequestBody(equalToJson(Json.toJson(matchingRequest).toString()))
          .willReturn(aResponse().withStatus(500)))

      intercept[InternalServerException] {
        await(underTest.matchCycleVat(matchId, correlationId, matchingRequest))
      }

      verify(auditHelper, times(1))
        .auditOrganisationsMatchingFailure(any(), any(), any(), any(), any())(any())
    }

    "fail with InternalServerException when organisations matching vat returns bad request" in new Setup {
      stubFor(
        post(urlMatching(orgsMatchingVatUrl(matchId, correlationId)))
          .withRequestBody(equalToJson(Json.toJson(matchingRequest).toString()))
          .willReturn(aResponse().withStatus(400).withBody("BAD_REQUEST")))

      intercept[InternalServerException] {
        await(underTest.matchCycleVat(matchId, correlationId, matchingRequest))
      }

      verify(auditHelper, times(1))
        .auditOrganisationsMatchingFailure(any(), any(), any(), any(), any())(any())
    }

    "return not found for a non matched request" in new Setup {
      stubFor(
        post(urlMatching(orgsMatchingVatUrl(matchId, correlationId)))
          .withRequestBody(equalToJson(Json.toJson(matchingRequest).toString()))
          .willReturn(aResponse()
            .withStatus(404).withBody("""{ "code" : "NOT_FOUND" }""")))

      intercept[MatchingException] {
        await(underTest.matchCycleVat(matchId, correlationId, matchingRequest))
      }

      verify(auditHelper, times(1))
        .auditOrganisationsMatchingResponse(any(), any(), any(), any(), any())(any())
    }

    "return a match for a matched request" in new Setup {
      stubFor(
        post(urlMatching(orgsMatchingVatUrl(matchId, correlationId)))
          .withRequestBody(equalToJson(Json.toJson(matchingRequest).toString()))
          .willReturn(aResponse()
            .withStatus(200)
            .withBody(Json.toJson("match").toString())))

      await(underTest.matchCycleVat(matchId, correlationId, matchingRequest)) shouldBe Json.toJson("match")

      verify(auditHelper, times(1))
        .auditOrganisationsMatchingResponse(any(), any(), any(), any(), any())(any())
    }
  }
}
