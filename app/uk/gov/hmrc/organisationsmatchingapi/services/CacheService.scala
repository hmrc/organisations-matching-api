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
import uk.gov.hmrc.organisationsmatchingapi.domain.models.{CtMatch, SaMatch}
import uk.gov.hmrc.organisationsmatchingapi.repository.MatchRepository

import scala.concurrent.{ExecutionContext, Future}

class CacheService @Inject()(
                              matchRepository: MatchRepository,
                              val conf: CacheConfiguration)(implicit ec: ExecutionContext) {

  lazy val cacheEnabled: Boolean = conf.cacheEnabled

  def cacheCtUtr(ctMatch: CtMatch, utr: String) = {
    save(ctMatch.matchId.toString, conf.key, ctMatch.copy(utr = Some(utr)))
  }

  def cacheSaUtr(saMatch: SaMatch, utr: String) = {
    save(saMatch.matchId.toString, conf.key, saMatch.copy(utr = Some(utr)))
  }

  def fetch[T: Format](matchId: UUID): Future[Option[T]] = {
    matchRepository.fetchAndGetEntry(matchId.toString, conf.key) flatMap  {
      result =>
        Future.successful(result)
    }
  }

  private def save[T](id: String, key: String, value: T)
                     (implicit formats: Format[T]): Future[Unit] = {
    matchRepository.cache(id, key, value)
  }
}
