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

package unit.uk.gov.hmrc.organisationsmatchingapi.domain.organisationsmatching

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json
import uk.gov.hmrc.organisationsmatchingapi.domain.integrationframework.{IfAddress, IfCorpTaxCompanyDetails, IfNameAndAddressDetails, IfNameDetails}
import uk.gov.hmrc.organisationsmatchingapi.domain.organisationsmatching.{CtKnownFacts, CtOrganisationsMatchingRequest}
import util.IfHelpers

class CtOrganisationsMatchingRequestSpec extends AnyWordSpec with Matchers with IfHelpers {

  "CtOrganisationsMatchingRequest" should {
    "Read and write" in {
      val ctKnownFacts = CtKnownFacts("test", "test", "test", "test")
      val name         = IfNameDetails(Some("test"), Some("test"))
      val address      = IfAddress(Some("test"), None, None, None, Some("test"))
      val details      = IfNameAndAddressDetails(Some(name), Some(address))
      val ctIfData     = IfCorpTaxCompanyDetails(Some("test"), Some("test"), Some(details), Some(details))
      val request      = CtOrganisationsMatchingRequest(ctKnownFacts, ctIfData)
      val asJson       = Json.toJson(request)

      asJson shouldBe Json.parse("""
        |{
        |    "knownFacts": {
        |        "crn": "test",
        |        "name": "test",
        |        "line1": "test",
        |        "postcode": "test"
        |    },
        |    "ifData": {
        |        "utr": "test",
        |        "crn": "test",
        |        "registeredDetails": {
        |          "name": {
        |            "name1": "test",
        |            "name2": "test"
        |          },
        |          "address": {
        |            "line1": "test",
        |            "postcode": "test"
        |          }
        |        },
        |        "communicationDetails": {
        |          "name": {
        |            "name1": "test",
        |            "name2": "test"
        |          },
        |          "address": {
        |            "line1": "test",
        |            "postcode": "test"
        |          }
        |        }
        |     }
        |}
        |""".stripMargin
      )
    }
  }
}
