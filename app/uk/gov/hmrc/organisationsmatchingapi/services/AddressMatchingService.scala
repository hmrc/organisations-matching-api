package uk.gov.hmrc.organisationsmatchingapi.services

class AddressMatchingService {

  /* Address Matching - Simplest form (Non fuzzy)

  Validation:

  1). The postcode should be present and meet the regex of a postcode as given in the IF schema for matching.
  2). Address lines 1 and2 should be populated

  If the above cannot be met then 400 error raised.

  Matching:

    Option 1:

  1). Concatenate the addresses (both input and IF) into single string fields for comparison. This will remove any missing fields for the match.
  “24” “My Street”
  “NEX XPY”

  Becomes “24 My Street NEX XPY”

  2) Comparing the two addresses directly ignoring case
  ”24 My Street” should be the same as “24 my street”

  3) Ignore case removing punctuation  and execute step 2.
  “24 my street.” should be the same as “24 my street”
  “24 my street!” should be the same as “24 my street”


  4) Ignore case Removing punctuation and whitespace  and execute step 2.
  “24 my street.” should be the same as “24mystreet”
  “24 my street!” should be the same as “24mystreet”

  5) Ignore case Removing punctuation and whitespace replacing  “PO, P O, P.O., P. O., POST OFFICE” with “PO” then execute step 2.
  “24 my street P.O. Box 123” should be the same as  “24mystreetPOBox123”
  “24 my street P O  Box 123” should be the same as  “24mystreetPOBox123”
  “24 my street POST OFFICE  Box 123” should be the same as  “24mystreetPOBox123”

  Option 2:


  Clean all of the above prior to the first match attempt.

  If any of the above criteria cannot be met then NON MATCH; else MATCH. */

}
