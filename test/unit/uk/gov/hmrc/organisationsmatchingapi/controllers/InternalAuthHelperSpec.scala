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

package unit.uk.gov.hmrc.organisationsmatchingapi.controllers

import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.`given`
import org.mockito.Mockito.{atLeastOnce, never, verify}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.Configuration
import play.api.http.Status.UNAUTHORIZED
import play.api.mvc.ControllerComponents
import play.api.test.Helpers
import uk.gov.hmrc.http.{Authorization, HeaderCarrier}
import uk.gov.hmrc.http.UpstreamErrorResponse
import uk.gov.hmrc.internalauth.client.BackendAuthComponents
import uk.gov.hmrc.internalauth.client.test.{BackendAuthComponentsStub, StubBehaviour}
import uk.gov.hmrc.organisationsmatchingapi.controllers.InternalAuthHelper

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.concurrent.Await

class InternalAuthHelperSpec extends AnyWordSpec with Matchers with MockitoSugar {

  private implicit val cc: ControllerComponents = Helpers.stubControllerComponents()
  private val internalAuthTokenHeaderCarrier: HeaderCarrier =
    HeaderCarrier(authorization = Some(Authorization("Bearer internal-token")))

  private def helperWith(
    enabled: Boolean,
    stubBehaviour: StubBehaviour
  ): InternalAuthHelper = {
    val backendAuthComponents: BackendAuthComponents = BackendAuthComponentsStub(stubBehaviour)
    new InternalAuthHelper(
      backendAuthComponents,
      Configuration(InternalAuthHelper.InternalAuthFeatureFlag -> enabled)
    )
  }

  "InternalAuthHelper" should {
    "skip internal auth checks when disabled by config" in {
      val mockInternalAuthBehaviour: StubBehaviour = mock[StubBehaviour]
      val helper = helperWith(enabled = false, mockInternalAuthBehaviour)
      implicit val hc: HeaderCarrier = HeaderCarrier()

      val result = Await.result(helper.isAuthorised, 2.seconds)

      result shouldBe false
      verify(mockInternalAuthBehaviour, never()).stubAuth(any(), any())
    }

    "return true when enabled and internal auth authorises" in {
      val mockInternalAuthBehaviour: StubBehaviour = mock[StubBehaviour]
      `given`(mockInternalAuthBehaviour.stubAuth(any(), any())).willReturn(Future.successful(true))
      val helper = helperWith(enabled = true, mockInternalAuthBehaviour)
      implicit val hc: HeaderCarrier = internalAuthTokenHeaderCarrier

      val result = Await.result(helper.isAuthorised, 2.seconds)

      result shouldBe true
      verify(mockInternalAuthBehaviour, atLeastOnce()).stubAuth(any(), any())
    }

    "return false when enabled and internal auth denies authorisation" in {
      val mockInternalAuthBehaviour: StubBehaviour = mock[StubBehaviour]
      `given`(mockInternalAuthBehaviour.stubAuth(any(), any())).willReturn(Future.successful(false))
      val helper = helperWith(enabled = true, mockInternalAuthBehaviour)
      implicit val hc: HeaderCarrier = internalAuthTokenHeaderCarrier

      val result = Await.result(helper.isAuthorised, 2.seconds)

      result shouldBe false
      verify(mockInternalAuthBehaviour, atLeastOnce()).stubAuth(any(), any())
    }

    "return false when enabled and internal auth throws an error" in {
      val mockInternalAuthBehaviour: StubBehaviour = mock[StubBehaviour]
      `given`(mockInternalAuthBehaviour.stubAuth(any(), any()))
        .willReturn(Future.failed(UpstreamErrorResponse("Unauthorized", UNAUTHORIZED)))
      val helper = helperWith(enabled = true, mockInternalAuthBehaviour)
      implicit val hc: HeaderCarrier = internalAuthTokenHeaderCarrier

      val result = Await.result(helper.isAuthorised, 2.seconds)

      result shouldBe false
      verify(mockInternalAuthBehaviour, atLeastOnce()).stubAuth(any(), any())
    }
  }
}
