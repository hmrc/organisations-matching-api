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
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, InternalServerException}
import uk.gov.hmrc.organisationsmatchingapi.audit.AuditHelper
import uk.gov.hmrc.organisationsmatchingapi.connectors.OrganisationsMatchingConnector
import uk.gov.hmrc.organisationsmatchingapi.domain.integrationframework.{IfAddress, IfCorpTaxCompanyDetails, IfNameAndAddressDetails, IfNameDetails, IfSaTaxPayerNameAddress, IfSaTaxpayerDetails}
import uk.gov.hmrc.organisationsmatchingapi.domain.organisationsmatching.{CtKnownFacts, CtOrganisationsMatchingRequest, SaKnownFacts, SaOrganisationsMatchingRequest}
import uk.gov.hmrc.organisationsmatchingapi.errorhandler.ErrorResponse.MatchingException
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import util.UnitSpec

import scala.concurrent.ExecutionContext

class OrganisationsMatchingConnectorSpec
  extends AnyWordSpec
    with BeforeAndAfterEach
    with UnitSpec
    with MockitoSugar
    with Matchers
    with GuiceOneAppPerSuite {

  val stubPort = sys.env.getOrElse("WIREMOCK", "11122").toInt
  val stubHost = "localhost"
  val wireMockServer = new WireMockServer(wireMockConfig().port(stubPort))

  def externalServices: Seq[String] = Seq.empty

  override lazy val fakeApplication = new GuiceApplicationBuilder()
    .bindings(bindModules: _*)
    .configure(
      "cache.enabled"  -> false,
      "microservice.services.organisations-matching.host" -> "localhost",
      "microservice.services.organisations-matching.port" -> "11122",
    )
    .build()

  implicit val ec: ExecutionContext =
    fakeApplication.injector.instanceOf[ExecutionContext]

  trait Setup {
    val matchId       = "80a6bb14-d888-436e-a541-4000674c60aa"
    val correlationId = "188e9400-b636-4a3b-80ba-230a8c72b92a"
    val ctKnownFacts  = CtKnownFacts("test", "test", "test", "test")
    val name          = IfNameDetails(Some("test"), Some("test"))
    val address       = IfAddress(Some("test"), None, None, None, Some("test"))
    val details       = IfNameAndAddressDetails(Some(name), Some(address))
    val ctIfData      = IfCorpTaxCompanyDetails(Some("test"), Some("test"), Some(details), Some(details))
    val ctPostData    = CtOrganisationsMatchingRequest(ctKnownFacts, ctIfData)

    val saKnownFacts    = SaKnownFacts("test", "Individual", "test", "test", "test")
    val saDetails       = IfSaTaxPayerNameAddress(Some("test"), None, Some(address))
    val taxpayerDetails = Some(Seq(saDetails))
    val saIfData        = IfSaTaxpayerDetails(Some("test"), Some("Individual"), taxpayerDetails)
    val saPostData      = SaOrganisationsMatchingRequest(saKnownFacts, saIfData)

    implicit val hc: HeaderCarrier = HeaderCarrier()

    val config: ServicesConfig   = fakeApplication.injector.instanceOf[ServicesConfig]
    val httpClient: HttpClient   = fakeApplication.injector.instanceOf[HttpClient]
    val auditHelper: AuditHelper = mock[AuditHelper]

    val underTest = new OrganisationsMatchingConnector(config, httpClient, auditHelper)
  }

  override def beforeEach() {
    wireMockServer.start()
    configureFor(stubHost, stubPort)
  }

  override def afterEach() {
    wireMockServer.stop()
  }

  "matchCycleCotax" should {

    "Fail when organisations matching returns an error CT" in new Setup {

      Mockito.reset(underTest.auditHelper)

      stubFor(
        post(urlMatching(s"/organisations-matching/perform-match/cotax\\?matchId=${matchId}&correlationId=${correlationId}"))
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
        await(
          underTest.matchCycleCotax(matchId, correlationId, ctPostData)(
            hc,
            FakeRequest(),
            ec
          )
        )
      }

      verify(underTest.auditHelper,
        times(1)).auditOrganisationsMatchingFailure(any(), any(), any(), any(), any())(any())

    }

    "Fail when organisations matching returns a bad request" in new Setup {

      Mockito.reset(underTest.auditHelper)

      stubFor(
        post(urlMatching(s"/organisations-matching/perform-match/cotax\\?matchId=${matchId}"))
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
        await(
          underTest.matchCycleCotax(matchId, correlationId, ctPostData)(
            hc,
            FakeRequest(),
            ec
          )
        )
      }

      verify(underTest.auditHelper,
        times(1)).auditOrganisationsMatchingFailure(any(), any(), any(), any(), any())(any())
    }

    "return a match for a matched request CT" in new Setup {

      Mockito.reset(underTest.auditHelper)

      stubFor(
        post(urlMatching(s"/organisations-matching/perform-match/cotax\\?matchId=${matchId}&correlationId=${correlationId}"))
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

      val result = await(
        underTest.matchCycleCotax(matchId, correlationId, ctPostData)(
          hc,
          FakeRequest(),
          ec
        )
      )

      result shouldBe Json.toJson("match")

      verify(underTest.auditHelper,
        times(1)).auditOrganisationsMatchingResponse(any(), any(), any(), any(), any())(any())

    }

    "return NOT_FOUND for a non matched request CT" in new Setup {

      Mockito.reset(underTest.auditHelper)

      stubFor(
        post(urlMatching(s"/organisations-matching/perform-match/cotax\\?matchId=${matchId}&correlationId=${correlationId}"))
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
            .withStatus(404).withBody("NOT_FOUND")))

      intercept[MatchingException] {
        await(
          underTest.matchCycleCotax(matchId, correlationId, ctPostData)(
            hc,
            FakeRequest(),
            ec
          )
        )
      }

      verify(underTest.auditHelper,
        times(1)).auditOrganisationsMatchingResponse(any(), any(), any(), any(), any())(any())

    }
  }

  "matchCycleSelfAssessment" should {

    "Fail when organisations matching returns an error SA" in new Setup {

      Mockito.reset(underTest.auditHelper)

      stubFor(
        post(urlMatching(s"/organisations-matching/perform-match/self-assessment\\?matchId=${matchId}&correlationId=${correlationId}"))
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
        await(
          underTest.matchCycleSelfAssessment(matchId, correlationId, saPostData)(
            hc,
            FakeRequest(),
            ec
          )
        )
      }

      verify(underTest.auditHelper,
        times(1)).auditOrganisationsMatchingFailure(any(), any(), any(), any(), any())(any())

    }

    "Fail when organisations matching returns a bad request" in new Setup {

      Mockito.reset(underTest.auditHelper)

      stubFor(
        post(urlMatching(s"/organisations-matching/perform-match/self-assessment\\?matchId=${matchId}"))
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
        await(
          underTest.matchCycleSelfAssessment(matchId, correlationId, saPostData)(
            hc,
            FakeRequest(),
            ec
          )
        )
      }

      verify(underTest.auditHelper,
        times(1)).auditOrganisationsMatchingFailure(any(), any(), any(), any(), any())(any())
    }

    "return a match for a matched request" in new Setup {

      Mockito.reset(underTest.auditHelper)

      stubFor(
        post(urlMatching(s"/organisations-matching/perform-match/self-assessment\\?matchId=${matchId}&correlationId=${correlationId}"))
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

      val result = await(
        underTest.matchCycleSelfAssessment(matchId, correlationId, saPostData)(
          hc,
          FakeRequest(),
          ec
        )
      )

      result shouldBe Json.toJson("match")

      verify(underTest.auditHelper,
        times(1)).auditOrganisationsMatchingResponse(any(), any(), any(), any(), any())(any())

    }

    "return NOT_FOUND for a non matched request" in new Setup {

      Mockito.reset(underTest.auditHelper)

      stubFor(
        post(urlMatching(s"/organisations-matching/perform-match/self-assessment\\?matchId=${matchId}&correlationId=${correlationId}"))
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
            .withStatus(404).withBody("NOT_FOUND")))

      intercept[MatchingException] {
        await(
          underTest.matchCycleCotax(matchId, correlationId, ctPostData)(
            hc,
            FakeRequest(),
            ec
          )
        )
      }

      verify(underTest.auditHelper,
        times(1)).auditOrganisationsMatchingResponse(any(), any(), any(), any(), any())(any())

    }
  }
}