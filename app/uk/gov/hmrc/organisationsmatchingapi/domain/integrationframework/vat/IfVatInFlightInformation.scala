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


case class IfVatCorrespondenceContactDetails(address: IfVatCustomerAddress)

object IfVatCorrespondenceContactDetails {
  implicit val format: Format[IfVatCorrespondenceContactDetails] = Json.format[IfVatCorrespondenceContactDetails]
}

case class IfVatInFlightInformation(inFlightChanges: Option[IfVatInFlightChanges])

object IfVatInFlightInformation {
  implicit val format: Format[IfVatInFlightInformation] = Json.format[IfVatInFlightInformation]
}

case class IfVatInFlightChanges(
                                 organisationDetails: Option[IfVatInFlightOrganisationDetails],
                                 PPOBDetails: Option[IfPPOB],
                                 correspondenceContactDetails: Option[IfVatCorrespondenceContactDetails]
                               )

object IfVatInFlightChanges {
  implicit val format: Format[IfVatInFlightChanges] = Json.format[IfVatInFlightChanges]
}

case class IfVatInFlightOrganisationDetails(organisationName: Option[String])

object IfVatInFlightOrganisationDetails {
  implicit val format: Format[IfVatInFlightOrganisationDetails] = Json.format[IfVatInFlightOrganisationDetails]
}