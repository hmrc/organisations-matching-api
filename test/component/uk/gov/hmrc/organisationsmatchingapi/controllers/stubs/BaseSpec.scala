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

package component.uk.gov.hmrc.organisationsmatchingapi.controllers.stubs

import java.util.concurrent.TimeUnit
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.scalatest._
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.http.HeaderNames.{ACCEPT, AUTHORIZATION, CONTENT_TYPE}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.{Helpers, TestServer}
import play.mvc.Http.MimeTypes.JSON
import uk.gov.hmrc.organisationsmatchingapi.repository.MatchRepository

import java.net.ServerSocket
import scala.concurrent.Await.result
import scala.concurrent.duration.{Duration, FiniteDuration}

import org.mongodb.scala.SingleObservableFuture



trait BaseSpec
    extends AnyFeatureSpec with BeforeAndAfterAll with BeforeAndAfterEach with Matchers with GuiceOneServerPerSuite
    with GivenWhenThen {

  protected def appBuilder: GuiceApplicationBuilder = GuiceApplicationBuilder()
    .configure(
      "cache.enabled"                              -> true,
      "auditing.enabled"                           -> false,
      "auditing.traceRequests"                     -> false,
      "mongodb.uri"                                -> "mongodb://localhost:27017/organisations-matching-api",
      "microservice.services.auth.port"            -> AuthStub.port,
      "microservice.services.internal-auth.port"   -> InternalAuthStub.port,
      "microservice.services.organisations-matching.port" -> MatchingStub.port,
      "microservice.services.integration-framework.port" -> IfStub.port,
      "run.mode"                                        -> "It",
      "versioning.unversionedContexts"                  -> List("/match-record")
    )

  implicit override lazy val app: Application = appBuilder.build()

  val timeout: FiniteDuration = Duration(5, TimeUnit.SECONDS)
  val serviceUrl = s"http://127.0.0.1:$port"
  val mocks = Seq(AuthStub, InternalAuthStub, IfStub, MatchingStub)
  val authToken = "Bearer AUTH_TOKEN"
  val clientId = "CLIENT_ID"
  val acceptHeaderP1: (String, String) = ACCEPT -> "application/vnd.hmrc.1.0+json"
  val correlationIdHeader: (String, String) = "CorrelationId" -> "188e9400-b636-4a3b-80ba-230a8c72b92a"
  val correlationIdHeaderMalformed: (String, String) = "CorrelationId" -> "foo"
  val mongoRepository: MatchRepository = app.injector.instanceOf[MatchRepository]

  protected def requestHeaders(acceptHeader: (String, String) = acceptHeaderP1): Map[String, String] =
    Map(CONTENT_TYPE -> JSON, AUTHORIZATION -> authToken, acceptHeader, correlationIdHeader)

  protected def requestHeadersInvalid(acceptHeader: (String, String) = acceptHeaderP1): Map[String, String] =
    Map(CONTENT_TYPE -> JSON, AUTHORIZATION -> authToken, acceptHeader)

  protected def requestHeadersMalformed(acceptHeader: (String, String) = acceptHeaderP1): Map[String, String] =
    Map(CONTENT_TYPE -> JSON, AUTHORIZATION -> authToken, acceptHeader, correlationIdHeaderMalformed)

  protected def invalidRequest(message: String) =
    s"""{"code":"INVALID_REQUEST","message":"$message"}"""

  protected def withConfiguredServer[A](overrides: (String, Any)*)(f: String => A): A = {
    val socket = new ServerSocket(0)
    val freePort = socket.getLocalPort
    socket.close()

    val testApp = appBuilder.configure(overrides*).build()

    Helpers.running(TestServer(freePort, testApp)) {
      f(s"http://127.0.0.1:$freePort")
    }
  }

  override protected def beforeEach(): Unit = {
    mocks.foreach(m => if (!m.server.isRunning) m.server.start())
    result(mongoRepository.collection.drop().toFuture(), timeout)
    result(mongoRepository.ensureIndexes(), timeout)
  }

  override protected def afterEach(): Unit =
    mocks.foreach(_.mock.resetMappings())

  override def afterAll(): Unit = {
    mocks.foreach(_.server.stop())
    result(mongoRepository.collection.drop().toFuture(), timeout)
  }
}

case class MockHost(port: Int) {
  val server = new WireMockServer(WireMockConfiguration.wireMockConfig().port(port))
  val mock = new WireMock("127.0.0.1", port)
  val url = s"http://127.0.0.1:9000"
}
