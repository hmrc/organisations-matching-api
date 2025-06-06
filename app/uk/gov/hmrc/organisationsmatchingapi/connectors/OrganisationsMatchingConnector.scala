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

import play.api.Logging
import play.api.http.Status.NOT_FOUND
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.RequestHeader
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException, JsValidationException, StringContextOps, UpstreamErrorResponse}
import uk.gov.hmrc.organisationsmatchingapi.audit.AuditHelper
import uk.gov.hmrc.organisationsmatchingapi.domain.models.MatchingException
import uk.gov.hmrc.organisationsmatchingapi.domain.organisationsmatching.{CtOrganisationsMatchingRequest, SaOrganisationsMatchingRequest, VatOrganisationsMatchingRequest}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class OrganisationsMatchingConnector @Inject()(
                                                servicesConfig: ServicesConfig,
                                                http: HttpClientV2,
                                                auditHelper: AuditHelper
                                              )(implicit ec: ExecutionContext) extends Logging {
  private val baseUrl = servicesConfig.baseUrl("organisations-matching")
  private val requiredHeaders: Seq[String] = Seq("X-Application-ID", "CorrelationId")

  def matchCycleCotax(matchId: String, correlationId: String, postData: CtOrganisationsMatchingRequest)(implicit
                                                                                                        hc: HeaderCarrier,
                                                                                                        request: RequestHeader
  ): Future[JsValue] = {
    val url = s"$baseUrl/organisations-matching/perform-match/cotax?matchId=$matchId&correlationId=$correlationId"

    recover(
      http.post(url"$url").withBody(Json.toJson(postData)).setHeader(hc.headers(requiredHeaders): _*).execute[Either[UpstreamErrorResponse, JsValue]]
        map { response =>
        response.foreach(response =>
          auditHelper.auditOrganisationsMatchingResponse(correlationId, matchId, request, url, response)
        )
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
                              )(implicit hc: HeaderCarrier, request: RequestHeader): Future[JsValue] = {
    val url =
      s"$baseUrl/organisations-matching/perform-match/self-assessment?matchId=$matchId&correlationId=$correlationId"

    recover(
      http.post(url"$url").withBody(Json.toJson(postData)).setHeader(hc.headers(requiredHeaders): _*).execute[Either[UpstreamErrorResponse, JsValue]]
        map { response =>
        response.foreach(response =>
          auditHelper.auditOrganisationsMatchingResponse(correlationId, matchId, request, url, response)
        )
        response
      },
      correlationId,
      matchId,
      request,
      url
    )
  }

  def matchCycleVat(matchId: UUID, correlationId: UUID, postData: VatOrganisationsMatchingRequest)(implicit
                                                                                                   hc: HeaderCarrier,
                                                                                                   request: RequestHeader
  ): Future[JsValue] = {
    val url = s"$baseUrl/organisations-matching/perform-match/vat?matchId=$matchId&correlationId=$correlationId"

    recover(
      http.post(url"$url").withBody(Json.toJson(postData)).setHeader(hc.headers(requiredHeaders): _*).execute[Either[UpstreamErrorResponse, JsValue]]
        .map { response =>
          response.foreach(response =>
            auditHelper
              .auditOrganisationsMatchingResponse(correlationId.toString, matchId.toString, request, url, response)
          )
          response
        },
      correlationId.toString,
      matchId.toString,
      request,
      url
    )
  }

  private def recover(
                       x: Future[Either[UpstreamErrorResponse, JsValue]],
                       correlationId: String,
                       matchId: String,
                       request: RequestHeader,
                       requestUrl: String
                     )(implicit hc: HeaderCarrier): Future[JsValue] = {
    x.recover {
      case validationError: JsValidationException =>
        logger.warn("Organisations Matching JsValidationException encountered")
        auditHelper.auditOrganisationsMatchingFailure(
          correlationId,
          matchId,
          request,
          requestUrl,
          s"Error parsing organisations matching response: ${validationError.errors}"
        )
        throw new InternalServerException("Something went wrong.")
      case e: Exception =>
        logger.error(s"Organisations Matching Exception encountered", e)
        auditHelper.auditOrganisationsMatchingFailure(correlationId, matchId, request, requestUrl, e.getMessage)
        throw new InternalServerException("Something went wrong.")
    }.flatMap {
      case Left(e) if e.statusCode == NOT_FOUND =>
        auditHelper.auditOrganisationsMatchingResponse(
          correlationId,
          matchId,
          request,
          requestUrl,
          Json.toJson(e.message)
        )
        // No Kibana for security reasons. Splunk only.
        Future.failed[JsValue](new MatchingException)
      case Left(UpstreamErrorResponse.Upstream5xxResponse(e)) =>
        logger.warn(s"Organisations Matching Upstream5xxResponse encountered: ${e.statusCode}")
        auditHelper.auditOrganisationsMatchingFailure(
          correlationId,
          matchId,
          request,
          requestUrl,
          s"Internal Server error: ${e.message}"
        )
        Future.failed[JsValue](new InternalServerException("Something went wrong."))
      case Left(e) =>
        logger.warn(s"Organisations Matching unexpected response encountered: ${e.statusCode}")
        auditHelper.auditOrganisationsMatchingFailure(correlationId, matchId, request, requestUrl, e.message)
        Future.failed[JsValue](new InternalServerException("Something went wrong."))
      case Right(json) =>
        Future.successful[JsValue](json)
    }
  }
}
