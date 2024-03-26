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

package uk.gov.hmrc.organisationsmatchingapi.services

import play.api.libs.json.Format

import java.util.UUID
import javax.inject.Inject
import uk.gov.hmrc.organisationsmatchingapi.cache.{CacheConfiguration, InsertResult}
import uk.gov.hmrc.organisationsmatchingapi.domain.models.{CtMatch, SaMatch, VatMatch}
import uk.gov.hmrc.organisationsmatchingapi.repository.MatchRepository

import scala.concurrent.{ExecutionContext, Future}

class CacheService @Inject() (matchRepository: MatchRepository, val conf: CacheConfiguration)(implicit
  ec: ExecutionContext
) {

  lazy val cacheEnabled: Boolean = conf.cacheEnabled

  def cacheCtUtr(ctMatch: CtMatch, utr: String): Future[InsertResult] =
    matchRepository.cache(ctMatch.matchId.toString, ctMatch.copy(utr = Some(utr)))

  def cacheSaUtr(saMatch: SaMatch, utr: String): Future[InsertResult] =
    matchRepository.cache(saMatch.matchId.toString, saMatch.copy(utr = Some(utr)))

  def cacheVatVrn(vatMatch: VatMatch): Future[InsertResult] =
    matchRepository.cache(vatMatch.matchId.toString, vatMatch)

  def fetch[T: Format](matchId: UUID): Future[Option[T]] =
    matchRepository.fetchAndGetEntry(matchId.toString).flatMap(Future.successful)
}
