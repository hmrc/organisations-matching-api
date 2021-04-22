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

import java.util.UUID

import javax.inject.Inject
import uk.gov.hmrc.organisationsmatchingapi.models.{CrnMatch, CrnMatchingRequest, SaUtrMatch, SaUtrMatchingRequest}
import uk.gov.hmrc.organisationsmatchingapi.repository.{CrnMatchRepository, SaUtrMatchRepository}

class MatchingService @Inject()(crnMatchRepository: CrnMatchRepository, saUtrMatchRepository: SaUtrMatchRepository) {

  def matchCrn(request: CrnMatchingRequest) = {
    //TODO Call DES API 1164
    CrnMatch(request)
  }

  def getCrnMatch(matchId: UUID) = {
    crnMatchRepository.fetchAndGetEntry(matchId.toString, matchId.toString)
  }

  def matchSaUtr(request: SaUtrMatchingRequest) = {
    //TODO call DES API 1164
    SaUtrMatch(request)
  }

  def getSaUtrMatch(matchId: UUID) = {
//    saUtrMatchRepository.findAndUpdate()
  }
}
