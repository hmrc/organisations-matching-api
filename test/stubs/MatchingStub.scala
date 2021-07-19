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

package stubs

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.matching.StringValuePattern
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.AUTHORIZATION
import play.api.http.{HeaderNames, Status}
import play.api.libs.json.Json.{arr, obj, toJson}
import play.api.libs.json.{JsArray, Json}
import uk.gov.hmrc.auth.core.Enrolment
import uk.gov.hmrc.auth.core.authorise.Predicate


object MatchingStub extends MockHost(9658) {

  def willReturnCtMatch(
    correlationId: String): StubMapping =
    mock.register(
      post(urlMatching(s"/organisations-matching/perform-match/cotax\\?matchId=[a-z0-9-]*&correlationId=${correlationId}"))
        .willReturn(aResponse()
          .withStatus(Status.OK)
          .withBody(s""""match"""")))

  def willReturnCtMatchNotFound(
                       correlationId: String): StubMapping =
    mock.register(
      post(urlMatching(s"/organisations-matching/perform-match/cotax\\?matchId=[a-z0-9-]*&correlationId=${correlationId}"))
        .willReturn(aResponse()
          .withStatus(Status.NOT_FOUND)))

  def willReturnSaMatch(
                         correlationId: String): StubMapping =
    mock.register(
      post(urlMatching(s"/organisations-matching/perform-match/self-assessment\\?matchId=[a-z0-9-]*&correlationId=${correlationId}"))
        .willReturn(aResponse()
          .withStatus(Status.OK)
          .withBody(s""""match"""")))

  def willReturnSaMatchNotFound(
                                 correlationId: String): StubMapping =
    mock.register(
      post(urlMatching(s"/organisations-matching/perform-match/self-assessment\\?matchId=[a-z0-9-]*&correlationId=${correlationId}"))
        .willReturn(aResponse()
          .withStatus(Status.NOT_FOUND)))

}
