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

package uk.gov.hmrc.organisationsmatchingapi.cache

import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.{Filters, IndexModel, IndexOptions}
import play.api.Configuration
import play.api.libs.json.{Format, JsValue}
import uk.gov.hmrc.crypto.json.{JsonDecryptor, JsonEncryptor}
import uk.gov.hmrc.crypto.{ApplicationCrypto, CompositeSymmetricCrypto, Protected}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.Codecs.toBson
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.organisationsmatchingapi.cache.InsertResult.{AlreadyExists, InsertSucceeded}
import uk.gov.hmrc.organisationsmatchingapi.cache.MongoErrors.Duplicate

import java.time.{LocalDateTime, ZoneOffset}
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ShortLivedCache @Inject() (val cacheConfig: CacheConfiguration,
                      configuration: Configuration,
                      mongo: MongoComponent)(implicit ec: ExecutionContext
                     ) extends PlayMongoRepository[Entry](
  mongoComponent = mongo,
  collectionName = cacheConfig.colName,
  domainFormat   = Entry.format,
  replaceIndexes = true,
  indexes        = Seq(
    IndexModel(
      ascending("id"),
      IndexOptions().name("_id").
        unique(true).
        background(false).
        sparse(true)),
    IndexModel(
      ascending("modifiedDetails.lastUpdated"),
      IndexOptions().name("lastUpdatedIndex").
        background(false).
        expireAfter(cacheConfig.cacheTtl, TimeUnit.SECONDS)))
) {

  implicit lazy val crypto: CompositeSymmetricCrypto = new ApplicationCrypto(
    configuration.underlying).JsonCrypto

  def cache[T](id: String, value: T)(implicit formats: Format[T]): Future[InsertResult] = {

    val jsonEncryptor           = new JsonEncryptor[T]()
    val encryptedValue: JsValue = jsonEncryptor.writes(Protected[T](value))

    val entry = new Entry(
      id,
      new Data(encryptedValue),
      new ModifiedDetails(
        LocalDateTime.now(ZoneOffset.UTC),
        LocalDateTime.now(ZoneOffset.UTC)
      )
    )

    collection
      .insertOne(entry)
      .toFuture
      .map(_ => InsertSucceeded)
      .recover {
        case Duplicate(_) => AlreadyExists
      }
  }

  def fetchAndGetEntry[T](id: String)(
    implicit formats: Format[T]): Future[Option[T]] = {
    val decryptor = new JsonDecryptor[T]()

    collection
      .find(Filters.equal("id", toBson(id)))
      .headOption
      .map {
        case Some(entry) => decryptor.reads(entry.data.value).asOpt map (_.decryptedValue)
        case None => None
      }
  }
}