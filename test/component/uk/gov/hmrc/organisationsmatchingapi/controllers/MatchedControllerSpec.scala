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

import scalaj.http.Http
import component.uk.gov.hmrc.organisationsmatchingapi.controllers.stubs.{AuthStub, BaseSpec}
import play.api.libs.json.Json
import play.api.test.Helpers._
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import uk.gov.hmrc.organisationsmatchingapi.domain.models.{CtMatch, SaMatch, VatMatch}
import uk.gov.hmrc.organisationsmatchingapi.domain.ogd.{CtMatchingRequest, SaMatchingRequest, VatMatchingRequest}

import java.util.UUID
import scala.concurrent.Await.result

class MatchedControllerSpec extends BaseSpec  {

  val matchId: UUID = UUID.randomUUID()
  val scopes    = List("read:organisations-matching-ho")
  val ctRequest: CtMatchingRequest = CtMatchingRequest("0123456789", "name", "line1", "postcode")
  val ctMatch: CtMatch = CtMatch(ctRequest, matchId, utr = Some("testutr"))
  val saRequest: SaMatchingRequest = SaMatchingRequest("utr", "Individual", "name", "line1", "postcode")
  val saMatch: SaMatch = SaMatch(saRequest, matchId, utr = Some("testutr"))
  val vatRequest = VatMatchingRequest("testvrn", "organisation", "line1", "postcode")
  val vatMatch = VatMatch(matchId, Some("testvrn"))

  Feature("cotax") {
    Scenario("a valid request is made for an existing match") {

      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      And("A valid match exist")
      result(mongoRepository.cache(matchId.toString, ctMatch), timeout)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/corporation-tax/$matchId")
        .headers(requestHeaders(acceptHeaderP1))
        .asString

      response.code shouldBe OK

      Json.parse(response.body) shouldBe Json.parse(
        s"""{
           |  "address": {
           |    "addressLine1": "line1",
           |    "postcode": "postcode"
           |  },
           |  "_links": {
           |    "getEmployeeCount": {
           |      "href": "/organisations/details/number-of-employees?matchId=$matchId",
           |      "title": "Find the number of employees for an organisation"
           |    },
           |    "self": {
           |      "href": "/organisations/matching/corporation-tax/$matchId"
           |    },
           |    "getCorporationTaxDetails": {
           |      "href": "/organisations/details/corporation-tax?matchId=$matchId",
           |      "title": "Get an organisation's Corporation Tax details"
           |    }
           |  },
           |  "companyRegistrationNumber": "${ctRequest.companyRegistrationNumber}",
           |  "employerName": "name"
           |}""".stripMargin
      )
    }

    Scenario("a request is made with a missing match id") {
      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/corporation-tax/")
        .headers(requestHeaders(acceptHeaderP1))
        .asString

      response.code shouldBe NOT_FOUND

      Json.parse(response.body) shouldBe Json.obj(
        "code"    -> "NOT_FOUND",
        "message" -> "The resource can not be found"
      )
    }

    Scenario("a valid request is made for an expired match") {
      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/corporation-tax/$matchId")
        .headers(requestHeaders(acceptHeaderP1))
        .asString

      response.code shouldBe NOT_FOUND

      Json.parse(response.body) shouldBe Json.obj(
        "code"    -> "NOT_FOUND",
        "message" -> "The resource can not be found"
      )
    }

    Scenario("a request is made with a missing correlation id") {
      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/corporation-tax/$matchId")
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
      val response = Http(s"$serviceUrl/corporation-tax/$matchId")
        .headers(requestHeadersMalformed(acceptHeaderP1))
        .asString

      response.code shouldBe BAD_REQUEST

      Json.parse(response.body) shouldBe Json.obj(
        "code"    -> "INVALID_REQUEST",
        "message" -> s"Malformed CorrelationId ${correlationIdHeaderMalformed._2}"
      )
    }

    Scenario("not authorized") {

      Given("an invalid privileged Auth bearer token")
      AuthStub.willNotAuthorizePrivilegedAuthToken(authToken, scopes)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/corporation-tax/$matchId")
        .headers(requestHeaders(acceptHeaderP1))
        .asString

      Then("the response status should be 401 (unauthorized)")
      response.code shouldBe UNAUTHORIZED
      Json.parse(response.body) shouldBe Json.obj(
        "code"    -> "UNAUTHORIZED",
        "message" -> "Bearer token is missing or not authorized"
      )
    }

    Scenario("a request is made with a malformed match id") {
      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/corporation-tax/foo")
        .headers(requestHeaders(acceptHeaderP1))
        .asString

      response.code shouldBe BAD_REQUEST

      Json.parse(response.body) shouldBe Json.obj(
        "code"    -> "INVALID_REQUEST",
        "message" -> "matchId format is invalid"
      )
    }
  }

