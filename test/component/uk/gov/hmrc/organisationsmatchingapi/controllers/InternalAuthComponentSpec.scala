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

package component.uk.gov.hmrc.organisationsmatchingapi.controllers

import component.uk.gov.hmrc.organisationsmatchingapi.controllers.stubs.{AuthStub, BaseSpec, InternalAuthStub}
import play.api.test.Helpers.OK
import scalaj.http.Http
import uk.gov.hmrc.organisationsmatchingapi.domain.models.CtMatch
import uk.gov.hmrc.organisationsmatchingapi.domain.ogd.CtMatchingRequest

import java.util.UUID
import scala.concurrent.Await.result

class InternalAuthComponentSpec extends BaseSpec {

  private val scopes: List[String] = List("read:organisations-matching-ho")

  Feature("internal auth precedence and fallback") {
    Scenario("internal auth success bypasses legacy auth") {
      val matchId = UUID.randomUUID()
      val ctMatch = CtMatch(
        CtMatchingRequest("0123456789", "name", "line1", "postcode"),
        matchId,
        utr = Some("utr")
      )

      Given("internal auth authorises the token")
      InternalAuthStub.willAuthorizeToken(authToken)

      And("the match exists in cache")
      result(mongoRepository.cache(matchId.toString, ctMatch), timeout)

      When("the endpoint is invoked")
      val response = Http(s"$serviceUrl/corporation-tax/$matchId")
        .headers(requestHeaders(acceptHeaderP1))
        .asString

      Then("the request succeeds and does not call legacy auth")
      response.code mustBe OK
      InternalAuthStub.verifyAuthRequestCount(expectedCount = 1)
      AuthStub.verifyAuthoriseRequestCount(expectedCount = 0)
    }

    Scenario("internal auth denial falls back to legacy scope auth") {
      val matchId = UUID.randomUUID()
      val ctMatch = CtMatch(
        CtMatchingRequest("0123456789", "name", "line1", "postcode"),
        matchId,
        utr = Some("utr")
      )

      Given("internal auth denies the token")
      InternalAuthStub.willNotAuthorizeToken(authToken)

      And("legacy auth allows the required scope")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      And("the match exists in cache")
      result(mongoRepository.cache(matchId.toString, ctMatch), timeout)

      When("the endpoint is invoked")
      val response = Http(s"$serviceUrl/corporation-tax/$matchId")
        .headers(requestHeaders(acceptHeaderP1))
        .asString

      Then("the request succeeds via fallback and both auth services were called")
      response.code mustBe OK
      InternalAuthStub.verifyAuthRequestCount(expectedCount = 1)
      AuthStub.verifyAuthoriseRequestCount(expectedCount = 1)
    }
  }

  Feature("internal auth feature flag") {
    Scenario("when internal auth is disabled, request is authorised via legacy scopes auth only") {
      val matchId = UUID.randomUUID()
      val ctMatch = CtMatch(
        CtMatchingRequest("0123456789", "name", "line1", "postcode"),
        matchId,
        utr = Some("utr")
      )

      Given("legacy auth allows the required scope")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      And("the match exists in cache")
      result(mongoRepository.cache(matchId.toString, ctMatch), timeout)

      When("the endpoint is invoked against an app with internal auth disabled")
      val response = withConfiguredServer("features.internal-auth.enabled" -> false) { disabledServiceUrl =>
        Http(s"$disabledServiceUrl/corporation-tax/$matchId")
          .headers(requestHeaders(acceptHeaderP1))
          .asString
      }

      Then("the request succeeds, using legacy auth without calling internal auth")
      response.code mustBe OK
      InternalAuthStub.verifyAuthRequestCount(expectedCount = 0)
      AuthStub.verifyAuthoriseRequestCount(expectedCount = 1)
    }
  }
}
