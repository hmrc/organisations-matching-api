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

class CrnMatchDataSpec extends AnyWordSpec with Matchers {

  "crnMatchData" should {

    "asString" should {
      "write the data as String" in {
        val data = models.CrnMatchData("mycrn", "myname", models.Address("foo", "bar", None, None, "code"))

        data.asString shouldBe "mycrn myname foo bar code"
      }

      "Write the full address as String" in {
        val data = models.CrnMatchData("mycrn", "myname", models.Address("foo", "bar", Some("one"), Some("two"), "code"))

        data.asString shouldBe "mycrn myname foo bar one two code"
      }
    }

    "ignoreCaseAndSpaces" should {
      "ignore case" in {
        val data = models.CrnMatchData("mycrN", "Myname", models.Address("Foo", "Bar", Some("One"), Some("Two"), "Code"))

        data.ignoreCaseAndSpaces shouldBe "mycrnmynamefoobaronetwocode"
      }

      "ignore case and space" in {
        val data = models.CrnMatchData(" mycrN", "Myn ame", models.Address("Foo  ", "Bar ", Some("One"), Some(" Two"), " Code"))

        data.ignoreCaseAndSpaces shouldBe "mycrnmynamefoobaronetwocode"
      }
    }

    "withoutPunctuation" should {
      "ignore case and remove punctuation" in {
        val data = models.CrnMatchData("mycrn!", "myname,", models.Address("foo!", "bar,", Some("one"), Some("two"), "code*"))

        data.withoutPunctuation shouldBe "mycrnmynamefoobaronetwocode"
      }
    }

    "cleanPostOfficeBox" should {
      "ignore case and remove punctuation without whitespace and clean post office box" in {
        val data = models.CrnMatchData("mycrn", "myname", models.Address(" P.O. Box 7169!", "bar,", Some("one "), Some("two"), "co de*"))

        data.cleanPostOfficeBox shouldBe "mycrnmynamepobox7169baronetwocode"
      }

      "ignore case and remove punctuation without whitespace and clean post office box full" in {
        val data = models.CrnMatchData("mycrn", "myname", models.Address(" POST OFFICE Box 7169!", "bar,  ", Some("one "), Some("two"), "co de*"))

        data.cleanPostOfficeBox shouldBe "mycrnmynamepobox7169baronetwocode"
      }
    }
  }
}