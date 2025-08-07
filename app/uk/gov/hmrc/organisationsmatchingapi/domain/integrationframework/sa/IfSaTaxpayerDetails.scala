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

package uk.gov.hmrc.organisationsmatchingapi.domain.integrationframework.sa

import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JsPath, Json}
import play.api.libs.json.Reads.pattern

import scala.util.matching.Regex

case class IfSaTaxpayerDetails(
  utr: Option[String],
  taxpayerType: Option[String],
  taxpayerDetails: Option[Seq[IfSaTaxpayerNameAddress]]
)

object IfSaTaxpayerDetails {

  private val utrPattern: Regex = "^[0-9]{10}$".r
  private val taxpayerTypePattern: Regex = "^[A-Za-z0-9\\s -]{1,24}$".r

  private val reads = (
    (JsPath \ "utr").readNullable[String](using pattern(utrPattern, "UTR is in the incorrect Format")) and
      (JsPath \ "taxpayerType").readNullable[String](using pattern(taxpayerTypePattern, "Taxpayer type is not valid")) and
      (JsPath \ "taxpayerDetails").readNullable[Seq[IfSaTaxpayerNameAddress]]
  )(IfSaTaxpayerDetails.apply)

  private val writes = Json.writes[IfSaTaxpayerDetails]

  implicit val format: Format[IfSaTaxpayerDetails] = Format(reads, writes)
}
