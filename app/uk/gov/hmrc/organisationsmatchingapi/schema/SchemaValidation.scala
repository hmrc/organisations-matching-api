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

package uk.gov.hmrc.organisationsmatchingapi.schema

import cats.implicits._
import com.eclipsesource.schema._
import play.api.libs.json.{JsError, JsSuccess, _}
import uk.gov.hmrc.organisationsmatchingapi.controllers.SchemaValidationError
import uk.gov.hmrc.organisationsmatchingapi.errorhandler.NestedError

trait SchemaValidation {

  def validateAs[T](schema: SchemaType, json: JsValue)
                   (implicit reads: Reads[T]): Either[IndexedSeq[NestedError], T] = {
    val result: Either[Seq[(JsPath, Seq[JsonValidationError])], T] =
    validateSchema[T](schema, json).asEither
    result.leftMap { e =>
    handleErrors(e)
  }
  }

  def validateSchema[T](schema: SchemaType, json: JsValue)(
    implicit R: Reads[T]): JsResult[T] = {
    val validator = SchemaValidator()
    validator.validate[T](schema, json, R)
  }

  def handleErrors(errors: Seq[(JsPath, Seq[JsonValidationError])])
  : IndexedSeq[NestedError] =
    errors.toJson.value
      .map(_.validate[SchemaValidationError])
      .map {
        case JsSuccess(schemaValidationError, _) =>
          NestedError(
            schemaValidationError.keyword.toUpperCase,
            schemaValidationError.msgs.mkString(", "),
            if (schemaValidationError.instancePath.isEmpty) "/"
            else schemaValidationError.instancePath
          )
        case JsError(parseErrors) =>
          NestedError("UNKNOWN", "Unknown schema validation error.", "")
      }

}

object SchemaValidation extends SchemaValidation
