/*
 * Copyright 2023 HM Revenue & Customs
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
import uk.gov.hmrc.organisationsmatchingapi.domain.ogd.CtMatchingRequest
import unit.uk.gov.hmrc.organisationsmatchingapi.util.IfHelpers

class CtMatchingRequestSpec extends AnyWordSpec with Matchers with IfHelpers {

  "PayeMatchingRequest" should {
    "Read and write" in {

      val payeMatchingRequest = CtMatchingRequest(
        companyRegistrationNumber = "1234567890",
        employerName = "name",
        addressLine1 = "line1",
        postcode = "postcode")

      val asJson = Json.toJson(payeMatchingRequest)

      asJson shouldBe Json.parse("""{
                                   |  "companyRegistrationNumber" : "1234567890",
                                   |  "employerName" : "name",
                                   |  "address": {
                                   |    "addressLine1" : "line1",
                                   |    "postcode" : "postcode"
                                   |  }
                                   |}""".stripMargin)
    }
  }
}
