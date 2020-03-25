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

import javax.inject.{Inject, Singleton}
import play.api.mvc.ControllerComponents
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.organisationsmatchingapi.actions.{ValidatedAction, VersionTransformer}
import uk.gov.hmrc.organisationsmatchingapi.models.{CrnMatch, OrganisationMatchingRequest}

import scala.concurrent.Future

@Singleton
class MatchingController @Inject()(val authConnector: AuthConnector, cc: ControllerComponents, validatedAction: ValidatedAction, versionTransformer: VersionTransformer) extends BaseApiController(cc) {

  val organisationMatchingRequestSchema = loadVersionedSchemas("organisation-matching-request.json")

  def matchOrganisation = Action.async(parse.json) { implicit request =>
    withPrivilegedAuth {
      validatedAction.andThen(versionTransformer).async(parse.json) { implicit request =>
        handleErrors {
          withVersionedJsonBody[OrganisationMatchingRequest](organisationMatchingRequestSchema) {
            orgMatchRequest =>
              Future successful Ok()
          }
        }
      }
    }
  }

}
