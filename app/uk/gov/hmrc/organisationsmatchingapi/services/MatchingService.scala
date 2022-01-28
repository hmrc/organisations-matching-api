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

package uk.gov.hmrc.organisationsmatchingapi.services

import play.api.libs.json.JsValue

import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import play.api.mvc.RequestHeader
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.organisationsmatchingapi.connectors.{IfConnector, OrganisationsMatchingConnector}
import uk.gov.hmrc.organisationsmatchingapi.domain.models.{CtMatch, SaMatch}
import uk.gov.hmrc.organisationsmatchingapi.domain.ogd.{CtMatchingRequest, SaMatchingRequest}
import uk.gov.hmrc.organisationsmatchingapi.domain.organisationsmatching.{CtKnownFacts, CtOrganisationsMatchingRequest, SaKnownFacts, SaOrganisationsMatchingRequest}

import scala.concurrent.{ExecutionContext, Future}

class MatchingService @Inject()( ifConnector: IfConnector,
                                 matchingConnector: OrganisationsMatchingConnector,
                                 cacheService: CacheService)
                               ( implicit val ec: ExecutionContext  ) {

  def matchCoTax(matchId: UUID, correlationId: String, ctMatchingRequest: CtMatchingRequest)(implicit hc: HeaderCarrier, header: RequestHeader): Future[JsValue] = {
    for {
      ifData <- ifConnector.fetchCorporationTax(matchId.toString, ctMatchingRequest.companyRegistrationNumber)
      matched   <-  matchingConnector.matchCycleCotax(matchId.toString,  correlationId, CtOrganisationsMatchingRequest(
        CtKnownFacts(
          ctMatchingRequest.companyRegistrationNumber,
          ctMatchingRequest.employerName,
          ctMatchingRequest.addressLine1,
          ctMatchingRequest.postcode
        ), ifData
      ))
      _ <- cacheService.cacheCtUtr(CtMatch(ctMatchingRequest, matchId, LocalDateTime.now(), ifData.utr), ifData.utr.getOrElse(""))
    } yield matched
  }

  def matchSaTax(matchId: UUID, correlationId: String, saMatchingRequest: SaMatchingRequest)(implicit hc: HeaderCarrier, header: RequestHeader): Future[JsValue] = {
    for {
      ifData <- ifConnector.fetchSelfAssessment(matchId.toString, saMatchingRequest.selfAssessmentUniqueTaxPayerRef)
      matched   <-  matchingConnector.matchCycleSelfAssessment(matchId.toString,  correlationId, SaOrganisationsMatchingRequest(
        SaKnownFacts(
          saMatchingRequest.selfAssessmentUniqueTaxPayerRef,
          saMatchingRequest.taxPayerType,
          saMatchingRequest.taxPayerName,
          saMatchingRequest.addressLine1,
          saMatchingRequest.postcode
        ), ifData
      ))
      _ <- cacheService.cacheSaUtr(SaMatch(saMatchingRequest, matchId, LocalDateTime.now(), ifData.utr), ifData.utr.getOrElse(""))
    } yield matched
  }


}
