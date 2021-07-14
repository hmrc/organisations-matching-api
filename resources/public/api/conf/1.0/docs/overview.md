This API allows government departments to check HM Revenue and Customs (HMRC) records for an organisation.

On a successful match, the API will give you a matchId.

###Corporation Tax and Self Assessment
If you have a Company Registration number (CRN), you will need to use the Corporation Tax endpoint. This is described in the endpoint documentation.

If you have a Self Assessment Unique Taxpayer Reference (UTR), you will need to use the Self Assessment endpoint. This is described in the endpoint documentation.

They will return a matchId and HATEOAS links that you can use to get further details about the matched organisation.

###HAL HATEOS RESTful APIs
This API is a HAL HATEOAS RESTful API. It has been designed to promote discoverability and to be self documenting.

A HATEOAS API makes it clear to client software what further actions are available when an action is completed. Responses from an endpoint include URLs to further endpoints you can call. New functionality can be added without breaking your client software.

This API is still under development and further enhancements are planned. We recommend following the HATEOAS approach from the start, so that your work is not affected by future changes.

Follow URLs as they are presented to you in the API at runtime. This will prevent you from building state into your client, and will decouple you from changes to the API.

The default Media Type for responses is hal+json.
