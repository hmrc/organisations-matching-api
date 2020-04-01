# CRN Matching Request #

This endpoint takes a JSON body that contains a Company's CRN and Known Facts in order to match against HMRC's records. Upon a successful match, a matchId is returned which can be used in subsequent calls.

## Request ##

#### Endpoint ####

> POST /organisations/matching/crn/

**JSON Body Example**

~~~~~~~~~~
{
  "crn": "AA123456",
  "name": "Example Company Ltd",
  "address": {
    "addressLine1": "123 Long Road",
    "addressLine2": "Some City",
    "addressLine3": "Some County",
    "addressLine4": ""
  },
  "postcode": "AB12 3CD"
}
~~~~~~~~~~

#### JSON Body Schema ####

~~~~~~~~~~
{
  "type": "object",
  "description": "Company's CRN and known facts",
  "required": [
    "crn",
    "name",
    "address",
    "postcode"
  ],
  "properties": {
    "crn": {
      "type": "string",
      "description": "Company Registration Number",
      "pattern": "^([A-Za-z0-9]{0,2})?([0-9]{1,6})$"
    },
    "name": {
      "type": "string",
      "description": "Company name"
    },
    "address": {
      "type": "object",
      "description": "Company address",
      "required": [
        "addressLine1"
      ],
      "properties": {
        "addressLine1": {
          "type": "string"
        },
        "addressLine2": {
          "type": "string"
        },
        "addressLine3": {
          "type": "string"
        },
        "addressLine4": {
          "type": "string"
        }
      }
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
  "description": "Company Matching Response",
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