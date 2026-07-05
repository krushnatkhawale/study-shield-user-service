# QA Acceptance Tests

Cucumber-based acceptance tests that exercise the API via HTTP (RestAssured).

## Prerequisites

The application must be running on `http://localhost:8080`:

```shell
./gradlew bootRun
```

## Running Tests

### All QA tests
```shell
./gradlew :qa:test
```

### By tag
```shell
./gradlew :qa:test -Dcucumber.tags="@smoke"
./gradlew :qa:test -Dcucumber.tags="@regression"
./gradlew :qa:test -Dcucumber.tags="@signout"
```

## Reports

- HTML: `qa/build/reports/cucumber.html`
- JUnit XML: `qa/build/reports/tests/test/index.html`

## Configuration

The API base URL defaults to `http://localhost:8080` (see `src/test/resources/application-test.yml`).

Override with a system property:
```shell
./gradlew :qa:test -Dapi.base-url=http://localhost:9000
```
