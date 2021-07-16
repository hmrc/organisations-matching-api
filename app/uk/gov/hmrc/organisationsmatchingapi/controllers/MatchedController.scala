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

package uk.gov.hmrc.organisationsmatchingapi.controllers

import play.api.hal.Hal.state
import play.api.hal.HalLink
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.organisationsmatchingapi.audit.AuditHelper
import uk.gov.hmrc.organisationsmatchingapi.domain.models.{CtMatch, SaMatch}
import uk.gov.hmrc.organisationsmatchingapi.domain.ogd.{Address, CtMatchingResponse, SaMatchingResponse}
import uk.gov.hmrc.organisationsmatchingapi.errorhandler.ErrorHandling
import uk.gov.hmrc.organisationsmatchingapi.play.RequestHeaderUtils.{maybeCorrelationId, validateCorrelationId}
import uk.gov.hmrc.organisationsmatchingapi.services.{MatchedService, ScopesHelper, ScopesService}

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MatchedController @Inject()(val authConnector: AuthConnector,
                                           cc: ControllerComponents,
                                           scopeService: ScopesService,
                                           scopesHelper: ScopesHelper,
                                           implicit val auditHelper: AuditHelper,
                                           matchedService: MatchedService)
                                          (implicit ec: ExecutionContext) extends BaseApiController(cc)
  with ErrorHandling
  with PrivilegedAuthentication {

  def matchedOrganisationCt(matchId: UUID): Action[AnyContent] = Action.async { implicit request =>
    authenticate(scopeService.getAllScopes, matchId.toString) { authScopes =>
      val correlationId = validateCorrelationId(request)
      matchedService.fetchCt(matchId) map { cacheData =>

        val selfLink = HalLink("self", s"/organisations/matching/corporation-tax?matchId=$matchId")
        val links    = scopesHelper.getHalLinks(matchId, None, authScopes, None, true) ++ selfLink
        val response = Json.toJson(state(ctResponse(cacheData)) ++ links)

        auditHelper.auditApiResponse(
          correlationId.toString, matchId.toString, authScopes.mkString(","), request, selfLink.toString, Some(response)
        )

        Ok(response)
      }
    } recover recoveryWithAudit(maybeCorrelationId(request), request.body.toString, s"/organisations/matching/corporation-tax?matchId=$matchId")
  }

  def matchedOrganisationSa(matchId: UUID) : Action[AnyContent] = Action.async { implicit request =>
    authenticate(scopeService.getAllScopes, matchId.toString) { authScopes =>
      val correlationId = validateCorrelationId(request)
      matchedService.fetchSa(matchId) map { cacheData =>

        val selfLink = HalLink("self", s"/organisations/matching/self-assessment/?matchId=$matchId")
        val links    = scopesHelper.getHalLinks(matchId, None, authScopes, None, true) ++ selfLink
        val response = Json.toJson(state(saResponse(cacheData)) ++ links)

        auditHelper.auditApiResponse(
          correlationId.toString, matchId.toString, authScopes.mkString(","), request, selfLink.toString, Some(response)
        )

        Ok(response)
      }
    } recover recoveryWithAudit(maybeCorrelationId(request), request.body.toString, s"/organisations/matching/self-assessment?matchId=$matchId")
  }

  private def ctResponse(cacheData: CtMatch) = {
    Json.toJson(
      CtMatchingResponse(
        cacheData.request.employerName,
        Address(
          Some(cacheData.request.addressLine1),
          Some(cacheData.request.postcode)
        )
      )
    )
  }

  private def saResponse(cacheData: SaMatch) = {
    Json.toJson(
      SaMatchingResponse(
        cacheData.request.taxPayerType,
        cacheData.request.taxPayerName,
        Address(
          Some(cacheData.request.addressLine1),
          Some(cacheData.request.postcode)
        )
      )
    )
  }
}
