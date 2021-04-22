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

package it.uk.gov.hmrc.organisationsmatchingapi.repository

import java.util.UUID

import org.scalatest.{BeforeAndAfterEach, TestData}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.{Application, Configuration, Mode}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsString, Json, OFormat}
import play.modules.reactivemongo.ReactiveMongoComponent
import uk.gov.hmrc.organisationsmatchingapi.cache.CacheConfiguration
import uk.gov.hmrc.organisationsmatchingapi.repository.ShortLivedCache
import util.UnitSpec

import scala.concurrent.ExecutionContext

class ShortLivedCacheSpec extends UnitSpec with Matchers with GuiceOneAppPerSuite with MockitoSugar with ScalaFutures with BeforeAndAfterEach {

  val cacheTtl = 60
  val id = UUID.randomUUID().toString
  val cachekey = "test-class-key"
  val testValue = TestClass("one", "two")

  override def fakeApplication: Application =
    GuiceApplicationBuilder()
      .configure("run.mode" -> "Stub")
      .configure(Map(
        "metrics.enabled" -> false,
        "auditing.enabled" -> false,
        "microservice.services.metrics.graphite.enabled" -> false
      ))
      .in(Mode.Test)
      .build()

  implicit val ec: ExecutionContext = fakeApplication.injector.instanceOf[ExecutionContext]
  val cacheConfig = fakeApplication.injector.instanceOf[CacheConfiguration]
  val configuration = fakeApplication.injector.instanceOf[Configuration]
  val mongo = fakeApplication.injector.instanceOf[ReactiveMongoComponent]

  val shortLivedCache = new ShortLivedCache[TestClass](cacheConfig, configuration, mongo, "test")

  override def beforeEach() {
    super.beforeEach()
    await(shortLivedCache.drop)
  }

  override def afterEach() {
    super.afterEach()
    await(shortLivedCache.drop)
  }

  "cache" should {
    "store the encrypted version of a value" in {
      await(shortLivedCache.cache(id, cachekey, testValue)(TestClass.format))
      retrieveRawCachedValue(id, cachekey) shouldBe JsString(
        "I9gl6p5GRucOfXOFmhtiYfePGl5Nnksdk/aJFXf0iVQ=")
    }

    "update a cached value for a given id and key" in {
      val newValue = TestClass("three", "four")

      await(shortLivedCache.cache(id, cachekey, testValue)(TestClass.format))
      retrieveRawCachedValue(id, cachekey) shouldBe JsString(
        "I9gl6p5GRucOfXOFmhtiYfePGl5Nnksdk/aJFXf0iVQ=")

      await(shortLivedCache.cache(id, cachekey, newValue)(TestClass.format))
      retrieveRawCachedValue(id, cachekey) shouldBe JsString(
        "6yAvgtwLMcdiqTvdRvLTVKSkY3JwUZ/TzklThFfSqvA=")
    }
  }

  "fetch" should {
    "retrieve the unencrypted cached value for a given id and key" in {
      await(shortLivedCache.cache(id, cachekey, testValue)(TestClass.format))
      await(
        shortLivedCache.fetchAndGetEntry(id, cachekey)(
          TestClass.format)) shouldBe Some(testValue)
    }

    "return None if no cached value exists for a given id and key" in {
      await(
        shortLivedCache.fetchAndGetEntry(id, cachekey)(
          TestClass.format)) shouldBe None
    }
  }

  private def retrieveRawCachedValue(id: String, key: String) = {
    val storedValue = await(shortLivedCache.findById(id)).get
    (storedValue.data.get \ cachekey).get
  }

  case class TestClass(one: String, two: String)

  object TestClass {
    implicit val format: OFormat[TestClass] = Json.format[TestClass]
  }
}
