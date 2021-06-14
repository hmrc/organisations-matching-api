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

import play.api.Configuration

trait ScopesConfig {

  val mockScopeOne: String = "scopeOne"
  val mockScopeTwo: String = "scopeTwo"

  val endpointKeyOne : String = "A"
  val endpointKeyTwo : String = "B"
  val endpointKeyThree : String = "C"

  val mockConfig : Configuration = Configuration(
    (s"api-config.scopes.$mockScopeOne.endpoints", Seq(endpointKeyOne, endpointKeyTwo)),
    (s"api-config.scopes.$mockScopeTwo.endpoints", Seq(endpointKeyTwo, endpointKeyThree)),

    (s"api-config.endpoints.sampleEndpointOne.key", endpointKeyOne),
    (s"api-config.endpoints.sampleEndpointOne.endpoint", "/test/1"),
    (s"api-config.endpoints.sampleEndpointOne.title", "Get the first endpoint"),

    (s"api-config.endpoints.sampleEndpointTwo.key", endpointKeyTwo),
    (s"api-config.endpoints.sampleEndpointTwo.endpoint", "/test/2"),
    (s"api-config.endpoints.sampleEndpointTwo.title", "Get the second endpoint"),

    (s"api-config.endpoints.sampleEndpointThree.key", endpointKeyThree),
    (s"api-config.endpoints.sampleEndpointThree.endpoint", "/test/3"),
    (s"api-config.endpoints.sampleEndpointThree.title", "Get the third endpoint"),
  )
}
