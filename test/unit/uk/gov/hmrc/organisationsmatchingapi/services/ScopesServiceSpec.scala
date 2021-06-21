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

package unit.uk.gov.hmrc.organisationsmatchingapi.services

import org.scalatest.matchers.should.Matchers
import uk.gov.hmrc.organisationsmatchingapi.services.ScopesService
import util.UnitSpec

class ScopesServiceSpec extends UnitSpec with Matchers with ScopesConfig {

  val scopesService = new ScopesService(mockConfig)

  "Gets correct endpoints from configuration for first scope" in {
    val endpoints = scopesService.getEndpoints(Seq(mockScopeOne))

    endpoints.size shouldBe 2
    endpoints.map(_.key) shouldBe Seq(endpointKeyOne, endpointKeyTwo)
    endpoints.map(_.link) shouldBe Seq("/test/1", "/test/2")
    endpoints.map(_.title) shouldBe Seq("Get the first endpoint", "Get the second endpoint")

  }

  "Gets correct endpoints from configuration for second scope" in {
    val endpoints = scopesService.getEndpoints(Seq(mockScopeTwo))

    endpoints.size shouldBe 2
    endpoints.map(_.key) shouldBe Seq(endpointKeyTwo, endpointKeyThree)
    endpoints.map(_.link) shouldBe Seq("/test/2", "/test/3")
    endpoints.map(_.title) shouldBe Seq("Get the second endpoint", "Get the third endpoint")

  }

  "Gets all scopes correctly" in {
    val scopes = scopesService.getAllScopes
    scopes.toSeq shouldBe Seq(mockScopeOne, mockScopeTwo)
  }
}
