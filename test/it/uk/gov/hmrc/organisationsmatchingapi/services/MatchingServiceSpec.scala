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

package it.uk.gov.hmrc.organisationsmatchingapi.services

import java.util.UUID
import java.util.UUID.randomUUID

import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import uk.gov.hmrc.organisationsmatchingapi.models.{Address, CrnMatchingRequest, SaUtrMatchingRequest}
import uk.gov.hmrc.organisationsmatchingapi.services.MatchingService
import util.UnitSpec

class MatchingServiceSpec extends UnitSpec with Matchers with GuiceOneAppPerSuite with MockitoSugar with ScalaFutures with BeforeAndAfterEach {


  override def beforeEach() {
  }

  trait Setup {
    val uuid:UUID = randomUUID()
    val matchingService = app.injector.instanceOf(classOf[MatchingService])
  }

  "microservice" in new Setup {
    matchingService.getCrnMatch(uuid)
  }

  "microservice2" in new Setup {

    val request:CrnMatchingRequest = new CrnMatchingRequest("crn", "name", Address(
      "line1",
      Some("line2"),
      Some("line3"),
      Some("line4")
    ), "postcode")

    matchingService.matchCrn(request)
  }

  "microservice3" in new Setup {
    matchingService.getSaUtrMatch(uuid)
  }

  "microservice4" in new Setup {

    val request = new SaUtrMatchingRequest("orgType", "utr", name = "name", Address(
      "line1",
      Some("line2"),
      Some("line3"),
      Some("line4")
    ), "postcode")

    matchingService.matchSaUtr(request)
  }

}
