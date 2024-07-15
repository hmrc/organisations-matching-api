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

package it.uk.gov.hmrc.organisationsmatchingapi.repository

import org.mongodb.scala.model.Filters
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsString, Json, OFormat}
import play.api.{Application, Configuration, Mode}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.Codecs.toBson
import uk.gov.hmrc.organisationsmatchingapi.cache.{CacheConfiguration, ShortLivedCache}
import util.UnitSpec

import java.util.UUID
import scala.concurrent.ExecutionContext

class ShortLivedCacheSpec extends UnitSpec with Matchers with GuiceOneAppPerSuite with MockitoSugar with ScalaFutures with BeforeAndAfterEach {

  val cacheTtl = 60
  val id: String = UUID.randomUUID().toString
  val cachekey: String = "test-class-key"
  val testValue: TestClass = TestClass("one", "two")

  override def fakeApplication(): Application =
    GuiceApplicationBuilder()
      .configure("run.mode" -> "Stub")
      .configure(Map(
        "metrics.enabled" -> false,
        "auditing.enabled" -> false,
        "microservice.services.metrics.graphite.enabled" -> false
      ))
      .in(Mode.Test)
      .build()

  implicit val ec: ExecutionContext = fakeApplication().injector.instanceOf[ExecutionContext]
  val cacheConfig: CacheConfiguration = fakeApplication().injector.instanceOf[CacheConfiguration]
  val configuration: Configuration = fakeApplication().injector.instanceOf[Configuration]
  val mongoComponent: MongoComponent = fakeApplication().injector.instanceOf[MongoComponent]

  val shortLivedCache = new ShortLivedCache(cacheConfig, configuration, mongoComponent)

  override def beforeEach(): Unit = {
    super.beforeEach()
    await(shortLivedCache.collection.drop().toFuture())
  }

  override def afterEach(): Unit = {
    super.afterEach()
    await(shortLivedCache.collection.drop().toFuture())
  }

  "cache" should {
    "store the encrypted version of a value" in {
      await(shortLivedCache.cache(id, testValue)(TestClass.format))
      retrieveRawCachedValue(id) shouldBe JsString(
        "I9gl6p5GRucOfXOFmhtiYfePGl5Nnksdk/aJFXf0iVQ=")
    }
  }

  "fetch" should {
    "retrieve the unencrypted cached value for a given id and key" in {
      await(shortLivedCache.cache(id, testValue)(TestClass.format))
      await(
        shortLivedCache.fetchAndGetEntry(id)(
          TestClass.format)) shouldBe Some(testValue)
    }

    "return None if no cached value exists for a given id and key" in {
      await(
        shortLivedCache.fetchAndGetEntry(id)(
          TestClass.format)) shouldBe None
    }
  }

  private def retrieveRawCachedValue(id: String) = {
    await(shortLivedCache.collection.find(Filters.equal("id", toBson(id)))
      .headOption()
      .map {
        case Some(entry) => entry.data.value
        case None => None
      })
  }

  case class TestClass(one: String, two: String)

  object TestClass {
    implicit val format: OFormat[TestClass] = Json.format[TestClass]
  }
}
