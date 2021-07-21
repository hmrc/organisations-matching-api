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

package uk.gov.hmrc.organisationsmatchingapi.controllers

import play.api.Logger
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json, Reads}
import play.api.mvc.{ControllerComponents, Request, RequestHeader, Result}
import uk.gov.hmrc.organisationsmatchingapi.errorhandler.{ErrorResponse}
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, InternalServerException, TooManyRequestException}
import uk.gov.hmrc.organisationsmatchingapi.errorhandler.NestedError
import uk.gov.hmrc.auth.core.{AuthorisationException, AuthorisedFunctions, Enrolment, InsufficientEnrolments}
import uk.gov.hmrc.organisationsmatchingapi.audit.AuditHelper
import uk.gov.hmrc.organisationsmatchingapi.domain.models.{ErrorInternalServer, ErrorInvalidRequest, ErrorMatchingFailed, ErrorNotFound, ErrorTooManyRequests, ErrorUnauthorized, InvalidBodyException, MatchNotFoundException, MatchingException}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import java.util.UUID
import scala.concurrent.Future.successful
import scala.util.{Success, Try}

import scala.concurrent.{ExecutionContext, Future}

abstract class BaseApiController (cc: ControllerComponents) extends BackendController(cc) with AuthorisedFunctions {

  protected val logger: Logger = play.api.Logger(this.getClass)

  protected override implicit def hc(implicit rh: RequestHeader): HeaderCarrier =
    HeaderCarrierConverter.fromRequest(rh)

  def withValidJson[T](f: T => Future[Result])(implicit ec: ExecutionContext,
                                               hc: HeaderCarrier,
                                               request: Request[JsValue],
                                               r: Reads[T]): Future[Result] =
    request.body.validate[T] match {
      case JsSuccess(t, _) => f(t)
      case JsError(errors) =>
        Future.failed(new BadRequestException(errors.toString()))
    }

  protected def withUuid(uuidString: String)(f: UUID => Future[Result]): Future[Result] =
    Try(UUID.fromString(uuidString)) match {
      case Success(uuid) => f(uuid)
      case _             => successful(ErrorResponse.NotFound.toResult)
    }

  def errorResult(errors: IndexedSeq[NestedError]): Future[Result] =
    Future.successful(
      BadRequest(
        Json.obj(
          "code" -> "BAD_REQUEST",
          "message" -> "The request body does not conform to the schema.",
          "errors" -> Json.toJson(errors.toList))))

  private[controllers] def recoveryWithAudit(correlationId: Option[String], matchId: String, url: String)(
    implicit request: RequestHeader,
    auditHelper: AuditHelper): PartialFunction[Throwable, Result] = {
    case _: MatchNotFoundException => {
      auditHelper.auditApiResponse(correlationId.getOrElse("-"), matchId, "", request, url, Some(Json.toJson("Not Found")))
      ErrorNotFound.toHttpResponse
    }
    case e: InvalidBodyException => {
      auditHelper.auditApiFailure(correlationId, matchId, request, url, e.getMessage)
      ErrorInvalidRequest(e.getMessage).toHttpResponse
    }
    case _: MatchingException => {
      auditHelper.auditApiFailure(correlationId, matchId, request, url, "Not Found")
      ErrorMatchingFailed.toHttpResponse
    }
    case e: InsufficientEnrolments => {
      auditHelper.auditApiFailure(correlationId, matchId, request, url, e.getMessage)
      ErrorUnauthorized("Insufficient Enrolments").toHttpResponse
    }
    case e: AuthorisationException => {
      auditHelper.auditApiFailure(correlationId, matchId, request, url, e.getMessage)
      ErrorUnauthorized(e.getMessage).toHttpResponse
    }
    case tmr: TooManyRequestException => {
      auditHelper.auditApiFailure(correlationId, matchId, request, url, tmr.getMessage)
      ErrorTooManyRequests.toHttpResponse
    }
    case br: BadRequestException => {
      auditHelper.auditApiFailure(correlationId, matchId, request, url, br.getMessage)
      ErrorInvalidRequest(br.getMessage).toHttpResponse
    }
    case e: IllegalArgumentException => {
      auditHelper.auditApiFailure(correlationId, matchId, request, url, e.getMessage)
      ErrorInvalidRequest(e.getMessage).toHttpResponse
    }
    case e: InternalServerException => {
      auditHelper.auditApiFailure(correlationId, matchId, request, url, e.getMessage)
      ErrorInternalServer("Something went wrong").toHttpResponse
    }
    case e => {
      auditHelper.auditApiFailure(correlationId, matchId, request, url, e.getMessage)
      ErrorInternalServer("Something went wrong").toHttpResponse
    }
  }

}

trait PrivilegedAuthentication extends AuthorisedFunctions {

  def authPredicate(scopes: Iterable[String]): Predicate =
    scopes.map(Enrolment(_): Predicate).reduce(_ or _)

  def authenticate(endpointScopes: Iterable[String], matchId: String)(f: Iterable[String] => Future[Result])(
    implicit hc: HeaderCarrier,
    request: RequestHeader,
    auditHelper: AuditHelper,
    ec: ExecutionContext): Future[Result] = {

    if (endpointScopes.isEmpty) throw new Exception("No scopes defined")

      authorised(authPredicate(endpointScopes)).retrieve(Retrievals.allEnrolments) {
        case scopes => {

          auditHelper.auditAuthScopes(matchId, scopes.enrolments.map(e => e.key).mkString(","), request)

          f(scopes.enrolments.map(e => e.key))
        }
      }
  }

  def requiresPrivilegedAuthentication(body: => Future[Result])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Result] =
    authorised(Enrolment("read:individuals-matching"))(body)
}
