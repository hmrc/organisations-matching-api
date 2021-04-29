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

class MatchDataBase {

  def asString = {
    "override me"
  }

  def ignoreCaseAndSpaces = {
    asString.toLowerCase.filterNot((x: Char) => x.isWhitespace)
  }

  def withoutPunctuation = {
    asString.replaceAll("""[\p{Punct}]""", "")
  }

  def cleanPostOfficeBox = {
    asString.replaceAll("p.o.|p.0|p0|p.0|post office|P.O.|P.0|P0|P.0|POST OFFICE|Post Office", "PO")
  }

  def cleanAll = {
    asString
      .replaceAll("""[\p{Punct}]""", "")
      .replaceAll("p.o.|p.0|p0|p.0|post office|P.O.|P.0|P0|P.0|POST OFFICE|Post Office", "PO")
      .toLowerCase.filterNot((x: Char) => x.isWhitespace)
  }

}
