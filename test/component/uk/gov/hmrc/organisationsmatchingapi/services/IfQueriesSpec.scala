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

package component.uk.gov.hmrc.organisationsmatchingapi.services

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.organisationsmatchingapi.services.ScopesHelper
import util.ComponentSpec

class IfQueriesSpec extends AnyWordSpec with Matchers with ComponentSpec {

    val helper: ScopesHelper = app.injector.instanceOf[ScopesHelper]

    val res1 =  "communicationsDetails(address(line1,postcode),name(name1,name2)),crn,registeredDetails(address(line1,postcode),name(name1,name2))"
    val res2 =  "taxPayerDetails(address(line1,postcode),name),taxPayerType,utr"


    "read:organisations-matching-ho" should {
      "have correct IF query string for corporation-tax" in {
        val queryString = helper.getQueryStringFor(Seq("read:organisations-matching-ho"), List("getCorporationTax"))
        queryString shouldBe res1
      }

      "have correct IF query string for corporation-tax-match" in {
        val queryString = helper.getQueryStringFor(Seq("read:organisations-matching-ho"), List("getCorporationTaxMatch"))
        queryString shouldBe res1
      }

      "have correct IF query string for self-assessment" in {
        val queryString = helper.getQueryStringFor(Seq("read:organisations-matching-ho"), List("getSelfAssessment"))
        queryString shouldBe res2
      }

      "have correct IF query string for self-assessment-match" in {
        val queryString = helper.getQueryStringFor(Seq("read:organisations-matching-ho"), List("getSelfAssessmentMatch"))
        queryString shouldBe res2
      }
    }
}
