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

package uk.gov.hmrc.organisationsmatchingapi.matching

sealed abstract class Match {
  def codes: Set[Int]

  def or(other: Match): Match = (this, other) match {
    case (Bad(a), Bad(b)) => Bad(a ++ b)
    case _ => Good(codes ++ other.codes)
  }

  def and(other: Match): Match = (this, other) match {
    case (Good(a), Good(b)) => Good(a ++ b)
    case _ => Bad(codes ++ other.codes)
  }
}

case class Good(codes: Set[Int]) extends Match

case object Good extends (Set[Int] => Match) {
  def apply(codes: Int*): Good = Good(codes.toSet)
}

case class Bad(codes: Set[Int]) extends Match

case object Bad extends (Set[Int] => Match) {
  def apply(codes: Int*): Bad = Bad(codes.toSet)
}
