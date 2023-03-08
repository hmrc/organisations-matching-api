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

import scala.util.matching.Regex

case class IfAddress( line1: Option[String],
                      line2: Option[String],
                      line3: Option[String],
                      line4: Option[String],
                      postcode: Option[String] )

object IfAddress {

  implicit val format: Format[IfAddress] = Format(
    (
      (JsPath \ "line1").readNullable[String](maxLength(100)) and
      (JsPath \ "line2").readNullable[String](maxLength(100)) and
      (JsPath \ "line3").readNullable[String](maxLength(100)) and
      (JsPath \ "line4").readNullable[String](maxLength(100)) and
      (JsPath \ "postcode").readNullable[String](maxLength(10))
      )(IfAddress.apply _),
    (
      (JsPath \ "line1").writeNullable[String] and
      (JsPath \ "line2").writeNullable[String] and
      (JsPath \ "line3").writeNullable[String] and
      (JsPath \ "line4").writeNullable[String] and
      (JsPath \ "postcode").writeNullable[String]
    )(unlift(IfAddress.unapply))
  )
}

case class IfSaTaxPayerNameAddress( name: Option[String],
                                    addressType: Option[String],
                                    address: Option[IfAddress] )

object IfSaTaxPayerNameAddress {

  val addressTypePattern = "^[A-Za-z0-9\\s -]{1,24}$".r

  implicit val format: Format[IfSaTaxPayerNameAddress] = Format(
    (
      (JsPath \ "name").readNullable[String](maxLength(100)) and
      (JsPath \ "addressType").readNullable[String](pattern(addressTypePattern, "Address type is not valid")) and
      (JsPath \ "address").readNullable[IfAddress]
    )(IfSaTaxPayerNameAddress.apply _),
    (
      (JsPath \ "name").writeNullable[String] and
      (JsPath \ "addressType").writeNullable[String] and
      (JsPath \ "address").writeNullable[IfAddress]
    )(unlift(IfSaTaxPayerNameAddress.unapply))
  )
}

case class IfSaTaxpayerDetails( utr:  Option[String],
                                taxpayerType: Option[String],
                                taxpayerDetails: Option[Seq[IfSaTaxPayerNameAddress]])

object IfSaTaxpayerDetails {

  val utrPattern: Regex = "^[0-9]{10}$".r
  val taxpayerTypePattern: Regex = "^[A-Za-z0-9\\s -]{1,24}$".r

  implicit val format: Format[IfSaTaxpayerDetails] = Format(
    (
      (JsPath \ "utr").readNullable[String](pattern(utrPattern, "UTR is in the incorrect Format")) and
      (JsPath \ "taxpayerType").readNullable[String](pattern(taxpayerTypePattern, "Taxpayer type is not valid")) and
      (JsPath \ "taxpayerDetails").readNullable[Seq[IfSaTaxPayerNameAddress]]
    )(IfSaTaxpayerDetails.apply _),
    (
      (JsPath \ "utr").writeNullable[String] and
      (JsPath \ "taxpayerType").writeNullable[String] and
      (JsPath \ "taxpayerDetails").writeNullable[Seq[IfSaTaxPayerNameAddress]]
    )(unlift(IfSaTaxpayerDetails.unapply))
  )
}
