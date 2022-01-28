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

package unit.uk.gov.hmrc.organisationsmatchingapi.domain.integrationframework

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json
import util.IfHelpers

class IfCorpTaxCompanyDetailsSpec extends AnyWordSpec with Matchers with IfHelpers {

    "Writes " in {

      val expected = Json.parse("""{
                                  |  "utr": "1234567890",
                                  |  "crn": "12345678",
                                  |  "registeredDetails": {
                                  |    "name": {
                                  |      "name1": "Waitrose",
                                  |      "name2": "And Partners"
                                  |    },
                                  |    "address": {
                                  |      "line1": "Alfie House",
                                  |      "line2": "Main Street",
                                  |      "line3": "Manchester",
                                  |      "line4": "Londonberry",
                                  |      "postcode": "LN1 1AG"
                                  |    }
                                  |  },
                                  |  "communicationDetails": {
                                  |    "name": {
                                  |      "name1": "Waitrose",
                                  |      "name2": "And Partners"
                                  |    },
                                  |    "address": {
                                  |      "line1": "Orange House",
                                  |      "line2": "Corporation Street",
                                  |      "line3": "London",
                                  |      "line4": "Londonberry",
                                  |      "postcode": "LN1 1AG"
                                  |    }
                                  |  }
                                  |}""".stripMargin)

      val parsed = Json.toJson(ifCorpTaxCompanyDetails)

      parsed shouldBe expected
    }
}
