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

package unit.uk.gov.hmrc.organisationsmatchingapi.services

import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.BDDMockito.`given`
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{JsString, Json}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException}
import uk.gov.hmrc.organisationsmatchingapi.cache.InsertResult
import uk.gov.hmrc.organisationsmatchingapi.connectors.{IfConnector, OrganisationsMatchingConnector}
import uk.gov.hmrc.organisationsmatchingapi.domain.integrationframework.common.IfAddress
import uk.gov.hmrc.organisationsmatchingapi.domain.integrationframework.ct.{IfCorpTaxCompanyDetails, IfNameAndAddressDetails, IfNameDetails}
import uk.gov.hmrc.organisationsmatchingapi.domain.integrationframework.sa.{IfSaTaxpayerDetails, IfSaTaxpayerNameAddress}
import uk.gov.hmrc.organisationsmatchingapi.domain.integrationframework.vat._
import uk.gov.hmrc.organisationsmatchingapi.domain.models.VatMatch
import uk.gov.hmrc.organisationsmatchingapi.domain.ogd.{CtMatchingRequest, SaMatchingRequest, VatMatchingRequest}
import uk.gov.hmrc.organisationsmatchingapi.domain.organisationsmatching.{VatKnownFacts, VatOrganisationsMatchingRequest}
import uk.gov.hmrc.organisationsmatchingapi.services.{CacheService, MatchingService}
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import unit.uk.gov.hmrc.organisationsmatchingapi.util.SpecBase

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MatchingServiceSpec extends AnyWordSpec with SpecBase with Matchers with MockitoSugar {
  val mockIfConnector: IfConnector = mock[IfConnector]
  val mockMatchingConnector: OrganisationsMatchingConnector = mock[OrganisationsMatchingConnector]
  val mockCacheService: CacheService = mock[CacheService]

  val matchingService = new MatchingService(
    mockIfConnector,
    mockMatchingConnector,
    mockCacheService
  )

  val utr = "0123456789"
  val matchId: UUID = UUID.randomUUID()

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

    `given`(mockIfConnector.fetchCorporationTax(any(), any())(using any(), any()))
      .willReturn(Future.successful(corpTaxCompanyDetails))

    `given`(mockCacheService.cacheCtUtr(any(), any())).willReturn(Future.successful(InsertResult.InsertSucceeded))

    "Matching connector returns a match" in {
      `given`(mockMatchingConnector.matchCycleCotax(any(), any(), any())(using any(), any()))
        .willReturn(Future.successful(JsString("match")))

      val result = await(matchingService.matchCoTax(
        UUID.randomUUID(),
        UUID.randomUUID().toString,
        CtMatchingRequest("0123456789", "name", "addressLine1", "postcode")))

      result.as[String] shouldBe "match"
    }

    "Matching connector returns a Not Found" in {
      `given`(mockMatchingConnector.matchCycleCotax(any(), any(), any())(using any(), any()))
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
      taxpayerDetails = Some(Seq(IfSaTaxpayerNameAddress(
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

    `given`(mockIfConnector.fetchSelfAssessment(any(), any())(using any(), any()))
      .willReturn(Future.successful(saTaxpayerDetails))

    `given`(mockCacheService.cacheSaUtr(any(), any())).willReturn(Future.successful(InsertResult.InsertSucceeded))

    "Matching connector returns a match" in {
      `given`(mockMatchingConnector.matchCycleSelfAssessment(any(), any(), any())(using any(), any()))
        .willReturn(Future.successful(JsString("match")))

      val result = await(matchingService.matchSaTax(
        UUID.randomUUID(),
        UUID.randomUUID().toString,
        SaMatchingRequest("0123456789", "", "name", "addressLine1", "postcode")))

      result.as[String] shouldBe "match"
    }

    "Matching connector returns a Not Found" in {
      `given`(mockMatchingConnector.matchCycleSelfAssessment(any(), any(), any())(using any(), any()))
        .willReturn(Future.failed(new NotFoundException("Not found")))

      intercept[NotFoundException] {
        await(matchingService.matchSaTax(
          UUID.randomUUID(),
          UUID.randomUUID().toString,
          SaMatchingRequest("0123456789", "", "name", "addressLine1", "postcode")))
      }
    }
  }

  "MatchVat" when {
    val request = VatMatchingRequest("vrn", "orgName", "line1", "postcode")
    val ifResponse = IfVatCustomerInformation(
      IfVatApprovedInformation(
        IfVatCustomerDetails(Some("orgName")),
        IfPPOB(Some(IfVatCustomerAddress(Some("line1"), Some("postcode"))))
      )
    )

    "matching return a match" in {
      `given`(mockIfConnector.fetchVat(eqTo(matchId.toString), eqTo(request.vrn))(using any(), any()))
        .willReturn(Future.successful(ifResponse))
      val orgsMatchingRequest = VatOrganisationsMatchingRequest(
        VatKnownFacts(request.vrn, request.organisationName, request.addressLine1, request.postcode),
        IfVatCustomerInformationSimplified(
          request.vrn,
          ifResponse.approvedInformation.customerDetails.organisationName.get,
          ifResponse.approvedInformation.PPOB.address.flatMap(_.line1),
          ifResponse.approvedInformation.PPOB.address.flatMap(_.postCode)
        )
      )
      `given`(mockMatchingConnector.matchCycleVat(eqTo(matchId), eqTo(UUID.randomUUID()), eqTo(orgsMatchingRequest))(using any(), any()))
        .willReturn(Future.successful(Json.toJson("test")))
      `given`(mockCacheService.cacheVatVrn(VatMatch(matchId, Some(request.vrn))))
    }
  }
}
