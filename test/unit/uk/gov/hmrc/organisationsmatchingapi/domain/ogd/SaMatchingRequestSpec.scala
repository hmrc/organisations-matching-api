/*
 * Copyright 2022 HM Revenue & Customs
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
import uk.gov.hmrc.organisationsmatchingapi.domain.ogd.SaMatchingRequest
import util.IfHelpers

class SaMatchingRequestSpec extends AnyWordSpec with Matchers with IfHelpers {

  "SaMatchingRequest" should {
    "Read and write" in {

      val saMatchingRequest = SaMatchingRequest(
        selfAssessmentUniqueTaxPayerRef = "1234567890",
        taxPayerType = "A",
        taxPayerName = "name",
        addressLine1 = "line1",
        postcode = "postcode")

      val asJson = Json.toJson(saMatchingRequest)

      asJson shouldBe Json.parse("""{
                                   |  "selfAssessmentUniqueTaxPayerRef" : "1234567890",
                                   |  "taxPayerType" : "A",
                                   |  "taxPayerName" : "name",
                                   |  "address": {
                                   |    "addressLine1" : "line1",
                                   |    "postcode" : "postcode"
                                   |  }
                                   |}""".stripMargin)
    }
  }
}
