/*
 * Copyright 2020 HM Revenue & Customs
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
import play.api.mvc.ControllerComponents
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.organisationsmatchingapi.actions.{PrivilegedAuthAction, ValidatedAction, VersionTransformer}
import uk.gov.hmrc.organisationsmatchingapi.errorhandler.ErrorHandling
import uk.gov.hmrc.organisationsmatchingapi.models.{CrnMatchingRequest, SaUtrMatchingRequest}
import uk.gov.hmrc.organisationsmatchingapi.services.MatchingService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class MatchingController @Inject()(val authConnector: AuthConnector,
                                   cc: ControllerComponents,
                                   privilegedAuthAction: PrivilegedAuthAction,
                                   validatedAction: ValidatedAction,
                                   versionTransformer: VersionTransformer,
                                   matchingService: MatchingService) extends BaseApiController(cc) with ErrorHandling {

  val crnMatchingRequestSchema = loadVersionedSchemas("crn-matching-request.json")
  val saUtrMatchingRequestSchema = loadVersionedSchemas("sautr-matching-request.json")

  private def commonAction = privilegedAuthAction
    .andThen(validatedAction)
    .andThen(versionTransformer)

  def matchCrn = commonAction.async(parse.json) { implicit request =>
    handleErrors {
      withVersionedJsonBody[CrnMatchingRequest](crnMatchingRequestSchema) {
        matchRequest =>
          Future successful Ok
      }
    }
  }

  def matchSaUtr = commonAction.async(parse.json) { implicit request =>
    handleErrors {
      withVersionedJsonBody[SaUtrMatchingRequest](saUtrMatchingRequestSchema) {
        matchRequest =>
          Future successful Ok
      }
    }
  }

}
