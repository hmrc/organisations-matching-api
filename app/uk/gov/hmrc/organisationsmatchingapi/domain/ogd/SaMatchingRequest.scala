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

case class SaMatchingRequest(
  selfAssessmentUniqueTaxPayerRef: String,
  taxPayerType: String,
  taxPayerName: String,
  addressLine1: String,
  postcode: String
)

object SaMatchingRequest {

  implicit val saMatchingformat: Format[SaMatchingRequest] = Format(
    (
      (JsPath \ "selfAssessmentUniqueTaxPayerRef").read[String] and
        (JsPath \ "taxPayerType").read[String] and
        (JsPath \ "taxPayerName").read[String] and
        (JsPath \ "address" \ "addressLine1").read[String] and
        (JsPath \ "address" \ "postcode").read[String]
    )(SaMatchingRequest.apply _),
    (
      (JsPath \ "selfAssessmentUniqueTaxPayerRef").write[String] and
        (JsPath \ "taxPayerType").write[String] and
        (JsPath \ "taxPayerName").write[String] and
        (JsPath \ "address" \ "addressLine1").write[String] and
        (JsPath \ "address" \ "postcode").write[String]
    )(o => Tuple.fromProductTyped(o))
  )

}
