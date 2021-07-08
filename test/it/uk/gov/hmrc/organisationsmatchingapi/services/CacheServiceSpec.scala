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

import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.`given`

import java.util.UUID
import java.util.UUID.randomUUID
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.organisationsmatchingapi.cache.CacheConfiguration
import uk.gov.hmrc.organisationsmatchingapi.models.{Address, CtMatch, CtMatchRequest, SaMatch, SaMatchRequest}
import uk.gov.hmrc.organisationsmatchingapi.repository.{MatchRepository, ShortLivedCache}
import uk.gov.hmrc.organisationsmatchingapi.services.CacheService
import org.mockito.ArgumentMatchers.{eq => eqTo}
import uk.gov.hmrc.cache.repository.CacheRepository
import util.UnitSpec

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class CacheServiceSpec extends UnitSpec with Matchers with GuiceOneAppPerSuite with MockitoSugar with ScalaFutures {

  trait Setup {
    val mockCacheConfig = mock[CacheConfiguration]
    val mockMatchRepo = mock[MatchRepository]
    val matchId:UUID = UUID.fromString("69f0da0d-4e50-4161-badc-fa39f769bed3")
    val cacheService = new CacheService(mockMatchRepo, mockCacheConfig)
    val ctRequest    = CtMatchRequest("crn", "name", Address("line1", "postcode"))
    val ctMatch      = CtMatch(ctRequest, matchId)
    val saRequest    = SaMatchRequest("utr", "Individual", "name", Address("line1", "postcode"))
    val saMatch      = SaMatch(saRequest, matchId)
  }

  "Retrieve CT match details from cache service" in new Setup {
    given(mockMatchRepo.fetchAndGetEntry[CtMatch](any(), any())(any()))
      .willReturn(Future.successful(Some(ctMatch)))

    val result = cacheService.getByMatchIdCT[CtMatch](matchId)
    await(result) shouldBe Some(ctMatch)
  }

  "Retrieve SA match details from cache service" in new Setup {
    given(mockMatchRepo.fetchAndGetEntry[SaMatch](any(), any())(any()))
      .willReturn(Future.successful(Some(saMatch)))

    val result = cacheService.getByMatchIdSA[SaMatch](matchId)
    await(result) shouldBe Some(saMatch)
  }

  "CT return none where no details found in cache" in new Setup {
    given(mockMatchRepo.fetchAndGetEntry[CtMatch](any(), any())(any()))
      .willReturn(Future.successful(None))

    val result = cacheService.getByMatchIdCT[CtMatch](matchId)
    await(result) shouldBe None
  }

  "SA return none where no details found in cache" in new Setup {
    given(mockMatchRepo.fetchAndGetEntry[SaMatch](any(), any())(any()))
      .willReturn(Future.successful(None))

    val result = cacheService.getByMatchIdSA[SaMatch](matchId)
    await(result) shouldBe None
  }

}
