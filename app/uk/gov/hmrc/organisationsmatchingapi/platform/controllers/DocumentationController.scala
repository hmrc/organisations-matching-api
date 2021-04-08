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

package uk.gov.hmrc.organisationsmatchingapi.platform.controllers

import controllers.Assets
import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.http.HttpErrorHandler
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.organisationsmatchingapi.platform.views.txt
import scala.concurrent.Future

@Singleton
class DocumentationController @Inject()( cc: ControllerComponents,
                                         httpErrorHandler: HttpErrorHandler,
                                         assets: Assets,
                                         config: Configuration)
  extends uk.gov.hmrc.api.controllers.DocumentationController(cc, assets, httpErrorHandler) {

  private lazy val whitelistedApplicationIds: Seq[String] = config
    .getOptional[Seq[String]]("api.access.whitelistedApplicationIds")
    .getOrElse(Seq.empty)

  override def definition(): Action[AnyContent] = Action.async {
    Future.successful(
      Ok(txt.definition(whitelistedApplicationIds))
        .withHeaders("Content-Type" -> "application/json"))
  }

  def raml(version: String, file: String): Action[AnyContent] =
    assets.at(s"/public/api/conf/$version", file)

}
