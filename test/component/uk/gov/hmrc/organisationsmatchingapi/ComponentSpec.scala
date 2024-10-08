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

package component.uk.gov.hmrc.organisationsmatchingapi

import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import util.UnitSpec

trait ComponentSpec extends UnitSpec with GuiceOneServerPerSuite {

  implicit override lazy val app: Application = GuiceApplicationBuilder()
    .configure(
      "auditing.enabled"                                       -> false,
      "auditing.traceRequests"                                 -> false,
      "run.mode"                                               -> "It"
    )
    .build()

}
