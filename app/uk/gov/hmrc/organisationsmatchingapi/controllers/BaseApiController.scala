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
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json, OFormat, Reads}
import play.api.mvc.{ControllerComponents, Request, RequestHeader, Result}
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier}
import uk.gov.hmrc.organisationsmatchingapi.errorhandler.NestedError
import uk.gov.hmrc.auth.core.{AuthorisedFunctions, Enrolment}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.organisationsmatchingapi.audit.AuditHelper
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.play.http.HeaderCarrierConverter

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

  def errorResult(errors: IndexedSeq[NestedError]): Future[Result] =
    Future.successful(
      BadRequest(
        Json.obj(
          "code" -> "BAD_REQUEST",
          "message" -> "The request body does not conform to the schema.",
          "errors" -> Json.toJson(errors.toList))))

}

case class SchemaValidationError(keyword: String,
                                 msgs: Seq[String],
                                 instancePath: String)

object SchemaValidationError {
  implicit val format: OFormat[SchemaValidationError] = Json.format
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
