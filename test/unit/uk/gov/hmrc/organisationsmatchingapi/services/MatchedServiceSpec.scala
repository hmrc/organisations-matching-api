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

package unit.uk.gov.hmrc.organisationsmatchingapi.services

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.BDDMockito.`given`
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.organisationsmatchingapi.domain.models.{CtMatch, MatchNotFoundException, SaMatch, UtrMatch}
import uk.gov.hmrc.organisationsmatchingapi.domain.ogd.{CtMatchingRequest, SaMatchingRequest}
import uk.gov.hmrc.organisationsmatchingapi.services.{CacheService, MatchedService}

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future.successful

class MatchedServiceSpec extends AnyWordSpec with Matchers with MockitoSugar {

  private val cacheService   = mock[CacheService]
  private val matchedService = new MatchedService(cacheService)
  private val matchId        = UUID.fromString("57072660-1df9-4aeb-b4ea-cd2d7f96e430")

  private val matchRequestCt = new CtMatchingRequest(
    "crn",
    "test",
    "test",
    "test"
  )

  private val matchRequestSa = new SaMatchingRequest(
    "utr",
    "individual",
    "test",
    "test",
    "test"
  )

  private val ctMatch  = new CtMatch(matchRequestCt, utr = Some("test"))
  private val saMatch  = new SaMatch(matchRequestSa, utr = Some("test"))
  private val utrMatch = new UtrMatch(matchId, "test")

  "fetchCt" should {
    "return cache entry" in {
      given(cacheService.fetch[CtMatch](eqTo(matchId))(any())).willReturn(successful(Some(ctMatch)))

      val res = await(matchedService.fetchCt(matchId))
      res shouldBe ctMatch
    }

    "return not found" in {
      given(cacheService.fetch[CtMatch](eqTo(matchId))(any())).willReturn(successful(None))

      intercept[MatchNotFoundException] {
        await(matchedService.fetchCt(matchId))
      }
    }
  }

  "fetchSa" should {
    "return cache entry" in {
      given(cacheService.fetch[SaMatch](eqTo(matchId))(any())).willReturn(successful(Some(saMatch)))

      val res = await(matchedService.fetchSa(matchId))
      res shouldBe saMatch
    }

    "return not found" in {
      given(cacheService.fetch[SaMatch](eqTo(matchId))(any())).willReturn(successful(None))

      intercept[MatchNotFoundException] {
        await(matchedService.fetchSa(matchId))
      }
    }
  }

  "fetch any" should {
    "return cache entry" in {
      given(cacheService.fetch[UtrMatch](eqTo(matchId))(any())).willReturn(successful(Some(utrMatch)))

      val res = await(matchedService.fetchMatchedOrganisationRecord(matchId))
      res shouldBe utrMatch
    }

    "return not found" in {
      given(cacheService.fetch[UtrMatch](eqTo(matchId))(any())).willReturn(successful(None))

      intercept[MatchNotFoundException] {
        await(matchedService.fetchMatchedOrganisationRecord(matchId))
      }
    }
  }
}
