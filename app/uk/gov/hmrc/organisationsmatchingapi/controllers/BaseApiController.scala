/*
 * Copyright 2020 HM Revenue & Customs
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

import com.eclipsesource.schema.SchemaType
import play.api.Logger
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json, OFormat, Reads}
import play.api.mvc.{ControllerComponents, Request, RequestHeader, Result}
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier}
import uk.gov.hmrc.organisationsmatchingapi.actions.VersionedRequest
import uk.gov.hmrc.organisationsmatchingapi.errorhandler.NestedError
import uk.gov.hmrc.organisationsmatchingapi.schema.{ApiVersion, SchemaValidation, Version_1_0}
import cats.syntax.show._
import uk.gov.hmrc.auth.core.AuthProvider.PrivilegedApplication
import uk.gov.hmrc.auth.core.{AuthProviders, AuthorisedFunctions, Enrolment, InsufficientEnrolments, NoActiveSession, UnsupportedAuthProvider}
import uk.gov.hmrc.play.HeaderCarrierConverter
import uk.gov.hmrc.play.bootstrap.controller.BackendController

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

abstract class BaseApiController (cc: ControllerComponents) extends BackendController(cc) with SchemaValidation with AuthorisedFunctions {

  protected val logger: Logger = play.api.Logger(this.getClass)

  override implicit def hc(implicit rh: RequestHeader): HeaderCarrier =
    HeaderCarrierConverter.fromHeadersAndSessionAndRequest(rh.headers,
      request = Some(rh))

  def withValidJson[T](f: T => Future[Result])(implicit ec: ExecutionContext,
                                               hc: HeaderCarrier,
                                               request: Request[JsValue],
                                               r: Reads[T]): Future[Result] =
    request.body.validate[T] match {
      case JsSuccess(t, _) => f(t)
      case JsError(errors) =>
        Future.failed(new BadRequestException(errors.toString()))
    }

  protected def minimumApiVersion: ApiVersion = Version_1_0

  type SchemaVersions = Map[ApiVersion, SchemaType]

  def loadVersionedSchemas(
                            path: String,
                            base: String = "/public/api/conf",
                            minimumApiVersion: ApiVersion = minimumApiVersion): SchemaVersions =
    ApiVersion.all.map { version =>
      version -> Json
        .parse(
          getClass.getResourceAsStream(s"$base/${version.show}/schemas/$path"))
        .validate[SchemaType]
        .get
    }.toMap

  protected def withVersionedJsonBody[T](schemas: SchemaVersions)(
    f: T => Future[Result])(implicit request: VersionedRequest[JsValue],
                            m: Manifest[T],
                            reads: Reads[T]): Future[Result] = {
    val result =
      SchemaValidation.validateAs[T](schemas(request.apiVersion), request.body)
    result.fold(errorResult, f)
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
