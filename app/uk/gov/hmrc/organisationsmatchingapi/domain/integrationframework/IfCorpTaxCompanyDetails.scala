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

package uk.gov.hmrc.organisationsmatchingapi.domain.integrationframework

import play.api.libs.functional.syntax.unlift
import play.api.libs.json.{Format, JsPath}
import play.api.libs.json.Reads.{pattern, _}
import play.api.libs.functional.syntax._

case class IfNameDetails( name1: Option[String],
                          name2: Option[String] )

object IfNameDetails {

  implicit val format: Format[IfNameDetails] = Format(
    (
      (JsPath \ "name1").readNullable[String](maxLength(100)) and
      (JsPath \ "name2").readNullable[String](maxLength(100))
    )(IfNameDetails.apply _),
    (
      (JsPath \ "name1").writeNullable[String] and
      (JsPath \ "name2").writeNullable[String]
    )(unlift(IfNameDetails.unapply))
  )
}

case class IfNameAndAddressDetails(  name: Option[IfNameDetails],
                                     address: Option[IfAddress] )

object IfNameAndAddressDetails {

  implicit val format: Format[IfNameAndAddressDetails] = Format(
    (
      (JsPath \ "name").readNullable[IfNameDetails] and
      (JsPath \ "address").readNullable[IfAddress]
    )(IfNameAndAddressDetails.apply _),
    (
      (JsPath \ "name").writeNullable[IfNameDetails] and
      (JsPath \ "address").writeNullable[IfAddress]
    )(unlift(IfNameAndAddressDetails.unapply))
  )
}

case class IfCorpTaxCompanyDetails( utr: Option[String],
                                    crn: Option[String],
                                    registeredDetails: Option[IfNameAndAddressDetails],
                                    communicationDetails: Option[IfNameAndAddressDetails])

object IfCorpTaxCompanyDetails {

  val utrPattern = "^[0-9]{10}$".r
  val crnPattern = "^[A-Z0-9]{1,10}$".r

  implicit val format: Format[IfCorpTaxCompanyDetails] = Format(
    (
      (JsPath \ "utr").readNullable[String](pattern(utrPattern, "UTR is in invalid format")) and
      (JsPath \ "crn").readNullable[String](pattern(crnPattern, "CRN is in invalid format")) and
      (JsPath \ "registeredDetails").readNullable[IfNameAndAddressDetails] and
      (JsPath \ "communicationDetails").readNullable[IfNameAndAddressDetails]
    )(IfCorpTaxCompanyDetails.apply _),
    (
      (JsPath \ "utr").writeNullable[String] and
      (JsPath \ "crn").writeNullable[String] and
      (JsPath \ "registeredDetails").writeNullable[IfNameAndAddressDetails] and
      (JsPath \ "communicationDetails").writeNullable[IfNameAndAddressDetails]
    )(unlift(IfCorpTaxCompanyDetails.unapply))
  )
}
