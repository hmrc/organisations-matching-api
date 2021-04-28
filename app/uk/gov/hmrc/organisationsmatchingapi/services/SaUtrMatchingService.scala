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

import uk.gov.hmrc.organisationsmatchingapi.models.SaUtrMatchData

class SaUtrMatchingService {

  //Option 1: Basic match no manipulation
  def basicMatch(knownFactsData: SaUtrMatchData, ifData: SaUtrMatchData) = {
    knownFactsData.asString.equals(ifData.asString)
  }

  // Option 2: A full match by cleaning both objects prior to the comparison
  def cleanMatch(knownFactsData: SaUtrMatchData, ifData: SaUtrMatchData) = {
    knownFactsData.cleanAll.equals(ifData.cleanAll)
  }

  // Option 3: Logic could clean the known facts first if no match; clean the IF (HoDs) data and
  // make a second pass (combine options 3 and 4)
  def tryMatch(knownFactsData: SaUtrMatchData, ifData: SaUtrMatchData) = {
    matchCleanKnownFacts(knownFactsData, ifData) ||
      matchCleanBothSets(knownFactsData, ifData)
  }

  private def matchCleanKnownFacts(knownFactsData: SaUtrMatchData, ifData: SaUtrMatchData) = {

    def check1 = knownFactsData.ignoreCaseAndSpaces.equals(ifData.ignoreCaseAndSpaces)
    def check2 = knownFactsData.withoutPunctuation.equals(ifData.ignoreCaseAndSpaces)
    def check3 = knownFactsData.cleanPostOfficeBox.equals(ifData.ignoreCaseAndSpaces)

    check1 || check2 || check3
  }

  private def matchCleanBothSets(knownFactsData: SaUtrMatchData, ifData: SaUtrMatchData) = {

    def check1 = knownFactsData.ignoreCaseAndSpaces.equals(ifData.ignoreCaseAndSpaces)
    def check2 = knownFactsData.withoutPunctuation.equals(ifData.withoutPunctuation)
    def check3 = knownFactsData.cleanPostOfficeBox.equals(ifData.cleanPostOfficeBox)

    check1 || check2 || check3
  }
}
