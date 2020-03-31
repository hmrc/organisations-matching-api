# Retrieve matched Company's details and links to other resources #

This endpoint takes a matchId and returns the details held against the match, as well as links to other resources

## Request ##

#### Endpoint ####

> GET /organisations/matching/company/\{matchId\}

#### URI Parameters ####

| Name    | Description                                           | Example                              |
| ------- | ----------------------------------------------------- | ------------------------------------ |
| matchId | A short-lived UUID, typically valid for several hours | 57072660-1df9-4aeb-b4ea-cd2d7f96e430 |

## Responses ##

#### HTTP Status: 200 ####

**JSON Body Example**

~~~~~~~~~~
{
  "_links": {
    "details": {
      "href": "/organisations/details/company/?matchId=57072660-1df9-4aeb-b4ea-cd2d7f96e430",
      "type": "GET"
    },
    "self": {
      "href": "/organisations/matching/company/?matchId=57072660-1df9-4aeb-b4ea-cd2d7f96e430",
      "type": "GET"
    }
  },
  "details": {
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
}
~~~~~~~~~~

**JSON Body Schema**

~~~~~~~~~~
{
  "type": "object",
  "description": "Matched Company Details and links to other resources",
  "properties": {
    "_links": {
      "type": "object",
      "properties": {
        "details": {
          "type": "object",
          "properties": {
            "href": {
              "type": "string",
              "example": "/organisations/details/company/?matchId=57072660-1df9-4aeb-b4ea-cd2d7f96e430"
            },
            "type": {
              "type": "string",
              "example": "GET"
            }
          }
        },
        "self": {
          "type": "object",
          "properties": {
            "href": {
              "type": "string",
              "example": "/organisations/matching/company/?matchId=57072660-1df9-4aeb-b4ea-cd2d7f96e430"
            },
            "type": {
              "type": "string",
              "example": "GET"
            }
          }
        }
      }
    },
    "details": {
      "type": "object",
      "properties": {
        "crn": {
          "type": "string",
          "example": "AA123456"
        },
        "name": {
          "type": "string",
          "example": "Example Company Ltd"
        },
        "address": {
          "type": "object",
          "properties": {
            "addressLine1": {
              "type": "string",
              "example": "123 Long Road"
            },
            "addressLine2": {
              "type": "string",
              "example": "Some City"
            },
            "addressLine3": {
              "type": "string",
              "example": "Some County"
            },
            "addressLine4": {
              "type": "string",
              "example": ""
            }
          }
        },
        "postcode": {
          "type": "string",
          "example": "AB12 3CD"
        }
      }
    }
  }
}
~~~~~~~~~~

#### HTTP Status: 404 ####

**JSON Body Example**

~~~~~~~~~~
{
  "code": "NOT_FOUND"
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
        "NotFound": {
          "description": "Not Found",
          "value": {
            "code": "NOT_FOUND"
          }
        }
      }
    }
  }
}
~~~~~~~~~~