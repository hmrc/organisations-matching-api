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

import play.api.libs.json.{Format, JsPath}
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads.pattern

case class CtMatchingRequest(
  companyRegistrationNumber: String,
  employerName: String,
  addressLine1: String,
  postcode: String
)

object CtMatchingRequest {

  val crnPattern = "^[A-Z0-9]{1,10}$".r

  implicit val ctMatchingformat: Format[CtMatchingRequest] = Format(
    (
      (JsPath \ "companyRegistrationNumber").read[String](pattern(crnPattern, "error.crn")) and
        (JsPath \ "employerName").read[String] and
        (JsPath \ "address" \ "addressLine1").read[String] and
        (JsPath \ "address" \ "postcode").read[String]
    )(CtMatchingRequest.apply _),
    (
      (JsPath \ "companyRegistrationNumber").write[String] and
        (JsPath \ "employerName").write[String] and
        (JsPath \ "address" \ "addressLine1").write[String] and
        (JsPath \ "address" \ "postcode").write[String]
    )(unlift(CtMatchingRequest.unapply))
  )
}
