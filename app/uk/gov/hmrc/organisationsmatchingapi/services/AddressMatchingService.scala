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

  // TODO - Logic could run option one followed by option 2 if no match
  // This means we could potentially trigger a match prior to manipulating the data too much.
  def tryMatch(knownAddress: Address, ifAddress: Address): Unit = {
    matchAddressCleanKnownFacts(knownAddress, ifAddress) ||
      matchAddressCleanBoth(knownAddress, ifAddress)
  }

  // TODO - A full match could be to full clean both objects prior to the comparison
  // Full manipulation and then attempt to match
  def cleanMatch(knownAddress: Address, ifAddress: Address): Unit = {
    matchAddressQuick(knownAddress, ifAddress)
  }

  // Option 1 will try a series of data cleanses on the known facts with a match attempt in between.
  // This means we could potentially trigger a match prior to manipulating the data too much.
  // Minimum manipulation of the HoDs data
  def matchAddressCleanKnownFacts(knownAddress: Address, ifAddress: Address) = {

    def check1 = knownAddress.ignoreCaseAndSpaces.equals(ifAddress.ignoreCaseAndSpaces)
    def check2 = knownAddress.withoutPunctuation.equals(ifAddress.ignoreCaseAndSpaces)
    def check3 = knownAddress.cleanPostOfficeBox.equals(ifAddress.ignoreCaseAndSpaces)

    check1 || check2 || check3
  }

  // Option 2 will try a series of data cleanses on both sets of data with a match attempt in between.
  // This means we could potentially trigger a match prior to manipulating the data too much.
  // Some manipulation of the HoDs data
  def matchAddressCleanBoth(knownAddress: Address, ifAddress: Address) = {

    def check1 = knownAddress.ignoreCaseAndSpaces.equals(ifAddress.ignoreCaseAndSpaces)
    def check2 = knownAddress.withoutPunctuation.equals(ifAddress.withoutPunctuation)
    def check3 = knownAddress.cleanPostOfficeBox.equals(ifAddress.cleanPostOfficeBox)

    check1 || check2 || check3
  }

  // Option 3 fully cleanses (manipulates) the data prior to the match.
  // This means we may get a quicker match however; we have heavily manipulated the data. Possibly unnecessarily.
  def matchAddressQuick(knownAddress: Address, ifAddress: Address) = {

    knownAddress.cleanAll.equals(ifAddress.cleanAll)

  }
}
