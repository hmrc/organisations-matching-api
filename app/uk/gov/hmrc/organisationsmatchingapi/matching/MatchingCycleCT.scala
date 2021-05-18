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
import uk.gov.hmrc.organisationsmatchingapi.models.MatchDataCT
import uk.gov.hmrc.organisationsmatchingapi.services.MatchingAlgorithm

class MatchingCycleCT @Inject() extends MatchingAlgorithm {

  override def performMatch(knownFactsData: MatchDataCT, ifData: MatchDataCT): Match = {

    val crn          = performCrnMatch(knownFactsData.crn, ifData.crn)
    val empName      = performEmployerNameMatch(knownFactsData, ifData)
    val addressLine1 = performAddressLine1Match(knownFactsData.address.addressLine1, ifData.address.addressLine1)
    val postcode     = performPostcodeMatch(knownFactsData.address.postCode, ifData.address.postCode)

    crn and empName and addressLine1 and postcode
  }
}
