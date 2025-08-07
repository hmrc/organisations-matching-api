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

package unit.uk.gov.hmrc.organisationsmatchingapi.controllers

import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.BDDMockito.`given`
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status
import play.api.libs.json.Json
import play.api.mvc.PlayBodyParsers
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.{AuthConnector, Enrolment, Enrolments}
import uk.gov.hmrc.organisationsmatchingapi.audit.AuditHelper
import uk.gov.hmrc.organisationsmatchingapi.controllers.MatchingController
import uk.gov.hmrc.organisationsmatchingapi.domain.models.MatchingException
import uk.gov.hmrc.organisationsmatchingapi.domain.ogd.VatMatchingRequest
import uk.gov.hmrc.organisationsmatchingapi.services.{MatchingService, ScopesHelper, ScopesService}
import unit.uk.gov.hmrc.organisationsmatchingapi.util.SpecBase

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class MatchingControllerSpec extends AnyWordSpec with SpecBase with Matchers with MockitoSugar {

  private implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  private val mockAuthConnector = mock[AuthConnector]
  private val mockAuditHelper = mock[AuditHelper]
  private val mockScopesService = mock[ScopesService]
  private val scopesHelper = new ScopesHelper(mockScopesService)
  private val mockMatchingService = mock[MatchingService]
  private val mockBodyParser = mock[PlayBodyParsers]

  private val controller = new MatchingController(
    mockAuthConnector,
    Helpers.stubControllerComponents(),
    Helpers.stubMessagesControllerComponents(),
    mockScopesService,
    scopesHelper,
    mockBodyParser,
    mockMatchingService
  )(using mockAuditHelper, ec)

  `given`(mockAuthConnector.authorise(any(), refEq(Retrievals.allEnrolments))(using any(), any()))
    .willReturn(Future.successful(Enrolments(Set(Enrolment("test-scope"), Enrolment("test-scope-1")))))

  `given`(mockScopesService.getAllScopes).willReturn(List("test-scope", "test-scope-1"))

  `given`(mockMatchingService.matchSaTax(any(), any(), any())(using any(), any())).willReturn(Future.successful(Json.toJson("match")))
  `given`(mockMatchingService.matchCoTax(any(), any(), any())(using any(), any())).willReturn(Future.successful(Json.toJson("match")))
  `given`(mockMatchingService.matchVat(any(), any(), any())(using any(), any())).willReturn(Future.successful(Json.toJson("match")))

  `given`(mockScopesService.getInternalEndpoints(any())).willReturn(Seq())
  `given`(mockScopesService.getExternalEndpoints(any())).willReturn(Seq())


  "POST matchOrganisationCt" should {

    val fakeRequest = FakeRequest("POST", "/")
      .withHeaders(("CorrelationId", UUID.randomUUID().toString))
      .withBody(Json.parse(
        """
          |{
          |   "companyRegistrationNumber":"1234567890",
          |   "employerName":"name",
          |   "address": {
          |     "postcode":"postcode",
          |     "addressLine1":"line1"
          |   }
          |}""".stripMargin))

    "return 200" in {
      val result = controller.matchOrganisationCt()(fakeRequest)
      status(result) shouldBe Status.OK
    }

    "return 404 when request does not match" in {

      `given`(mockMatchingService.matchCoTax(any(), any(), any())(using any(), any())).willReturn(Future.failed(new MatchingException))

      val result = controller.matchOrganisationCt()(
        FakeRequest()
          .withHeaders(("CorrelationId", UUID.randomUUID().toString))
          .withBody(Json.parse(
            """
              |{
              |   "companyRegistrationNumber":"999999999",
              |   "employerName":"notAname",
              |   "address": {
              |     "postcode":"notApostcode",
              |     "addressLine1":"notAline1"
              |   }
              |}""".stripMargin))
      )

      status(result) shouldBe Status.NOT_FOUND

      contentAsJson(result) shouldBe errorResponse("MATCHING_FAILED", "There is no match for the information provided")
    }

    "return 400 when request is invalid" in {
      val result = controller.matchOrganisationCt()(
        FakeRequest()
          .withHeaders(("CorrelationId", UUID.randomUUID().toString))
          .withBody(Json.obj()))

      status(result) shouldBe Status.BAD_REQUEST
    }
  }

  "POST matchOrganisationSa" should {

    val fakeRequest = FakeRequest("POST", "/")
      .withHeaders(("CorrelationId", UUID.randomUUID().toString))
      .withBody(Json.parse(
        """
          |{
          |   "selfAssessmentUniqueTaxPayerRef":"1234567890",
          |   "taxPayerType":"A",
          |   "taxPayerName":"name",
          |   "address":{
          |     "postcode":"postcode",
          |     "addressLine1":"line1"
          |   }
          | }""".stripMargin))

    "return 200" in {
      val result = controller.matchOrganisationSa()(fakeRequest)
      status(result) shouldBe Status.OK
    }

    "return 404 when request does not match" in {

      `given`(mockMatchingService.matchSaTax(any(), any(), any())(using any(), any())).willReturn(Future.failed(new MatchingException))

      val result = controller.matchOrganisationSa()(
        FakeRequest()
          .withHeaders(("CorrelationId", UUID.randomUUID().toString))
          .withBody(Json.parse(
            """
              |{
              |   "selfAssessmentUniqueTaxPayerRef":"999999999",
              |   "taxPayerType":"A",
              |   "taxPayerName":"notAname",
              |   "address":{
              |     "postcode":"notApostcode",
              |     "addressLine1":"notAline1"
              |   }
              | }""".stripMargin))
      )

      status(result) shouldBe Status.NOT_FOUND
      contentAsJson(result) shouldBe errorResponse("MATCHING_FAILED", "There is no match for the information provided")
    }

    "return 400 when request is invalid" in {

      val result = controller.matchOrganisationSa()(
        FakeRequest()
          .withHeaders(("CorrelationId", UUID.randomUUID().toString))
          .withBody(Json.obj())
      )

      status(result) shouldBe Status.BAD_REQUEST
    }
  }

  "POST matchOrganisationVat" should {
    val requestContent = VatMatchingRequest("123456789", "organisationName", "line1", "postcode")
    val fakeRequest = FakeRequest("POST", "/")
      .withHeaders(("CorrelationId", UUID.randomUUID().toString))
      .withBody(Json.toJson(requestContent))

    "return 200" in {
      val response = controller.matchOrganisationVat()(fakeRequest)
      status(response) shouldBe Status.OK
    }

    "return 404 when the matching fails" in {
      `given`(mockMatchingService.matchVat(any(), any(), any())(using any(), any())).willReturn(Future.failed(new MatchingException))
      val response = controller.matchOrganisationVat()(fakeRequest)
      status(response) shouldBe Status.NOT_FOUND
      contentAsJson(response) shouldBe errorResponse("MATCHING_FAILED", "There is no match for the information provided")
    }

    "return 400 when the request is missing invalid fields" in {
      val fakeRequest = FakeRequest("POST", "/")
        .withHeaders(("CorrelationId", UUID.randomUUID().toString))
        .withBody(Json.obj())
      val response = controller.matchOrganisationVat()(fakeRequest)
      contentAsJson(response) shouldBe errorResponse(
        "INVALID_REQUEST", "Missing required field(s): [/organisationName, /postcode, /vrn, /addressLine1]"
      )
    }
  }
}
