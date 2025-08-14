# Organisations Matching API

This API allows users to match HMRC data for organisations.

### Documentation
The documentation on [confluence](https://confluence.tools.tax.service.gov.uk/display/MDS/Development+space) includes:
- Configuration driven management of data and scopes
- Scope driven query strings for Integration Framework (IF)
- Caching strategy to alleviate load on backend systems

Please ensure you reference the OGD Data Item matrix to ensure the right data items are mapped and keep this document up to date if further data items are added.
(The matrix was last validated at V1.1, please ensure you update with any changes you make.)

### Running the service

Ensure mongodb and service manager (for auth wizard and organisations-matching microservice) are running.

```sm2 --start OVHO```

```sm2 --stop ORGANISATIONS_MATCHING_API```

The service runs on port 9657 with:

```sbt run```

Headers, endpoints, and example request bodies can be found in the documentation on [DevHub](https://developer.qa.tax.service.gov.uk/api-documentation/docs/api/service/organisations-matching-api/1.0).

### Running tests

Run all the tests with coverage report:

    sbt clean compile coverage test it:test component:test coverageReport

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
