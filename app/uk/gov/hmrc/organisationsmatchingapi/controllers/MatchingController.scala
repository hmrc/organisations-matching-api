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
import uk.gov.hmrc.organisationsmatchingapi.actions.{PrivilegedAuthAction, ValidatedAction, VersionTransformer}
import uk.gov.hmrc.organisationsmatchingapi.errorhandler.ErrorHandling
import uk.gov.hmrc.organisationsmatchingapi.models.{CompanyMatchingRequest, PartnershipMatchingRequest}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class MatchingController @Inject()(val authConnector: AuthConnector,
                                   cc: ControllerComponents,
                                   privilegedAuthAction: PrivilegedAuthAction,
                                   validatedAction: ValidatedAction,
                                   versionTransformer: VersionTransformer) extends BaseApiController(cc) with ErrorHandling {

  val companyMatchingRequestSchema = loadVersionedSchemas("company-matching-request.json")
  val partnershipMatchingRequestSchema = loadVersionedSchemas("partnership-matching-request.json")

  def matchCompany = privilegedAuthAction
    .andThen(validatedAction)
    .andThen(versionTransformer).async(parse.json) { implicit request =>
    handleErrors {
      withVersionedJsonBody[CompanyMatchingRequest](companyMatchingRequestSchema) {
        matchRequest =>
          Future successful Ok
      }
    }
  }

  def matchPartnership = privilegedAuthAction
    .andThen(validatedAction)
    .andThen(versionTransformer).async(parse.json) { implicit request =>
    handleErrors {
      withVersionedJsonBody[PartnershipMatchingRequest](partnershipMatchingRequestSchema) {
        matchRequest =>
          Future successful Ok
      }
    }
  }

}