/*
 * Copyright 2026 HM Revenue & Customs
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
import play.api.libs.json.{Format, Json}
import play.api.mvc.RequestHeader
import uk.gov.hmrc.http.*
import uk.gov.hmrc.organisationsmatchingapi.audit.AuditHelper
import uk.gov.hmrc.organisationsmatchingapi.domain.integrationframework.vat.IfVatCustomerInformation
import uk.gov.hmrc.organisationsmatchingapi.domain.models.MatchingException
import uk.gov.hmrc.organisationsmatchingapi.play.RequestHeaderUtils.validateCorrelationId

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

trait MigrationConnector extends Logging {

  protected def auditHelper: AuditHelper
  implicit protected def ec: ExecutionContext

  def fetchVat(matchId: String, vrn: String)(implicit
    hc: HeaderCarrier,
    request: RequestHeader
  ): Future[IfVatCustomerInformation]

  def extractCorrelationId(requestHeader: RequestHeader): String =
    validateCorrelationId(requestHeader).toString

  def auditResponse[R: Format](url: String, matchId: String)(
    response: R
  )(implicit hc: HeaderCarrier, request: RequestHeader): R = {
    val responseStr = Json.toJson(response).toString
    val correlationId = extractCorrelationId(request)
    auditHelper.auditIfApiResponse(correlationId, matchId, request, url, responseStr)
    response
  }

  def recover[T](
    x: Future[T],
    correlationId: String,
    matchId: String,
    request: RequestHeader,
    requestUrl: String,
    apiName: String
  )(implicit hc: HeaderCarrier): Future[T] = x.recoverWith {
    case validationError: JsValidationException =>
      logger.warn(s"$apiName JsValidationException encountered")
      auditHelper.auditIfApiFailure(
        correlationId,
        matchId,
        request,
        requestUrl,
        s"Error parsing $apiName response: ${validationError.errors}"
      )
      Future.failed(new InternalServerException("Something went wrong."))

    case UpstreamErrorResponse(msg, 404, _, _) =>
      auditHelper.auditIfApiFailure(correlationId, matchId, request, requestUrl, msg)
      logger.warn(s"$apiName NotFoundException encountered through 404")
      Future.failed(new MatchingException)

    case UpstreamErrorResponse(msg, 422, _, _) =>
      // Check if this is the specific HIP 422 error code '002'
      def extractHipErrorCode(msg: String): Option[String] = {
        val JsonBodyRegex = """(\{.*\})""".r
        for {
          jsonStr <- JsonBodyRegex.findFirstIn(msg)
          json    <- Try(Json.parse(jsonStr)).toOption
          code    <- (json \ "error" \ "code").asOpt[String]
        } yield code
      }
      val isHipNotFoundError = extractHipErrorCode(msg).contains("002")

      if (isHipNotFoundError) {
        auditHelper.auditIfApiFailure(correlationId, matchId, request, requestUrl, msg)
        logger.warn(s"$apiName NotFoundException encountered through 422")
        Future.failed(new MatchingException)
      } else {
        logger.warn(s"$apiName Upstream4xxResponse through 422 encountered: $msg")
        auditHelper.auditIfApiFailure(correlationId, matchId, request, requestUrl, msg)
        Future.failed(new InternalServerException("Something went wrong."))
      }

    case UpstreamErrorResponse.Upstream5xxResponse(m) =>
      logger.warn(s"$apiName Upstream5xxResponse encountered: ${m.statusCode}")
      auditHelper.auditIfApiFailure(correlationId, matchId, request, requestUrl, s"Internal Server error: ${m.message}")
      Future.failed(new InternalServerException("Something went wrong."))

    case UpstreamErrorResponse(msg, 429, _, _) =>
      logger.warn(s"$apiName Rate limited: $msg")
      auditHelper.auditIfApiFailure(correlationId, matchId, request, requestUrl, s"$apiName Rate limited: $msg")
      Future.failed(new TooManyRequestException(msg))

    case UpstreamErrorResponse.Upstream4xxResponse(m) =>
      logger.warn(s"$apiName Upstream4xxResponse encountered: ${m.statusCode}")
      auditHelper.auditIfApiFailure(correlationId, matchId, request, requestUrl, m.message)
      Future.failed(new InternalServerException("Something went wrong."))

    case e: Exception =>
      logger.error(s"$apiName Exception encountered", e)
      auditHelper.auditIfApiFailure(correlationId, matchId, request, requestUrl, e.getMessage)
      Future.failed(new InternalServerException("Something went wrong."))
  }
}
