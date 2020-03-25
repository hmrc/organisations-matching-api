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

package uk.gov.hmrc.organisationsmatchingapi.schema

import com.eclipsesource.schema.SchemaType
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.organisationsmatchingapi.errorhandler.NestedError
import uk.gov.hmrc.organisationsmatchingapi.helpers.BaseSpec


case class TestObject(testValue: String)

object TestObject {
  implicit val format: OFormat[TestObject] = Json.format[TestObject]
}

class SchemaValidationSpec extends BaseSpec {

  val testSchema = Json
    .parse("""
             |{
             |  "type": "object",
             |  "description": "A test JSON schema",
             |  "required": [
             |    "testValue"
             |  ],
             |  "properties": {
             |    "testValue": {
             |      "type": "string",
             |      "description": "The test value which must be a string"
             |    }
             |  }
             |}
           """.stripMargin)
    .validate[SchemaType]
    .asOpt
    .getOrElse(throw new IllegalArgumentException("invalid schema json"))

  "validateAs" should {
    "pass for a valid JSON payload" in {
      val outcome = SchemaValidation.validateAs[TestObject](testSchema, Json.toJson(TestObject("a valid string")))

      outcome.isRight shouldBe true
    }

    "return a schema validation error for an invalid JSON payload" in {
      val outcome = SchemaValidation.validateAs[TestObject](testSchema, Json.obj("testValue" -> 1))

      assertErrorPath("/testValue", outcome) shouldBe true
    }
  }

  def assertErrorPath[A](path: String, outcome: Either[IndexedSeq[NestedError], A]): Boolean =
    outcome.fold(e => assertError(path, e.toList), _ => false)

  private def assertError(path: String, errors: List[NestedError]): Boolean =
    errors.head.path == path

}
