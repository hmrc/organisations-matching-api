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
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, InternalServerException}
import uk.gov.hmrc.organisationsmatchingapi.audit.AuditHelper
import uk.gov.hmrc.organisationsmatchingapi.connectors.OrganisationsMatchingConnector
import uk.gov.hmrc.organisationsmatchingapi.domain.integrationframework.common.IfAddress
import uk.gov.hmrc.organisationsmatchingapi.domain.integrationframework.ct.{IfCorpTaxCompanyDetails, IfNameAndAddressDetails, IfNameDetails}
import uk.gov.hmrc.organisationsmatchingapi.domain.integrationframework.sa.{IfSaTaxpayerDetails, IfSaTaxpayerNameAddress}
import uk.gov.hmrc.organisationsmatchingapi.domain.integrationframework.vat.IfVatCustomerInformationSimplified
import uk.gov.hmrc.organisationsmatchingapi.domain.models.MatchingException
import uk.gov.hmrc.organisationsmatchingapi.domain.organisationsmatching.{CtKnownFacts, CtOrganisationsMatchingRequest, SaKnownFacts, SaOrganisationsMatchingRequest, VatKnownFacts, VatOrganisationsMatchingRequest}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import util.UnitSpec

import java.util.UUID
import scala.concurrent.ExecutionContext

