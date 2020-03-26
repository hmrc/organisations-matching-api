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

package uk.gov.hmrc.organisationsmatchingapi.actions

import javax.inject.Inject
import play.api.Logger
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.auth.core.AuthProvider.PrivilegedApplication
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.play.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

class PrivilegedAuthAction @Inject()(val authConnector: AuthConnector, cc: ControllerComponents)
  extends ActionBuilder[Request, AnyContent] with AuthorisedFunctions {

  protected val logger: Logger = play.api.Logger(this.getClass)

  override val parser: BodyParser[AnyContent] = cc.parsers.defaultBodyParser
  override protected val executionContext: ExecutionContext = cc.executionContext

  override def invokeBlock[A](request: Request[A],
                              block: Request[A] => Future[Result]): Future[Result] = {
    implicit val hc = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))
    implicit val ec = executionContext

    authorised(AuthProviders(PrivilegedApplication) and Enrolment("read:organisations-matching")) {
      block(request)
    }.recover {
      case _: NoActiveSession =>
        logger.warn(s"user not logged in")
        Unauthorized
      case _: InsufficientEnrolments =>
        logger.warn(s"stride user doesn't have permission to terminate an agent")
        Forbidden
      case _: UnsupportedAuthProvider =>
        logger.warn(s"user logged in with unsupported auth provider")
        Forbidden
    }
  }

}
