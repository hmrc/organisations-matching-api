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

package unit.uk.gov.hmrc.organisationsmatchingapi.models

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsString, JsSuccess, Json}
import uk.gov.hmrc.organisationsmatchingapi.models.JsonFormatters._

import java.util.UUID

class JsonFormattersSpec extends AnyWordSpec with Matchers {

  "Writes UUID as String" in {
    val testId = UUID.fromString("69f0da0d-4e50-4161-badc-fa39f769bed3")
    val result = Json.toJson(testId)

    result shouldBe JsString("69f0da0d-4e50-4161-badc-fa39f769bed3")
  }

  "Reads UUID String as UUID" in {
    val jsString = JsString("69f0da0d-4e50-4161-badc-fa39f769bed3")
    val result = Json.fromJson(jsString)

    result shouldBe JsSuccess(UUID.fromString("69f0da0d-4e50-4161-badc-fa39f769bed3"))
  }



}