  Feature("self-assessment") {

    Scenario("a valid request is made for an existing match") {
      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      And("A valid match exist")
      result(mongoRepository.cache(matchId.toString, saMatch), timeout)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/self-assessment/$matchId")
        .headers(requestHeaders(acceptHeaderP1))
        .asString

      response.code shouldBe OK

      Json.parse(response.body) shouldBe Json.parse(
        s"""{
           |  "address": {
           |    "addressLine1": "line1",
           |    "postcode": "postcode"
           |  },
           |  "_links": {
           |    "getEmployeeCount": {
           |      "href": "/organisations/details/number-of-employees?matchId=$matchId",
           |      "title": "Find the number of employees for an organisation"
           |    },
           |    "getSelfAssessmentDetails": {
           |      "href": "/organisations/details/self-assessment?matchId=$matchId",
           |      "title": "Get an organisation's Self Assessment details"
           |    },
           |    "self": {
           |      "href": "/organisations/matching/self-assessment/$matchId"
           |    }
           |  },
           |  "selfAssessmentUniqueTaxPayerRef": "utr",
           |  "taxPayerType": "Individual",
           |  "taxPayerName": "name"
           |}""".stripMargin
      )
    }

    Scenario("a request is made with a missing match id") {
      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/self-assessment/")
        .headers(requestHeaders(acceptHeaderP1))
        .asString

      response.code shouldBe NOT_FOUND

      Json.parse(response.body) shouldBe Json.obj(
        "code"    -> "NOT_FOUND",
        "message" -> "The resource can not be found"
      )
    }

    Scenario("a valid request is made for an expired match") {

      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/self-assessment/$matchId")
        .headers(requestHeaders(acceptHeaderP1))
        .asString

      response.code shouldBe NOT_FOUND

      Json.parse(response.body) shouldBe Json.obj(
        "code"    -> "NOT_FOUND",
        "message" -> "The resource can not be found"
      )
    }

    Scenario("a request is made with a missing correlation id") {
      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/self-assessment/$matchId")
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
      val response = Http(s"$serviceUrl/self-assessment/$matchId")
        .headers(requestHeadersMalformed(acceptHeaderP1))
        .asString

      response.code shouldBe BAD_REQUEST

      Json.parse(response.body) shouldBe Json.obj(
        "code"    -> "INVALID_REQUEST",
        "message" -> s"Malformed CorrelationId ${correlationIdHeaderMalformed._2}"
      )
    }

    Scenario("not authorized") {

      Given("an invalid privileged Auth bearer token")
      AuthStub.willNotAuthorizePrivilegedAuthToken(authToken, scopes)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/self-assessment/$matchId")
        .headers(requestHeaders(acceptHeaderP1))
        .asString

      Then("the response status should be 401 (unauthorized)")
      response.code shouldBe UNAUTHORIZED
      Json.parse(response.body) shouldBe Json.obj(
        "code"    -> "UNAUTHORIZED",
        "message" -> "Bearer token is missing or not authorized"
      )
    }

    Scenario("a request is made with a malformed match id") {
      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/self-assessment/foo")
        .headers(requestHeaders(acceptHeaderP1))
        .asString

      response.code shouldBe BAD_REQUEST

      Json.parse(response.body) shouldBe Json.obj(
        "code" ->"INVALID_REQUEST",
        "message" ->"matchId format is invalid"
      )
    }
  }

