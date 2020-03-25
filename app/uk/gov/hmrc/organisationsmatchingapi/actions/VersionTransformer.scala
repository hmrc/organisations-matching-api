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

import com.google.inject.{Inject, Singleton}
import play.api.mvc.Results.BadRequest
import play.api.mvc.{ActionRefiner, Request, Result, WrappedRequest}
import uk.gov.hmrc.organisationsmatchingapi.schema.{ApiVersion, Version_1_0}
import uk.gov.hmrc.play.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

case class VersionedRequest[A](apiVersion: ApiVersion, request: Request[A])
  extends WrappedRequest[A](request)

@Singleton
class VersionTransformer @Inject()()(implicit ec: ExecutionContext)
  extends ActionRefiner[Request, VersionedRequest] {

  override val executionContext: ExecutionContext = ec
  override protected def refine[A](request: Request[A]): Future[Either[Result, VersionedRequest[A]]] = {

    val headers = HeaderCarrierConverter
      .fromHeadersAndSessionAndRequest(request.headers, request = Some(request))
      .headers

    val version: Option[ApiVersion] = Option(Version_1_0)

    Future.successful(
      version.fold[Either[Result, VersionedRequest[A]]](errorResponse)(
        requestWithVersion[A](request)))

  }

  def errorResponse = Left(BadRequest("Missing ApiVersion Header"))

  def requestWithVersion[A](request: Request[A])(version: ApiVersion) =
    Right(VersionedRequest(version, request))
}
