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
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.`given`
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.organisationsmatchingapi.audit.AuditHelper
import uk.gov.hmrc.organisationsmatchingapi.controllers.MatchingController
import uk.gov.hmrc.organisationsmatchingapi.services.{CacheService, MatchingService, ScopesHelper, ScopesService}
import util.SpecBase
import play.api.test.Helpers
import uk.gov.hmrc.http.HeaderCarrier
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.api.controllers.ErrorNotFound
import uk.gov.hmrc.organisationsmatchingapi.domain.models.{MatchNotFoundException, UtrMatch}
import unit.uk.gov.hmrc.organisationsmatchingapi.services.ScopesConfig
import org.mockito.ArgumentMatchers.{any, eq => eqTo}

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MatchingControllerSpec extends AnyWordSpec with Matchers with MockitoSugar with SpecBase {

  implicit lazy val materializer: Materializer = fakeApplication.materializer

  trait Setup extends ScopesConfig {
    val sampleCorrelationId       = "188e9400-b636-4a3b-80ba-230a8c72b92a"
    val sampleCorrelationIdHeader = "CorrelationId" -> sampleCorrelationId
    val badCorrelationIdHeader    = "CorrelationId" -> "foo"
    val fakeRequest               = FakeRequest("GET", "/").withHeaders(sampleCorrelationIdHeader)
    val fakeRequestMalformed      = FakeRequest("GET", "/").withHeaders(badCorrelationIdHeader)
    val mockAuthConnector         = mock[AuthConnector]

    lazy val scopeService: ScopesService = new ScopesService(mockConfig)
    lazy val scopesHelper: ScopesHelper = new ScopesHelper(scopeService)

    val auditHelper     = mock[AuditHelper]
    val cacheService    = mock[CacheService]
    val matchingService = mock[MatchingService]
    val matchId         = UUID.fromString("57072660-1df9-4aeb-b4ea-cd2d7f96e430")

    implicit val hc: HeaderCarrier = HeaderCarrier()

    val controller = new MatchingController(
      mockAuthConnector,
      Helpers.stubControllerComponents(),
      cacheService,
      auditHelper,
      matchingService
    )

    val utrMatch = new UtrMatch(matchId, "test")

  }

  "verify matched organisation from cache data" when {
    "given a valid matchId" should {
      "return 200" in new Setup {
        given(matchingService.fetchMatchedOrganisationRecord(eqTo(matchId))(any())).willReturn(Future.successful(utrMatch))

        val result = await(controller.matchedOrganisation(matchId)(fakeRequest))
        status(result) shouldBe OK

        jsonBodyOf(result) shouldBe Json.parse(
          s"""{
             |  "id": "57072660-1df9-4aeb-b4ea-cd2d7f96e430",
             |  "utr": "test"
             |}""".stripMargin
        )
      }
    }

    "match is expired or not present in cache" should {
      "return NOT_FOUND 404" in new Setup {
        given(matchingService.fetchMatchedOrganisationRecord(eqTo(matchId))(any())).willThrow(new MatchNotFoundException)

        val result = await(controller.matchedOrganisation(matchId)(fakeRequest))
        status(result) shouldBe NOT_FOUND

        //jsonBodyOf(result) shouldBe Json.toJson(ErrorNotFound)
      }
    }
  }

}