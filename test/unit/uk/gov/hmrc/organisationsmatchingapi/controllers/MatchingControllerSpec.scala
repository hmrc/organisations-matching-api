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

import java.util.UUID

import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.BDDMockito.`given`
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status
import play.api.libs.json.Json
import play.api.mvc.PlayBodyParsers
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.auth.core.{AuthConnector, Enrolment, Enrolments}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.organisationsmatchingapi.audit.AuditHelper
import uk.gov.hmrc.organisationsmatchingapi.controllers.MatchingController
import uk.gov.hmrc.organisationsmatchingapi.services.{CacheService, MatchingService, ScopesHelper, ScopesService}
import util.SpecBase

import scala.concurrent.Future

class MatchingControllerSpec extends AnyWordSpec with SpecBase with Matchers with MockitoSugar {

  private implicit val ec  = scala.concurrent.ExecutionContext.Implicits.global

  private val mockAuthConnector = mock[AuthConnector]
  private val mockCacheService = mock[CacheService]
  private val mockAuditHelper = mock[AuditHelper]
  private val mockScopesService = mock[ScopesService]
  private val scopesHelper = new ScopesHelper(mockScopesService)
  private val mockMatchingService = mock[MatchingService]
  private val mockBodyParser = mock[PlayBodyParsers]

  private val controller = new MatchingController(
    mockAuthConnector,
    Helpers.stubControllerComponents(),
    mockScopesService,
    scopesHelper,
    mockBodyParser,
    mockCacheService,
    mockMatchingService
  )(ec, mockAuditHelper)

  given(mockAuthConnector.authorise(any(), refEq(Retrievals.allEnrolments))(any(), any()))
    .willReturn(Future.successful(Enrolments(Set(Enrolment("test-scope"), Enrolment("test-scope-1")))))

  given(mockScopesService.getAllScopes).willReturn(List("test-scope", "test-scope-1"))

  given(mockMatchingService.matchSaTax(any(), any(), any())(any(), any())).willReturn(Future.successful(Json.toJson("match")))
  given(mockMatchingService.matchCoTax(any(), any(), any())(any(), any())).willReturn(Future.successful(Json.toJson("match")))

  given(mockScopesService.getInternalEndpoints(any())).willReturn(Seq())
  given(mockScopesService.getExternalEndpoints(any())).willReturn(Seq())


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
  }
}