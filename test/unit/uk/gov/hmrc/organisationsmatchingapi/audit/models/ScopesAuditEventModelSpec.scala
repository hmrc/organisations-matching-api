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

package unit.uk.gov.hmrc.organisationsmatchingapi.audit.models

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json
import uk.gov.hmrc.organisationsmatchingapi.audit.models.ScopesAuditEventModel

class ScopesAuditEventModelSpec extends AnyWordSpec with Matchers {
  "Serializes to Json Correctly" in {
    val ifApiResponseEventModel = ScopesAuditEventModel(
      "test",
      "test",
      "test",
      "test",
      "test",
      "test",
      "test",
      "test"
    )

    val result = Json.toJson(ifApiResponseEventModel)

    result shouldBe Json.obj(
      "deviceId" -> "test",
      "input" -> "test",
      "method" -> "test",
      "userAgent" -> "test",
      "apiVersion" -> "test",
      "matchId" -> "test",
      "clientId" -> "test",
      "scopes" -> "test"
    )
  }

  "Deserializes to Json Correctly" in {
    val json = Json.obj(
      "deviceId" -> "test",
      "input" -> "test",
      "method" -> "test",
      "userAgent" -> "test",
      "apiVersion" -> "test",
      "matchId" -> "test",
      "clientId" -> "test",
      "scopes" -> "test"
    )

    val result = Json.fromJson[ScopesAuditEventModel](json)

    result.get shouldBe ScopesAuditEventModel(
      "test",
      "test",
      "test",
      "test",
      "test",
      "test",
      "test",
      "test"
    )
  }
}