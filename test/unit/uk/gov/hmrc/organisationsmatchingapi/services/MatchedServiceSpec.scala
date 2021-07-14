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
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.organisationsmatchingapi.audit.AuditHelper
import uk.gov.hmrc.organisationsmatchingapi.controllers.MatchedController
import uk.gov.hmrc.organisationsmatchingapi.services.{CacheService, MatchedService, ScopesHelper, ScopesService}

import scala.concurrent.ExecutionContext.Implicits.global

class MatchedServiceSpec extends AnyWordSpec with Matchers with MockitoSugar {
  private val cacheService   = mock[CacheService]
  private val matchedService = new MatchedService(cacheService)


  "fetchCt" should {
    "return cache entry" in {
    }

    "return not found" in {
    }
  }

  "fetchSa" should {
    "return cache entry" in {
    }

    "return not found" in {
    }
  }
}
