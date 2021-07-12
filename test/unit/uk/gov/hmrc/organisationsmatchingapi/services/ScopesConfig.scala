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
    (s"api-config.scopes.$mockScopeOne.fields", Seq("A", "B", "C", "D")),
    (s"api-config.scopes.$mockScopeTwo.endpoints", Seq(endpointKeyTwo, endpointKeyThree)),
    (s"api-config.scopes.$mockScopeTwo.fields", Seq("E", "F", "G", "H", "I")),

    (s"api-config.endpoints.internal.sampleEndpointOne.key", endpointKeyOne),
    (s"api-config.endpoints.internal.sampleEndpointOne.endpoint", "/internal/1"),
    (s"api-config.endpoints.internal.sampleEndpointOne.title", "Get the first endpoint"),
    (s"api-config.endpoints.internal.sampleEndpointOne.fields", Seq("A", "B", "C")),

    (s"api-config.endpoints.internal.sampleEndpointTwo.key", endpointKeyTwo),
    (s"api-config.endpoints.internal.sampleEndpointTwo.endpoint", "/internal/2"),
    (s"api-config.endpoints.internal.sampleEndpointTwo.title", "Get the second endpoint"),
    (s"api-config.endpoints.internal.sampleEndpointTwo.fields", Seq("D", "E", "F")),

    (s"api-config.endpoints.internal.sampleEndpointThree.key", endpointKeyThree),
    (s"api-config.endpoints.internal.sampleEndpointThree.endpoint", "/internal/3"),
    (s"api-config.endpoints.internal.sampleEndpointThree.title", "Get the third endpoint"),
    (s"api-config.endpoints.internal.sampleEndpointThree.fields", Seq("G", "H", "I")),

    (s"api-config.endpoints.external.sampleEndpointOne.key", endpointKeyOne),
    (s"api-config.endpoints.external.sampleEndpointOne.endpoint", "/external/1"),
    (s"api-config.endpoints.external.sampleEndpointOne.title", "Get the first endpoint"),

    (s"api-config.endpoints.external.sampleEndpointTwo.key", endpointKeyTwo),
    (s"api-config.endpoints.external.sampleEndpointTwo.endpoint", "/external/2"),
    (s"api-config.endpoints.external.sampleEndpointTwo.title", "Get the second endpoint"),

    (s"api-config.endpoints.external.sampleEndpointThree.key", endpointKeyThree),
    (s"api-config.endpoints.external.sampleEndpointThree.endpoint", "/external/3"),
    (s"api-config.endpoints.external.sampleEndpointThree.title", "Get the third endpoint"),

    (s"api-config.fields.A", "path/to/a"),
    (s"api-config.fields.B", "path/to/b"),
    (s"api-config.fields.C", "path/to/c"),

    (s"api-config.fields.D", "path/to/d"),
    (s"api-config.fields.E", "path/to/e"),
    (s"api-config.fields.F", "path/to/f"),

    (s"api-config.fields.G", "path/to/g"),
    (s"api-config.fields.H", "path/to/h"),
    (s"api-config.fields.I", "path/to/i"),
  )
}
