package com.booker.tests;

import com.booker.base.BaseTest;
import com.booker.pojo.Booking;
import com.booker.utils.ConfigReader;
import com.booker.utils.TestDataGenerator;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;


public class NegativeTests extends BaseTest {

    private String validToken;
    private int existingId;

    // Runs once before all tests: create a booking and a valid token to use in the tests below
    @BeforeClass
    public void setUp() {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", ConfigReader.get("username"));
        credentials.put("password", ConfigReader.get("password"));

        Response authResponse = given().spec(requestSpec).body(credentials)
                .post("/auth");
        authResponse.then()
                .spec(successSpec)
                .body("token", notNullValue());
        validToken = authResponse.jsonPath().getString("token");

        Booking booking = TestDataGenerator.newBooking();
        Response bookingResponse = given().spec(requestSpec).body(booking)
                .post("/booking");
        bookingResponse.then()
                .spec(successSpec)
                .body("bookingid", notNullValue());
        existingId = bookingResponse.jsonPath().getInt("bookingid");
    }

    // Runs once after all tests: clean up the booking we created
    @AfterClass
    public void tearDown() {
        if (validToken == null || existingId <= 0) {
            return;
        }

        given().spec(requestSpec)
                .cookie("token", validToken)
                .delete("/booking/" + existingId);
    }

    // ---------- Invalid authentication ----------
    @Test
    public void invalidAuthentication() {
        Map<String, String> wrongCredentials = new HashMap<>();
        wrongCredentials.put("username", "admin");
        wrongCredentials.put("password", "wrongpassword");

        given().spec(requestSpec).body(wrongCredentials)
                .when().post("/auth")
                .then()
                .statusCode(200)                        // API quirk: still 200
                .body("token", nullValue())
                .body("reason", equalTo("Bad credentials"));
    }

    // ---------- Missing authentication (no credentials sent) ----------
    @Test
    public void missingAuthentication() {
        given().spec(requestSpec).body("{}")
                .when().post("/auth")
                .then()
                .statusCode(200)
                .body("token", nullValue())
                .body("reason", equalTo("Bad credentials"));
    }

    // ---------- Invalid booking ID ----------
    @Test
    public void getBookingWithInvalidId() {
        given().spec(requestSpec)
                .pathParam("id", 999999999)
                .when().get("/booking/{id}")
                .then()
                .statusCode(404);
    }

    // ---------- Invalid request payload ----------
    @Test
    public void createBookingWithInvalidPayload() {
        given().spec(requestSpec)
                .body("{ this is not valid json")       // broken JSON
                .when().post("/booking")
                .then()
                .statusCode(anyOf(is(400), is(500)));   // server rejects it (usually 500)
    }

    // ---------- Missing mandatory fields ----------
    @Test
    public void createBookingWithoutFirstname() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("lastname", "Sharma");              // firstname and others are missing
        payload.put("totalprice", 100);

        given().spec(requestSpec).body(payload)
                .when().post("/booking")
                .then()
                .statusCode(anyOf(is(400), is(500)));
    }

    // ---------- Update without authorization ----------
    @Test
    public void updateWithoutToken() {
        given().spec(requestSpec)                       // no Cookie header = not authorised
                .pathParam("id", existingId)
                .body(TestDataGenerator.newBooking())
                .when().put("/booking/{id}")
                .then()
                .statusCode(403);
    }

    // ---------- Delete without authorization ----------
    @Test
    public void deleteWithoutToken() {
        given().spec(requestSpec)
                .pathParam("id", existingId)
                .when().delete("/booking/{id}")
                .then()
                .statusCode(403);
    }

    // ---------- Invalid token ----------
    @Test
    public void deleteWithInvalidToken() {
        given().spec(requestSpec)
                .cookie("token", "invalid123")
                .pathParam("id", existingId)
                .when().delete("/booking/{id}")
                .then()
                .statusCode(403);
    }
}
