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
import uk.gov.hmrc.organisationsmatchingapi.models.{MatchDataCT, MatchDataSA}
import uk.gov.hmrc.organisationsmatchingapi.services.MatchingAlgorithm

class MatchingCycleSA @Inject() extends MatchingAlgorithm {

  override def performMatch(knownFactsData: MatchDataSA, ifData: MatchDataSA): Match = {

    val utr                     = performUtrMatch(knownFactsData.utr, ifData.utr)
    val taxpayerType            = peformTaxPayerTypeMatch(knownFactsData.taxPayerType, ifData.taxPayerType)
    val individualTaxpayerName  = performIndividualTaxpayerNameMatch(knownFactsData, ifData)
    val partnershipTaxPayerName = performPartnershipTaxpayerNameMatch(knownFactsData, ifData)
    val addressLine1            = performAddressLine1Match(knownFactsData.address.addressLine1, ifData.address.addressLine1)
    val postcode                = performPostcodeMatch(knownFactsData.address.postCode, ifData.address.postCode)

    def common = {
      utr and taxpayerType and addressLine1 and postcode
    }

    def individual = {
      common and individualTaxpayerName
    }

    def partnership = {
      common and partnershipTaxPayerName
    }

    def taxpayerTypeFail = {
      Bad(Set(13))
    }

    knownFactsData.taxPayerType.getOrElse("").toLowerCase match {
      case "individual"  => individual
      case "partnership" => partnership
      case _             => taxpayerTypeFail
    }

  }
}
