/*
 * Copyright 2026 HM Revenue & Customs
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

package uk.gov.hmrc.organisationsmatchingapi.connectors

import play.api.mvc.RequestHeader
import uk.gov.hmrc.http.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.organisationsmatchingapi.audit.AuditHelper
import uk.gov.hmrc.organisationsmatchingapi.domain.hip.HipSuccessWrapper
import uk.gov.hmrc.organisationsmatchingapi.domain.integrationframework.vat.IfVatCustomerInformation
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Base64
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class HipConnector @Inject() (
  servicesConfig: ServicesConfig,
  http: HttpClientV2,
  val auditHelper: AuditHelper
)(implicit val ec: ExecutionContext)
    extends MigrationConnector {

  private val apiName = "Hip"
  private val baseUrl = servicesConfig.baseUrl("hip")
  private val hipClientIdV1: String = servicesConfig.getString("microservice.services.hip.clientId")
  private val hipSecretV1: String = servicesConfig.getString("microservice.services.hip.secret")

  def hipAuthorizationToken: String =
    Base64.getEncoder.encodeToString(s"$hipClientIdV1:$hipSecretV1".getBytes("UTF-8"))

  implicit val hipReads: HttpReads[HipSuccessWrapper] = new HttpReads[HipSuccessWrapper] {
    override def read(method: String, url: String, response: HttpResponse): HipSuccessWrapper = {
      val isHipNotFoundError = response.status == 422 && Try {
        (response.json \ "error" \ "code").asOpt[String].contains("002")
      }.getOrElse(false)

      if (isHipNotFoundError) {
        logger.warn(s"$apiName NotFoundException encountered through 422 002 , which is translated to a 404")
        throw UpstreamErrorResponse(response.body, 404, 404)
      } else {
        HttpReads.Implicits.readFromJson[HipSuccessWrapper].read(method, url, response)
      }
    }
  }

  override def fetchVat(matchId: String, vrn: String)(implicit
    hc: HeaderCarrier,
    request: RequestHeader
  ): Future[IfVatCustomerInformation] = {
    val vatUrl = s"$baseUrl/etmp/RestAdapter/vat/subscription/vrn/$vrn"

    val requestExecution = http
      .get(url"$vatUrl")
      .setHeader(setHeaders(request)*)
      .execute[HipSuccessWrapper](using hipReads, ec)
      .map(res => auditResponse[IfVatCustomerInformation](vatUrl, matchId)(res.success))

    recover[IfVatCustomerInformation](
      requestExecution,
      extractCorrelationId(request),
      matchId,
      request,
      vatUrl,
      apiName
    )
  }

  private def setHeaders(requestHeader: RequestHeader): Seq[(String, String)] =
    Seq(
      "correlationid"         -> extractCorrelationId(requestHeader),
      "X-Originating-System"  -> "MDTP",
      "X-Receipt-Date"        -> DateTimeFormatter.ISO_INSTANT.format(Instant.now().truncatedTo(ChronoUnit.SECONDS)),
      "X-Transmitting-System" -> "HIP",
      "Authorization"         -> s"Basic $hipAuthorizationToken"
    )
}
