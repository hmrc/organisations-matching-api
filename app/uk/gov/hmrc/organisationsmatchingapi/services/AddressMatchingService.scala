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

import uk.gov.hmrc.organisationsmatchingapi.models.Address

class AddressMatchingService {

  //Option 1: Basic match no manipulation
  def basicMatch(knownAddress: Address, ifAddress: Address) = {
    knownAddress.asString.equals(ifAddress.asString)
  }

  // Option 2: A full match by cleaning both objects prior to the comparison
  def cleanMatch(knownAddress: Address, ifAddress: Address) = {
    knownAddress.cleanAll.equals(ifAddress.cleanAll)
  }

  // Option 3: Logic could clean the known facts first if no match; clean the IF (HoDs) data and
  // make a second pass (combine options 3 and 4)
  def tryMatch(knownAddress: Address, ifAddress: Address) = {
    matchAddressCleanKnownFacts(knownAddress, ifAddress) ||
      matchAddressCleanBoth(knownAddress, ifAddress)
  }

  private def matchAddressCleanKnownFacts(knownAddress: Address, ifAddress: Address) = {

    def check1 = knownAddress.ignoreCaseAndSpaces.equals(ifAddress.ignoreCaseAndSpaces)
    def check2 = knownAddress.withoutPunctuation.equals(ifAddress.ignoreCaseAndSpaces)
    def check3 = knownAddress.cleanPostOfficeBox.equals(ifAddress.ignoreCaseAndSpaces)

    check1 || check2 || check3
  }

  private def matchAddressCleanBoth(knownAddress: Address, ifAddress: Address) = {

    def check1 = knownAddress.ignoreCaseAndSpaces.equals(ifAddress.ignoreCaseAndSpaces)
    def check2 = knownAddress.withoutPunctuation.equals(ifAddress.withoutPunctuation)
    def check3 = knownAddress.cleanPostOfficeBox.equals(ifAddress.cleanPostOfficeBox)

    check1 || check2 || check3
  }
}