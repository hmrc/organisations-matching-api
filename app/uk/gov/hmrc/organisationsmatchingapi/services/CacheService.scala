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
import uk.gov.hmrc.organisationsmatchingapi.models.{CrnMatch, SaUtrMatch}
import uk.gov.hmrc.organisationsmatchingapi.repository.{CrnMatchRepository, SaUtrMatchRepository, ShortLivedCache}

import scala.concurrent.{ExecutionContext, Future}

class CacheService @Inject()(
    crnMatchRepository: CrnMatchRepository,
    saUtrMatchRepository: SaUtrMatchRepository,
    conf: CacheConfiguration)(implicit ec: ExecutionContext) {

  lazy val cacheEnabled: Boolean = conf.cacheEnabled

  def getCtUtr(cacheId: UUID, fallbackFunction: () => Future[CrnMatch]): Future[CrnMatch] =
    get(cacheId, crnMatchRepository, fallbackFunction)

  def getSaUtr(cacheId: UUID, fallbackFunction: () => Future[SaUtrMatch]): Future[SaUtrMatch] =
    get(cacheId, saUtrMatchRepository, fallbackFunction)

  private def get[T: Format](cacheId: UUID,
                     cachingClient: ShortLivedCache[T],
                     fallbackFunction: () => Future[T]): Future[T] = {
    if (cacheEnabled)
      cachingClient.fetchAndGetEntry(cacheId.toString, conf.key) flatMap {
        case Some(value) =>
          Future.successful(value)
        case None =>
          fallbackFunction.apply() map { result =>
            cachingClient.cache(cacheId.toString, conf.key, result)
            result
          }
      } else {
      fallbackFunction.apply()
    }
  }
}
