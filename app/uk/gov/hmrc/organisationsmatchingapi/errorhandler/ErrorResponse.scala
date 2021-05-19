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

package uk.gov.hmrc.organisationsmatchingapi.errorhandler

import play.api.libs.json.Json.toJson
import play.api.libs.json.{JsValue, Json, Writes}
import play.api.mvc.Result
import play.api.mvc.Results._

sealed abstract class ErrorResponse(val httpStatusCode: Int,
                                    val errorCode: String,
                                    val message: String) {
  def toResult: Result =
    Status(httpStatusCode)(toJson(this)(ErrorResponse.writes))
}

object ErrorResponse {

  case object BadRequest
    extends ErrorResponse(400, "BAD_REQUEST", "Bad knownFacts")
  case object Unauthorized
    extends ErrorResponse(401, "UNAUTHORIZED", "Bearer token is missing or not authorized")
  case object MatchingFailed
    extends ErrorResponse(403, "MATCHING_FAILED", "There is no match for the information provided")
  case object NotFound
    extends ErrorResponse(404, "NOT_FOUND", "Resource was not found")
  case object InvalidAcceptHeader
    extends ErrorResponse(406,
      "ACCEPT_HEADER_INVALID",
      "The accept header is missing or invalid")
  case object InternalServerError
    extends ErrorResponse(500,
      "INTERNAL_SERVER_ERROR",
      "Internal server error")

  implicit val writes: Writes[ErrorResponse] = new Writes[ErrorResponse] {
    override def writes(o: ErrorResponse): JsValue =
      Json.obj("code" -> o.errorCode, "message" -> o.message)
  }

}
