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

package unit.uk.gov.hmrc.organisationsmatchingapi.errorhandler

import org.scalatest.matchers.should.Matchers
import play.api.libs.json.Json
import uk.gov.hmrc.organisationsmatchingapi.errorhandler.ErrorResponse
import util.UnitSpec

class ErrorResponseSpec extends UnitSpec with Matchers {

  "BadRequest Serialises to JSON as expected" in {
    val errorResponse = ErrorResponse.BadRequest
    val result = Json.toJson(errorResponse)
    result shouldBe Json.obj("code" -> "BAD_REQUEST", "message" -> "Bad request")
  }

  "Unauthorized Serialises to JSON as expected" in {
    val errorResponse = ErrorResponse.Unauthorized
    val result = Json.toJson(errorResponse)
    result shouldBe Json.obj("code" -> "UNAUTHORIZED", "message" -> "Bearer token is missing or not authorized")
  }

  "MatchingFailed Serialises to JSON as expected" in {
    val errorResponse = ErrorResponse.MatchingFailed
    val result = Json.toJson(errorResponse)
    result shouldBe Json.obj("code" -> "MATCHING_FAILED", "message" -> "There is no match for the information provided")
  }

  "NotFound Serialises to JSON as expected" in {
    val errorResponse = ErrorResponse.NotFound
    val result = Json.toJson(errorResponse)
    result shouldBe Json.obj("code" -> "NOT_FOUND", "message" -> "Resource was not found")
  }

  "InvalidAcceptHeader Serialises to JSON as expected" in {
    val errorResponse = ErrorResponse.InvalidAcceptHeader
    val result = Json.toJson(errorResponse)
    result shouldBe Json.obj("code" -> "ACCEPT_HEADER_INVALID", "message" -> "The accept header is missing or invalid")
  }

  "InternalServerError Serialises to JSON as expected" in {
    val errorResponse = ErrorResponse.InternalServerError
    val result = Json.toJson(errorResponse)
    result shouldBe Json.obj("code" -> "INTERNAL_SERVER_ERROR", "message" -> "Internal server error")
  }

}
