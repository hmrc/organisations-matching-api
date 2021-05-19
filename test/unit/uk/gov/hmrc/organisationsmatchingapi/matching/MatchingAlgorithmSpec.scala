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

package unit.uk.gov.hmrc.organisationsmatchingapi.matching

import uk.gov.hmrc.organisationsmatchingapi.models
import util.UnitSpec
import org.scalatest.matchers.should.Matchers
import uk.gov.hmrc.organisationsmatchingapi.matching.{Bad, MatchingCycleCT, Good, Match, MatchingCycleSA}

class MatchingAlgorithmSpec extends UnitSpec with Matchers {

  trait Setup {
    val ctMatching = new MatchingCycleCT
    val saMatching = new MatchingCycleSA
  }

  "The matching algorithm for CT" should {

    "return a GoodMatch when everything matches" in new Setup {

      val knownFactsData = models.MatchDataCT(
        Some("mycrn"), Some("myname"), models.Address(Some("foo"), Some("bar"), None, None, Some("code"))
      )

      val ifData = models.MatchDataCT(
        Some("mycrn"), Some("myname"), models.Address(Some("foo"), Some("bar"), None, None, Some("code"))
      )

      val matchResult: Match = ctMatching.performMatch(knownFactsData, ifData)
      matchResult shouldBe Good()

    }

    "return a GoodMatch when everything matches but the name contains different apostrophes" in new Setup {

      val knownFactsData = models.MatchDataCT(
        Some("mycrn"), Some("my'name"), models.Address(Some("foo"), Some("bar"), None, None, Some("code"))
      )

      val ifData = models.MatchDataCT(
        Some("mycrn"), Some("my’name"), models.Address(Some("foo"), Some("bar"), None, None, Some("code"))
      )

      val matchResult: Match = ctMatching.performMatch(knownFactsData, ifData)
      matchResult shouldBe Good()

    }

    "return a BadMatch with error code when crn does not match" in new Setup {

      val knownFactsData = models.MatchDataCT(
        Some("mcrn"), Some("myname"), models.Address(Some("foo"), Some("bar"), None, None, Some("code"))
      )

      val ifData = models.MatchDataCT(
        Some("mycrn"), Some("myname"), models.Address(Some("foo"), Some("bar"), None, None, Some("code"))
      )

      val matchResult: Match = ctMatching.performMatch(knownFactsData, ifData)
      matchResult shouldBe Bad(Set(31))

    }

    "return a BadMatch with error code when name does not match" in new Setup {

      val knownFactsData = models.MatchDataCT(
        Some("mycrn"), Some("mname"), models.Address(Some("foo"), Some("bar"), None, None, Some("code"))
      )

      val ifData = models.MatchDataCT(
        Some("mycrn"), Some("myname"), models.Address(Some("foo"), Some("bar"), None, None, Some("code"))
      )

      val matchResult: Match = ctMatching.performMatch(knownFactsData, ifData)
      matchResult shouldBe Bad(Set(34))

    }

    "return a GoodMatch with error code when name does not match after the first 4 characters" in new Setup {

      val knownFactsData = models.MatchDataCT(
        Some("mycrn"), Some("mynam"), models.Address(Some("foo"), Some("bar"), None, None, Some("code"))
      )

      val ifData = models.MatchDataCT(
        Some("mycrn"), Some("myname"), models.Address(Some("foo"), Some("bar"), None, None, Some("code"))
      )

      val matchResult: Match = ctMatching.performMatch(knownFactsData, ifData)
      matchResult shouldBe Good()

    }

    "return a BadMatch with error code when first line of the address does not match" in new Setup {

      val knownFactsData = models.MatchDataCT(
        Some("mycrn"), Some("myname"), models.Address(Some("foobar"), Some("bar"), None, None, Some("code"))
      )

      val ifData = models.MatchDataCT(
        Some("mycrn"), Some("myname"), models.Address(Some("foo"), Some("bar"), None, None, Some("code"))
      )

      val matchResult: Match = ctMatching.performMatch(knownFactsData, ifData)
      matchResult shouldBe Bad(Set(36))

    }

    "return a GoodMatch when first line of the address does not match but after 4 characters" in new Setup {

      val knownFactsData = models.MatchDataCT(
        Some("mycrn"), Some("myname"), models.Address(Some("foobor"), Some("bar"), None, None, Some("code"))
      )

      val ifData = models.MatchDataCT(
        Some("mycrn"), Some("myname"), models.Address(Some("foobar"), Some("bar"), None, None, Some("code"))
      )

      val matchResult: Match = ctMatching.performMatch(knownFactsData, ifData)
      matchResult shouldBe Good()

    }

    "return a BadMatch with error code when postcode does not match" in new Setup {

      val knownFactsData = models.MatchDataCT(
        Some("mycrn"), Some("myname"), models.Address(Some("foo"), Some("bar"), None, None, Some("coded"))
      )

      val ifData = models.MatchDataCT(
        Some("mycrn"), Some("myname"), models.Address(Some("foo"), Some("bar"), None, None, Some("code"))
      )

      val matchResult: Match = ctMatching.performMatch(knownFactsData, ifData)
      matchResult shouldBe Bad(Set(37))

    }

    "return a BadMatch with error code when multiple properties do not match" in new Setup {

      val knownFactsData = models.MatchDataCT(
        Some("mcrn"), Some("mname"), models.Address(Some("fo"), Some("bar"), None, None, Some("coded"))
      )

      val ifData = models.MatchDataCT(
        Some("mycrn"), Some("myname"), models.Address(Some("foo"), Some("bar"), None, None, Some("code"))
      )

      val matchResult: Match = ctMatching.performMatch(knownFactsData, ifData)
      matchResult shouldBe Bad(Set(31, 34, 36, 37))

    }

  }

