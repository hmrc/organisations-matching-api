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

import java.util.UUID
import javax.inject.{Inject, Singleton}
import play.api.Configuration
import uk.gov.hmrc.http.NotImplementedException
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.organisationsmatchingapi.models.SaUtrMatch

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SaUtrMatchRepository @Inject()(mongo: MongoComponent, config: Configuration) (implicit ec:ExecutionContext)
  extends PlayMongoRepository[SaUtrMatch] (
    collectionName = "sautr-match",
    mongoComponent =  mongo,
    domainFormat = SaUtrMatch.formats,
    indexes = Seq.empty ) {

  private lazy val matchTtl: Int = config.get[Int]("mongodb.matchTtlInSeconds")

  def create(record: SaUtrMatch): Future[SaUtrMatch] = {
   throw new NotImplementedException("Im not implemented")
  }

  def read(uuid: UUID): Future[Option[SaUtrMatch]] = findById(uuid)

  def findById(id: UUID)(implicit ec: ExecutionContext): Future[Option[SaUtrMatch]] = Future.successful(None)
}
