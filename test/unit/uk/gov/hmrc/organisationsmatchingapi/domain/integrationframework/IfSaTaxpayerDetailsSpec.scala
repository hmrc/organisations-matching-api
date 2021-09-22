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

package unit.uk.gov.hmrc.organisationsmatchingapi.domain.integrationframework

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json
import util.IfHelpers

class IfSaTaxpayerDetailsSpec extends AnyWordSpec with Matchers with IfHelpers {

  val saTaxPayerDetailsString: String = """{
                                  |  "utr": "1234567890",
                                  |  "taxpayerType": "Individual",
                                  |  "taxpayerDetails": [
                                  |    {
                                  |      "name": "John Smith II",
                                  |      "addressType": "Base",
                                  |      "address": {
                                  |        "line1": "Alfie House",
                                  |        "line2": "Main Street",
                                  |        "line3": "Birmingham",
                                  |        "line4": "West midlands",
                                  |        "postcode": "B14 6JH"
                                  |      }
                                  |    },
                                  |    {
                                  |      "name": "Joanne Smith",
                                  |      "addressType": "Correspondence",
                                  |      "address": {
                                  |        "line1": "Alice House",
                                  |        "line2": "Main Street",
                                  |        "line3": "Manchester",
                                  |        "postcode": "MC1 4AA"
                                  |      }
                                  |    },
                                  |    {
                                  |      "name": "Daffy Duck",
                                  |      "addressType": "Correspondence",
                                  |      "address": {
                                  |        "line1": "1 Main Street",
                                  |        "line2": "Disneyland",
                                  |        "line3": "Liverpool",
                                  |        "postcode": "MC1 4AA"
                                  |      }
                                  |    }
                                  |  ]
                                  |}""".stripMargin

  "IfSaTaxpayerDetails" should {
    "Read and write" in {
      val parsed = Json.toJson(saTaxpayerDetails)
      parsed shouldBe Json.parse(saTaxPayerDetailsString)
    }
  }
}
