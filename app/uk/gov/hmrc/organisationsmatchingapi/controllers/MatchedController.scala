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

package uk.gov.hmrc.organisationsmatchingapi.controllers

import play.api.hal.Hal.state
import play.api.hal.HalLink
import play.api.libs.json.Json
import play.api.libs.json.Json.toJson
import play.api.mvc.{Action, AnyContent, ControllerComponents, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.organisationsmatchingapi.audit.AuditHelper
import uk.gov.hmrc.organisationsmatchingapi.domain.models.{CtMatch, SaMatch}
import uk.gov.hmrc.organisationsmatchingapi.domain.ogd.VatMatchingResponse
import uk.gov.hmrc.organisationsmatchingapi.play.RequestHeaderUtils.{maybeCorrelationId, validateCorrelationId}
import uk.gov.hmrc.organisationsmatchingapi.services.{MatchedService, ScopesHelper, ScopesService}

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class MatchedController @Inject() (
  val authConnector: AuthConnector,
  cc: ControllerComponents,
  mcc: MessagesControllerComponents,
  scopeService: ScopesService,
  scopesHelper: ScopesHelper,
  matchedService: MatchedService
)(implicit auditHelper: AuditHelper, ec: ExecutionContext)
    extends BaseApiController(mcc, cc) with PrivilegedAuthentication {

  def matchedOrganisationCt(matchId: String): Action[AnyContent] = Action.async { implicit request =>
    val self = s"/organisations/matching/corporation-tax/$matchId"
    authenticate(scopeService.getAllScopes, matchId) { authScopes =>
      val correlationId = validateCorrelationId(request)
      withValidUuid(matchId, "matchId") { matchIdUuid =>
        matchedService.fetchCt(matchIdUuid) map { cacheData =>
          val exclude = Some(List("getSelfAssessmentDetails", "getVatDetails"))
          val selfLink = HalLink("self", self)
          val links = scopesHelper.getHalLinks(matchIdUuid, exclude, authScopes, None, true) ++ selfLink
          val response = Json.toJson(state(CtMatch.convert(cacheData)) ++ links)

          auditHelper.auditApiResponse(
            correlationId.toString,
            matchId,
            authScopes.mkString(","),
            request,
            selfLink.toString,
            Some(response)
          )

          Ok(response)
        }
      }
    } recover recoveryWithAudit(maybeCorrelationId(request), request.body.toString, self)
  }

  def matchedOrganisationSa(matchId: String): Action[AnyContent] = Action.async { implicit request =>
    val self = s"/organisations/matching/self-assessment/$matchId"
    authenticate(scopeService.getAllScopes, matchId) { authScopes =>
      val correlationId = validateCorrelationId(request)
      withValidUuid(matchId, "matchId") { matchIdUuuid =>
        matchedService.fetchSa(matchIdUuuid) map { cacheData =>
          val exclude = Some(List("getCorporationTaxDetails", "getVatDetails"))
          val selfLink = HalLink("self", self)
          val links = scopesHelper.getHalLinks(matchIdUuuid, exclude, authScopes, None, true) ++ selfLink
          val response = Json.toJson(state(SaMatch.convert(cacheData)) ++ links)

          auditHelper.auditApiResponse(
            correlationId.toString,
            matchId,
            authScopes.mkString(","),
            request,
            selfLink.toString,
            Some(response)
          )

          Ok(response)
        }
      }
    } recover recoveryWithAudit(maybeCorrelationId(request), request.body.toString, self)
  }

  def matchedOrganisationVat(matchId: UUID) = Action.async { implicit request =>
    authenticate(scopeService.getAllScopes, matchId.toString) { authScopes =>
      val correlationId = validateCorrelationId(request)
      matchedService
        .fetchMatchedOrganisationVatRecord(matchId)
        .map { vatMatch =>
          val self = s"/organisations/matching/vat/$matchId"
          val selfLink = HalLink("self", self)
          val links = scopesHelper.getHalLinks(matchId, None, authScopes, Some(List("getVatDetails")), true) ++ selfLink
          val response = Json.toJson(state(VatMatchingResponse(vatMatch.vrn.mkString)) ++ links)

          auditHelper.auditApiResponse(
            correlationId.toString,
            matchId.toString,
            authScopes.mkString(","),
            request,
            selfLink.toString,
            Some(response)
          )
          Ok(response)
        }
    }.recover(recoveryWithAudit(maybeCorrelationId(request), matchId.toString, s"/match-record/vat/$matchId"))
  }

  def matchedOrganisation(matchId: String) = Action.async { implicit request =>
    withValidUuid(matchId, "matchId") { matchIdUuid =>
      matchedService.fetchMatchedOrganisationRecord(matchIdUuid) map { matchedOrganisation =>
        Ok(toJson(matchedOrganisation))
      }
    } recover recoveryWithAudit(maybeCorrelationId(request), matchId, s"/match-record/$matchId")
  }

  def matchedVatRecord(matchId: java.util.UUID) = Action.async { implicit request =>
    matchedService.fetchMatchedOrganisationVatRecord(matchId).map { vatMatch =>
      Ok(toJson(vatMatch))
    } recover recoveryWithAudit(maybeCorrelationId(request), matchId.toString, s"/match-record/vat/$matchId")
  }
}
