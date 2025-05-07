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

package it.uk.gov.hmrc.organisationsmatchingapi.services

import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.BDDMockito.`given`
import org.mockito.Mockito
import org.mockito.Mockito.{times, verify}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.organisationsmatchingapi.cache.{CacheConfiguration, InsertResult}
import uk.gov.hmrc.organisationsmatchingapi.domain.models.{CtMatch, SaMatch, VatMatch}
import uk.gov.hmrc.organisationsmatchingapi.domain.ogd.{CtMatchingRequest, SaMatchingRequest}
import uk.gov.hmrc.organisationsmatchingapi.repository.MatchRepository
import uk.gov.hmrc.organisationsmatchingapi.services.CacheService

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CacheServiceSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with MockitoSugar with ScalaFutures {

  val mockCacheConfig: CacheConfiguration = mock[CacheConfiguration]
  val mockMatchRepo: MatchRepository = mock[MatchRepository]
  val matchId: UUID = UUID.fromString("69f0da0d-4e50-4161-badc-fa39f769bed3")
  val cacheService = new CacheService(mockMatchRepo, mockCacheConfig)
  val ctRequest: CtMatchingRequest = CtMatchingRequest("crn", "name", "line1", "postcode")
  val ctMatch: CtMatch = CtMatch(ctRequest, matchId)
  val saRequest: SaMatchingRequest = SaMatchingRequest("utr", "Individual", "name", "line1", "postcode")
  val saMatch: SaMatch = SaMatch(saRequest, matchId)
  val vatMatch: VatMatch = VatMatch(matchId, Some("somevrn"))

  "getByMatchId" should {
    "Retrieve CT match details from cache service" in {
      Mockito.reset(mockMatchRepo)

      `given`(mockMatchRepo.fetchAndGetEntry[CtMatch](any())(any()))
        .willReturn(Future.successful(Some(ctMatch)))

      val result = cacheService.fetch[CtMatch](matchId)
      await(result) shouldBe Some(ctMatch)
    }

    "CT return none where no details found in cache" in {
      Mockito.reset(mockMatchRepo)

      `given`(mockMatchRepo.fetchAndGetEntry[CtMatch](any())(any()))
        .willReturn(Future.successful(None))

      val result = cacheService.fetch[CtMatch](matchId)
      await(result) shouldBe None
    }

    "Retrieve SA match details from cache service" in {
      Mockito.reset(mockMatchRepo)

      `given`(mockMatchRepo.fetchAndGetEntry[SaMatch](any())(any()))
        .willReturn(Future.successful(Some(saMatch)))

      val result = cacheService.fetch[SaMatch](matchId)
      await(result) shouldBe Some(saMatch)
    }

    "SA return none where no details found in cache" in {
      Mockito.reset(mockMatchRepo)

      `given`(mockMatchRepo.fetchAndGetEntry[SaMatch](any())(any()))
        .willReturn(Future.successful(None))

      val result = cacheService.fetch[SaMatch](matchId)
      await(result) shouldBe None
    }
  }

  "cacheCtUtr" should {
    "save a CTUTR to the cache" in {
      Mockito.reset(mockMatchRepo)

      val ctUtr = "SOMECTUTR"
      val expected = ctMatch.copy(utr = Some(ctUtr))

      `given`(mockMatchRepo.cache(any(), any())(any()))
        .willReturn(Future.successful(InsertResult.InsertSucceeded))

      await {
        cacheService.cacheCtUtr(ctMatch, ctUtr)
      }
      verify(mockMatchRepo, times(1)).cache(any(), eqTo(expected))(any())
    }
  }

  "cacheVatVrn" should {
    "save VRN to the cache" in {
      Mockito.reset(mockMatchRepo)

      `given`(mockMatchRepo.cache(any(), any())(any()))
        .willReturn(Future.successful(InsertResult.InsertSucceeded))

      await(cacheService.cacheVatVrn(vatMatch))
      verify(mockMatchRepo, times(1)).cache(any(), eqTo(vatMatch))(any())
    }
  }

  "cacheSaUtr" should {
    "save an SAUTR to the cache" in {
      Mockito.reset(mockMatchRepo)

      val saUtr = "SOMESAUTR"
      val expected = saMatch.copy(utr = Some(saUtr))

      `given`(mockMatchRepo.cache(any(), any())(any()))
        .willReturn(Future.successful(InsertResult.InsertSucceeded))

      await {
        cacheService.cacheSaUtr(saMatch, saUtr)
      }
      verify(mockMatchRepo, times(1)).cache(any(), eqTo(expected))(any())
    }
  }
}
