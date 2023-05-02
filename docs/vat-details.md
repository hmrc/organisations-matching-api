# VAT Matching Request #

This endpoint takes a JSON body that contains a Company's CRN and Known Facts in order to match against HMRC's records. Upon a successful match, a matchId is returned which can be used in subsequent calls.

## Request ##

#### Endpoint ####

> POST /organisations/matching/vat/

**JSON Body Example**

~~~~~~~~~~
{
  "vrn": "123456789",
  "organisationName": "Example Company Ltd",
  "addressLine1": "123 Long Road",
  "postcode": "AB12 3CD"
}
~~~~~~~~~~

#### JSON Body Schema ####

~~~~~~~~~~
{
  "type": "object",
  "description": "Company's VRN and known facts",
  "required": [
    "vrn",
    "organisationName",
    "addressLine1",
    "postcode"
  ],
  "properties": {
    "vrn": {
      "type": "string",
      "description": "VAT Registration Number",
      "pattern": "^[0-9]{1,9}$"
    },
    "name": {
      "type": "string",
      "description": "Organisation name"
    },
    "addressLine1": {
      "type": "string",
      "description": "Company address line 1",
    },
    "postcode": {
      "type": "string",
      "description": "Company's postcode"
    }
  }
}
~~~~~~~~~~

## Responses ##

#### HTTP Status: 200 ####

**JSON Body Example**

~~~~~~~~~~
{
  "matchId": "57072660-1df9-4aeb-b4ea-cd2d7f96e430",
}
~~~~~~~~~~

**JSON Body Schema**

~~~~~~~~~~
{
  "type": "object",
  "description": "VAT Matching Response",
  "properties": {
    "matchId": {
      "type": "string",
      "example": "57072660-1df9-4aeb-b4ea-cd2d7f96e430"
    }
  }
}
~~~~~~~~~~

#### HTTP Status: 400 ####

**JSON Body Example**

~~~~~~~~~~
{
  "code": "BAD_REQUEST"
}
~~~~~~~~~~

**JSON Body Schema**

~~~~~~~~~~
{
  "$schema": "http://json-schema.org/draft-04/schema#",
  "$ref": "#/definitions/schema",
  "definitions": {
    "schema": {
      "type": "object",
      "x-amf-merge": [
        {
          "$ref": "#/definitions/errorResponse"
        }
      ],
      "x-amf-examples": {
        "BadRequest": {
          "description": "The request body does not conform to the schema",
          "value": {
            "code": "BAD_REQUEST"
          }
        }
      }
    }
  }
}
~~~~~~~~~~

#### HTTP Status: 403 ####

**JSON Body Example**

~~~~~~~~~~
{
  "code": "MATCHING_FAILED"
}
~~~~~~~~~~

**JSON Body Schema**

~~~~~~~~~~
{
  "$schema": "http://json-schema.org/draft-04/schema#",
  "$ref": "#/definitions/schema",
  "definitions": {
    "schema": {
      "type": "object",
      "x-amf-merge": [
        {
          "$ref": "#/definitions/errorResponse"
        }
      ],
      "x-amf-examples": {
        "Forbidden": {
          "description": "The organisation's details provided did not match with HMRC's records",
          "value": {
            "code": "MATCHING_FAILED"
          }
        }
      }
    }
  }
}
~~~~~~~~~~