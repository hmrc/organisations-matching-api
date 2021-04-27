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

package unit.uk.gov.hmrc.organisationsmatchingapi.models

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.organisationsmatchingapi.models

class addressSpec extends AnyWordSpec with Matchers {

  "Address" should {

    "asString" should {
      "Write the address as String" in {
        val address = models.Address("foo", "bar", None, None, "code")

        address.asString shouldBe "foo bar code"
      }

      "Write the full address as String" in {
        val address = models.Address("foo", "bar", Some("one"), Some("two"), "code")

        address.asString shouldBe "foo bar one two code"
      }
    }

    "ignoreCaseAndSpaces" should {
      "ignore case" in {
        val address = models.Address("Foo", "Bar", Some("One"), Some("Two"), "Code")

        address.ignoreCaseAndSpaces shouldBe "foobaronetwocode"
      }

      "ignore case and space" in {
        val address = models.Address("Foo  ", "Bar ", Some("One"), Some(" Two"), " Code")

        address.ignoreCaseAndSpaces shouldBe "foobaronetwocode"
      }
    }

    "withoutPunctuation" should {
      "ignore case and remove punctuation" in {
        val address = models.Address("foo!", "bar,", Some("one"), Some("two"), "code*")

        address.withoutPunctuation shouldBe "foobaronetwocode"
      }
    }

    "cleanPostOfficeBox" should {
      "ignore case and remove punctuation without whitespace and clean post office box" in {
        val address = models.Address(" P.O. Box 7169!", "bar,", Some("one "), Some("two"), "co de*")

        address.cleanPostOfficeBox shouldBe "pobox7169baronetwocode"
      }

      "ignore case and remove punctuation without whitespace and clean post office box full" in {
        val address = models.Address(" POST OFFICE Box 7169!", "bar,  ", Some("one "), Some("two"), "co de*")

        address.cleanPostOfficeBox shouldBe "pobox7169baronetwocode"
      }
    }
  }
}