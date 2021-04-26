package uk.gov.hmrc.organisationsmatchingapi.services

import uk.gov.hmrc.organisationsmatchingapi.models.Address

class AddressMatchingService {

  def matchAddress(knownAddress: Address, ifAddress: Address) = {
    knownAddress.ignoreCase match {
      case ifAddress.ignoreCase => true
      case _ => {
        knownAddress.withoutPunctuation match {
          case ifAddress.withoutPunctuation => true
          case _ => {
            knownAddress.withoutWitespace match {
              case ifAddress.withoutWitespace => true
              case _ => {
                knownAddress.cleanPostOfficeBox match {
                  case ifAddress.cleanPostOfficeBox => true
                  case _ => false
                }
              }
            }
          }
        }
      }
    }
  }

  def matchAddressCleanAll(knownAddress: Address, ifAddress: Address) = {
    knownAddress.cleanAll match {
      case ifAddress.cleanAll => true
      case _                  => false
    }
  }
}