  "The matching algorithm for SA" should {

    "return a GoodMatch when everything matches" in new Setup {

      val knownFactsData = models.MatchDataSA(
        Some("myutr"), Some("myname"), Some("individual"), models.Address(Some("foo"), Some("bar"), None, None, Some("code"))
      )

      val ifData = models.MatchDataSA(
        Some("myutr"), Some("myname"), Some("individual"), models.Address(Some("foo"), Some("bar"), None, None, Some("code"))
      )

      val matchResult: Match = saMatching.performMatch(knownFactsData, ifData)
      matchResult shouldBe Good()

    }

    "return a BadMatch with error code when utr does not match" in new Setup {

      val knownFactsData = models.MatchDataSA(
        Some("mutr"), Some("myname"), Some("individual"), models.Address(Some("foo"), Some("bar"), None, None, Some("code"))
      )

      val ifData = models.MatchDataSA(
        Some("myutr"), Some("myname"), Some("individual"), models.Address(Some("foo"), Some("bar"), None, None, Some("code"))
      )

      val matchResult: Match = saMatching.performMatch(knownFactsData, ifData)
      matchResult shouldBe Bad(Set(32))

    }

    "return a BadMatch with error code when taxpayer type does not match" in new Setup {

      val knownFactsData = models.MatchDataSA(
        Some("myutr"), Some("myname"), Some("partnership"), models.Address(Some("foo"), Some("bar"), None, None, Some("code"))
      )

      val ifData = models.MatchDataSA(
        Some("myutr"), Some("myname"), Some("individual"), models.Address(Some("foo"), Some("bar"), None, None, Some("code"))
      )

      val matchResult: Match = saMatching.performMatch(knownFactsData, ifData)
      matchResult shouldBe Bad(Set(33))

    }

    "return a BadMatch with error code when taxpayer name does not match for individual" in new Setup {

      val knownFactsData = models.MatchDataSA(
        Some("myutr"), Some("mynameia"), Some("individual"), models.Address(Some("foo"), Some("bar"), None, None, Some("code"))
      )

      val ifData = models.MatchDataSA(
        Some("myutr"), Some("mynameis"), Some("individual"), models.Address(Some("foo"), Some("bar"), None, None, Some("code"))
      )

      val matchResult: Match = saMatching.performMatch(knownFactsData, ifData)
      matchResult shouldBe Bad(Set(35))

    }

    "return a BadMatch with error code when taxpayer name does not match for partnership" in new Setup {

      val knownFactsData = models.MatchDataSA(
        Some("myutr"), Some("mnameis"), Some("partnership"), models.Address(Some("foo"), Some("bar"), None, None, Some("code"))
      )

      val ifData = models.MatchDataSA(
        Some("myutr"), Some("mynameis"), Some("partnership"), models.Address(Some("foo"), Some("bar"), None, None, Some("code"))
      )

      val matchResult: Match = saMatching.performMatch(knownFactsData, ifData)
      matchResult shouldBe Bad(Set(35))

    }

    "return a GoodMatch when taxpayer name does not match for partnership after 4 characters" in new Setup {

      val knownFactsData = models.MatchDataSA(
        Some("myutr"), Some("mynameid"), Some("partnership"), models.Address(Some("foo"), Some("bar"), None, None, Some("code"))
      )

      val ifData = models.MatchDataSA(
        Some("myutr"), Some("mynameis"), Some("partnership"), models.Address(Some("foo"), Some("bar"), None, None, Some("code"))
      )

      val matchResult: Match = saMatching.performMatch(knownFactsData, ifData)
      matchResult shouldBe Good()

    }

    "return a BadMatch with error code when address line 1 does not match" in new Setup {

      val knownFactsData = models.MatchDataSA(
        Some("myutr"), Some("mynameis"), Some("partnership"), models.Address(Some("foobar"), Some("bar"), None, None, Some("code"))
      )

      val ifData = models.MatchDataSA(
        Some("myutr"), Some("mynameis"), Some("partnership"), models.Address(Some("foo"), Some("bar"), None, None, Some("code"))
      )

      val matchResult: Match = saMatching.performMatch(knownFactsData, ifData)
      matchResult shouldBe Bad(Set(36))

    }

    "return a GoodMatch when address line 1 does not match after 4 characters" in new Setup {

      val knownFactsData = models.MatchDataSA(
        Some("myutr"), Some("mynameis"), Some("partnership"), models.Address(Some("foobar"), Some("bar"), None, None, Some("code"))
      )

      val ifData = models.MatchDataSA(
        Some("myutr"), Some("mynameis"), Some("partnership"), models.Address(Some("foobor"), Some("bar"), None, None, Some("code"))
      )

      val matchResult: Match = saMatching.performMatch(knownFactsData, ifData)
      matchResult shouldBe Good()

    }

    "return a BadMatch with error code when postcode does not match" in new Setup {

      val knownFactsData = models.MatchDataSA(
        Some("myutr"), Some("mynameis"), Some("partnership"), models.Address(Some("foo"), Some("bar"), None, None, Some("codes"))
      )

      val ifData = models.MatchDataSA(
        Some("myutr"), Some("mynameis"), Some("partnership"), models.Address(Some("foo"), Some("bar"), None, None, Some("code"))
      )

      val matchResult: Match = saMatching.performMatch(knownFactsData, ifData)
      matchResult shouldBe Bad(Set(37))

    }

    "return a BadMatch with error code when multiple properties do not match" in new Setup {

      val knownFactsData = models.MatchDataSA(
        Some("mutr"), Some("mnameis"), Some("partnership"), models.Address(Some("oo"), Some("bar"), None, None, Some("codes"))
      )

      val ifData = models.MatchDataSA(
        Some("myutr"), Some("mynameis"), Some("partnership"), models.Address(Some("foo"), Some("bar"), None, None, Some("code"))
      )

      val matchResult: Match = saMatching.performMatch(knownFactsData, ifData)
      matchResult shouldBe Bad(Set(32, 36, 37, 35))

    }

  }

}
