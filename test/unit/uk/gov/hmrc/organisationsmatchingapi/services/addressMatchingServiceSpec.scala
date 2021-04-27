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

package unit.uk.gov.hmrc.organisationsmatchingapi.services

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.organisationsmatchingapi.models
import uk.gov.hmrc.organisationsmatchingapi.services.AddressMatchingService

class addressMatchingServiceSpec extends AnyWordSpec with Matchers {

  trait Fixture {
    val addressMatchingService = new AddressMatchingService
  }

  "addressMatchingService" should {
    "basicMatch" should {
      "match a basic address" in new Fixture {
        val knownFacts = models.Address("foo", "bar", None, None, "code")
        val ifAddress = models.Address("foo", "bar", None, None, "code")

        addressMatchingService.basicMatch(knownFacts, ifAddress) shouldBe true
      }

      "fail to match where white space differs" in new Fixture {
        val knownFacts = models.Address("foo ", "bar", None, None, "code")
        val ifAddress = models.Address("foo", "bar", None, None, "code")

        addressMatchingService.basicMatch(knownFacts, ifAddress) shouldBe false
      }

      "fail to match where punctuation differs" in new Fixture {
        val knownFacts = models.Address("foo", "bar!", None, None, "code")
        val ifAddress = models.Address("foo", "bar", None, None, "code")

        addressMatchingService.basicMatch(knownFacts, ifAddress) shouldBe false

      }

      "fail to match where PO box differs" in new Fixture {
        val knownFacts = models.Address("P.O. Box787", "bar", None, None, "code")
        val ifAddress = models.Address("PO Box787", "bar", None, None, "code")

        addressMatchingService.basicMatch(knownFacts, ifAddress) shouldBe false
      }
    }

    "cleanMatch" should {
      "match a basic address" in new Fixture {
        val knownFacts = models.Address("foo", "bar", None, None, "code")
        val ifAddress = models.Address("foo", "bar", None, None, "code")

        addressMatchingService.cleanMatch(knownFacts, ifAddress) shouldBe true
      }

      "match where white space differs" in new Fixture {
        val knownFacts = models.Address("foo ", "bar", None, None, "code")
        val ifAddress = models.Address("foo", "bar", None, None, "code")

        addressMatchingService.cleanMatch(knownFacts, ifAddress) shouldBe true
      }

      "match where punctuation differs" in new Fixture {
        val knownFacts = models.Address("foo", "bar!", None, None, "code")
        val ifAddress = models.Address("foo", "bar", None, None, "code")

        addressMatchingService.cleanMatch(knownFacts, ifAddress) shouldBe true

      }

      "match where PO box differs" in new Fixture {
        val knownFacts = models.Address("P.O. Box787", "bar", None, None, "code")
        val ifAddress = models.Address("PO Box787", "bar", None, None, "code")

        addressMatchingService.cleanMatch(knownFacts, ifAddress) shouldBe true
      }

      "fail to match where the addess itself differs" in new Fixture {
        val knownFacts = models.Address("foo", "bar", None, None, "code")
        val ifAddress = models.Address("foo", "bat", None, None, "code")

        addressMatchingService.cleanMatch(knownFacts, ifAddress) shouldBe false
      }
    }

    "tryMatch" should {
      "match an exact address" in new Fixture {
        val knownFacts = models.Address("foo", "bar", None, None, "code")
        val ifAddress = models.Address("foo", "bar", None, None, "code")

        addressMatchingService.tryMatch(knownFacts, ifAddress) shouldBe true
      }

      "match an address where case differs" in new Fixture {
        val knownFacts = models.Address("Foo", "Bar", None, None, "Code")
        val ifAddress = models.Address("foo", "bar", None, None, "code")

        addressMatchingService.tryMatch(knownFacts, ifAddress) shouldBe true
      }

      "match an address where known facts has punctuation marks" in new Fixture {
        val knownFacts = models.Address("foo.", "bar!", None, None, "code,")
        val ifAddress = models.Address("foo", "bar", None, None, "code")

        addressMatchingService.tryMatch(knownFacts, ifAddress) shouldBe true
      }

      "match an address where known facts has whitespace" in new Fixture {
        val knownFacts = models.Address(" foo ", " ba  r", None, None, " c ode")
        val ifAddress = models.Address("foo", "bar", None, None, "code")

        addressMatchingService.tryMatch(knownFacts, ifAddress) shouldBe true
      }

      "match an address where case differs in the HoDs" in new Fixture {
        val knownFacts = models.Address("Foo", "Bar", None, None, "Code")
        val ifAddress = models.Address("foo", "baR", None, None, "cOde")

        addressMatchingService.tryMatch(knownFacts, ifAddress) shouldBe true
      }

      "match an address where known facts has punctuation marks in the HoDs" in new Fixture {
        val knownFacts = models.Address("foo.", "bar!", None, None, "code,")
        val ifAddress = models.Address("foo", "bar.", None, None, "co^de")

        addressMatchingService.tryMatch(knownFacts, ifAddress) shouldBe true
      }

      "match an address where known facts has whitespace in the HoDs" in new Fixture {
        val knownFacts = models.Address(" foo ", " ba  r", None, None, " c ode")
        val ifAddress = models.Address("foo", "b ar", None, None, "cod    e")

        addressMatchingService.tryMatch(knownFacts, ifAddress) shouldBe true
      }
    }
  }
}