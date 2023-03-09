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

package uk.gov.hmrc.organisationsmatchingapi.handlers

import javax.inject.Inject
import play.api.{Configuration, OptionalDevContext}
import play.api.http.{HttpConfiguration, HttpErrorHandler, HttpFilters}
import play.api.mvc.{Handler, RequestHeader}
import play.api.routing.Router
import play.core.WebCommands
import uk.gov.hmrc.organisationsmatchingapi.play.RequestHeaderUtils._
import uk.gov.hmrc.play.bootstrap.http.RequestHandler

import scala.util.Try

class VersioningRequestHandler @Inject()(webCommands: WebCommands,
                                         optDevContext: OptionalDevContext,
                                         config: Configuration,
                                         router: Router,
                                         errorHandler: HttpErrorHandler,
                                         httpConfiguration: HttpConfiguration,
                                         filters: HttpFilters)
  extends RequestHandler(webCommands, optDevContext, router, errorHandler, httpConfiguration, filters) {

  private lazy val unversionedContexts = Try {
    config
      .getOptional[Seq[String]]("versioning.unversionedContexts")
      .getOrElse(Seq.empty[String])
  }.getOrElse(Seq.empty[String])

  override def routeRequest(request: RequestHeader): Option[Handler] = {
    val requestContext = extractUriContext(request)
    if (unversionedContexts.contains(requestContext)) {
      super.routeRequest(request)
    } else {
      super.routeRequest(getVersionedRequest(request))
    }

  }
}