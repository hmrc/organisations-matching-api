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
import uk.gov.hmrc.organisationsmatchingapi.domain.integrationframework.IfAddress
import uk.gov.hmrc.organisationsmatchingapi.domain.ogd.{SaMatchingRequest, SaMatchingResponse}
import util.IfHelpers

class SaMatchingResponseSpec extends AnyWordSpec with Matchers with IfHelpers {

  "SaMatchingResponse" should {
    "Read and write" in {

      val saMatchingResponse = SaMatchingResponse(
        taxPayerType = "A",
        taxPayerName = "name",
        address = IfAddress(
          line1 = Some("line1"),
          line2 = Some("line2"),
          line3 = Some("line3"),
          line4 = Some("line4"),
          postcode = Some("postcode")))

      val asJson = Json.toJson(saMatchingResponse)

      asJson shouldBe Json.parse("""{
                                   |  "taxPayerType" : "A",
                                   |  "taxPayerName" : "name",
                                   |  "address" : {
                                   |    "line1" : "line1",
                                   |    "line2" : "line2",
                                   |    "line3" : "line3",
                                   |    "line4" : "line4",
                                   |    "postcode" : "postcode"
                                   |  }
                                   |}""".stripMargin)
    }
  }
}
