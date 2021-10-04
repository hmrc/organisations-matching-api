# organisations-matching-api

This API allows users to match HMRC data for organisations.

### Documentation
The documentation on [confluence](https://confluence.tools.tax.service.gov.uk/display/MDS/Development+space) includes:
- Configuration driven management of data and scopes
- Scope driven query strings for Integration Framework (IF)
- Caching strategy to alleviate load on backend systems

Please ensure you reference the OGD Data Item matrix to ensure the right data items are mapped and keep this document up to date if further data items are added.
(The matrix was last validated at V1.1, please ensure you update with any changes you make.)

### Running the service

The service can be run with:

    sbt run

And will run on port 9657. 

### Running tests

Unit, integration and component tests can be run with the following:

    sbt test it:test component:test

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
