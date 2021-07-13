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
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.RequestHeader
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, InternalServerException, JsValidationException, NotFoundException, Upstream4xxResponse, Upstream5xxResponse}
import uk.gov.hmrc.organisationsmatchingapi.audit.AuditHelper
import uk.gov.hmrc.organisationsmatchingapi.domain.organisationsmatching.{CtOrganisationsMatchingRequest, SaOrganisationsMatchingRequest}
import uk.gov.hmrc.organisationsmatchingapi.errorhandler.ErrorResponse.MatchingException
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class OrganisationsMatchingConnector @Inject()(
                                                servicesConfig: ServicesConfig,
                                                http: HttpClient,
                                                val auditHelper: AuditHelper
                                              )(implicit ec: ExecutionContext) {

  private val baseUrl = servicesConfig.baseUrl("organisations-matching")

  def matchCycleCotax(matchId: String, correlationId: String, postData: CtOrganisationsMatchingRequest)
                     (implicit hc: HeaderCarrier, request: RequestHeader, ec: ExecutionContext) = {

    val url = s"$baseUrl/organisations-matching/perform-match/cotax?matchId=$matchId&correlationId=$correlationId"

    recover(http.POST[CtOrganisationsMatchingRequest, JsValue](url, postData) map {
      response =>
        auditHelper.auditOrganisationsMatchingResponse(correlationId, matchId, request, url, response)
        response
    }, correlationId, matchId, request, url)
  }

  def matchCycleSelfAssessment(matchId: String, correlationId: String, postData: SaOrganisationsMatchingRequest)
                              (implicit hc: HeaderCarrier, request: RequestHeader, ec: ExecutionContext) = {

    val url = s"$baseUrl/organisations-matching/perform-match/self-assessment?matchId=$matchId&correlationId=$correlationId"

    recover(http.POST[SaOrganisationsMatchingRequest, JsValue](url, postData) map {
      response =>
        auditHelper.auditOrganisationsMatchingResponse(correlationId, matchId, request, url, response)
        response
    }, correlationId, matchId, request, url)

  }

  private def recover(x: Future[JsValue],
                      correlationId: String,
                      matchId: String,
                      request: RequestHeader,
                      requestUrl: String)
                     (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[JsValue] = x.recoverWith {
    case notFound: NotFoundException => {
      auditHelper.auditOrganisationsMatchingResponse(correlationId, matchId, request, requestUrl, Json.toJson(notFound.getMessage))
      // No Kibana for security reasons. Splunk only.
      throw new MatchingException
    }
    case validationError: JsValidationException => {
      Logger.warn("Organisations Matching JsValidationException encountered")
      auditHelper.auditOrganisationsMatchingFailure(correlationId, matchId, request, requestUrl, s"Error parsing organisations matching response: ${validationError.errors}")
      Future.failed(new InternalServerException("Something went wrong."))
    }
    case Upstream5xxResponse(msg, code, _, _) => {
      Logger.warn(s"Organisations Matching Upstream5xxResponse encountered: $code")
      auditHelper.auditOrganisationsMatchingFailure(correlationId, matchId, request, requestUrl, s"Internal Server error: $msg")
      Future.failed(new InternalServerException("Something went wrong."))
    }
    case Upstream4xxResponse(msg, code, _, _) => {
      Logger.warn(s"Organisations Matching Upstream4xxResponse encountered: $code")
      auditHelper.auditOrganisationsMatchingFailure(correlationId, matchId, request, requestUrl, msg)
      Future.failed(new InternalServerException("Something went wrong."))
    }
    case e: Exception => {
      Logger.warn(s"Organisations Matching Exception encountered")
      auditHelper.auditOrganisationsMatchingFailure(correlationId, matchId, request, requestUrl, e.getMessage)
      Future.failed(new InternalServerException("Something went wrong."))
    }
  }
}
