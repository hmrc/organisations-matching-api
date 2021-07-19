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

package component.uk.gov.hmrc.organisationsmatchingapi.controllers

import scalaj.http.Http
import component.uk.gov.hmrc.organisationsmatchingapi.controllers.stubs.{AuthStub, BaseSpec}
import play.api.libs.json.Json
import play.api.test.Helpers._
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import uk.gov.hmrc.organisationsmatchingapi.domain.models.{CtMatch, SaMatch}
import uk.gov.hmrc.organisationsmatchingapi.domain.ogd.{CtMatchingRequest, SaMatchingRequest}

import java.util.UUID
import scala.concurrent.Await.result

class MatchedControllerSpec extends BaseSpec  {

  val matchId   = UUID.randomUUID()
  val scopes    = List("read:organisations-matching-ho-ssp")
  val ctRequest = CtMatchingRequest("crn", "name", "line1", "postcode")
  val ctMatch   = CtMatch(ctRequest, matchId)
  val saRequest = SaMatchingRequest("utr", "Individual", "name", "line1", "postcode")
  val saMatch   = SaMatch(saRequest, matchId)

  Feature("cotax") {
    Scenario("a valid request is made for an existing match") {
      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      And("A valid match exist")
      result(mongoRepository.cache(matchId.toString, "organisations-matching", ctMatch), timeout)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/matching/corporation-tax/$matchId")
        .headers(requestHeaders(acceptHeaderP1))
        .asString

      response.code shouldBe OK

      Json.parse(response.body) shouldBe Json.parse(
        s"""{
           |  "address": {
           |    "line1": "line1",
           |    "postcode": "postcode"
           |  },
           |  "_links": {
           |    "details-number-of-employees": {
           |      "href": "/organisations/details/number-of-employees?matchId=$matchId",
           |      "title": "Get the organisation's paye employee count data."
           |    },
           |    "self": {
           |      "href": "/organisations/matching/corporation-tax/$matchId"
           |    },
           |    "details-corporation-tax": {
           |      "href": "/organisations/details/corporation-tax?matchId=$matchId",
           |      "title": "Get the organisation's Corporation Tax details data."
           |    }
           |  },
           |  "employerName": "name"
           |}""".stripMargin
      )
    }

    Scenario("a valid request is made for an expired match") {
      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/matching/corporation-tax/$matchId")
        .headers(requestHeaders(acceptHeaderP1))
        .asString

      response.code shouldBe NOT_FOUND

      Json.parse(response.body) shouldBe Json.obj(
        "code"    -> "NOT_FOUND",
        "message" -> "The resource can not be found"
      )

    }

    Scenario("not authorized") {

      Given("an invalid privileged Auth bearer token")
      AuthStub.willNotAuthorizePrivilegedAuthToken(authToken, scopes)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/matching/corporation-tax/$matchId")
        .headers(requestHeaders(acceptHeaderP1))
        .asString

      Then("the response status should be 401 (unauthorized)")
      response.code shouldBe UNAUTHORIZED
      Json.parse(response.body) shouldBe Json.obj(
        "code"    -> "UNAUTHORIZED",
        "message" -> "Bearer token is missing or not authorized"
      )
    }

    Scenario("a request is made with a missing match id") {
      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/matching/corporation-tax/")
        .headers(requestHeaders(acceptHeaderP1))
        .asString

      response.code shouldBe NOT_FOUND
    }

    Scenario("a request is made with a malformed match id") {
      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/matching/corporation-tax/foo")
        .headers(requestHeaders(acceptHeaderP1))
        .asString

      response.code shouldBe BAD_REQUEST

      Json.parse(response.body) shouldBe Json.obj(
        "statusCode"    -> 400,
        "message" -> "bad request"
      )
    }

    Scenario("a request is made with a missing correlation id") {
      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/matching/corporation-tax/$matchId")
        .headers(requestHeadersInvalid(acceptHeaderP1))
        .asString

      response.code shouldBe BAD_REQUEST

      Json.parse(response.body) shouldBe Json.obj(
        "code"    -> "INVALID_REQUEST",
        "message" -> "CorrelationId is required"
      )
    }

    Scenario("a request is made with a malformed correlation id") {
      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/matching/corporation-tax/$matchId")
        .headers(requestHeadersMalformed(acceptHeaderP1))
        .asString

      response.code shouldBe BAD_REQUEST

      Json.parse(response.body) shouldBe Json.obj(
        "code"    -> "INVALID_REQUEST",
        "message" -> "Malformed CorrelationId"
      )
    }
  }

