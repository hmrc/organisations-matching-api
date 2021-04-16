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

package uk.gov.hmrc.organisationsmatchingapi.controllers

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status
import play.api.test.Helpers._
import play.api.{Configuration, Environment}
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.organisationsmatchingapi.config.AppConfig
import uk.gov.hmrc.organisationsmatchingapi.services.MatchingService
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.util.UUID

class MatchingControllerSpec extends AnyWordSpec with Matchers with MockitoSugar {
  private val fakeRequest = FakeRequest("GET", "/")

  private val env           = Environment.simple()
  private val configuration = Configuration.load(env)

  private val mockAuthConnector = mock[AuthConnector]
  private val mockMatchingService = mock[MatchingService]

  private val serviceConfig = new ServicesConfig(configuration)
  private val appConfig     = new AppConfig(configuration, serviceConfig)

  private val controller = new MatchingController(mockAuthConnector, Helpers.stubControllerComponents(), mockMatchingService)


  "GET matchCrn TO BE IMPLEMENTED /" should {
    "return 200" in {
      val result = controller.matchCrn()(fakeRequest)
      status(result) shouldBe Status.OK
    }
  }

  "GET matchSaUtr TO BE IMPLEMENTED /" should {
    "return 200" in {
      val result = controller.matchSaUtr()(fakeRequest)
      status(result) shouldBe Status.OK
    }
  }
}
