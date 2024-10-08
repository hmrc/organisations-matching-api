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

package unit.uk.gov.hmrc.organisationsmatchingapi.domain.organisationsmatching

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json
import uk.gov.hmrc.organisationsmatchingapi.domain.integrationframework.common.IfAddress
import uk.gov.hmrc.organisationsmatchingapi.domain.integrationframework.sa.{IfSaTaxpayerDetails, IfSaTaxpayerNameAddress}
import uk.gov.hmrc.organisationsmatchingapi.domain.organisationsmatching.{SaKnownFacts, SaOrganisationsMatchingRequest}
import unit.uk.gov.hmrc.organisationsmatchingapi.util.IfHelpers

class SaOrganisationsMatchingRequestSpec extends AnyWordSpec with Matchers with IfHelpers {

  "SaOrganisationsMatchingRequest" should {
    "Read and write" in {
      val saKnownFacts    = SaKnownFacts("test", "Individual", "test", "test", "test")
      val address         = IfAddress(Some("test"), None, None, None, Some("test"))
      val saDetails       = IfSaTaxpayerNameAddress(Some("test"), None, Some(address))
      val taxpayerDetails = Some(Seq(saDetails))
      val saIfData        = IfSaTaxpayerDetails(Some("test"), Some("Individual"), taxpayerDetails)
      val request         = SaOrganisationsMatchingRequest(saKnownFacts, saIfData)
      val asJson          = Json.toJson(request)

      asJson shouldBe Json.parse("""
        |{
        |    "knownFacts": {
        |        "utr": "test",
        |        "taxpayerType": "Individual",
        |        "name": "test",
        |        "line1": "test",
        |        "postcode": "test"
        |    },
        |    "ifData": {
        |        "utr": "test",
        |        "taxpayerType": "Individual",
        |        "taxpayerDetails": [{
        |          "name": "test",
        |          "address": {
        |            "line1": "test",
        |            "postcode": "test"
        |          }
        |        }]
        |     }
        |}
        """.stripMargin
      )

    }
  }
}
