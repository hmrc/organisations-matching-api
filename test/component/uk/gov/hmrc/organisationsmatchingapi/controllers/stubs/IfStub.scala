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

package component.uk.gov.hmrc.organisationsmatchingapi.controllers.stubs

import com.github.tomakehurst.wiremock.client.WireMock._
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.organisationsmatchingapi.domain.integrationframework.ct.IfCorpTaxCompanyDetails
import uk.gov.hmrc.organisationsmatchingapi.domain.integrationframework.sa.IfSaTaxpayerDetails
import uk.gov.hmrc.organisationsmatchingapi.domain.integrationframework.vat.IfVatCustomerInformation

object IfStub extends MockHost(8443) {


  def searchCorpTaxCompanyDetails(crn: String, ifCorpTaxCompanyDetails: IfCorpTaxCompanyDetails): Unit =
    mock.register(
      get(urlPathEqualTo(s"/organisations/corporation-tax/$crn/company/details"))
        .willReturn(aResponse().withStatus(Status.OK).withBody(Json.toJson(ifCorpTaxCompanyDetails).toString())))

  def searchCorpTaxCompanyDetailsNotFound(crn: String): Unit =
    mock.register(
      get(urlPathEqualTo(s"/organisations/corporation-tax/$crn/company/details"))
        .willReturn(aResponse().withStatus(Status.NOT_FOUND).withBody("NO_DATA_FOUND")))

  def searchCorpTaxCompanyDetailsRateLimited(crn: String): Unit =
    mock.register(
      get(urlPathEqualTo(s"/organisations/corporation-tax/$crn/company/details"))
        .willReturn(aResponse().withStatus(Status.TOO_MANY_REQUESTS)))

  def searchCorpTaxCompanyDetailsCustomResponse(crn: String, status: Int, response: JsValue): Unit =
    mock.register(
      get(urlPathEqualTo(s"/organisations/corporation-tax/$crn/company/details"))
        .willReturn(aResponse().withStatus(status).withBody(Json.toJson(response.toString()).toString())))

  def searchSaCompanyDetails(utr: String, ifSaTaxpayerDetails: IfSaTaxpayerDetails): Unit =
    mock.register(
      get(urlPathEqualTo(s"/organisations/self-assessment/$utr/taxpayer/details"))
        .willReturn(aResponse().withStatus(Status.OK).withBody(Json.toJson(ifSaTaxpayerDetails).toString())))

  def searchSaCompanyDetailsNotFound(utr: String): Unit =
    mock.register(
      get(urlPathEqualTo(s"/organisations/self-assessment/$utr/taxpayer/details"))
        .willReturn(aResponse().withStatus(Status.NOT_FOUND).withBody("NO_DATA_FOUND")))

  def searchSaCompanyDetailsRateLimited(utr: String): Unit =
    mock.register(
      get(urlPathEqualTo(s"/organisations/self-assessment/$utr/taxpayer/details"))
        .willReturn(aResponse().withStatus(Status.TOO_MANY_REQUESTS)))

  def searchSaCompanyDetailsCustomResponse(utr: String, status: Int, response: JsValue): Unit =
    mock.register(
      get(urlPathEqualTo(s"/organisations/self-assessment/$utr/taxpayer/details"))
        .willReturn(aResponse().withStatus(status).withBody(Json.toJson(response.toString()).toString())))

  def searchVatInformation(vrn: String, data: IfVatCustomerInformation): Unit =
    mock.register(
      get(urlPathEqualTo(s"/vat/customer/vrn/$vrn/information"))
        .willReturn(aResponse().withStatus(Status.OK).withBody(Json.toJson(data).toString()))
    )

  def searchVatInformationNotFound(vrn: String): Unit = {
    mock.register(
      get(urlPathEqualTo(s"/vat/customer/vrn/$vrn/information"))
        .willReturn(aResponse().withStatus(Status.NOT_FOUND))
    )
  }
}
