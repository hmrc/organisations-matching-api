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

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class IfConnector @Inject()(
                             servicesConfig: ServicesConfig,
                             http: HttpClient,
                             val auditHelper: AuditHelper) {

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

  private def testVatAuthentication() = {
    Try {
      import scala.concurrent.ExecutionContext.Implicits.global
      implicit val hc: HeaderCarrier = HeaderCarrier()
      http.GET[IfVatCustomerInformation](
          s"$baseUrl/vat/customer/vrn/123456789/information",
          Seq(
            HeaderNames.authorisation -> s"Bearer ${BearerTokens.vat}",
            "Environment" -> integrationFrameworkEnvironment,
            "CorrelationId" -> UUID.randomUUID().toString
          )
        )
        .map(_ => logger.info("TestVatAuthentication: IF API#1363 returned 200"))
        .recover(t => logger.error(s"TestVatAuthentication: IF API#1363 returned error", t))
    }.failed.map(t => logger.error("TestVatAuthentication: Failed to call IF API#1363", t))
  }

  testVatAuthentication()

  def fetchSelfAssessment(matchId: String, utr: String)(
    implicit hc: HeaderCarrier,
    request: RequestHeader,
    ec: ExecutionContext): Future[IfSaTaxpayerDetails] = {

    val SAUrl = s"$baseUrl/organisations/self-assessment/$utr/taxpayer/details"

    callIF[IfSaTaxpayerDetails](SAUrl, matchId, BearerTokens.sa)
  }

  def fetchCorporationTax(matchId: String, crn: String)(
    implicit hc: HeaderCarrier,
    request: RequestHeader,
    ec: ExecutionContext): Future[IfCorpTaxCompanyDetails] = {

    val CTUrl = s"$baseUrl/organisations/corporation-tax/$crn/company/details"

    callIF[IfCorpTaxCompanyDetails](CTUrl, matchId, BearerTokens.ct)
  }

  def fetchVat(matchId: String, vrn: String)(
    implicit hc: HeaderCarrier,
    request: RequestHeader,
    ec: ExecutionContext): Future[IfVatCustomerInformation] = {
    val vatUrl = s"$baseUrl/vat/customer/vrn/$vrn/information"

    callIF[IfVatCustomerInformation](vatUrl, matchId, BearerTokens.vat)
  }

  private def extractCorrelationId(requestHeader: RequestHeader): String = validateCorrelationId(requestHeader).toString

  private def setHeaders(requestHeader: RequestHeader, bearerToken: String): Seq[(String, String)] = Seq(
    HeaderNames.authorisation -> s"Bearer $bearerToken",
    "Environment" -> integrationFrameworkEnvironment,
    "CorrelationId" -> extractCorrelationId(requestHeader)
  )

  private def callIF[R: Format : Manifest](url: String, matchId: String, bearerToken: String)
                                          (implicit hc: HeaderCarrier, request: RequestHeader, ec: ExecutionContext) = {
    recover[R](
      http.GET[R](url, headers = setHeaders(request, bearerToken)).map(auditResponse(url, matchId)),
      extractCorrelationId(request),
      matchId,
      request,
      url
    )
  }

  private def auditResponse[R: Format](url: String, matchId: String)(response: R)
                                      (implicit hc: HeaderCarrier, request: RequestHeader): R = {
    val responseStr = Json.toJson(response).toString
    val correlationId = extractCorrelationId(request)
    auditHelper.auditIfApiResponse(correlationId, matchId, request, url, responseStr)
    response
  }

  private def recover[T](x: Future[T],
                         correlationId: String,
                         matchId: String,
                         request: RequestHeader,
                         requestUrl: String)
                        (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[T] = x.recoverWith {
    case validationError: JsValidationException =>
      logger.warn("Integration Framework JsValidationException encountered")
      auditHelper.auditIfApiFailure(correlationId, matchId, request, requestUrl, s"Error parsing IF response: ${validationError.errors}")
      Future.failed(new InternalServerException("Something went wrong."))
    case Upstream4xxResponse(msg, 404, _, _) =>
      auditHelper.auditIfApiFailure(correlationId, matchId, request, requestUrl, msg)
      logger.warn("Integration Framework NotFoundException encountered")
      Future.failed(new MatchingException)
    case Upstream5xxResponse(msg, code, _, _) =>
      logger.warn(s"Integration Framework Upstream5xxResponse encountered: $code")
      auditHelper.auditIfApiFailure(correlationId, matchId, request, requestUrl, s"Internal Server error: $msg")
      Future.failed(new InternalServerException("Something went wrong."))
    case Upstream4xxResponse(msg, 429, _, _) =>
      logger.warn(s"Integration Framework Rate limited: $msg")
      auditHelper.auditIfApiFailure(correlationId, matchId, request, requestUrl, s"IF Rate limited: $msg")
      Future.failed(new TooManyRequestException(msg))
    case Upstream4xxResponse(msg, code, _, _) =>
      logger.warn(s"Integration Framework Upstream4xxResponse encountered: $code")
      auditHelper.auditIfApiFailure(correlationId, matchId, request, requestUrl, msg)
      Future.failed(new InternalServerException("Something went wrong."))
    case e: Exception =>
      logger.error(s"Integration Framework Exception encountered", e)
      auditHelper.auditIfApiFailure(correlationId, matchId, request, requestUrl, e.getMessage)
      Future.failed(new InternalServerException("Something went wrong."))
  }
}
