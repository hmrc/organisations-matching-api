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

package uk.gov.hmrc.organisationsmatchingapi.repository

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.libs.json.{Format, JsValue}
import play.modules.reactivemongo.ReactiveMongoComponent
import uk.gov.hmrc.cache.TimeToLive
import uk.gov.hmrc.cache.repository.CacheMongoRepository
import uk.gov.hmrc.crypto.json.{JsonDecryptor, JsonEncryptor}
import uk.gov.hmrc.crypto.{ApplicationCrypto, CompositeSymmetricCrypto, Protected}
import uk.gov.hmrc.organisationsmatchingapi.cache.CacheConfiguration
import uk.gov.hmrc.organisationsmatchingapi.models.CrnMatch

import scala.concurrent.{ExecutionContext, Future}

class ShortLivedCache[T] (cacheConfig: CacheConfiguration,
                                   configuration: Configuration,
                                   mongo: ReactiveMongoComponent,
                                   collName: String)(implicit ec: ExecutionContext)
  extends CacheMongoRepository(collName, cacheConfig.cacheTtl)(mongo.mongoConnector.db, ec)
  with TimeToLive {

  implicit lazy val crypto: CompositeSymmetricCrypto = new ApplicationCrypto(
    configuration.underlying).JsonCrypto

  def cache(id: String, key: String, value: T)(
    implicit formats: Format[T]): Future[Unit] = {
    val jsonEncryptor = new JsonEncryptor[T]()
    val encryptedValue: JsValue = jsonEncryptor.writes(Protected[T](value))
    createOrUpdate(id, key, encryptedValue).map(_ => ())
  }

  def fetchAndGetEntry(id: String, key: String)(
    implicit formats: Format[T]): Future[Option[T]] = {
    val decryptor = new JsonDecryptor[T]()

    findById(id) map {
      case Some(cache) =>
        cache.data flatMap { json =>
          (json \ key).toOption flatMap { jsValue =>
            decryptor.reads(jsValue).asOpt map (_.decryptedValue)
          }
        }
      case None => None
    }
  }
}
