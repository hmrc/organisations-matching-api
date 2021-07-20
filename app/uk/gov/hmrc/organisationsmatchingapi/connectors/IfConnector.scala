/*
 * Copyright 2021 HM Revenue & Customs
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
import play.api.libs.json.Json
import play.api.mvc.RequestHeader
import uk.gov.hmrc.http._
import uk.gov.hmrc.organisationsmatchingapi.audit.AuditHelper
import uk.gov.hmrc.organisationsmatchingapi.domain.integrationframework.{IfCorpTaxCompanyDetails, IfSaTaxpayerDetails}
import uk.gov.hmrc.organisationsmatchingapi.play.RequestHeaderUtils.validateCorrelationId
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IfConnector @Inject()(
                             servicesConfig: ServicesConfig,
                             http: HttpClient,
                             val auditHelper: AuditHelper)(implicit ec: ExecutionContext) {

  private val logger = Logger(classOf[IfConnector].getName)

  private val baseUrl = servicesConfig.baseUrl("integration-framework")

  private val integrationFrameworkBearerToken =
    servicesConfig.getString(
      "microservice.services.integration-framework.authorization-token"
    )

  private val integrationFrameworkEnvironment = servicesConfig.getString(
    "microservice.services.integration-framework.environment"
  )

  def fetchSelfAssessment(matchId: String, utr: String)(
    implicit hc: HeaderCarrier,
    request: RequestHeader,
    ec: ExecutionContext) = {

    val SAUrl =
      s"$baseUrl/organisations/self-assessment/$utr/taxpayer/details"

    callSa(SAUrl, matchId) map {
      response => Json.fromJson[IfSaTaxpayerDetails](Json.toJson(response))
    }

  }

  def fetchCorporationTax(matchId: String, crn: String)(
    implicit hc: HeaderCarrier,
    request: RequestHeader,
    ec: ExecutionContext) = {

    val CTUrl =
      s"$baseUrl/organisations/corporation-tax/$crn/company/details"

    callCt(CTUrl, matchId) map {
      response => Json.fromJson[IfCorpTaxCompanyDetails](Json.toJson(response))
    }

  }

  private def extractCorrelationId(requestHeader: RequestHeader) = validateCorrelationId(requestHeader).toString

  private def header(extraHeaders: (String, String)*)(
    implicit hc: HeaderCarrier) =
    hc.copy(
      authorization =
        Some(Authorization(s"Bearer $integrationFrameworkBearerToken")))
      .withExtraHeaders(
        Seq("Environment" -> integrationFrameworkEnvironment) ++ extraHeaders: _*)

  private def callSa(url: String, matchId: String)
                    (implicit hc: HeaderCarrier, request: RequestHeader, ec: ExecutionContext) =
    recover[IfSaTaxpayerDetails](http.GET[IfSaTaxpayerDetails](url)(implicitly, header(), ec) map { response =>
      auditHelper.auditIfApiResponse(extractCorrelationId(request), matchId, request, url, Json.toJson(response).toString)
      response
    }, extractCorrelationId(request), matchId, request, url)

  private def callCt(url: String, matchId: String)
                    (implicit hc: HeaderCarrier, request: RequestHeader, ec: ExecutionContext) =
    recover[IfCorpTaxCompanyDetails](http.GET[IfCorpTaxCompanyDetails](url)(implicitly, header(), ec) map { response =>
      auditHelper.auditIfApiResponse(extractCorrelationId(request), matchId, request, url, Json.toJson(response).toString)
      response
    }, extractCorrelationId(request), matchId, request, url)

  private def recover[T](x: Future[T],
                      correlationId: String,
                      matchId: String,
                      request: RequestHeader,
                      requestUrl: String)
                     (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[T] = x.recoverWith {

    case validationError: JsValidationException => {
      logger.warn("Integration Framework JsValidationException encountered")
      auditHelper.auditIfApiFailure(correlationId, matchId, request, requestUrl, s"Error parsing IF response: ${validationError.errors}")
      Future.failed(new InternalServerException("Something went wrong."))
    }
    case notFound: NotFoundException => {
      auditHelper.auditIfApiFailure(correlationId, matchId, request, requestUrl, notFound.getMessage)
      logger.warn("Integration Framework NotFoundException encountered")
      Future.failed(notFound)
    }
    case Upstream5xxResponse(msg, code, _, _) => {
      logger.warn(s"Integration Framework Upstream5xxResponse encountered: $code")
      auditHelper.auditIfApiFailure(correlationId, matchId, request, requestUrl, s"Internal Server error: $msg")
      Future.failed(new InternalServerException("Something went wrong."))
    }
    case Upstream4xxResponse(msg, 429, _, _) => {
      logger.warn(s"Integration Framework Rate limited: $msg")
      auditHelper.auditIfApiFailure(correlationId, matchId, request, requestUrl, s"IF Rate limited: $msg")
      Future.failed(new TooManyRequestException(msg))
    }
    case Upstream4xxResponse(msg, code, _, _) => {
      logger.warn(s"Integration Framework Upstream4xxResponse encountered: $code")
      auditHelper.auditIfApiFailure(correlationId, matchId, request, requestUrl, msg)
      Future.failed(new InternalServerException("Something went wrong."))
    }
    case e: Exception => {
      logger.warn(s"Integration Framework Exception encountered")
      auditHelper.auditIfApiFailure(correlationId, matchId, request, requestUrl, e.getMessage)
      Future.failed(new InternalServerException("Something went wrong."))
    }
  }
}