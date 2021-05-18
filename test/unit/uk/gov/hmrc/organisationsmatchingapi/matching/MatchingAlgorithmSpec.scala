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

    "return a GoodMatch with no error code when everything matches" in new Setup {

      val knownFactsData = models.MatchDataCT(
        Some("mycrn"), Some("myname"), models.Address(Some("foo"), Some("bar"), None, None, Some("code"))
      )

      val ifData = models.MatchDataCT(
        Some("mycrn"), Some("myname"), models.Address(Some("foo"), Some("bar"), None, None, Some("code"))
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

  }

  "The matching algorithm for SA" should {

    "return a GoodMatch with no error code when everything matches" in new Setup {

      val knownFactsData = models.MatchDataSA(
        Some("myutr"), Some("myname"), Some("individual"), models.Address(Some("foo"), Some("bar"), None, None, Some("code"))
      )

      val ifData = models.MatchDataSA(
        Some("myutr"), Some("myname"), Some("individual"), models.Address(Some("foo"), Some("bar"), None, None, Some("code"))
      )

      val matchResult: Match = saMatching.performMatch(knownFactsData, ifData)

      matchResult shouldBe Good()

    }

    "return a BadMatch with error code when crn does not match" in new Setup {

      val knownFactsData = models.MatchDataSA(
        Some("mutr"), Some("myname"), Some("individual"), models.Address(Some("foo"), Some("bar"), None, None, Some("code"))
      )

      val ifData = models.MatchDataSA(
        Some("myutr"), Some("myname"), Some("individual"), models.Address(Some("foo"), Some("bar"), None, None, Some("code"))
      )

      val matchResult: Match = saMatching.performMatch(knownFactsData, ifData)

      matchResult shouldBe Bad(Set(32))

    }

  }

}
