/*
 * Copyright 2022 HM Revenue & Customs
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

package util

import uk.gov.hmrc.organisationsmatchingapi.domain.integrationframework.{IfAddress, IfCorpTaxCompanyDetails, IfNameAndAddressDetails, IfNameDetails, IfSaTaxPayerNameAddress, IfSaTaxpayerDetails}

trait IfHelpers {

  val ifCorpTaxCompanyDetails: IfCorpTaxCompanyDetails = IfCorpTaxCompanyDetails(
    utr = Some("1234567890"),
    crn = Some("12345678"),
    registeredDetails = Some(IfNameAndAddressDetails(
      name = Some(IfNameDetails(
        name1 = Some("Waitrose"),
        name2 = Some("And Partners")
      )),
      address = Some(IfAddress(
        line1 = Some("Alfie House"),
        line2 = Some("Main Street"),
        line3 = Some("Manchester"),
        line4 = Some("Londonberry"),
        postcode = Some("LN1 1AG")
      ))
    )),
    communicationDetails = Some(IfNameAndAddressDetails(
      name = Some(IfNameDetails(
        name1 = Some("Waitrose"),
        name2 = Some("And Partners")
      )),
      address = Some(IfAddress(
        line1 = Some("Orange House"),
        line2 = Some("Corporation Street"),
        line3 = Some("London"),
        line4 = Some("Londonberry"),
        postcode = Some("LN1 1AG")
      ))
    ))
  )

  val saTaxpayerDetails: IfSaTaxpayerDetails = IfSaTaxpayerDetails(
    utr = Some( "1234567890"),
    taxpayerType = Some( "Individual"),
    taxpayerDetails = Some( Seq (
      IfSaTaxPayerNameAddress (
        name = Some( "John Smith II" ),
        addressType = Some( "Base" ),
        address = Some( IfAddress (
          line1 = Some( "Alfie House" ),
          line2 = Some( "Main Street" ),
          line3 = Some( "Birmingham" ),
          line4 = Some( "West midlands" ),
          postcode = Some( "B14 6JH" )
        ))),
      IfSaTaxPayerNameAddress (
        name = Some( "Joanne Smith" ),
        addressType = Some( "Correspondence"),
        address = Some( IfAddress (
          line1 = Some( "Alice House" ),
          line2 = Some( "Main Street" ),
          line3 = Some( "Manchester" ),
          line4 = None,
          postcode = Some( "MC1 4AA" )
        ))),
      IfSaTaxPayerNameAddress (
        name = Some( "Daffy Duck" ),
        addressType = Some( "Correspondence"),
        address = Some( IfAddress (
          line1 = Some( "1 Main Street" ),
          line2 = Some( "Disneyland" ),
          line3 = Some( "Liverpool" ),
          line4 = None,
          postcode = Some( "MC1 4AA" )
        )))
    ))
  )
}
