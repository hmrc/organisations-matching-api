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

package uk.gov.hmrc.organisationsmatchingapi.models

import play.api.libs.json.Json

case class CrnMatchData(crn: Option[String], employerName: Option[String], address: Address)

object CrnMatchData {
  implicit val formats = Json.format[CrnMatchData]
}

case class MatchingResult(matchedIFData: Option[CrnMatchData], errorCodes: Set[Int])

object MatchingResult {
  implicit val writes = Json.writes[MatchingResult]
}
