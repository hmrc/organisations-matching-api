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

import com.google.inject.Inject
import uk.gov.hmrc.organisationsmatchingapi.domain.models.{CtMatch, MatchNotFoundException, SaMatch, UtrMatch}

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class MatchedService @Inject()(cacheService: CacheService) {

  def fetchCt(matchId: UUID)(implicit ec: ExecutionContext): Future[CtMatch] =
    cacheService.fetch[CtMatch](matchId) flatMap {
      case Some(entry) => Future.successful(entry)
      case _           => Future.failed(new MatchNotFoundException)
    }

  def fetchSa(matchId: UUID)(implicit ec: ExecutionContext): Future[SaMatch] =
    cacheService.fetch[SaMatch](matchId) flatMap {
      case Some(entry) => Future.successful(entry)
      case _           => Future.failed(new MatchNotFoundException)
    }

  def fetchMatchedOrganisationRecord(matchId: UUID)
                                    (implicit ec: ExecutionContext): Future[UtrMatch] =
    cacheService.fetch[UtrMatch](matchId) flatMap {
      case Some(utrMatch) => Future.successful(UtrMatch(utrMatch.matchId, utrMatch.utr))
      case _              => Future.failed(new MatchNotFoundException)
    }
}
