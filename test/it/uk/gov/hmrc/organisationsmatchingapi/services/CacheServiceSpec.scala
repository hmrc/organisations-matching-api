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

package it.uk.gov.hmrc.organisationsmatchingapi.services

import org.mockito.BDDMockito.`given`
import org.mockito.Mockito.{times, verify}

import java.util.UUID
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito
import uk.gov.hmrc.organisationsmatchingapi.cache.CacheConfiguration
import uk.gov.hmrc.organisationsmatchingapi.models.{Address, CtMatch, CtMatchRequest, SaMatch, SaMatchRequest}
import uk.gov.hmrc.organisationsmatchingapi.repository.MatchRepository
import uk.gov.hmrc.organisationsmatchingapi.services.CacheService
import util.UnitSpec

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class CacheServiceSpec extends UnitSpec with Matchers with GuiceOneAppPerSuite with MockitoSugar with ScalaFutures {

    val mockCacheConfig = mock[CacheConfiguration]
    val mockMatchRepo = mock[MatchRepository]
    val matchId: UUID = UUID.fromString("69f0da0d-4e50-4161-badc-fa39f769bed3")
    val cacheService = new CacheService(mockMatchRepo, mockCacheConfig)
    val ctRequest = CtMatchRequest("crn", "name", Address("line1", "postcode"))
    val ctMatch = CtMatch(ctRequest, matchId)
    val saRequest = SaMatchRequest("utr", "Individual", "name", Address("line1", "postcode"))
    val saMatch = SaMatch(saRequest, matchId)

  "getByMatchId" should {
    "Retrieve CT match details from cache service" in  {
      Mockito.reset(mockMatchRepo)

      given(mockMatchRepo.fetchAndGetEntry[CtMatch](any(), any())(any()))
        .willReturn(Future.successful(Some(ctMatch)))

      val result = cacheService.fetch[CtMatch](matchId)
      await(result) shouldBe Some(ctMatch)
    }

    "CT return none where no details found in cache" in {
      Mockito.reset(mockMatchRepo)

      given(mockMatchRepo.fetchAndGetEntry[CtMatch](any(), any())(any()))
        .willReturn(Future.successful(None))

      val result = cacheService.fetch[CtMatch](matchId)
      await(result) shouldBe None
    }

    "Retrieve SA match details from cache service" in {
      Mockito.reset(mockMatchRepo)

      given(mockMatchRepo.fetchAndGetEntry[SaMatch](any(), any())(any()))
        .willReturn(Future.successful(Some(saMatch)))

      val result = cacheService.fetch[SaMatch](matchId)
      await(result) shouldBe Some(saMatch)
    }

    "SA return none where no details found in cache" in {
      Mockito.reset(mockMatchRepo)

      given(mockMatchRepo.fetchAndGetEntry[SaMatch](any(), any())(any()))
        .willReturn(Future.successful(None))

      val result = cacheService.fetch[SaMatch](matchId)
      await(result) shouldBe None
    }
  }

  "save" should {
    "save updated CT match data to the cache" in {
      Mockito.reset(mockMatchRepo)

      val updatedMatch = ctMatch.copy(utr = Some("TESTUTR"))

      given(mockMatchRepo.cache(any(), any(), any())(any()))
        .willReturn(Future.successful())

      await { cacheService.save(matchId.toString, mockCacheConfig.key, updatedMatch) }
      verify(mockMatchRepo, times(1)).cache(any(), any(), eqTo(updatedMatch))(any())
    }

    "save updated SA match data to the cache" in {
      Mockito.reset(mockMatchRepo)

      val updatedMatch = saMatch.copy(utr = Some("TESTSAUTR"))

      given(mockMatchRepo.cache(any(), any(), any())(any()))
        .willReturn(Future.successful())

      await { cacheService.save(matchId.toString, mockCacheConfig.key, updatedMatch) }
      verify(mockMatchRepo, times(1)).cache(any(), any(), eqTo(updatedMatch))(any())
    }

    "save a CTUTR to the cache" in {
      Mockito.reset(mockMatchRepo)

      val ctUtr    = "SOMECTUTR"
      val expected = ctMatch.copy(utr = Some(ctUtr))

      given(mockMatchRepo.cache(any(), any(), any())(any()))
        .willReturn(Future.successful())

      await { cacheService.cacheCtUtr(ctMatch, ctUtr) }
      verify(mockMatchRepo, times(1)).cache(any(), any(), eqTo(expected))(any())

    }

    "save an SAUTR to the cache" in {
      Mockito.reset(mockMatchRepo)

      val saUtr    = "SOMESAUTR"
      val expected = saMatch.copy(utr = Some(saUtr))

      given(mockMatchRepo.cache(any(), any(), any())(any()))
        .willReturn(Future.successful())

      await { cacheService.cacheSaUtr(saMatch, saUtr) }
      verify(mockMatchRepo, times(1)).cache(any(), any(), eqTo(expected))(any())

    }
  }
}