class OrganisationsMatchingConnectorSpec
  extends AnyWordSpec
    with BeforeAndAfterEach
    with UnitSpec
    with MockitoSugar
    with Matchers
    with GuiceOneAppPerSuite {

  val stubPort: Int = sys.env.getOrElse("WIREMOCK", "11122").toInt
  val stubHost = "127.0.0.1"
  val wireMockServer = new WireMockServer(wireMockConfig().port(stubPort))

  def externalServices: Seq[String] = Seq.empty

  override lazy val fakeApplication = new GuiceApplicationBuilder()
    .bindings(bindModules: _*)
    .configure(
      "auditing.enabled" -> false,
      "cache.enabled" -> false,
      "microservice.services.organisations-matching.host" -> "127.0.0.1",
      "microservice.services.organisations-matching.port" -> "11122",
    )
    .build()

  implicit val ec: ExecutionContext = fakeApplication.injector.instanceOf[ExecutionContext]

  trait Setup {
    val matchId = UUID.randomUUID()
    val matchIdStr = matchId.toString
    val correlationId = UUID.randomUUID()
    val correlationIdStr = correlationId.toString
    val applicationId = "12345"
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
    implicit val request = FakeRequest()

    val config: ServicesConfig = fakeApplication.injector.instanceOf[ServicesConfig]
    val httpClient: HttpClient = fakeApplication.injector.instanceOf[HttpClient]
    val auditHelper: AuditHelper = mock[AuditHelper]

    val underTest = new OrganisationsMatchingConnector(config, httpClient, auditHelper)
  }

  override def beforeEach(): Unit = {
    wireMockServer.start()
    configureFor(stubHost, stubPort)
  }

  override def afterEach(): Unit = {
    wireMockServer.stop()
  }

  "matchCycleCotax" should {

    "Fail when organisations matching returns an error CT" in new Setup {

      Mockito.reset(underTest.auditHelper)

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

      verify(underTest.auditHelper, times(1))
        .auditOrganisationsMatchingFailure(any(), any(), any(), any(), any())(any())

    }

    "Fail when organisations matching returns a bad request" in new Setup {

      Mockito.reset(underTest.auditHelper)

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

      verify(underTest.auditHelper,
        times(1)).auditOrganisationsMatchingFailure(any(), any(), any(), any(), any())(any())
    }

    "return a match for a matched request CT" in new Setup {

      Mockito.reset(underTest.auditHelper)

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

      verify(underTest.auditHelper, times(1))
        .auditOrganisationsMatchingResponse(any(), any(), any(), any(), any())(any())

    }

    "return NOT_FOUND for a non matched request CT" in new Setup {

      Mockito.reset(underTest.auditHelper)

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

      verify(underTest.auditHelper, times(1))
        .auditOrganisationsMatchingResponse(any(), any(), any(), any(), any())(any())

    }
  }

  "matchCycleSelfAssessment" should {

    "Fail when organisations matching returns an error SA" in new Setup {
      Mockito.reset(underTest.auditHelper)

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

      verify(underTest.auditHelper, times(1))
        .auditOrganisationsMatchingFailure(any(), any(), any(), any(), any())(any())

    }

    "Fail when organisations matching returns a bad request" in new Setup {

      Mockito.reset(underTest.auditHelper)

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

      verify(underTest.auditHelper, times(1))
        .auditOrganisationsMatchingFailure(any(), any(), any(), any(), any())(any())
    }

    "return a match for a matched request" in new Setup {

      Mockito.reset(underTest.auditHelper)

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

      verify(underTest.auditHelper, times(1))
        .auditOrganisationsMatchingResponse(any(), any(), any(), any(), any())(any())

    }

    "return NOT_FOUND for a non matched request" in new Setup {

      Mockito.reset(underTest.auditHelper)

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

      verify(underTest.auditHelper, times(1))
        .auditOrganisationsMatchingResponse(any(), any(), any(), any(), any())(any())

    }

    "return appropriate header carrier" in new Setup {
      hc.headers(underTest.requiredHeaders) shouldBe Seq("CorrelationId" -> correlationIdStr, "X-Application-ID" -> "12345")
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
      Mockito.reset(underTest.auditHelper)

      stubFor(
        post(urlMatching(orgsMatchingVatUrl(matchId, correlationId)))
          .withRequestBody(equalToJson(Json.toJson(matchingRequest).toString()))
          .willReturn(aResponse().withStatus(500)))

      intercept[InternalServerException] {
        await(underTest.matchCycleVat(matchId, correlationId, matchingRequest))
      }

      verify(underTest.auditHelper, times(1))
        .auditOrganisationsMatchingFailure(any(), any(), any(), any(), any())(any())
    }

    "fail with InternalServerException when organisations matching vat returns bad request" in new Setup {
      Mockito.reset(underTest.auditHelper)

      stubFor(
        post(urlMatching(orgsMatchingVatUrl(matchId, correlationId)))
          .withRequestBody(equalToJson(Json.toJson(matchingRequest).toString()))
          .willReturn(aResponse().withStatus(400).withBody("BAD_REQUEST")))

      intercept[InternalServerException] {
        await(underTest.matchCycleVat(matchId, correlationId, matchingRequest))
      }

      verify(underTest.auditHelper, times(1))
        .auditOrganisationsMatchingFailure(any(), any(), any(), any(), any())(any())
    }

    "return not found for a non matched request" in new Setup {
      Mockito.reset(underTest.auditHelper)

      stubFor(
        post(urlMatching(orgsMatchingVatUrl(matchId, correlationId)))
          .withRequestBody(equalToJson(Json.toJson(matchingRequest).toString()))
          .willReturn(aResponse()
            .withStatus(404).withBody("""{ "code" : "NOT_FOUND" }""")))

      intercept[MatchingException] {
        await(underTest.matchCycleVat(matchId, correlationId, matchingRequest))
      }

      verify(underTest.auditHelper, times(1))
        .auditOrganisationsMatchingResponse(any(), any(), any(), any(), any())(any())
    }

    "return a match for a matched request" in new Setup {
      Mockito.reset(underTest.auditHelper)

      stubFor(
        post(urlMatching(orgsMatchingVatUrl(matchId, correlationId)))
          .withRequestBody(equalToJson(Json.toJson(matchingRequest).toString()))
          .willReturn(aResponse()
            .withStatus(200)
            .withBody(Json.toJson("match").toString())))

      await(underTest.matchCycleVat(matchId, correlationId, matchingRequest)) shouldBe Json.toJson("match")

      verify(underTest.auditHelper, times(1))
        .auditOrganisationsMatchingResponse(any(), any(), any(), any(), any())(any())
    }

  }
}
