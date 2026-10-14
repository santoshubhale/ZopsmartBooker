# Restful Booker API Automation

API automation for the complete Restful Booker CRUD lifecycle using **Java, REST Assured, TestNG, and Maven**.

## Prerequisites
* JDK 11 or newer (`java -version`)
* Maven 3.6 or newer (`mvn -version`)
* Internet access to `https://restful-booker.herokuapp.com`

## How to run
From the directory containing `pom.xml`, run:

```bash
mvn clean test
```

Run either test class individually:

```bash
mvn clean test -Dtest=BookingCrudTest
mvn clean test -Dtest=NegativeTests
```

In IntelliJ IDEA, run `testng.xml` as a TestNG suite or run either test class.

## Test reports and submission evidence
Maven Surefire and TestNG create the execution report after the suite finishes:

* `target/surefire-reports/index.html` – TestNG HTML report
* `target/surefire-reports/emailable-report.html` – printable, single-page report
* `target/surefire-reports/TestSuite.txt` – concise test totals
* `target/surefire-reports/TEST-TestSuite.xml` – machine-readable test results

Open `index.html` or `emailable-report.html` in a browser to view the results. Capture that report in a screenshot if a separate screenshot is required. The console also logs each request and response.

## Scenario coverage
* **Authentication:** generate a token and verify the JSON response, token, status, and response-time limit.
* **CRUD lifecycle:** create a booking and save its generated ID; retrieve and verify all fields; search by query parameters; update with PUT; partially update with PATCH and verify unchanged values; delete and verify the subsequent GET returns 404.
* **Negative scenarios:** invalid credentials, missing credentials, invalid booking ID, malformed JSON, missing booking fields, unauthenticated update/delete, and an invalid token.

## Project structure
```
pom.xml                      dependencies + how tests run
testng.xml                   which test classes to run
src/test/resources
   config.properties         base URL, username, password
   schemas/booking-schema.json   JSON schema used for validation
src/test/java/com/booker
   base/BaseTest.java        request/response specifications + logging
   pojo/Booking.java, BookingDates.java   Java classes matching the JSON
   utils/ConfigReader.java   reads config.properties
   utils/TestDataGenerator.java   random booking data
   tests/BookingCrudTest.java     Auth → Create → Get → Search → PUT → PATCH → DELETE
   tests/NegativeTests.java       8 negative tests
```

## Requirement coverage
| Requirement | Where |
|---|---|
| Request and response specifications | `BaseTest.requestSpec` and `BaseTest.successSpec` |
| POJO serialization/deserialization | `BookingCrudTest`: send with `.body(booking)` and read with `.as(Booking.class)` |
| Path and query parameters | CRUD requests and `searchBookingByName` |
| JSON Schema validation | `getBooking` and `schemas/booking-schema.json` |
| Logging and response-time checks | Shared specifications in `BaseTest` |
| Reusable utilities and configuration | `ConfigReader`, `TestDataGenerator`, `config.properties` |
| Dynamic test data | `TestDataGenerator` generates unique booking data |
| Execution reporting | TestNG and Maven Surefire reports under `target/surefire-reports` |

The request specification sets `Accept: application/json` explicitly. This avoids the expanded media-type list produced by REST Assured's `ContentType.JSON` accept helper, which can trigger the demo API's intentional HTTP 418 header bug.
