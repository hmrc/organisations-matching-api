package uk.gov.hmrc.organisationsmatchingapi.models

import play.api.libs.json.Json

case class OrganisationMatchingRequest(crn: String, postcode: String)

object OrganisationMatchingRequest {
  implicit val formats = Json.format[OrganisationMatchingRequest]
}
