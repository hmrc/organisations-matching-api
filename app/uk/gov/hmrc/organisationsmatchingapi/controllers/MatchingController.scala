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

import play.api.libs.json.Json.toJson
import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.organisationsmatchingapi.audit.AuditHelper
import uk.gov.hmrc.organisationsmatchingapi.errorhandler.ErrorHandling
import uk.gov.hmrc.organisationsmatchingapi.services.CacheService
import uk.gov.hmrc.organisationsmatchingapi.play.RequestHeaderUtils.maybeCorrelationId
import uk.gov.hmrc.organisationsmatchingapi.services.MatchingService

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MatchingController @Inject()(val authConnector: AuthConnector,
                                   cc: ControllerComponents,
                                   cacheService: CacheService,
                                   implicit val auditHelper: AuditHelper,
                                   matchingService: MatchingService)
                                  (implicit ec: ExecutionContext) extends BaseApiController(cc) with ErrorHandling {

  def matchOrganisationCt() : Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok("IMPLEMENT ME!"))
  }

  def matchOrganisationSa() : Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok("IMPLEMENT ME!"))
  }

  // two methods below need to be deleted once merged with HODS-154
  def matchedOrganisationSa(matchId: UUID) : Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok("IMPLEMENT ME!"))
  }

  def matchedOrganisationCt(matchId: UUID) : Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok("IMPLEMENT ME!"))
  }

  def matchedOrganisation(matchId: UUID) = Action.async { implicit request =>
    withUuid(matchId.toString) { matchUuid =>
      matchingService.fetchMatchedOrganisationRecord(matchUuid) map { matchedOrganisation =>
        Ok(toJson(matchedOrganisation))
      }
    } recover recoveryWithAudit(maybeCorrelationId(request), matchId.toString, s"/match-record/$matchId")
  }

}
