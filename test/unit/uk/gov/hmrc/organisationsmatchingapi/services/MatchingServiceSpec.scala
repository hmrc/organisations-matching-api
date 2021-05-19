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
import uk.gov.hmrc.organisationsmatchingapi.matching.{MatchingCycleCT, MatchingCycleSA}
import uk.gov.hmrc.organisationsmatchingapi.models
import uk.gov.hmrc.organisationsmatchingapi.models.{MatchingResultCT, MatchingResultSA}
import uk.gov.hmrc.organisationsmatchingapi.services.MatchingService
import util.UnitSpec

class MatchingServiceSpec extends UnitSpec with Matchers {

  trait Setup {
    val crnMatching        = new MatchingCycleCT
    val saMatching         = new MatchingCycleSA
    val crnMatchingService = new MatchingService(crnMatching, saMatching)
  }

  "The matching algorithm for CT" should {

    "return a GoodMatch with no error code when everything matches" in new Setup {

      val knownFactsData = models.MatchDataCT(
        Some("mycrn"), Some("myname"), models.Address(Some("foo"), Some("bar"), None, None, Some("code"))
      )

      val ifData = models.MatchDataCT(
        Some("mycrn"), Some("myname"), models.Address(Some("foo"), Some("bar"), None, None, Some("code"))
      )

      val expected =  MatchingResultCT(Some(ifData),Set())
      val result   = await(crnMatchingService.performMatchCT(knownFactsData, ifData))
      result shouldBe expected

    }

    "return a BadMatch with error codes when something does not match" in new Setup {

      val knownFactsData = models.MatchDataCT(
        Some("myrn"), Some("myname"), models.Address(Some("foo"), Some("bar"), None, None, Some("code"))
      )

      val ifData = models.MatchDataCT(
        Some("mycrn"), Some("myname"), models.Address(Some("foo"), Some("bar"), None, None, Some("code"))
      )

      val expected =  MatchingResultCT(None,Set(31))
      val result   = await(crnMatchingService.performMatchCT(knownFactsData, ifData))
      result shouldBe expected

    }

    "return a BadMatch with error codes when something is not present in IF" in new Setup {

      val knownFactsData = models.MatchDataCT(
        Some("mycrn"), Some("mname"), models.Address(Some("foo"), Some("bar"), None, None, Some("code"))
      )

      val ifData = models.MatchDataCT(
        None, Some("mname"), models.Address(Some("foo"), Some("bar"), None, None, Some("code"))
      )

      val expected =  MatchingResultCT(None,Set(21))
      val result   = await(crnMatchingService.performMatchCT(knownFactsData, ifData))
      result shouldBe expected

    }

    "return a BadMatch with error codes when something is not present in known facts" in new Setup {

      val knownFactsData = models.MatchDataCT(
        None, Some("mname"), models.Address(Some("foo"), Some("bar"), None, None, Some("code"))
      )

      val ifData = models.MatchDataCT(
        Some("mycrn"), Some("mname"), models.Address(Some("foo"), Some("bar"), None, None, Some("code"))
      )

      val expected =  MatchingResultCT(None,Set(11))
      val result   = await(crnMatchingService.performMatchCT(knownFactsData, ifData))
      result shouldBe expected

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

      val expected =  MatchingResultSA(Some(ifData),Set())
      val result   = await(crnMatchingService.performMatchSA(knownFactsData, ifData))
      result shouldBe expected

    }

    "return a BadMatch with error codes when something does not match" in new Setup {

      val knownFactsData = models.MatchDataSA(
        Some("mutr"), Some("myname"), Some("individual"), models.Address(Some("foo"), Some("bar"), None, None, Some("code"))
      )

      val ifData = models.MatchDataSA(
        Some("myutr"), Some("myname"), Some("individual"), models.Address(Some("foo"), Some("bar"), None, None, Some("code"))
      )

      val expected =  MatchingResultSA(None,Set(32))
      val result   = await(crnMatchingService.performMatchSA(knownFactsData, ifData))
      result shouldBe expected

    }

    "return a BadMatch with error codes when something is not present in IF" in new Setup {

      val knownFactsData = models.MatchDataSA(
        Some("mutr"), Some("myname"), Some("individual"), models.Address(Some("foo"), Some("bar"), None, None, Some("code"))
      )

      val ifData = models.MatchDataSA(
        None, Some("myname"), Some("individual"), models.Address(Some("foo"), Some("bar"), None, None, Some("code"))
      )

      val expected =  MatchingResultSA(None,Set(22))
      val result   = await(crnMatchingService.performMatchSA(knownFactsData, ifData))
      result shouldBe expected

    }

    "return a BadMatch with error codes when something is not present in known facts" in new Setup {

      val knownFactsData = models.MatchDataSA(
        None, Some("myname"), Some("individual"), models.Address(Some("foo"), Some("bar"), None, None, Some("code"))
      )

      val ifData = models.MatchDataSA(
        Some("mutr"), Some("myname"), Some("individual"), models.Address(Some("foo"), Some("bar"), None, None, Some("code"))
      )

      val expected =  MatchingResultSA(None,Set(12))
      val result   = await(crnMatchingService.performMatchSA(knownFactsData, ifData))
      result shouldBe expected

    }

  }

}
