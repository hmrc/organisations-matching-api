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

import play.api.libs.json.Format.GenericFormat

import javax.inject.{Inject, Singleton}
import java.util.UUID
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.organisationsmatchingapi.connectors.OrganisationsMatchingConnector
import uk.gov.hmrc.organisationsmatchingapi.domain.models.UtrMatch
import uk.gov.hmrc.organisationsmatchingapi.domain.organisationsmatching.MatchedOrganisationRecord
import uk.gov.hmrc.organisationsmatchingapi.repository.MatchRepository

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.Future.{failed, successful}

@Singleton
class MatchingService @Inject()(
                               cacheService: CacheService)
                               (implicit ec: ExecutionContext) {

  def fetchMatchedOrganisationRecord(matchId: UUID)
                                    (implicit hc: HeaderCarrier) =
    cacheService.fetch[UtrMatch](matchId) flatMap {
      case Some(utrMatch) =>
        successful(MatchedOrganisationRecord(utrMatch.utr, utrMatch.id))
      case _ => failed(new Exception)
          // following merge with HODS-154:
//      case _ => failed(new MatchNotFoundException)
    }
}
