/*
 * Copyright 2022 HM Revenue & Customs
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

import component.uk.gov.hmrc.organisationsmatchingapi.controllers.stubs.{AuthStub, BaseSpec, IfStub, MatchingStub}
import play.api.http.Status
import play.api.libs.json.Json
import scalaj.http.{Http, HttpResponse}
import uk.gov.hmrc.organisationsmatchingapi.domain.integrationframework.{IfAddress, IfCorpTaxCompanyDetails, IfSaTaxPayerNameAddress, IfSaTaxpayerDetails}
import uk.gov.hmrc.organisationsmatchingapi.domain.ogd.{CtMatchingRequest, SaMatchingRequest}

import java.util.concurrent.TimeUnit
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import uk.gov.hmrc.organisationsmatchingapi.domain.models.{CtMatch, SaMatch}


class MatchingControllerSpec extends BaseSpec  {

  val scopes = List("read:organisations-matching-ho")
  val ctRequest: CtMatchingRequest = CtMatchingRequest("0213456789", "name", "line1", "NE11NE")
  val ctRequestString: String = Json.prettyPrint(Json.toJson(ctRequest))

  val saRequest: SaMatchingRequest = SaMatchingRequest("0213456789", "A", "name", "line1", "NE11NE")
  val saRequestString: String = Json.prettyPrint(Json.toJson(saRequest))

  val ifCorpTax: IfCorpTaxCompanyDetails = IfCorpTaxCompanyDetails(
    utr = Some("0123456789"),
    crn = Some("0123456789"),
    registeredDetails = None,
    communicationDetails = None
  )

  val ifSa: IfSaTaxpayerDetails = IfSaTaxpayerDetails(
    utr = Some("0123456789"),
    taxpayerType = Some("Individual"),
    taxpayerDetails = Some(Seq(IfSaTaxPayerNameAddress(
      name = Some("Billy Billyson"),
      addressType = Some("Base"),
      address = Some(IfAddress(
        line1 = Some("line 1"),
        line2 = None,
        line3 = None,
        line4 = None,
        postcode = Some("ABC DEF")
      )))
    )))

  Feature("corporation-tax endpoint") {

    Scenario("Valid POST request") {

      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      Given("Data found in IF")
      IfStub.searchCorpTaxCompanyDetails(ctRequest.companyRegistrationNumber, ifCorpTax)

      Given("A successful match")
      MatchingStub.willReturnCtMatch(correlationIdHeader._2)

      val response: HttpResponse[String] = Http(s"$serviceUrl/corporation-tax")
        .headers(requestHeaders(acceptHeaderP1))
        .postData(ctRequestString)
        .asString

      Then("The response status should be 200 (Ok)")
      response.code mustBe Status.OK

      And("The response should have a valid payload")
      val responseJson = Json.parse(response.body)

      val matchId = (responseJson \ "matchId").get.as[String]

      responseJson mustBe Json.parse(
        s"""{
           |  "_links" : {
           |    "getCorporationTaxMatch" : {
           |      "href" : "/organisations/matching/corporation-tax/$matchId",
           |      "title" : "Get links to Corporation Tax and number of employees details for a matched organisation"
           |    },
           |    "self" : {
           |      "href" : "/organisations/matching/corporation-tax"
           |    }
           |  },
           |  "matchId" : "$matchId"
           |}""".stripMargin)


      val cachedData: Option[CtMatch] = Await.result(mongoRepository.fetchAndGetEntry[CtMatch](matchId), Duration(5, TimeUnit.SECONDS))
      cachedData.isEmpty mustBe false
    }

    Scenario("Bad request with missing fields") {

      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      val requestWithMissingFields =
        """{
          | "companyRegistrationNumber": "0213456789",
          | "employerName": "name",
          | "address": {
          |    "postcode": "NE11NE"}
          | }""".stripMargin

      val response: HttpResponse[String] = Http(s"$serviceUrl/corporation-tax")
        .headers(requestHeaders(acceptHeaderP1))
        .postData(requestWithMissingFields)
        .asString

      Then("The response status should be 400 (Bad request)")
      response.code mustBe Status.BAD_REQUEST

      And("The response should have a valid payload")
      val responseJson = Json.parse(response.body)

      responseJson mustBe Json.parse(
        s"""{ "code": "INVALID_REQUEST",
           | "message" : "/address/addressLine1 is required" }""".stripMargin)
    }

    Scenario("Bad request with malformed CRN") {

      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      val requestWithMalformedCrn =
        """{
          | "companyRegistrationNumber": "021xxx6789",
          | "employerName": "name",
          | "address": {
          |    "addressLine1": "line1",
          |    "postcode": "NE11NE"}
          | }""".stripMargin

      val response: HttpResponse[String] = Http(s"$serviceUrl/corporation-tax")
        .headers(requestHeaders(acceptHeaderP1))
        .postData(requestWithMalformedCrn)
        .asString

      Then("The response status should be 400 (Bad request)")
      response.code mustBe Status.BAD_REQUEST

      And("The response should have a valid payload")
      val responseJson = Json.parse(response.body)

      responseJson mustBe Json.parse(
        s"""{ "code": "INVALID_REQUEST",
           | "message" : "Malformed CRN submitted" }""".stripMargin)
    }

    Scenario("Valid POST request but IF data not found") {

      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      Given("Data not found in IF")
      IfStub.searchCorpTaxCompanyDetailsNotFound(ctRequest.companyRegistrationNumber, ifCorpTax)

      val response: HttpResponse[String] = Http(s"$serviceUrl/corporation-tax")
        .headers(requestHeaders(acceptHeaderP1))
        .postData(ctRequestString)
        .asString

      Then("The response status should be 404 (Not Found)")
      response.code mustBe Status.NOT_FOUND

      And("The response should have a valid payload")
      val responseJson = Json.parse(response.body)

      responseJson mustBe Json.parse(s"""{"code":"MATCHING_FAILED","message":"There is no match for the information provided"}""".stripMargin)
    }


    Scenario("Valid POST request but match not found") {

      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      Given("Data found in IF")
      IfStub.searchCorpTaxCompanyDetails(ctRequest.companyRegistrationNumber, ifCorpTax)

      Given("An unsuccessful match")
      MatchingStub.willReturnCtMatchNotFound(correlationIdHeader._2)

      val response: HttpResponse[String] = Http(s"$serviceUrl/corporation-tax")
        .headers(requestHeaders(acceptHeaderP1))
        .postData(ctRequestString)
        .asString

      Then("The response status should be 404 (Not Found)")
      response.code mustBe Status.NOT_FOUND

      And("The response should have a valid payload")
      val responseJson = Json.parse(response.body)

      responseJson mustBe Json.parse(s"""{"code":"MATCHING_FAILED","message":"There is no match for the information provided"}""".stripMargin)
    }

    Scenario("A missing correlation id") {

      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/corporation-tax")
        .headers(requestHeadersInvalid(acceptHeaderP1))
        .postData(ctRequestString)
        .asString

      response.code mustBe Status.BAD_REQUEST

      Json.parse(response.body) mustBe Json.obj(
        "code"    -> "INVALID_REQUEST",
        "message" -> "CorrelationId is required"
      )
    }

    Scenario("a request is made with a malformed correlation id") {

      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/corporation-tax")
        .headers(requestHeadersMalformed(acceptHeaderP1))
        .postData(ctRequestString)
        .asString

      response.code mustBe Status.BAD_REQUEST

      Json.parse(response.body) mustBe Json.obj(
        "code"    -> "INVALID_REQUEST",
        "message" -> "Malformed CorrelationId"
      )
    }

    Scenario("not authorized") {

      val requestString: String = Json.prettyPrint(Json.toJson(ctRequest))

      Given("an invalid privileged Auth bearer token")
      AuthStub.willNotAuthorizePrivilegedAuthToken(authToken, scopes)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/corporation-tax")
        .headers(requestHeaders(acceptHeaderP1))
        .postData(requestString)
        .asString

      Then("the response status should be 401 (unauthorized)")
      response.code mustBe Status.UNAUTHORIZED
      Json.parse(response.body) mustBe Json.obj(
        "code"    -> "UNAUTHORIZED",
        "message" -> "Bearer token is missing or not authorized"
      )
    }
  }

  Feature("self-assessment endpoint") {

    Scenario("Valid POST request") {

      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      Given("Data found in IF")
      IfStub.searchSaCompanyDetails(saRequest.selfAssessmentUniqueTaxPayerRef, ifSa)

      Given("A successful match")
      MatchingStub.willReturnSaMatch(correlationIdHeader._2)

      val response: HttpResponse[String] = Http(s"$serviceUrl/self-assessment")
        .headers(requestHeaders(acceptHeaderP1))
        .postData(saRequestString)
        .asString

      Then("The response status should be 200 (Ok)")
      response.code mustBe Status.OK

      And("The response should have a valid payload")
      val responseJson = Json.parse(response.body)

      val matchId = (responseJson \ "matchId").get.as[String]
      responseJson mustBe Json.parse(
        s"""{
           |  "_links" : {
           |    "getSelfAssessmentMatch" : {
           |      "href" : "/organisations/matching/self-assessment/$matchId",
           |      "title" : "Get links to Self Assessment and number of employees details for a matched organisation"
           |    },
           |    "self" : {
           |      "href" : "/organisations/matching/self-assessment"
           |    }
           |  },
           |  "matchId" : "$matchId"
           |}""".stripMargin)

      val cachedData: Option[SaMatch] = Await.result(mongoRepository.fetchAndGetEntry[SaMatch](matchId), Duration(5, TimeUnit.SECONDS))
      cachedData.isEmpty mustBe false
    }

    Scenario("Valid POST request but IF data not found") {

      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      Given("Data not found in IF")
      IfStub.searchSaCompanyDetailsNotFound(saRequest.selfAssessmentUniqueTaxPayerRef, ifSa)

      val response: HttpResponse[String] = Http(s"$serviceUrl/self-assessment")
        .headers(requestHeaders(acceptHeaderP1))
        .postData(saRequestString)
        .asString

      Then("The response status should be 404 (Not Found)")
      response.code mustBe Status.NOT_FOUND

      And("The response should have a valid payload")
      val responseJson = Json.parse(response.body)

      responseJson mustBe Json.parse(s"""{"code":"MATCHING_FAILED","message":"There is no match for the information provided"}""".stripMargin)
    }

    Scenario("Valid POST request but match not found") {

      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      Given("Data found in IF")
      IfStub.searchSaCompanyDetails(saRequest.selfAssessmentUniqueTaxPayerRef, ifSa)

      Given("An unsuccessful match")
      MatchingStub.willReturnSaMatchNotFound(correlationIdHeader._2)

      val response: HttpResponse[String] = Http(s"$serviceUrl/self-assessment")
        .headers(requestHeaders(acceptHeaderP1))
        .postData(saRequestString)
        .asString

      Then("The response status should be 404 (Not Found)")
      response.code mustBe Status.NOT_FOUND

      And("The response should have a valid payload")
      val responseJson = Json.parse(response.body)

      responseJson mustBe Json.parse(s"""{"code":"MATCHING_FAILED","message":"There is no match for the information provided"}""".stripMargin)
    }

    Scenario("Bad request with missing fields") {

      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      val requestWithMissingFields =
        """{
          | "selfAssessmentUniqueTaxPayerRef": "0213456789",
          | "taxPayerType": "A",
          | "taxPayerName": "name",
          | "address": {
          |    "postcode": "NE11NE"
        |     }
          | }""".stripMargin

      val response: HttpResponse[String] = Http(s"$serviceUrl/self-assessment")
        .headers(requestHeaders(acceptHeaderP1))
        .postData(requestWithMissingFields)
        .asString

      Then("The response status should be 400 (Bad request)")
      response.code mustBe Status.BAD_REQUEST

      And("The response should have a valid payload")
      val responseJson = Json.parse(response.body)

      responseJson mustBe Json.parse(
        s"""{ "code": "INVALID_REQUEST",
           | "message" : "/address/addressLine1 is required" }""".stripMargin)
    }

    Scenario("A missing correlation id") {

      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/corporation-tax")
        .headers(requestHeadersInvalid(acceptHeaderP1))
        .postData(ctRequestString)
        .asString

      response.code mustBe Status.BAD_REQUEST

      Json.parse(response.body) mustBe Json.obj(
        "code"    -> "INVALID_REQUEST",
        "message" -> "CorrelationId is required"
      )
    }

    Scenario("a request is made with a malformed correlation id") {

      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/self-assessment")
        .headers(requestHeadersMalformed(acceptHeaderP1))
        .postData(saRequestString)
        .asString

      response.code mustBe Status.BAD_REQUEST

      Json.parse(response.body) mustBe Json.obj(
        "code"    -> "INVALID_REQUEST",
        "message" -> "Malformed CorrelationId"
      )
    }

    Scenario("not authorized") {

      Given("an invalid privileged Auth bearer token")
      AuthStub.willNotAuthorizePrivilegedAuthToken(authToken, scopes)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/self-assessment")
        .headers(requestHeaders(acceptHeaderP1))
        .postData(saRequestString)
        .asString

      Then("the response status should be 401 (unauthorized)")
      response.code mustBe Status.UNAUTHORIZED
      Json.parse(response.body) mustBe Json.obj(
        "code"    -> "UNAUTHORIZED",
        "message" -> "Bearer token is missing or not authorized"
      )
    }
  }
}