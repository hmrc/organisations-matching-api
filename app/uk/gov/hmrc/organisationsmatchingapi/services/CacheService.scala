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

package uk.gov.hmrc.organisationsmatchingapi.services

import play.api.libs.json.Format
import java.util.UUID
import javax.inject.Inject
import uk.gov.hmrc.organisationsmatchingapi.cache.CacheConfiguration
import uk.gov.hmrc.organisationsmatchingapi.repository.{MatchRepository, ShortLivedCache}

import scala.concurrent.{ExecutionContext, Future}

class CacheService @Inject()(
                              matchRepository: MatchRepository,
                              val conf: CacheConfiguration)(implicit ec: ExecutionContext) {

  lazy val cacheEnabled: Boolean = conf.cacheEnabled

  def getByMatchIdCT[T: Format](matchId: UUID) = {
    get(matchId, matchRepository)
  }

  def getByMatchIdSA[T: Format](matchId: UUID) = {
    get(matchId, matchRepository)
  }

  private def get[T: Format](matchId: UUID,
                             cachingClient: ShortLivedCache): Future[Option[T]] = {
    cachingClient.fetchAndGetEntry(matchId.toString, conf.key) flatMap  {
      result =>
        Future.successful(result)
    }
  }
}
