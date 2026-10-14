# Explanation Guide (for presenting / interview)

## 1. What is REST Assured?
A Java library for testing REST APIs. You write the request and the checks in one readable chain:

```java
given()                         // 1. prepare the request (headers, body, params)
    .spec(requestSpec)
    .body(booking)
.when()                         // 2. send it (GET / POST / PUT / PATCH / DELETE)
    .post("/booking")
.then()                         // 3. verify the response
    .statusCode(200)
    .body("booking.firstname", equalTo("Asha"));
```
Think of it as **Given** (setup) → **When** (action) → **Then** (check).

## 2. Role of each tool
| Tool | Job |
|---|---|
| Maven | Downloads libraries (pom.xml) and runs the tests (`mvn test`) |
| TestNG | Runs the tests (`@Test`), gives assertions and the HTML report |
| REST Assured | Sends the HTTP requests and validates responses |
| Jackson | Converts Java object ↔ JSON (used behind the scenes) |

## 3. Key concepts and where they are in the code
* **Specification** (`BaseTest`) – reusable settings (base URL, JSON headers, logging, expected 200 / JSON / response time) so we don't repeat them in each test.
* **POJO** (`Booking`) – a Java class with the same fields as the JSON.
  * *Serialization*: object → JSON, happens in `.body(booking)`.
  * *Deserialization*: JSON → object, happens in `.extract().as(Booking.class)`.
* **Path parameter** – part of the URL: `/booking/{id}` with `.pathParam("id", 5)` → `/booking/5`.
* **Query parameter** – after the `?`: `.queryParam("firstname","Asha")` → `/booking?firstname=Asha`.
* **JSON schema validation** – checks the *structure and data types* of the response (is `totalprice` an integer? is `bookingdates` an object?), not just values.
* **Dynamic data** – `TestDataGenerator` makes new random bookings so tests are not tied to fixed data.
* **Storing the booking ID** – we read it from the create response (`jsonPath().getInt("bookingid")`) and reuse it in later tests.
* **Authorization** – we get a token from `/auth` and send it as header `Cookie: token=...` for PUT / PATCH / DELETE.

## 4. TestNG things to mention
* `@Test(priority = n)` – controls execution order.
* `dependsOnMethods` – skips a test if the earlier one failed (no point deleting a booking that was never created).
* `@BeforeClass` / `@AfterClass` – setup and cleanup once per class (used in `NegativeTests`).
* `testng.xml` – lists which classes to run.

## 5. 1-minute walkthrough script
"I used Java, REST Assured, TestNG and Maven. `BaseTest` holds the request and response specifications so the base URL, headers and logging are defined once. `Booking` is a POJO, so I can send and receive objects instead of raw JSON. `BookingCrudTest` runs the full flow: get a token, create a booking and save its ID, get it and compare every field plus a schema check, search using query parameters, update with PUT, partially update with PATCH, then delete and confirm a 404. `NegativeTests` covers wrong login, missing token, invalid ID, bad payload and missing fields. Test data is random, config lives in a properties file, and the TestNG report is generated in `target/surefire-reports`."

## 6. Likely questions
**PUT vs PATCH?** PUT replaces the whole booking (all fields required). PATCH changes only the fields you send.

**Why does the delete test expect 201?** This demo API returns 201 instead of the usual 204/200. I assert what the API really returns.

**Why does the wrong-login test expect 200?** This API returns 200 with `{"reason":"Bad credentials"}` instead of 401. Same reason.

**What does `dependsOnMethods` do?** Skips the test if the one it depends on failed.

**What is `extract()`?** Pulls data out of the response (a value, an object, or the whole response) so I can reuse it later.

**What is `Response` vs `ResponseSpecification`?** `Response` is the actual reply from the server. `ResponseSpecification` is a reusable set of expectations.

**How do you authenticate?** POST `/auth` returns a token, which I send as `Cookie: token=<value>`.

## 7. If a test fails when you run it
* Read the console: the request/response log is printed for every call.
* The first call to the Heroku server can be slow. Re-run it, or raise the `lessThan(10000L)` value in `BaseTest`.
* If a negative test fails because of a status code (e.g. 400 vs 500), just change the expected number to what the log shows.
