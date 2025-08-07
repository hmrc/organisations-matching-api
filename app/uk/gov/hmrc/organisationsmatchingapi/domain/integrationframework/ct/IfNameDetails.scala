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

package uk.gov.hmrc.organisationsmatchingapi.domain.integrationframework.ct

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads.maxLength
import play.api.libs.json.{Format, JsPath, Json}

case class IfNameDetails(name1: Option[String], name2: Option[String])

object IfNameDetails {

  private val reads = (
    (JsPath \ "name1").readNullable[String](using maxLength(100)) and
      (JsPath \ "name2").readNullable[String](using maxLength(100))
  )(IfNameDetails.apply)

  private val writes = Json.writes[IfNameDetails]

  implicit val format: Format[IfNameDetails] = Format(reads, writes)

}
