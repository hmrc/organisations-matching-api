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

package unit.uk.gov.hmrc.organisationsmatchingapi.audit

import org.mockito.Mockito.{times, verify}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.{ArgumentCaptor, Mockito}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.organisationsmatchingapi.audit.AuditHelper
import uk.gov.hmrc.organisationsmatchingapi.audit.models._
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

class AuditHelperSpec extends AsyncWordSpec with Matchers with MockitoSugar {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val auditConnector: AuditConnector = mock[AuditConnector]
  val auditHelper = new AuditHelper(auditConnector)
  val correlationId = "test"
  val matchId = "80a6bb14-d888-436e-a541-4000674c60aa"
  val applicationId = "80a6bb14-d888-436e-a541-4000674c60bb"
  val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withHeaders("X-Application-Id" -> applicationId)
  val endpoint = "/test"
  val ifResponse = "bar"
  val crn = "12345678"
  val scopes = "test"
  val ifUrl = s"host/organisations/corporation-tax/$crn/company/details"
  val matchingUrlCt = s"/organisations-matching/perform-match/cotax?matchId=${matchId}&correlationId=${correlationId}"
  val matchingUrlSa = s"/organisations-matching/perform-match/self-assessment?matchId=${matchId}&correlationId=${correlationId}"
  val matchingResponse: JsValue = Json.toJson("match")
  val matchingNonMatchResponse = "Not Found"

  "auditAuthScopes" in {

    Mockito.reset(auditConnector)

    val captor = ArgumentCaptor.forClass(classOf[ScopesAuditEventModel])

    auditHelper.auditAuthScopes(matchId, scopes, request)

    verify(auditConnector, times(1)).sendExplicitAudit(eqTo("AuthScopesAuditEvent"),
      captor.capture())(using any(), any(), any())

    val capturedEvent = captor.getValue.asInstanceOf[ScopesAuditEventModel]
    capturedEvent.apiVersion shouldEqual "1.0"
    capturedEvent.matchId shouldEqual matchId
    capturedEvent.scopes shouldBe scopes
    capturedEvent.applicationId shouldBe applicationId

  }

  "auditApiFailure" in {

    Mockito.reset(auditConnector)

    val msg = "Something went wrong"

    val captor = ArgumentCaptor.forClass(classOf[ApiFailureResponseEventModel])

    auditHelper.auditApiFailure(Some(correlationId), matchId, request, "/test", msg)

    verify(auditConnector, times(1)).sendExplicitAudit(eqTo("ApiFailureEvent"),
      captor.capture())(using any(), any(), any())

    val capturedEvent = captor.getValue.asInstanceOf[ApiFailureResponseEventModel]
    capturedEvent.matchId shouldEqual matchId
    capturedEvent.correlationId shouldEqual Some(correlationId)
    capturedEvent.requestUrl shouldEqual endpoint
    capturedEvent.response shouldEqual msg
    capturedEvent.applicationId shouldBe applicationId

  }

  "auditIfApiResponse" in {

    Mockito.reset(auditConnector)

    val captor = ArgumentCaptor.forClass(classOf[IfApiResponseEventModel])

    auditHelper.auditIfApiResponse(correlationId, matchId, request, ifUrl, ifResponse)

    verify(auditConnector, times(1)).sendExplicitAudit(eqTo("IntegrationFrameworkApiResponseEvent"),
      captor.capture())(using any(), any(), any())

    val capturedEvent = captor.getValue.asInstanceOf[IfApiResponseEventModel]
    capturedEvent.matchId shouldEqual matchId
    capturedEvent.correlationId shouldEqual correlationId
    capturedEvent.requestUrl shouldBe ifUrl
    capturedEvent.ifResponse shouldBe ifResponse
    capturedEvent.applicationId shouldBe applicationId

  }

  "auditIfApiFailure" in {

    Mockito.reset(auditConnector)

    val msg = "Something went wrong"

    val captor = ArgumentCaptor.forClass(classOf[ApiFailureResponseEventModel])

    auditHelper.auditIfApiFailure(correlationId, matchId, request, ifUrl, msg)

    verify(auditConnector, times(1)).sendExplicitAudit(eqTo("IntegrationFrameworkApiFailureEvent"),
      captor.capture())(using any(), any(), any())

    val capturedEvent = captor.getValue.asInstanceOf[ApiFailureResponseEventModel]
    capturedEvent.matchId shouldEqual matchId
    capturedEvent.correlationId shouldEqual Some(correlationId)
    capturedEvent.requestUrl shouldEqual ifUrl
    capturedEvent.response shouldEqual msg
    capturedEvent.applicationId shouldBe applicationId

  }

  "auditOrganisationsMatchingResponse match CT" in {

    Mockito.reset(auditConnector)

    val captor = ArgumentCaptor.forClass(classOf[OrganisationsMatchingResponseEventModel])

    auditHelper.auditOrganisationsMatchingResponse(correlationId, matchId, request, matchingUrlCt, matchingResponse)

    verify(auditConnector, times(1)).sendExplicitAudit(eqTo("OrganisationsMatchingResponseEvent"),
      captor.capture())(using any(), any(), any())

    val capturedEvent = captor.getValue.asInstanceOf[OrganisationsMatchingResponseEventModel]
    capturedEvent.matchId shouldEqual matchId
    capturedEvent.correlationId shouldEqual correlationId
    capturedEvent.requestUrl shouldBe matchingUrlCt
    capturedEvent.matchingResponse shouldBe matchingResponse
    capturedEvent.applicationId shouldBe applicationId

  }