  Feature("self-assessment") {
    Scenario("a valid request is made for an existing match") {
      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      And("A valid match exist")
      result(mongoRepository.cache(matchId.toString, "organisations-matching", saMatch), timeout)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/matching/self-assessment/$matchId")
        .headers(requestHeaders(acceptHeaderP1))
        .asString

      response.code shouldBe OK

      Json.parse(response.body) shouldBe Json.parse(
        s"""{
           |  "address": {
           |    "line1": "line1",
           |    "postcode": "postcode"
           |  },
           |  "_links": {
           |    "details-number-of-employees": {
           |      "href": "/organisations/details/number-of-employees?matchId=$matchId",
           |      "title": "Get the organisation's paye employee count data."
           |    },
           |    "details-self-assessment": {
           |      "href": "/organisations/details/self-assessment/?matchId=$matchId",
           |      "title": "Get the organisation's self-assessment details data."
           |    },
           |    "self": {
           |      "href": "/organisations/matching/self-assessment/$matchId"
           |    }
           |  },
           |  "taxPayerType": "Individual",
           |  "taxPayerName": "name"
           |}""".stripMargin
      )
    }

    Scenario("a valid request is made for an expired match") {
      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/matching/self-assessment/$matchId")
        .headers(requestHeaders(acceptHeaderP1))
        .asString

      response.code shouldBe NOT_FOUND

      Json.parse(response.body) shouldBe Json.obj(
        "code"    -> "NOT_FOUND",
        "message" -> "The resource can not be found"
      )

    }

    Scenario("not authorized") {

      Given("an invalid privileged Auth bearer token")
      AuthStub.willNotAuthorizePrivilegedAuthToken(authToken, scopes)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/matching/self-assessment/$matchId")
        .headers(requestHeaders(acceptHeaderP1))
        .asString

      Then("the response status should be 401 (unauthorized)")
      response.code shouldBe UNAUTHORIZED
      Json.parse(response.body) shouldBe Json.obj(
        "code"    -> "UNAUTHORIZED",
        "message" -> "Bearer token is missing or not authorized"
      )
    }

    Scenario("a request is made with a missing match id") {
      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/matching/self-assessment/")
        .headers(requestHeaders(acceptHeaderP1))
        .asString

      response.code shouldBe NOT_FOUND
    }

    Scenario("a request is made with a malformed match id") {
      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/matching/self-assessment/foo")
        .headers(requestHeaders(acceptHeaderP1))
        .asString

      response.code shouldBe BAD_REQUEST

      Json.parse(response.body) shouldBe Json.obj(
        "statusCode"    -> 400,
        "message" -> "bad request"
      )
    }

    Scenario("a request is made with a missing correlation id") {
      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/matching/self-assessment/$matchId")
        .headers(requestHeadersInvalid(acceptHeaderP1))
        .asString

      response.code shouldBe BAD_REQUEST

      Json.parse(response.body) shouldBe Json.obj(
        "code"    -> "INVALID_REQUEST",
        "message" -> "CorrelationId is required"
      )
    }

    Scenario("a request is made with a malformed correlation id") {
      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/matching/self-assessment/$matchId")
        .headers(requestHeadersMalformed(acceptHeaderP1))
        .asString

      response.code shouldBe BAD_REQUEST

      Json.parse(response.body) shouldBe Json.obj(
        "code"    -> "INVALID_REQUEST",
        "message" -> "Malformed CorrelationId"
      )
    }
  }
}
