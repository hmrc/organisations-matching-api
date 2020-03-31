/*
 * Copyright 2020 HM Revenue & Customs
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

import java.util.UUID

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.ReadPreference
import reactivemongo.api.commands.MultiBulkWriteResult
import reactivemongo.api.indexes.Index
import reactivemongo.api.indexes.IndexType.Ascending
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json._
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.organisationsmatchingapi.models.{CompanyMatch, JsonFormatters}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CompanyMatchRepository @Inject()(mongo: ReactiveMongoComponent,
                                       config: Configuration)
  extends ReactiveRepository[CompanyMatch, UUID] (
    "company-match",
    mongo.mongoConnector.db,
    CompanyMatch.formats,
    JsonFormatters.uuidJsonFormat) {

  private lazy val matchTtl: Int = config.get[Int]("mongodb.matchTtlInSeconds")

  override lazy val indexes = Seq(
    Index(Seq(("id", Ascending)), Some("idIndex"), background = true, unique = true),
    Index(
      Seq(("createdAt", Ascending)),
      Some("createdAtIndex"),
      options = BSONDocument("expireAfterSeconds" -> matchTtl),
      background = true)
  )

  def create(record: CompanyMatch): Future[CompanyMatch] = {
    insert(record) map { writeResult =>
      if (writeResult.n == 1) record
      else throw new RuntimeException(s"failed to persist company match $record")
    }
  }

  def read(uuid: UUID): Future[Option[CompanyMatch]] = findById(uuid)

  override def findById(id: UUID, readPreference: ReadPreference)(
    implicit ec: ExecutionContext): Future[Option[CompanyMatch]] =
    collection.find(Json.obj("id" -> id.toString), Some(Json.obj())).one[CompanyMatch]

  override def bulkInsert(entities: Seq[CompanyMatch])(implicit ec: ExecutionContext): Future[MultiBulkWriteResult] =
    throw new UnsupportedOperationException
}
