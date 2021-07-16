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

package unit.uk.gov.hmrc.organisationsmatchingapi.controllers

import akka.stream.Materializer
import org.mockito.ArgumentMatchers.{any, refEq, eq => eqTo}
import org.mockito.BDDMockito.`given`
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core.{AuthConnector, Enrolment, Enrolments}
import uk.gov.hmrc.organisationsmatchingapi.audit.AuditHelper
import uk.gov.hmrc.organisationsmatchingapi.controllers.MatchedController
import uk.gov.hmrc.organisationsmatchingapi.services.{MatchedService, ScopesHelper, ScopesService}
import util.SpecBase
import play.api.test.Helpers
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.organisationsmatchingapi.domain.models.{CtMatch, ErrorInternalServer, ErrorNotFound, MatchNotFoundException, SaMatch}
import uk.gov.hmrc.organisationsmatchingapi.domain.models.JsonFormatters._
import uk.gov.hmrc.organisationsmatchingapi.domain.ogd.{CtMatchingRequest, SaMatchingRequest}
import unit.uk.gov.hmrc.organisationsmatchingapi.services.ScopesConfig

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MatchedControllerSpec extends AnyWordSpec with Matchers with MockitoSugar with SpecBase {

  implicit lazy val materializer: Materializer = fakeApplication.materializer

  trait Setup extends ScopesConfig {
    val sampleCorrelationId = "188e9400-b636-4a3b-80ba-230a8c72b92a"
    val sampleCorrelationIdHeader = "CorrelationId" -> sampleCorrelationId
    val badCorrelationIdHeader = "CorrelationId" -> "foo"
    val fakeRequest = FakeRequest("GET", "/").withHeaders(sampleCorrelationIdHeader)
    val fakeRequestMalformed = FakeRequest("GET", "/").withHeaders(badCorrelationIdHeader)
    val mockAuthConnector = mock[AuthConnector]
    lazy val scopeService: ScopesService = new ScopesService(mockConfig)
    lazy val scopesHelper: ScopesHelper = new ScopesHelper(scopeService)
    val auditHelper = mock[AuditHelper]
    val matchedService = mock[MatchedService]
    val matchId = UUID.fromString("57072660-1df9-4aeb-b4ea-cd2d7f96e430")
    implicit val hc: HeaderCarrier = HeaderCarrier()

    val matchRequestCt = new CtMatchingRequest(
      "crn",
      "test",
      "test",
      "test"
    )

    val matchRequestSa = new SaMatchingRequest(
      "utr",
      "individual",
      "test",
      "test",
      "test"
    )

    val ctMatch = new CtMatch(matchRequestCt, utr = Some("test"))
    val saMatch = new SaMatch(matchRequestSa, utr = Some("test"))

    val controller = new MatchedController(
      mockAuthConnector,
      Helpers.stubControllerComponents(),
      scopeService,
      scopesHelper,
      auditHelper,
      matchedService
    )

    given(
      mockAuthConnector.authorise(
        refEq(authPredicate(List("scopeOne", "scopeTwo"))), refEq(Retrievals.allEnrolments))(any(), any())
    ).willReturn(Future.successful(Enrolments(Set(Enrolment("scopeOne"), Enrolment("scopeTwo")))))
  }

  "GET matchedOrganisationCt" when {
    "given a valid matchId" should {
      "return 200" in new Setup {
        given(matchedService.fetchCt(matchId)).willReturn(Future.successful(ctMatch))

        val result = await(controller.matchedOrganisationCt(matchId)(fakeRequest))
        status(result) shouldBe OK

        jsonBodyOf(result) shouldBe Json.parse(
          s"""{
             |  "employerName": "test",
             |  "address": {
             |    "line1": "test",
             |    "postcode": "test"
             |  },
             |  "_links": {
             |    "self": {
             |      "href": "/organisations/matching/corporation-tax/$matchId"
             |    },
             |    "sampleEndpointThree": {
             |      "href": "/external/3",
             |      "title": "Get the third endpoint"
             |    },
             |    "sampleEndpointTwo": {
             |      "href": "/external/2",
             |      "title": "Get the second endpoint"
             |    },
             |    "sampleEndpointOne": {
             |      "href": "/external/1",
             |      "title": "Get the first endpoint"
             |    }
             |  }
             |}""".stripMargin
        )
      }
    }

    "when the match has expired" should {
      "return NOT_FOUND 404" in new Setup {
        given(matchedService.fetchCt(matchId)).willThrow(new MatchNotFoundException)

        val result = await(controller.matchedOrganisationCt(matchId)(fakeRequest))
        status(result) shouldBe NOT_FOUND

        jsonBodyOf(result) shouldBe Json.toJson(ErrorNotFound)
      }
    }

    "with a malformed correlation id" should {
      "return BAD_REQUEST" in new Setup {
        given(matchedService.fetchCt(matchId)).willReturn(Future.successful(ctMatch))

        val result = await(controller.matchedOrganisationCt(matchId)(fakeRequestMalformed))
        status(result) shouldBe BAD_REQUEST

        jsonBodyOf(result) shouldBe Json.parse(
          """
            |{
            |  "code": "INVALID_REQUEST",
            |  "message": "Malformed CorrelationId"
            |}
            |""".stripMargin
        )
      }
    }

    "with a missing correlation id" should {
      "return BAD_REQUEST" in new Setup {
        given(matchedService.fetchCt(matchId)).willReturn(Future.successful(ctMatch))

        val result = await(controller.matchedOrganisationCt(matchId)(FakeRequest()))
        status(result) shouldBe BAD_REQUEST

        jsonBodyOf(result) shouldBe Json.parse(
          """
            |{
            |  "code": "INVALID_REQUEST",
            |  "message": "CorrelationId is required"
            |}
            |""".stripMargin
        )
      }
    }

    "when an exception is thrown" should {
      "return INTERNAL_SERVER_ERROR 500" in new Setup {
        given(matchedService.fetchCt(matchId)).willThrow(new RuntimeException)

        val result = await(controller.matchedOrganisationCt(matchId)(fakeRequest))
        status(result) shouldBe INTERNAL_SERVER_ERROR

        jsonBodyOf(result) shouldBe Json.parse(
          """
            |{
            |  "code": "INTERNAL_SERVER_ERROR",
            |  "message": "Something went wrong"
            |}
            |""".stripMargin
        )
      }
    }
  }

  "GET matchedOrganisationSa" should {
    "given a valid matchId" should {
      "return 200" in new Setup {
        given(matchedService.fetchSa(matchId)).willReturn(Future.successful(saMatch))

        val result = await(controller.matchedOrganisationSa(matchId)(fakeRequest))
        status(result) shouldBe OK

        jsonBodyOf(result) shouldBe Json.parse(
          s"""{
             |  "taxPayerType": "individual",
             |  "taxPayerName": "test",
             |  "address": {
             |    "line1": "test",
             |    "postcode": "test"
             |  },
             |  "_links": {
             |    "self": {
             |      "href": "/organisations/matching/self-assessment/$matchId"
             |    },
             |    "sampleEndpointThree": {
             |      "href": "/external/3",
             |      "title": "Get the third endpoint"
             |    },
             |    "sampleEndpointTwo": {
             |      "href": "/external/2",
             |      "title": "Get the second endpoint"
             |    },
             |    "sampleEndpointOne": {
             |      "href": "/external/1",
             |      "title": "Get the first endpoint"
             |    }
             |  }
             |}""".stripMargin
        )
      }
    }

    "when the match has expired" should {
      "return NOT_FOUND 404" in new Setup {
        given(matchedService.fetchSa(matchId)).willThrow(new MatchNotFoundException)

        val result = await(controller.matchedOrganisationSa(matchId)(fakeRequest))
        status(result) shouldBe NOT_FOUND

        jsonBodyOf(result) shouldBe Json.toJson(ErrorNotFound)
      }
    }

    "with a malformed correlation id" should {
      "return BAD_REQUEST" in new Setup {
        given(matchedService.fetchSa(matchId)).willReturn(Future.successful(saMatch))

        val result = await(controller.matchedOrganisationSa(matchId)(fakeRequestMalformed))
        status(result) shouldBe BAD_REQUEST

        jsonBodyOf(result) shouldBe Json.parse(
          """
            |{
            |  "code": "INVALID_REQUEST",
            |  "message": "Malformed CorrelationId"
            |}
            |""".stripMargin
        )
      }
    }

    "with a missing correlation id" should {
      "return BAD_REQUEST" in new Setup {
        given(matchedService.fetchSa(matchId)).willReturn(Future.successful(saMatch))

        val result = await(controller.matchedOrganisationSa(matchId)(FakeRequest()))
        status(result) shouldBe BAD_REQUEST

        jsonBodyOf(result) shouldBe Json.parse(
          """
            |{
            |  "code": "INVALID_REQUEST",
            |  "message": "CorrelationId is required"
            |}
            |""".stripMargin
        )
      }
    }

    "when an exception is thrown" should {
      "return INTERNAL_SERVER_ERROR 500" in new Setup {
        given(matchedService.fetchSa(matchId)).willThrow(new RuntimeException)

        val result = await(controller.matchedOrganisationSa(matchId)(fakeRequest))
        status(result) shouldBe INTERNAL_SERVER_ERROR

        jsonBodyOf(result) shouldBe Json.parse(
          """
            |{
            |  "code": "INTERNAL_SERVER_ERROR",
            |  "message": "Something went wrong"
            |}
            |""".stripMargin
        )
      }
    }
  }
  def authPredicate(scopes: Iterable[String]): Predicate =
    scopes.map(Enrolment(_): Predicate).reduce(_ or _)
}
