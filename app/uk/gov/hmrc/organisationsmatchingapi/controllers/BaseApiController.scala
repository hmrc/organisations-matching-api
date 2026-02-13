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

package uk.gov.hmrc.organisationsmatchingapi.controllers

import play.api.Logger
import play.api.i18n.Lang
import play.api.libs.json._
import play.api.mvc._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.{AuthorisationException, AuthorisedFunctions, Enrolment, InsufficientEnrolments}
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, TooManyRequestException}
import uk.gov.hmrc.organisationsmatchingapi.audit.AuditHelper
import uk.gov.hmrc.organisationsmatchingapi.domain.models._
import uk.gov.hmrc.organisationsmatchingapi.utils.UuidValidator
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.Future.successful
import scala.concurrent.{ExecutionContext, Future}

abstract class BaseApiController @Inject() (mcc: MessagesControllerComponents, cc: ControllerComponents)
    extends BackendController(cc) with AuthorisedFunctions {

  protected val logger: Logger = play.api.Logger(this.getClass)

  protected implicit val lang: Lang = mcc.langs.availables.head

  protected override implicit def hc(implicit rh: RequestHeader): HeaderCarrier =
    HeaderCarrierConverter.fromRequest(rh)

  protected def withValidUuid(uuidString: String, uuidName: String)(f: UUID => Future[Result]): Future[Result] =
    UuidValidator
      .validated(uuidString)
      .map(f)
      .getOrElse(successful(ErrorInvalidRequest(s"$uuidName format is invalid").toHttpResponse))

  def withValidJson[T](f: T => Future[Result])(implicit request: Request[JsValue], r: Reads[T]): Future[Result] =
    request.body.validate[T] match {
      case JsSuccess(t, _) => f(t)
      case JsError(errors) =>
        val missingFields = errors.filter(_._2.exists(_.message == "error.path.missing")).map(_._1.toString())
        if (missingFields.nonEmpty) {
          Future.failed(
            new BadRequestException(s"Missing required field(s): ${missingFields.mkString("[", ", ", "]")}")
          )
        } else {
          Future.failed(new BadRequestException(mcc.messagesApi(errors.head._2.head.message, errors.head._1)))
        }
    }

  protected def auditApiResponse(
    response: JsValue,
    correlationId: String,
    matchId: String,
    authScopes: Iterable[String],
    selfLink: String
  )(implicit request: Request[?], auditHelper: AuditHelper): Result = {
    auditHelper.auditApiResponse(
      correlationId,
      matchId,
      authScopes.mkString(","),
      request,
      selfLink,
      Some(response)
    )
    Ok(response)
  }

  private[controllers] def recoveryWithAudit(correlationId: Option[String], matchId: String, url: String)(implicit
    request: RequestHeader,
    auditHelper: AuditHelper
  ): PartialFunction[Throwable, Result] = {
    case _: MatchNotFoundException =>
      auditHelper.auditApiResponse(
        correlationId.getOrElse("-"),
        matchId,
        "",
        request,
        url,
        Some(Json.toJson("Not Found"))
      )
      ErrorNotFound.toHttpResponse
    case e: InvalidBodyException =>
      auditHelper.auditApiFailure(correlationId, matchId, request, url, e.getMessage)
      ErrorInvalidRequest(e.getMessage).toHttpResponse
    case _: MatchingException =>
      auditHelper.auditApiFailure(correlationId, matchId, request, url, "Not Found")
      ErrorMatchingFailed.toHttpResponse
    case e: InsufficientEnrolments =>
      auditHelper.auditApiFailure(correlationId, matchId, request, url, e.getMessage)
      ErrorUnauthorized("Insufficient Enrolments").toHttpResponse
    case e: AuthorisationException =>
      auditHelper.auditApiFailure(correlationId, matchId, request, url, e.getMessage)
      ErrorUnauthorized(e.getMessage).toHttpResponse
    case tmr: TooManyRequestException =>
      auditHelper.auditApiFailure(correlationId, matchId, request, url, tmr.getMessage)
      ErrorTooManyRequests.toHttpResponse
    case br: BadRequestException =>
      auditHelper.auditApiFailure(correlationId, matchId, request, url, br.getMessage)
      ErrorInvalidRequest(br.getMessage).toHttpResponse
    case e: IllegalArgumentException =>
      auditHelper.auditApiFailure(correlationId, matchId, request, url, e.getMessage)
      ErrorInvalidRequest(e.getMessage).toHttpResponse
    case e =>
      logger.error("Unexpected exception", e)
      auditHelper.auditApiFailure(correlationId, matchId, request, url, e.getMessage)
      ErrorInternalServer().toHttpResponse
  }

}

trait PrivilegedAuthentication extends AuthorisedFunctions {

  val internalAuthHelper: InternalAuthHelper

  def authPredicate(scopes: Iterable[String]): Predicate =
    scopes.map(Enrolment(_): Predicate).reduce(_ or _)

  def authenticate(endpointScopes: Iterable[String], matchId: String)(f: Iterable[String] => Future[Result])(implicit
    hc: HeaderCarrier,
    request: RequestHeader,
    auditHelper: AuditHelper,
    ec: ExecutionContext
  ): Future[Result] = {
    if (endpointScopes.isEmpty) throw new Exception("No scopes defined")

    internalAuthHelper.isAuthorised.flatMap {
      case true =>
        auditHelper.auditAuthScopes(matchId, "internal-auth", request)
        f(endpointScopes)
      case false =>
        authorised(authPredicate(endpointScopes)).retrieve(Retrievals.allEnrolments) { scopes =>
          auditHelper.auditAuthScopes(matchId, scopes.enrolments.map(e => e.key).mkString(","), request)
          f(scopes.enrolments.map(e => e.key))
        }
    }
  }
}
