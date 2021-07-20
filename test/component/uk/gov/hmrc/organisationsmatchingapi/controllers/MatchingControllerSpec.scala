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

import java.util.concurrent.TimeUnit

import play.api.http.Status
import play.api.libs.json.{JsString, Json}
import scalaj.http.{Http, HttpResponse}
import stubs.{AuthStub, BaseSpec, MatchingStub}
import uk.gov.hmrc.cache.model.Cache
import uk.gov.hmrc.organisationsmatchingapi.domain.ogd.{CtMatchingRequest, SaMatchingRequest}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration


class MatchingControllerSpec extends BaseSpec  {

  val scopes = List("read:organisations-matching-ho-ssp")
  val ctRequest = CtMatchingRequest("0213456789", "name", "line1", "NE11NE")
  val saRequest = SaMatchingRequest("0213456789", "A", "name", "line1", "NE11NE")

  Scenario("Valid POST request to corporation-tax endpoint") {

    Given("A valid privileged Auth bearer token")
    AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

    Given("A successful match")
    MatchingStub.willReturnCtMatch( correlationIdHeader._2)

    val requestString: String = Json.prettyPrint(Json.toJson(ctRequest))

    val response: HttpResponse[String] = Http(s"$serviceUrl/corporation-tax")
      .headers(requestHeaders(acceptHeaderP1))
      .postData(requestString)
      .asString

    Then("The response status should be 200 (Ok)")
    response.code mustBe Status.OK

    And("The response should have a valid payload")
    val responseJson = Json.parse(response.body)



    val matchId = (responseJson \ "matchId").get.as[String]
    responseJson mustBe Json.parse(s"""{
                                       |  "_links" : {
                                       |    "getCorporationTaxMatch" : {
                                       |      "href" : "/organisations/matching/corporation-tax/$matchId",
                                       |      "title" : "Get the organisation's details"
                                       |    },
                                       |    "self" : {
                                       |      "href" : "/organisations/matching/corporation-tax"
                                       |    }
                                       |  },
                                       |  "matchId" : "$matchId"
                                       |}""".stripMargin)


    val cachedData:List[Cache] = Await.result(mongoRepository.find(("_id", JsString(matchId.toString))), Duration(5, TimeUnit.SECONDS))
    cachedData.isEmpty mustBe false
  }



  Scenario("Valid POST request to corporation-tax endpoint not found") {

    Given("A valid privileged Auth bearer token")
    AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

    Given("A successful match")
    MatchingStub.willReturnCtMatchNotFound(correlationIdHeader._2)

    val requestString: String = Json.prettyPrint(Json.toJson(ctRequest))

    val response: HttpResponse[String] = Http(s"$serviceUrl/corporation-tax")
      .headers(requestHeaders(acceptHeaderP1))
      .postData(requestString)
      .asString

    Then("The response status should be 404 (Not Found)")
    response.code mustBe Status.NOT_FOUND

    And("The response should have a valid payload")
    val responseJson = Json.parse(response.body)

    responseJson mustBe Json.parse(s"""{"code":"NOT_FOUND","message":"Resource was not found"}""".stripMargin)
  }

  Scenario("Valid POST request to self-assessment endpoint") {

    Given("A valid privileged Auth bearer token")
    AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

    Given("A successful match")
    MatchingStub.willReturnSaMatch( correlationIdHeader._2)

    val requestString: String = Json.prettyPrint(Json.toJson(saRequest))

    val response: HttpResponse[String] = Http(s"$serviceUrl/self-assessment")
      .headers(requestHeaders(acceptHeaderP1))
      .postData(requestString)
      .asString

    Then("The response status should be 200 (Ok)")
    response.code mustBe Status.OK

    And("The response should have a valid payload")
    val responseJson = Json.parse(response.body)

    val matchId = (responseJson \ "matchId").get.as[String]
    responseJson mustBe Json.parse(s"""{
                                      |  "_links" : {
                                      |    "getSelfAssessmentMatch" : {
                                      |      "href" : "/organisations/matching/self-assessment/$matchId",
                                      |      "title" : "Get the organisation's self assessment details"
                                      |    },
                                      |    "self" : {
                                      |      "href" : "/organisations/matching/self-assessment"
                                      |    }
                                      |  },
                                      |  "matchId" : "$matchId"
                                      |}""".stripMargin)

    val cachedData:List[Cache] = Await.result(mongoRepository.find(("_id", JsString(matchId.toString))), Duration(5, TimeUnit.SECONDS))
    cachedData.isEmpty mustBe false
  }

  Scenario("Valid POST request to self-assessment endpoint not found") {

    Given("A valid privileged Auth bearer token")
    AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

    Given("A successful match")
    MatchingStub.willReturnSaMatchNotFound(correlationIdHeader._2)

    val requestString: String = Json.prettyPrint(Json.toJson(saRequest))

    val response: HttpResponse[String] = Http(s"$serviceUrl/self-assessment")
      .headers(requestHeaders(acceptHeaderP1))
      .postData(requestString)
      .asString

    Then("The response status should be 404 (Not Found)")
    response.code mustBe Status.NOT_FOUND

    And("The response should have a valid payload")
    val responseJson = Json.parse(response.body)

    responseJson mustBe Json.parse(s"""{"code":"NOT_FOUND","message":"Resource was not found"}""".stripMargin)
  }

}
