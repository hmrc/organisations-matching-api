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

///*
// * Copyright 2021 HM Revenue & Customs
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package uk.gov.hmrc.organisationsmatchingapi.matching
//
//import uk.gov.hmrc.organisationsmatchingapi.models.FailureReasons._
//import uk.gov.hmrc.organisationsmatchingapi.matching.Homoglyph
//
//trait MatchingHelper {
//
//  lazy val homoglyphs: List[Homoglyph] = List(Homoglyph("apostrophe", '\'', Set('\'','’')))
//
//  protected def matching[T](request: Option[T], cid: Option[T], fieldCode: Int, matchFunction: (T, T) => Boolean): Match = {
//    println("ACHI Matching")
//    (request, cid) match {
//      case (Some(request), Some(cid)) => {
//        println("ACHI Good/Bad check")
//        if (matchFunction(request, cid)) {
//          println("ACHI Good")
//          Good()
//        }
//        else {
//          println("ACHI Bad")
//          Bad(Set(fieldCode + MATCHING_CODE_RANGE))
//        }
//      }
//      case (None, Some(_)) => {
//        println("ACHI None, Some(_)")
//        Bad(Set(fieldCode + REQUEST_CODE_RANGE))
//      }
//      case (Some(_), None) => {
//        println("ACHI Some, None")
//        Bad(Set(fieldCode + CID_CODE_RANGE))
//      }
//      case (None, None) => {
//        println("ACHI None, None")
//        Bad(Set(fieldCode + REQUEST_CODE_RANGE, fieldCode + CID_CODE_RANGE))
//      }
//    }
//  }
//
//  protected def namesMatch(length: Int)(knownFactsName: Option[String], ifName: Option[String], code: Int): Match =
//    matching(knownFactsName, ifName, code,
//      compose[String, String](_ equalsIgnoreCase _, firstNLetters(length).andThen(homoglyphs.canonicalize)))
//
//  protected def addressLine1Match(length: Int)(knownFactsAddressLine: Option[String], ifAddressLine: Option[String]): Match =
//    matching(knownFactsAddressLine, ifAddressLine, ADDRESS_LINE1_FIELD_CODE,
//      compose[String, String](_ equalsIgnoreCase _, firstNLetters(length).andThen(homoglyphs.canonicalize)))
//
//  protected def compose[T, U](equate: (U, U) => Boolean, mutate: T => U): (T, T) => Boolean =
//    (lhs, rhs) => equate(mutate(lhs), mutate(rhs))
//
//  protected def firstNLetters(length: Int): String => String = name =>
//    name.trim.take(length)
//
//}
