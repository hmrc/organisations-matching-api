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

package uk.gov.hmrc.organisationsmatchingapi.domain.integrationframework.vat

import play.api.libs.json.{Json, OFormat}

case class IfVatCustomerInformationSimplified(
  vrn: String,
  organisationName: String,
  addressLine1: Option[String],
  postcode: Option[String]
)

object IfVatCustomerInformationSimplified {
  implicit val ifVatCustomerInformationSimplifiedFormat: OFormat[IfVatCustomerInformationSimplified] =
    Json.format[IfVatCustomerInformationSimplified]

  def fromOriginalIfData(
    vrn: String,
    data: IfVatCustomerInformation
  ): Either[Exception, IfVatCustomerInformationSimplified] =
    for {
      organisationName <-
        data.approvedInformation.customerDetails.organisationName.toRight(missingIfData("organisationName"))
      address <- data.approvedInformation.PPOB.address.toRight(missingIfData("address"))
    } yield IfVatCustomerInformationSimplified(vrn, organisationName, address.line1, address.postCode)

  private def missingIfData(field: String): Exception = new Exception(s"Missing IF data $field")
}
