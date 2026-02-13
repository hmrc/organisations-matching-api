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

Ensure mongodb and service manager (for auth wizard, internal-auth and organisations-matching microservice) are running.

```sm2 --start OVHO```

```sm2 --stop ORGANISATIONS_MATCHING_API```

The service runs on port 9657 with:

```sbt run```

Headers, endpoints, and example request bodies can be found in the documentation on [DevHub](https://developer.qa.tax.service.gov.uk/api-documentation/docs/api/service/organisations-matching-api/1.0).

### Internal auth local testing

Internal auth is wired via `conf/application.conf`:
- `play.modules.enabled += "uk.gov.hmrc.internalauth.client.modules.InternalAuthModule"`
- `microservice.services.internal-auth` (defaults to `http://localhost:8470`)

For internal-auth first behaviour, call endpoints with an `Authorization` header. If internal-auth denies/errs, service 
falls back to existing scope-based privileged auth checks. Following steps outline local verification:

1. Start internal-auth via service manager (as part of `OVHO`):

```bash
sm2 --start OVHO
```

2. Create a token that allows access to API:

```bash
curl -i -X POST 'http://localhost:8470/test-only/token' \
  -H 'Content-Type: application/json' \
  -d '{
    "token":"ia-allow-token",
    "principal":"organisations-matching-api-local",
    "permissions":[{"resourceType":"organisations-matching-api","resourceLocation":"*","actions":["READ"]}]
  }'
```

3. Verify the token and permissions using commands from `internal-auth` [README](https://github.com/hmrc/internal-auth?tab=readme-ov-file#test-only-api), replacing the token with `ia-allow-token`.

4. Call a missing record using the token, expected response is `404` (auth succeeds, record is missing):

```bash
MISSING_ID="90D91C50-5B47-4FD5-858C-AC7DC22EDB3D" (should be a valid guid otherwise it fails validation before auth)
CORRELATION_ID="188e9400-b636-4a3b-80ba-230a8c72b92a"

curl -i "http://localhost:9657/match-record/vat/$MISSING_ID" \
  -H 'Authorization: Bearer ia-allow-token' \
  -H 'Accept: application/vnd.hmrc.1.0+json' \
  -H "CorrelationId: $CORRELATION_ID"
```

5. Call the same endpoint with an invalid token, expected response is `401`:

```bash
curl -i "http://localhost:9657/match-record/vat/$MISSING_ID" \
  -H 'Authorization: Bearer invalid-token' \
  -H 'Accept: application/vnd.hmrc.1.0+json' \
  -H "CorrelationId: $CORRELATION_ID"
```

### Running tests

Run all the tests with coverage report:

    sbt clean compile coverage test it:test component:test coverageReport

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
