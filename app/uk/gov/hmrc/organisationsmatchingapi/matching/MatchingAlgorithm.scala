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

import uk.gov.hmrc.organisationsmatchingapi.matching.FailureReasons._
import uk.gov.hmrc.organisationsmatchingapi.matching.{Bad, Good, Homoglyph, Match}
import uk.gov.hmrc.organisationsmatchingapi.models.{MatchDataCT, MatchDataSA}

trait MatchingAlgorithm {

  lazy val homoglyphs: List[Homoglyph] = List(Homoglyph("apostrophe", '\'', Set('\'', '’')))

  def performMatch(knownFactsData: MatchDataCT, ifData: MatchDataCT): Match =
    throw new Exception("Override me")

  def performMatch(knownFactsData: MatchDataSA, ifData: MatchDataSA): Match =
    throw new Exception("Override me")

  protected def performCrnMatch(knownFactsCrn: Option[String], ifCrn: Option[String]): Match =
    matching[String](knownFactsCrn, ifCrn, CRN_FIELD_CODE, (a, b) => a.equalsIgnoreCase(b))

  protected def performUtrMatch(knownFactsUtr: Option[String], ifUtr: Option[String]): Match =
    matching[String](knownFactsUtr, ifUtr, UTR_FIELD_CODE, (a, b) => a.equalsIgnoreCase(b))

  protected def peformTaxPayerTypeMatch(knownFactsType: Option[String], ifType: Option[String]): Match =
    matching[String](knownFactsType, ifType, TAXPAYER_TYPE_FIELD_CODE, (a, b) => a.equalsIgnoreCase(b))

  protected def performIndividualTaxpayerNameMatch(knownFactsData: MatchDataSA, ifData: MatchDataSA): Match =
    namesMatch(8)(knownFactsData.taxPayerName, ifData.taxPayerName, TAX_PAYER_NAME_FIELD_CODE)

  protected def performPartnershipTaxpayerNameMatch(knownFactsData: MatchDataSA, ifData: MatchDataSA): Match =
    namesMatch(4)(knownFactsData.taxPayerName, ifData.taxPayerName, TAX_PAYER_NAME_FIELD_CODE)

  protected def performEmployerNameMatch(knownFactsData: MatchDataCT, ifData: MatchDataCT): Match =
    namesMatch(4)(knownFactsData.employerName, ifData.employerName, EMPLOYER_NAME_FIELD_CODE)

  protected def performAddressLine1Match(knownFactsAddressLine: Option[String], ifAddressLine: Option[String]): Match =
    addressLine1Match(4)(knownFactsAddressLine, ifAddressLine)

  protected def performPostcodeMatch(knownFactsPostcode: Option[String], ifPostcode: Option[String]): Match =
    matching[String](knownFactsPostcode, ifPostcode, POSTCODE_FIELD_CODE, (a, b) => a.equalsIgnoreCase(b))

  protected def namesMatch(length: Int)(knownFactsName: Option[String], ifName: Option[String], code: Int): Match =
    matching(knownFactsName, ifName, code,
      compose[String, String](_ equalsIgnoreCase _, firstNLetters(length).andThen(homoglyphs.canonicalize)))

  protected def addressLine1Match(length: Int)(knownFactsAddressLine: Option[String], ifAddressLine: Option[String]): Match =
    matching(knownFactsAddressLine, ifAddressLine, ADDRESS_LINE1_FIELD_CODE,
      compose[String, String](_ equalsIgnoreCase _, firstNLetters(length).andThen(homoglyphs.canonicalize)))

  protected def compose[T, U](equate: (U, U) => Boolean, mutate: T => U): (T, T) => Boolean =
    (lhs, rhs) =>equate(mutate(lhs), mutate(rhs))

  protected def firstNLetters(length: Int): String => String = name =>
    name.trim.take(length)

  protected def matching[T](knownFacts: Option[T], ifData: Option[T], fieldCode: Int, matchFunction: (T, T) => Boolean): Match = {
    (knownFacts, ifData) match {
      case (Some(request), Some(ifData)) => {
        if (matchFunction(request, ifData)) {
          Good()
        }
        else {
          Bad(Set(fieldCode + MATCHING_CODE_RANGE))
        }
      }
      case (None, Some(_)) => {
        Bad(Set(fieldCode + KNOWN_FACTS_CODE_RANGE))
      }
      case (Some(_), None) => {
        Bad(Set(fieldCode + IF_CODE_RANGE))
      }
      case (None, None) => {
        Bad(Set(fieldCode + KNOWN_FACTS_CODE_RANGE, fieldCode + IF_CODE_RANGE))
      }
    }
  }
}
