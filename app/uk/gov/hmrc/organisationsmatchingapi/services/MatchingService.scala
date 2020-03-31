/*
 * Copyright 2020 HM Revenue & Customs
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
import uk.gov.hmrc.organisationsmatchingapi.models.{CompanyMatch, CompanyMatchingRequest, PartnershipMatch, PartnershipMatchingRequest}
import uk.gov.hmrc.organisationsmatchingapi.repository.{CompanyMatchRepository, PartnershipMatchRepository}

class MatchingService @Inject()(companyMatchRepository: CompanyMatchRepository, partnershipMatchRepository: PartnershipMatchRepository) {

  def matchCompany(request: CompanyMatchingRequest) = {
    //TODO Call DES API 1164
    CompanyMatch(request)
  }

  def getCompanyMatch(matchId: UUID) = {
    companyMatchRepository.read(matchId)
  }

  def matchPartnership(request: PartnershipMatchingRequest) = {
    //TODO call DES API 1164
    PartnershipMatch(request)
  }

  def getPartnershipMatch(matchId: UUID) = {
    partnershipMatchRepository.read(matchId)
  }

}
