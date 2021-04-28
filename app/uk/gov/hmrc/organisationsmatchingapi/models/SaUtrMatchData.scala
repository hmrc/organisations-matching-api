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

case class SaUtrMatchData(utr: String, taxPayerName: String, taxPayerType: String, address: Address) extends MatchDataBase {
  override def asString = {
    List[Option[String]](
      Some(utr),
      Some(taxPayerName),
      Some(taxPayerType),
      Some(address.asString),
    ).flatten.mkString(" ")
  }
}

object SaUtrMatchData {
  implicit val formats = Json.format[SaUtrMatchData]
}
