# Retrieve matched Partnership's details #

This endpoint takes a matchId and returns self assessment information for the partnership

## Request ##

#### Endpoint ####

> GET /organisations/details/partnership/\{matchId\}

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
    "self": {
      "href": "/organisations/details/partnership/?matchId=57072660-1df9-4aeb-b4ea-cd2d7f96e430",
      "type": "GET"
    }
  },
  "details": {
    "turnover": 1000000.00,
    "registrationDate": "2001-01-01",
    "taxSolvencyStatus": "S"
  }
}
~~~~~~~~~~

**JSON Body Schema**

~~~~~~~~~~
{
  "tyoe": "object",
  "description": "Partnership Details Response",
  "properties": {
    "_links": {
      "type": "object",
      "prorperties": {
        "self": {
          "type": "object",
          "description": "URI to this resource",
          "properties": {
            "href": {
              "type": "string",
              "example": "/organisations/details/partnership/?matchId=57072660-1df9-4aeb-b4ea-cd2d7f96e430"
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
        "turnover": {
          "type": "number",
          "example": 1000000.00
        },
        "registrationDate": {
          "type": "string",
          "example": "2001-01-01"
        },
        "taxSolvencyStatus": {
          "type": "string",
          "description": "S = Solvent, I = Insolvent, A = Administration Order, V = Voluntary Arrangement",
          "example": "S"
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