  ignore("vat") {
    Scenario("a valid request is made for an existing match") {
      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      And("A valid match exist")
      result(mongoRepository.cache(matchId.toString, vatMatch), timeout)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/vat/$matchId")
        .headers(requestHeaders(acceptHeaderP1))
        .asString

      response.code shouldBe OK
      Json.parse(response.body) shouldBe Json.parse(
        s"""{
          |  "_links": {
          |    "self": {
          |      "href": "/organisations/matching/vat/$matchId"
          |    },
          |    "getVatDetails": {
          |      "href": "/organisations/details/vat?matchId=$matchId",
          |      "title": "Get an organisation's VAT details"
          |    }
          |  },
          |  "vrn": "testvrn"
          |}""".stripMargin
      )
    }

    Scenario("a request is made with a missing match id") {
      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/vat/")
        .headers(requestHeaders(acceptHeaderP1))
        .asString

      response.code shouldBe NOT_FOUND

      Json.parse(response.body) shouldBe Json.obj(
        "code" -> "NOT_FOUND",
        "message" -> "The resource can not be found"
      )
    }

    Scenario("a request is made with a missing correlation id") {
      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/vat/$matchId")
        .headers(requestHeadersInvalid(acceptHeaderP1))
        .asString

      response.code shouldBe BAD_REQUEST

      Json.parse(response.body) shouldBe Json.obj(
        "code" -> "INVALID_REQUEST",
        "message" -> "CorrelationId is required"
      )
    }

    Scenario("a request is made with a malformed correlation id") {
      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/vat/$matchId")
        .headers(requestHeadersMalformed(acceptHeaderP1))
        .asString

      response.code shouldBe BAD_REQUEST

      Json.parse(response.body) shouldBe Json.obj(
        "code" -> "INVALID_REQUEST",
        "message" -> s"Malformed CorrelationId ${correlationIdHeaderMalformed._2}"
      )
    }

    Scenario("not authorized") {
      Given("an invalid privileged Auth bearer token")
      AuthStub.willNotAuthorizePrivilegedAuthToken(authToken, scopes)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/vat/$matchId")
        .headers(requestHeaders(acceptHeaderP1))
        .asString

      Then("the response status should be 401 (unauthorized)")
      response.code shouldBe UNAUTHORIZED
      Json.parse(response.body) shouldBe Json.obj(
        "code" -> "UNAUTHORIZED",
        "message" -> "Bearer token is missing or not authorized"
      )
    }

    Scenario("a request is made with a malformed match id") {
      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/vat/foo")
        .headers(requestHeaders(acceptHeaderP1))
        .asString

      response.code shouldBe BAD_REQUEST

      Json.parse(response.body) shouldBe Json.obj(
        "code" -> "INVALID_REQUEST",
        "message" -> "matchId format is invalid"
      )
    }
  }

  Feature("matched organisation") {
    Scenario("a valid request is made for an existing match id") {
      Given("A valid privileged Auth bearer token")

      And("A valid match exist")
      result(mongoRepository.cache(matchId.toString, saMatch), timeout)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/match-record/$matchId").asString

      response.code shouldBe OK

      Json.parse(response.body) shouldBe Json.parse(
        s"""{ "matchId": "$matchId", "utr": "testutr" }"""
      )
    }

    Scenario("a valid request is made for a match id that does not exist") {
      Given("A valid privileged Auth bearer token")

      And("A valid match does not exist")

      When("the API is invoked")
      val response = Http(s"$serviceUrl/match-record/$matchId").asString

      response.code shouldBe NOT_FOUND

      Json.parse(response.body) shouldBe Json.obj(
        "code"    -> "NOT_FOUND",
        "message" -> "The resource can not be found"
      )
    }

    Scenario("a valid request is made for an invalid match id") {
      Given("A valid privileged Auth bearer token")

      And("A valid match exist")
      result(mongoRepository.cache(matchId.toString, saMatch), timeout)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/match-record/foo").asString

      response.code shouldBe BAD_REQUEST

      Json.parse(response.body) shouldBe Json.obj(
        "code"    -> "INVALID_REQUEST",
        "message" -> "matchId format is invalid"
      )
    }
  }

  Feature("matched organisation vat") {
    Scenario("a valid request is made for an existing match id") {
      Given("A valid privileged Auth bearer token")

      And("A valid match exist")
      result(mongoRepository.cache(matchId.toString, vatMatch), timeout)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/match-record/vat/$matchId").headers(acceptHeaderP1).asString

      response.code shouldBe OK

      Json.parse(response.body) shouldBe Json.parse(
        s"""{ "matchId": "$matchId", "vrn": "${vatMatch.vrn.mkString}" }"""
      )
    }

    Scenario("a valid request is made for a match id that does not exist") {
      Given("A valid privileged Auth bearer token")

      And("A valid match does not exist")

      When("the API is invoked")
      val response = Http(s"$serviceUrl/match-record/vat/$matchId").asString

      response.code shouldBe NOT_FOUND

      Json.parse(response.body) shouldBe Json.obj(
        "code" -> "NOT_FOUND",
        "message" -> "The resource can not be found"
      )
    }

    Scenario("a valid request is made for an invalid match id") {
      Given("A valid privileged Auth bearer token")

      And("A valid match exist")
      result(mongoRepository.cache(matchId.toString, saMatch), timeout)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/match-record/vat/foo").asString

      response.code shouldBe BAD_REQUEST

      Json.parse(response.body) shouldBe Json.obj(
        "code" -> "INVALID_REQUEST",
        "message" -> "matchId format is invalid"
      )
    }
  }

}
