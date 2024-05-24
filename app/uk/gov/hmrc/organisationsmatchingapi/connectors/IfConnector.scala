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

package uk.gov.hmrc.organisationsmatchingapi.connectors

import play.api.Logger
import play.api.libs.json.{Format, Json}
import play.api.mvc.RequestHeader
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http._
import uk.gov.hmrc.organisationsmatchingapi.audit.AuditHelper
import uk.gov.hmrc.organisationsmatchingapi.domain.integrationframework.ct.IfCorpTaxCompanyDetails
import uk.gov.hmrc.organisationsmatchingapi.domain.integrationframework.vat.IfVatCustomerInformation
import uk.gov.hmrc.organisationsmatchingapi.domain.integrationframework.sa.IfSaTaxpayerDetails
import uk.gov.hmrc.organisationsmatchingapi.domain.models.MatchingException
import uk.gov.hmrc.organisationsmatchingapi.play.RequestHeaderUtils.validateCorrelationId
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IfConnector @Inject() (servicesConfig: ServicesConfig, http: HttpClient, val auditHelper: AuditHelper) {

  private val logger = Logger(classOf[IfConnector].getName)

  private val baseUrl = servicesConfig.baseUrl("integration-framework")

  private object BearerTokens {
    val sa = getIFToken("sa")
    val ct = getIFToken("ct")
    val vat = getIFToken("vat")

    private def getIFToken(key: String): String =
      servicesConfig.getString(s"microservice.services.integration-framework.authorization-token.$key")
  }

  private val integrationFrameworkEnvironment = servicesConfig.getString(
    "microservice.services.integration-framework.environment"
  )

  def fetchSelfAssessment(matchId: String, utr: String)(implicit
    hc: HeaderCarrier,
    request: RequestHeader,
    ec: ExecutionContext
  ): Future[IfSaTaxpayerDetails] = {

    val SAUrl = s"$baseUrl/organisations/self-assessment/$utr/taxpayer/details"

    callIF[IfSaTaxpayerDetails](SAUrl, matchId, BearerTokens.sa)
  }

  def fetchCorporationTax(matchId: String, crn: String)(implicit
    hc: HeaderCarrier,
    request: RequestHeader,
    ec: ExecutionContext
  ): Future[IfCorpTaxCompanyDetails] = {

    val CTUrl = s"$baseUrl/organisations/corporation-tax/$crn/company/details"

    callIF[IfCorpTaxCompanyDetails](CTUrl, matchId, BearerTokens.ct)
  }

  def fetchVat(matchId: String, vrn: String)(implicit
    hc: HeaderCarrier,
    request: RequestHeader,
    ec: ExecutionContext
  ): Future[IfVatCustomerInformation] = {
    val vatUrl = s"$baseUrl/vat/customer/vrn/$vrn/information"

    callIF[IfVatCustomerInformation](vatUrl, matchId, BearerTokens.vat)
  }

  private def extractCorrelationId(requestHeader: RequestHeader): String = validateCorrelationId(requestHeader).toString

  private def setHeaders(requestHeader: RequestHeader, bearerToken: String): Seq[(String, String)] = Seq(
    HeaderNames.authorisation -> s"Bearer $bearerToken",
    "Environment"             -> integrationFrameworkEnvironment,
    "CorrelationId"           -> extractCorrelationId(requestHeader)
  )

  private def callIF[R: Format: Manifest](url: String, matchId: String, bearerToken: String)(implicit
    hc: HeaderCarrier,
    request: RequestHeader,
    ec: ExecutionContext
  ) = {
    logger.info(s"[IfConnector][callIF] url - $url")
    recover[R](
      http.GET[R](url, headers = setHeaders(request, bearerToken)).map(auditResponse(url, matchId)),
      extractCorrelationId(request),
      matchId,
      request,
      url
    )
  }

  private def auditResponse[R: Format](url: String, matchId: String)(
    response: R
  )(implicit hc: HeaderCarrier, request: RequestHeader): R = {
    val responseStr = Json.toJson(response).toString
    val correlationId = extractCorrelationId(request)
    auditHelper.auditIfApiResponse(correlationId, matchId, request, url, responseStr)
    response
  }

  private def recover[T](
    x: Future[T],
    correlationId: String,
    matchId: String,
    request: RequestHeader,
    requestUrl: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[T] = x.recoverWith {
    case validationError: JsValidationException =>
      logger.warn("Integration Framework JsValidationException encountered")
      auditHelper.auditIfApiFailure(
        correlationId,
        matchId,
        request,
        requestUrl,
        s"Error parsing IF response: ${validationError.errors}"
      )
      Future.failed(new InternalServerException(s"[IfConnector] Error parsing IF response: ${validationError.errors}"))
    case UpstreamErrorResponse(msg, 404, _, _) =>
      auditHelper.auditIfApiFailure(correlationId, matchId, request, requestUrl, msg)
      logger.warn("[IfConnector] Integration Framework NotFoundException encountered")
      Future.failed(new MatchingException)
    case UpstreamErrorResponse.Upstream5xxResponse(m) =>
      logger.warn(s"[IfConnector] Integration Framework Upstream5xxResponse encountered: ${m.statusCode}")
      auditHelper.auditIfApiFailure(correlationId, matchId, request, requestUrl, s"Internal Server error: ${m.message}")
      Future.failed(new InternalServerException(s"[IfConnector] IF Upstream5xxResponse Status Code: ${m.statusCode} Error Message: ${m.message} "))
    case UpstreamErrorResponse(msg, 429, _, _) =>
      logger.warn(s"[IfConnector] Integration Framework Rate limited: $msg")
      auditHelper.auditIfApiFailure(correlationId, matchId, request, requestUrl, s"IF Rate limited: $msg")
      Future.failed(new TooManyRequestException(msg))
    case UpstreamErrorResponse.Upstream4xxResponse(m) =>
      logger.warn(s"[IfConnector] Integration Framework Upstream4xxResponse encountered: ${m.statusCode}")
      auditHelper.auditIfApiFailure(correlationId, matchId, request, requestUrl, m.message)
      Future.failed(new InternalServerException(s"[IfConnector] IF Upstream4xxResponse Status Code: ${m.statusCode} Error Message: ${m.message} "))
    case e: Exception =>
      logger.error(s"Integration Framework Exception encountered", e)
      auditHelper.auditIfApiFailure(correlationId, matchId, request, requestUrl, e.getMessage)
      Future.failed(new InternalServerException(s"[IfConnector] IF Exception encountered - Error Message: ${e.getMessage}"))
  }
}
