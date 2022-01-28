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

package unit.uk.gov.hmrc.organisationsmatchingapi.services

import java.util.UUID
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.`given`
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.JsString
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException}
import uk.gov.hmrc.organisationsmatchingapi.cache.InsertResult
import uk.gov.hmrc.organisationsmatchingapi.connectors.{IfConnector, OrganisationsMatchingConnector}
import uk.gov.hmrc.organisationsmatchingapi.domain.integrationframework.{IfAddress, IfCorpTaxCompanyDetails, IfNameAndAddressDetails, IfNameDetails, IfSaTaxPayerNameAddress, IfSaTaxpayerDetails}
import uk.gov.hmrc.organisationsmatchingapi.domain.ogd.{CtMatchingRequest, SaMatchingRequest}
import uk.gov.hmrc.organisationsmatchingapi.services.{CacheService, MatchingService}
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import util.SpecBase

import scala.concurrent.{ExecutionContext, Future}

class MatchingServiceSpec extends AnyWordSpec with SpecBase with Matchers with MockitoSugar {

  private implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  val mockIfConnector: IfConnector = mock[IfConnector]
  val mockMatchingConnector: OrganisationsMatchingConnector = mock[OrganisationsMatchingConnector]
  val mockCacheService: CacheService = mock[CacheService]

  val matchingService = new MatchingService(
    mockIfConnector,
    mockMatchingConnector,
    mockCacheService
  )

  val utr = "0123456789"

  implicit val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/")
  implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(fakeRequest)

  "MatchCoTax" when {

    val corpTaxCompanyDetails = IfCorpTaxCompanyDetails(
      utr = Some(utr),
      crn = Some(utr),
      registeredDetails = Some(IfNameAndAddressDetails(
        name = Some(IfNameDetails(Some("name"), None)),
        address = Some(IfAddress(
          line1 = Some("line1"),
          line2 = None,
          line3 = None,
          line4 = None,
          postcode = Some("postcode"))))),
      communicationDetails = Some(IfNameAndAddressDetails(
        name = Some(IfNameDetails(Some("name"), None)),
        address = Some(IfAddress(
          line1 = Some("line1"),
          line2 = None,
          line3 = None,
          line4 = None,
          postcode = Some("postcode"))))))

    given(mockIfConnector.fetchCorporationTax(any(), any())(any(), any(), any()))
      .willReturn(Future.successful(corpTaxCompanyDetails))

    given(mockCacheService.cacheCtUtr(any(), any())).willReturn(InsertResult.InsertSucceeded)

    "Matching connector returns a match" in {

      given(mockMatchingConnector.matchCycleCotax(any(), any(), any())(any(), any(), any()))
        .willReturn(Future.successful(JsString("match")))

      val result = await(matchingService.matchCoTax(
        UUID.randomUUID(),
        UUID.randomUUID().toString,
        CtMatchingRequest("0123456789", "name", "addressLine1", "postcode")))

      result.as[String] shouldBe "match"
    }

    "Matching connector returns a Not Found" in {
      given(mockMatchingConnector.matchCycleCotax(any(), any(), any())(any(), any(), any()))
        .willReturn(Future.failed(new NotFoundException("Not found")))

      intercept[NotFoundException] {
        await(matchingService.matchCoTax(
          UUID.randomUUID(),
          UUID.randomUUID().toString,
          CtMatchingRequest("0123456789", "name", "addressLine1", "postcode")))
      }
    }
  }

  "MatchSaTax" when {

    val saTaxpayerDetails = IfSaTaxpayerDetails(
      utr = Some(utr),
      taxpayerType = Some("Aa"),
      taxpayerDetails = Some(Seq(IfSaTaxPayerNameAddress(
        name = Some("name"),
        addressType = Some("type"),
        address = Some(IfAddress(
          line1 = Some("line1"),
          line2 = None,
          line3 = None,
          line4 = None,
          postcode = Some("postcode")))
      )))
    )

    given(mockIfConnector.fetchSelfAssessment(any(), any())(any(), any(), any()))
      .willReturn(Future.successful(saTaxpayerDetails))

    given(mockCacheService.cacheSaUtr(any(), any())).willReturn(InsertResult.InsertSucceeded)

    "Matching connector returns a match" in {
      given(mockMatchingConnector.matchCycleSelfAssessment(any(), any(), any())(any(), any(), any()))
        .willReturn(Future.successful(JsString("match")))

      val result = await(matchingService.matchSaTax(
        UUID.randomUUID(),
        UUID.randomUUID().toString,
        SaMatchingRequest("0123456789", "", "name", "addressLine1", "postcode")))

      result.as[String] shouldBe "match"
    }

    "Matching connector returns a Not Found" in {

      given(mockMatchingConnector.matchCycleSelfAssessment(any(), any(), any())(any(), any(), any()))
        .willReturn(Future.failed(new NotFoundException("Not found")))

      intercept[NotFoundException] {
        await(matchingService.matchSaTax(
          UUID.randomUUID(),
          UUID.randomUUID().toString,
          SaMatchingRequest("0123456789", "", "name", "addressLine1", "postcode")))
      }
    }
  }


}
