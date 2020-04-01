# CRN Details #

This endpoint takes a matchId and returns corporation tax information for the company

## Request ##

#### Endpoint ####

> GET /organisations/details/crn/\{matchId\}

#### Query Parameters ####

| Name     | Description                                                                                           | Example | Required |
| -------- | ----------------------------------------------------------------------------------------------------- | ------- | -------- |
| fromYear | The earliest Tax Year from which you would like details                                               | 2017    | Yes      |
| toYear   | The latest Tax Year from which you would like details, if not provided then current year will be used | 2019    | No       |

#### URI Parameters ####

| Name    | Description                                     | Example                              |
| ------- | ----------------------------------------------- | ------------------------------------ |
| matchId | A short-lived UUID, typically valid for 5 hours | 57072660-1df9-4aeb-b4ea-cd2d7f96e430 |

## Responses ##

#### HTTP Status: 200 ####

**JSON Body Example**

~~~~~~~~~~
{
  "details": [
    {
      "year": 2017,
      "employees": 100,
      "turnover": 1000000.00,
      "registrationDate": "2001-01-01",
      "taxSolvencyStatus": "S"
    },
    {
      "year": 2018,
      "employees": 150,
      "turnover": 1500000.00,
      "registrationDate": "2001-01-01",
      "taxSolvencyStatus": "S"
    },
    {
      "year": 2019,
      "employees": 200,
      "turnover": 2000000.00,
      "registrationDate": "2001-01-01",
      "taxSolvencyStatus": "S"
    }
  ]
}
~~~~~~~~~~

**JSON Body Schema**

~~~~~~~~~~
{
  "tyoe": "object",
  "description": "Company Details Response",
  "properties": {
    "details": {
      "type": "array",
      "items": {
        "type": "object",
        "properties": {
          "year": {
            "type": "number",
            "example": 2019
          },
          "employees": {
            "type": "number",
            "example": 100
          },
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
            "enum": [
              "S",
              "I",
              "A",
              "V"
            ]
          }
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