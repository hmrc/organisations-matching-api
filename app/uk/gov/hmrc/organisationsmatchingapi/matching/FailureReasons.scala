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

object FailureReasons {

  val KNOWN_FACTS_CODE_RANGE    = 10
  val IF_CODE_RANGE             = 20
  val MATCHING_CODE_RANGE       = 30

  val CRN_FIELD_CODE            = 1
  val UTR_FIELD_CODE            = 2
  val TAXPAYER_TYPE_FIELD_CODE  = 3
  val EMPLOYER_NAME_FIELD_CODE  = 4
  val TAX_PAYER_NAME_FIELD_CODE = 5
  val ADDRESS_LINE1_FIELD_CODE  = 6
  val POSTCODE_FIELD_CODE       = 7

}
