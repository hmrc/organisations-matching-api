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

import java.util.UUID

import javax.inject.{Inject, Singleton}
import play.api.hal.Hal.state
import play.api.hal.HalLink
import play.api.libs.json.Json.toJson
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, ControllerComponents, PlayBodyParsers}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.organisationsmatchingapi.audit.AuditHelper
import uk.gov.hmrc.organisationsmatchingapi.domain.ogd.{CtMatchingRequest, MatchIdResponse, SaMatchingRequest}
import uk.gov.hmrc.organisationsmatchingapi.play.RequestHeaderUtils._
import uk.gov.hmrc.organisationsmatchingapi.services.{CacheService, MatchingService, ScopesHelper, ScopesService}

import scala.concurrent.ExecutionContext

@Singleton
class MatchingController @Inject()(val authConnector: AuthConnector,
                                   cc: ControllerComponents,
                                   scopeService: ScopesService,
                                   scopesHelper: ScopesHelper,
                                   bodyParsers: PlayBodyParsers,
                                   cacheService: CacheService,
                                   matchingService: MatchingService
                                  )(implicit val ec: ExecutionContext, auditHelper: AuditHelper) extends BaseApiController(cc)
  with PrivilegedAuthentication {

  def matchOrganisationCt() : Action[JsValue] = Action.async(bodyParsers.json) { implicit request =>
    val matchId = UUID.randomUUID()
    val self =  "/organisations/matching/corporation-tax"
    authenticate(scopeService.getAllScopes, matchId.toString) { authScopes =>
      withJsonBody[CtMatchingRequest] { matchRequest => {
        val correlationId = validateCorrelationId(request)
          matchingService.matchCoTax(matchId, correlationId.toString, matchRequest).map { _ => {

            val selfLink = HalLink("self", self)
            val data = toJson(MatchIdResponse(matchId))
            val response = Json.toJson(state(data) ++ scopesHelper.getHalLinks(matchId, None, authScopes, Some(List("getCorporationTaxMatch"))) ++ selfLink)

            auditHelper.auditApiResponse(
              correlationId.toString,
              matchId.toString,
              authScopes.mkString(","),
              request,
              selfLink.toString,
              Some(response))

            Ok(response)
          }
        }
      }}
    } recover recoveryWithAudit(maybeCorrelationId(request), request.body.toString, self)
  }

  def matchOrganisationSa() : Action[JsValue] = Action.async(bodyParsers.json) { implicit request =>
    val matchId = UUID.randomUUID()
    val self =  "/organisations/matching/self-assessment"
    authenticate(scopeService.getAllScopes, matchId.toString) { authScopes =>
      withJsonBody[SaMatchingRequest] { matchRequest => {
        val correlationId = validateCorrelationId(request)
          matchingService.matchSaTax(matchId, correlationId.toString, matchRequest).map { _ => {
            val selfLink = HalLink("self", self)
            val data = toJson(MatchIdResponse(matchId))
            val response = Json.toJson(state(data) ++ scopesHelper.getHalLinks(matchId, None, authScopes, Some(List("getSelfAssessmentMatch"))) ++ selfLink)

            auditHelper.auditApiResponse(
              correlationId.toString,
              matchId.toString,
              authScopes.mkString(","),
              request,
              selfLink.toString,
              Some(response))

            Ok(response)
          }
        }
      }}
    } recover recoveryWithAudit(maybeCorrelationId(request), request.body.toString, self)
  }
}