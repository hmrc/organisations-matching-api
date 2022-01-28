/*
 * Copyright 2022 HM Revenue & Customs
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

package unit.uk.gov.hmrc.organisationsmatchingapi.util

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.organisationsmatchingapi.utils.MatchUuidPathBinder

import java.util.UUID

class MatchUuidPathBinderSpec
  extends AnyWordSpec
    with Matchers {

  private val queryStringParameterName = "matchId"

  private val matchUuidQueryStringBinder = new MatchUuidPathBinder

  "Match UUID query string binder" should {
    "fail to bind missing or malformed uuid parameter" in {
      val fixtures = Seq(
        ("", s"$queryStringParameterName is required"),
        ("Not-A-Uuid", s"$queryStringParameterName format is invalid")
      )

      fixtures foreach {
        case (parameters, response) =>
          matchUuidQueryStringBinder.bind("", parameters) shouldBe Left(response)
      }
    }
  }

  "bind well formed uuid strings" in {
    val fixtures = Seq(
      ("a7b7945e-3ba8-4334-a9cd-2348f98d6867", UUID.fromString("a7b7945e-3ba8-4334-a9cd-2348f98d6867")),
      ("b5a9afcb-c7ec-4343-b275-9a3ca0a8f362", UUID.fromString("b5a9afcb-c7ec-4343-b275-9a3ca0a8f362")),
      ("c44ca64d-2451-4449-9a9a-70e099efe279", UUID.fromString("c44ca64d-2451-4449-9a9a-70e099efe279"))
    )

    fixtures foreach {
      case (parameters, response) =>
        matchUuidQueryStringBinder.bind("", parameters) shouldBe Right(response)
    }
  }

  "unbind uuid strings to query parameters" in {
    val fixtures = Seq(
      (UUID.fromString("a7b7945e-3ba8-4334-a9cd-2348f98d6867"), s"$queryStringParameterName=a7b7945e-3ba8-4334-a9cd-2348f98d6867"),
      (UUID.fromString("b5a9afcb-c7ec-4343-b275-9a3ca0a8f362"), s"$queryStringParameterName=b5a9afcb-c7ec-4343-b275-9a3ca0a8f362"),
      (UUID.fromString("c44ca64d-2451-4449-9a9a-70e099efe279"), s"$queryStringParameterName=c44ca64d-2451-4449-9a9a-70e099efe279")
    )

    fixtures foreach {
      case (parameters, response) =>
        matchUuidQueryStringBinder.unbind(queryStringParameterName, parameters) shouldBe response
    }
  }
}

