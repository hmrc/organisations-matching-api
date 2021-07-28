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

package unit.uk.gov.hmrc.organisationsmatchingapi.domain.ogd

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json
import uk.gov.hmrc.organisationsmatchingapi.domain.ogd.{Address, CtMatchingResponse}
import util.IfHelpers

class CtMatchingResponseSpec extends AnyWordSpec with Matchers with IfHelpers {

  "PayeMatchingResponse" should {
    "Read and write" in {

      val payeMatchingResponse = CtMatchingResponse(
        employerName = "1234567890",
        address = Address(
          line1 = Some("line1"),
          postcode = Some("postcode")))

      val asJson = Json.toJson(payeMatchingResponse)

      asJson shouldBe Json.parse("""{
                                   |  "employerName" : "1234567890",
                                   |  "address" : {
                                   |    "line1" : "line1",
                                   |    "postcode" : "postcode"
                                   |  }
                                   |}""".stripMargin)
    }
  }
}
