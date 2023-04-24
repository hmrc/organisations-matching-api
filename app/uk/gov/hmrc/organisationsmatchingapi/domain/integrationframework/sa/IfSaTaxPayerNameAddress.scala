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
import play.api.libs.json.Reads.{maxLength, pattern}
import uk.gov.hmrc.organisationsmatchingapi.domain.integrationframework.common.IfAddress

case class IfSaTaxpayerNameAddress(name: Option[String],
                                   addressType: Option[String],
                                   address: Option[IfAddress])

object IfSaTaxpayerNameAddress {

  val addressTypePattern = "^[A-Za-z0-9\\s -]{1,24}$".r

  private val reads = (
    (JsPath \ "name").readNullable[String](maxLength(100)) and
      (JsPath \ "addressType").readNullable[String](pattern(addressTypePattern, "Address type is not valid")) and
      (JsPath \ "address").readNullable[IfAddress]
    )(IfSaTaxpayerNameAddress.apply _)

  private val writes = Json.writes[IfSaTaxpayerNameAddress]

  implicit val format: Format[IfSaTaxpayerNameAddress] = Format(reads, writes)
}
