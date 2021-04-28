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
import uk.gov.hmrc.organisationsmatchingapi.models.{Address, CrnMatch, CrnMatchData, SaUtrMatch, SaUtrMatchingRequest}
import uk.gov.hmrc.organisationsmatchingapi.services.CacheService
import util.UnitSpec

import scala.concurrent.Future

class CacheServiceSpec extends UnitSpec with Matchers with GuiceOneAppPerSuite with MockitoSugar with ScalaFutures {

  trait Setup {
    val uuid:UUID = randomUUID()
    val cacheService = app.injector.instanceOf(classOf[CacheService])

    val crnRequest = CrnMatchData(
      "crn",
      "name",
      Address(
        "line1",
        "line2",
        Some("line3"),
        Some("line4"),
      "postcode"
    ))

    val crnMatch = CrnMatch(
      crnRequest,
      uuid
    )

    val saUtrRequest = SaUtrMatchingRequest("", "", "", Address(
      "line1",
      "line2",
      Some("line3"),
      Some("line4"),
      "postcode"
    ))

    val saUtrMatch = SaUtrMatch(
      saUtrRequest,
      uuid
    )
  }

  "Retrieve CtUtr from cache service" in new Setup {
    val result = cacheService.getCtUtr(uuid, () => Future.successful(crnMatch))
    await(result) shouldBe crnMatch
  }

  "Retrieve SaUtr from cache service" in new Setup {
    val result = cacheService.getSaUtr(uuid, () => Future.successful(saUtrMatch))
    await(result) shouldBe saUtrMatch
  }
}
