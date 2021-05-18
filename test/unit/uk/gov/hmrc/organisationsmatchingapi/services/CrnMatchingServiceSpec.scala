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
import uk.gov.hmrc.organisationsmatchingapi.matching.CrnMatchingCycle
import uk.gov.hmrc.organisationsmatchingapi.models
import uk.gov.hmrc.organisationsmatchingapi.models.MatchingResult
import uk.gov.hmrc.organisationsmatchingapi.services.CrnMatchingService
import util.UnitSpec

class CrnMatchingServiceSpec extends UnitSpec with Matchers {

  trait Setup {
    val crnMatching = new CrnMatchingCycle
    val crnMatchingService = new CrnMatchingService(crnMatching)
  }

  "The matching algorithm for CRN" should {

    "return a GoodMatch with no error code when everything matches" in new Setup {

      val knownFactsData = models.CrnMatchData(
        Some("mycrn"), Some("myname"), models.Address(Some("foo"), Some("bar"), None, None, Some("code"))
      )

      val ifData = models.CrnMatchData(
        Some("mycrn"), Some("myname"), models.Address(Some("foo"), Some("bar"), None, None, Some("code"))
      )

      val expected =  MatchingResult(Some(ifData),Set())

      val result = await(crnMatchingService.performMatch(knownFactsData, ifData))

      result shouldBe expected

    }

    "return a BadMatch with error codes when crn does not match in known facts" in new Setup {

      val knownFactsData = models.CrnMatchData(
        Some("mycrn"), Some("mname"), models.Address(Some("foo"), Some("bar"), None, None, Some("code"))
      )

      val ifData = models.CrnMatchData(
        Some("mycrn"), Some("myname"), models.Address(Some("foo"), Some("bar"), None, None, Some("code"))
      )

      val expected =  MatchingResult(None,Set(34))

      val result = await(crnMatchingService.performMatch(knownFactsData, ifData))

      result shouldBe expected

    }

    "return a BadMatch with error codes when crn does not match as not present in IF" in new Setup {

      val knownFactsData = models.CrnMatchData(
        Some("mycrn"), Some("mname"), models.Address(Some("foo"), Some("bar"), None, None, Some("code"))
      )

      val ifData = models.CrnMatchData(
        Some("mycrn"), None, models.Address(Some("foo"), Some("bar"), None, None, Some("code"))
      )

      val expected =  MatchingResult(None,Set(24))

      val result = await(crnMatchingService.performMatch(knownFactsData, ifData))

      result shouldBe expected

    }

  }

}
