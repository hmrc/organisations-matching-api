# Submit an Organisation Matching Request For a Partnership #

This endpoint takes a JSON body that contains a Partnership's UTR and Known Facts in order to match against HMRC's records. Upon a successful match, a matchId is returned which can be used in subsequent calls.

## Request ##

#### Endpoint ####

> POST /organisations/matching/partnership/

**JSON Body Example**

~~~~~~~~~~
{
  "utr": "1234567890",
  "name": "Example Partnership",
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
  "description": "Partnership's UTR and known facts",
  "required": [
    "utr",
    "name",
    "address",
    "postcode"
  ],
  "properties": {
    "utr": {
      "type": "string",
      "description": "Partnership's UTR",
      "pattern": "^[0-9]{10}$"
    },
    "name": {
      "type": "string",
      "description": "Partnership name"
    },
    "address": {
      "type": "object",
      "description": "Partnership address",
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
      "description": "Partnership's postcode"
    }
  }
}
~~~~~~~~~~

## Responses ##

#### HTTP Status: 200 ####

**JSON Body Example**

~~~~~~~~~~
{
  "_links": {
    "partnership": {
      "href": "/organisations/matching/partnership/?matchId=57072660-1df9-4aeb-b4ea-cd2d7f96e430",
      "type": "GET"
    },
    "self": {
      "href": "/organisations/matching/partnership/",
      "type": "POST"
    }
  }
}
~~~~~~~~~~

**JSON Body Schema**

~~~~~~~~~~
{
  "type": "object",
  "description": "Partnership Matching Response",
  "properties": {
    "_links": {
      "type": "object",
      "properties": {
        "partnership": {
          "type": "object",
          "description": "URI to get company links",
          "properties": {
            "href": {
              "type": "string",
              "example": "/organisations/matching/partnership/?matchId=57072660-1df9-4aeb-b4ea-cd2d7f96e430"
            },
            "type": {
              "type": "string",
              "example": "GET"
            }
          }
        },
        "self": {
          "type": "object",
          "description": "URI to this resource",
          "properties": {
            "href": {
              "type": "string",
              "example": "/organisations/matching/partnership/"
            },
            "type": {
              "type": "string",
              "example": "POST"
            }
          }
        }
      }
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