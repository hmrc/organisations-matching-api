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
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.RequestHeader
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, InternalServerException, JsValidationException, NotFoundException, Upstream4xxResponse, Upstream5xxResponse}
import uk.gov.hmrc.organisationsmatchingapi.audit.AuditHelper
import uk.gov.hmrc.organisationsmatchingapi.domain.organisationsmatching.{CtOrganisationsMatchingRequest, SaOrganisationsMatchingRequest, VatOrganisationsMatchingRequest}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.Inject
import uk.gov.hmrc.organisationsmatchingapi.domain.models.MatchingException

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class OrganisationsMatchingConnector @Inject() (
  servicesConfig: ServicesConfig,
  http: HttpClient,
  val auditHelper: AuditHelper
) {

  val logger: Logger = Logger(this.getClass)

  private val baseUrl = servicesConfig.baseUrl("organisations-matching")
  val requiredHeaders: Seq[String] = Seq("X-Application-ID", "CorrelationId")

  def matchCycleCotax(matchId: String, correlationId: String, postData: CtOrganisationsMatchingRequest)(implicit
    hc: HeaderCarrier,
    request: RequestHeader,
    ec: ExecutionContext
  ): Future[JsValue] = {

    val url = s"$baseUrl/organisations-matching/perform-match/cotax?matchId=$matchId&correlationId=$correlationId"

    recover(
      http.POST[CtOrganisationsMatchingRequest, JsValue](url, postData, hc.headers(requiredHeaders)) map { response =>
        auditHelper.auditOrganisationsMatchingResponse(correlationId, matchId, request, url, response)
        response
      },
      correlationId,
      matchId,
      request,
      url
    )
  }

  def matchCycleSelfAssessment(
    matchId: String,
    correlationId: String,
    postData: SaOrganisationsMatchingRequest
  )(implicit hc: HeaderCarrier, request: RequestHeader, ec: ExecutionContext): Future[JsValue] = {

    val url =
      s"$baseUrl/organisations-matching/perform-match/self-assessment?matchId=$matchId&correlationId=$correlationId"

    recover(
      http.POST[SaOrganisationsMatchingRequest, JsValue](url, postData, hc.headers(requiredHeaders)) map { response =>
        auditHelper.auditOrganisationsMatchingResponse(correlationId, matchId, request, url, response)
        response
      },
      correlationId,
      matchId,
      request,
      url
    )

  }

  def matchCycleVat(matchId: UUID, correlationId: UUID, data: VatOrganisationsMatchingRequest)(implicit
    hc: HeaderCarrier,
    request: RequestHeader,
    ec: ExecutionContext
  ): Future[JsValue] = {
    val url = s"$baseUrl/organisations-matching/perform-match/vat?matchId=$matchId&correlationId=$correlationId"

    recover(
      http.POST[VatOrganisationsMatchingRequest, JsValue](url, data, hc.headers(requiredHeaders)).map { response =>
        auditHelper.auditOrganisationsMatchingResponse(correlationId.toString, matchId.toString, request, url, response)
        response
      },
      correlationId.toString,
      matchId.toString,
      request,
      url
    )
  }

  private def recover(
    x: Future[JsValue],
    correlationId: String,
    matchId: String,
    request: RequestHeader,
    requestUrl: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[JsValue] = x.recoverWith {
    case notFound: NotFoundException =>
      auditHelper.auditOrganisationsMatchingResponse(
        correlationId,
        matchId,
        request,
        requestUrl,
        Json.toJson(notFound.getMessage)
      )
      // No Kibana for security reasons. Splunk only.
      Future.failed(new MatchingException)
    case validationError: JsValidationException =>
      logger.warn("Organisations Matching JsValidationException encountered")
      auditHelper.auditOrganisationsMatchingFailure(
        correlationId,
        matchId,
        request,
        requestUrl,
        s"Error parsing organisations matching response: ${validationError.errors}"
      )
      Future.failed(new InternalServerException("Something went wrong."))
    case Upstream5xxResponse(msg, code, _, _) =>
      logger.warn(s"Organisations Matching Upstream5xxResponse encountered: $code")
      auditHelper.auditOrganisationsMatchingFailure(
        correlationId,
        matchId,
        request,
        requestUrl,
        s"Internal Server error: $msg"
      )
      Future.failed(new InternalServerException("Something went wrong."))
    case Upstream4xxResponse(msg, code, _, _) =>
      logger.warn(s"Organisations Matching Upstream4xxResponse encountered: $code")
      auditHelper.auditOrganisationsMatchingFailure(correlationId, matchId, request, requestUrl, msg)
      Future.failed(new InternalServerException("Something went wrong."))
    case e: Exception =>
      logger.error(s"Organisations Matching Exception encountered", e)
      auditHelper.auditOrganisationsMatchingFailure(correlationId, matchId, request, requestUrl, e.getMessage)
      Future.failed(new InternalServerException("Something went wrong."))
  }
}
