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

package uk.gov.hmrc.organisationsmatchingapi.matching

import javax.inject.Inject
import uk.gov.hmrc.organisationsmatchingapi.models.CrnMatchData
import uk.gov.hmrc.organisationsmatchingapi.services.{MatchingAlgorithm, Match}

class CrnMatchingCycle @Inject() extends MatchingAlgorithm {

  override def performMatch(knownFactsData: CrnMatchData, ifData: CrnMatchData): Match = {

    val crn = performCrnMatch(knownFactsData.crn, ifData.crn)
    val empName = performEmployerNameMatch(knownFactsData, ifData)
    val addrLineOne = performAddressLine1Match(knownFactsData, ifData)
    val pcode = performPostcodeMatch(knownFactsData.address.postCode, ifData.address.postCode)

    crn and empName and addrLineOne and pcode
  }
}
