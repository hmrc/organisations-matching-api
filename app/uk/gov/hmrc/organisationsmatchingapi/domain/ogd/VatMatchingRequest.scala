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

package uk.gov.hmrc.organisationsmatchingapi.domain.ogd

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

case class VatMatchingRequest(vrn: String, organisationName: String, addressLine1: String, postcode: String)

object VatMatchingRequest {
  private val reads: Reads[VatMatchingRequest] = (
    (JsPath \ "vrn").read[String](pattern("^[0-9]{9}$".r, "VRN must be 9-character numeric")) and
      (JsPath \ "organisationName").read[String] and
      (JsPath \ "addressLine1").read[String] and
      (JsPath \ "postcode").read[String]
  )(VatMatchingRequest.apply _)
  private val writes: Writes[VatMatchingRequest] = Json.writes[VatMatchingRequest]
  implicit val vatMatchingRequestFormat: Format[VatMatchingRequest] = Format(reads, writes)
}