  "auditOrganisationsMatchingResponse match SA" in {

    Mockito.reset(auditConnector)

    val captor = ArgumentCaptor.forClass(classOf[OrganisationsMatchingResponseEventModel])

    auditHelper.auditOrganisationsMatchingResponse(correlationId, matchId, request, matchingUrlSa, matchingResponse)

    verify(auditConnector, times(1)).sendExplicitAudit(eqTo("OrganisationsMatchingResponseEvent"),
      captor.capture())(using any(), any(), any())

    val capturedEvent = captor.getValue.asInstanceOf[OrganisationsMatchingResponseEventModel]
    capturedEvent.matchId shouldEqual matchId
    capturedEvent.correlationId shouldEqual correlationId
    capturedEvent.requestUrl shouldBe matchingUrlSa
    capturedEvent.matchingResponse shouldBe matchingResponse
    capturedEvent.applicationId shouldBe applicationId

  }

  "auditOrganisationsMatchingResponse non match CT" in {

    Mockito.reset(auditConnector)

    val captor = ArgumentCaptor.forClass(classOf[OrganisationsMatchingResponseEventModel])

    auditHelper.auditOrganisationsMatchingResponse(correlationId, matchId, request, matchingUrlCt, Json.toJson(matchingNonMatchResponse))

    verify(auditConnector, times(1)).sendExplicitAudit(eqTo("OrganisationsMatchingResponseEvent"),
      captor.capture())(using any(), any(), any())

    val capturedEvent = captor.getValue.asInstanceOf[OrganisationsMatchingResponseEventModel]
    capturedEvent.matchId shouldEqual matchId
    capturedEvent.correlationId shouldEqual correlationId
    capturedEvent.requestUrl shouldBe matchingUrlCt
    capturedEvent.matchingResponse shouldBe Json.toJson(matchingNonMatchResponse)
    capturedEvent.applicationId shouldBe applicationId

  }

  "auditOrganisationsMatchingResponse non match SA" in {

    Mockito.reset(auditConnector)

    val captor = ArgumentCaptor.forClass(classOf[OrganisationsMatchingResponseEventModel])

    auditHelper.auditOrganisationsMatchingResponse(correlationId, matchId, request, matchingUrlSa, Json.toJson(matchingNonMatchResponse))

    verify(auditConnector, times(1)).sendExplicitAudit(eqTo("OrganisationsMatchingResponseEvent"),
      captor.capture())(using any(), any(), any())

    val capturedEvent = captor.getValue.asInstanceOf[OrganisationsMatchingResponseEventModel]
    capturedEvent.matchId shouldEqual matchId
    capturedEvent.correlationId shouldEqual correlationId
    capturedEvent.requestUrl shouldBe matchingUrlSa
    capturedEvent.matchingResponse shouldBe Json.toJson(matchingNonMatchResponse)
    capturedEvent.applicationId shouldBe applicationId

  }

  "auditOrganisationsMatchingFailure CT" in {

    Mockito.reset(auditConnector)

    val msg = "Something went wrong"

    val captor = ArgumentCaptor.forClass(classOf[OrganisationsMatchingFailureResponseEventModel])

    auditHelper.auditOrganisationsMatchingFailure(correlationId, matchId, request, matchingUrlCt, msg)

    verify(auditConnector, times(1)).sendExplicitAudit(eqTo("OrganisationsMatchingFailureEvent"),
      captor.capture())(using any(), any(), any())

    val capturedEvent = captor.getValue.asInstanceOf[OrganisationsMatchingFailureResponseEventModel]
    capturedEvent.matchId shouldEqual matchId
    capturedEvent.correlationId shouldEqual Some(correlationId)
    capturedEvent.requestUrl shouldEqual matchingUrlCt
    capturedEvent.response shouldEqual msg
    capturedEvent.applicationId shouldBe applicationId

  }

  "auditOrganisationsMatchingFailure SA" in {

    Mockito.reset(auditConnector)

    val msg = "Something went wrong"

    val captor = ArgumentCaptor.forClass(classOf[OrganisationsMatchingFailureResponseEventModel])

    auditHelper.auditOrganisationsMatchingFailure(correlationId, matchId, request, matchingUrlSa, msg)

    verify(auditConnector, times(1)).sendExplicitAudit(eqTo("OrganisationsMatchingFailureEvent"),
      captor.capture())(using any(), any(), any())

    val capturedEvent = captor.getValue.asInstanceOf[OrganisationsMatchingFailureResponseEventModel]
    capturedEvent.matchId shouldEqual matchId
    capturedEvent.correlationId shouldEqual Some(correlationId)
    capturedEvent.requestUrl shouldEqual matchingUrlSa
    capturedEvent.response shouldEqual msg
    capturedEvent.applicationId shouldBe applicationId

  }
}
