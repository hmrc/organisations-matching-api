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

import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.organisationsmatchingapi.domain.integrationframework.ct.IfCorpTaxCompanyDetails

case class IfVatCustomerInformationSimplified(vrn: String, organisationName: String, addressLine1: Option[String], postcode: Option[String])

object IfVatCustomerInformationSimplified {
  implicit val ifVatCustomerInformationSimplifiedFormat: Format[IfVatCustomerInformationSimplified] =
    Json.format[IfVatCustomerInformationSimplified]

  def fromOriginalIfData(vrn: String, data: IfCorpTaxCompanyDetails): Either[Exception, IfVatCustomerInformationSimplified] = {
    for {
      registeredDetails <- data.registeredDetails.toRight(missingIfData("registeredDetails"))
      name1 <- registeredDetails.name.flatMap(_.name1).toRight(missingIfData("name1"))
      name2 = registeredDetails.name.flatMap(_.name2)
      address <- registeredDetails.address.toRight(missingIfData("address"))
    } yield IfVatCustomerInformationSimplified(vrn, s"$name1${name2.map(" " + _).mkString}", address.line1, address.postcode)
  }

  private def missingIfData(field: String): Exception = new Exception(s"Missing IF data $field")
}
