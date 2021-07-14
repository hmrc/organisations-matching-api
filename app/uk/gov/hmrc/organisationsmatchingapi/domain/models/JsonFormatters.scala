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

package uk.gov.hmrc.organisationsmatchingapi.domain.models

import java.util.UUID
import play.api.libs.json.{Format, JsString, JsSuccess, JsValue, Json, Writes}

object JsonFormatters {

  implicit val errorResponseWrites = new Writes[ErrorResponse] {
    def writes(e: ErrorResponse): JsValue = Json.obj("code" -> e.errorCode, "message" -> e.message)
  }

  implicit val uuidJsonFormat = new Format[UUID] {
    override def writes(uuid: UUID) = JsString(uuid.toString)

    override def reads(json: JsValue) =
      JsSuccess(UUID.fromString(json.asInstanceOf[JsString].value))
  }

}
