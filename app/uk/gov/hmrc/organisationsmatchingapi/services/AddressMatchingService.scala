package uk.gov.hmrc.organisationsmatchingapi.services

import uk.gov.hmrc.organisationsmatchingapi.models.Address

class AddressMatchingService {

  // Option 1 will try a series of data cleanses with a match attempt in between.
  // This means we could potentially trigger a match prior to manipulating the data too much.
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

  // Option two fully cleanses (manipulates) the data prior to the match.
  // This means we may get a quicker match however; we have heavily manipulated the data. Possibly unnecessarily.
  def matchAddressCleanAll(knownAddress: Address, ifAddress: Address) = {
    knownAddress.cleanAll match {
      case ifAddress.cleanAll => true
      case _                  => false
    }
  }
}
