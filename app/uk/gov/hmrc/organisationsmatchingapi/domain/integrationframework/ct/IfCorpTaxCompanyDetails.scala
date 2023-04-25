/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.organisationsmatchingapi.domain.integrationframework.ct

import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JsPath, Json}
import play.api.libs.json.Reads.pattern

case class IfCorpTaxCompanyDetails(utr: Option[String],
                                   crn: Option[String],
                                   registeredDetails: Option[IfNameAndAddressDetails],
                                   communicationDetails: Option[IfNameAndAddressDetails])

object IfCorpTaxCompanyDetails {

  val utrPattern = "^[0-9]{10}$".r
  val crnPattern = "^[A-Z0-9]{1,10}$".r

  private val reads = (
    (JsPath \ "utr").readNullable[String](pattern(utrPattern, "UTR is in invalid format")) and
      (JsPath \ "crn").readNullable[String](pattern(crnPattern, "CRN is in invalid format")) and
      (JsPath \ "registeredDetails").readNullable[IfNameAndAddressDetails] and
      (JsPath \ "communicationDetails").readNullable[IfNameAndAddressDetails]
    )(IfCorpTaxCompanyDetails.apply _)

  private val writes = Json.writes[IfCorpTaxCompanyDetails]

  implicit val format: Format[IfCorpTaxCompanyDetails] = Format(reads, writes)
}
