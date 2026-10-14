package com.booker.tests;

import com.booker.base.BaseTest;
import com.booker.pojo.Booking;
import com.booker.utils.ConfigReader;
import com.booker.utils.TestDataGenerator;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Full CRUD lifecycle. Tests run in order (priority) and share data through the
 * fields below (token, bookingId, booking). dependsOnMethods skips a test if the
 * previous one failed, because it would fail for the wrong reason.
 *
 * REST Assured pattern:   given()  -> request setup
 *                         when()   -> send the request (get/post/put/patch/delete)
 *                         then()   -> validate the response
 */
public class BookingCrudTest extends BaseTest {

    private String token;
    private int bookingId;
    private Booking booking;       // the data we expect the server to hold

    // ---------- 1. AUTH ----------
    @Test(priority = 1)
    public void generateToken() {
        // request body as a simple Map -> becomes {"username":"admin","password":"password123"}
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", ConfigReader.get("username"));
        credentials.put("password", ConfigReader.get("password"));

        Response response = given()
                .spec(requestSpec)
                .body(credentials)
                .when()
                .post("/auth")
                .then()
                .spec(successSpec)                      // 200, JSON, response time
                .body("token", notNullValue())          // token must exist
                .extract().response();

        token = response.jsonPath().getString("token"); // save token for later tests
        Assert.assertFalse(token.isEmpty(), "Token should not be empty");
    }

    // ---------- 2. CREATE ----------
    @Test(priority = 2, dependsOnMethods = "generateToken")
    public void createBooking() {
        booking = TestDataGenerator.newBooking();       // random data

        Response response = given()
                .spec(requestSpec)
                .body(booking)                          // POJO -> JSON (serialization)
                .when()
                .post("/booking")
                .then()
                .spec(successSpec)
                .body("bookingid", notNullValue())
                .body("booking.firstname", equalTo(booking.getFirstname()))
                .body("booking.lastname", equalTo(booking.getLastname()))
                .body("booking.totalprice", equalTo(booking.getTotalprice()))
                .body("booking.depositpaid", equalTo(booking.isDepositpaid()))
                .body("booking.bookingdates.checkin", equalTo(booking.getBookingdates().getCheckin()))
                .body("booking.bookingdates.checkout", equalTo(booking.getBookingdates().getCheckout()))
                .body("booking.additionalneeds", equalTo(booking.getAdditionalneeds()))
                .extract().response();

        bookingId = response.jsonPath().getInt("bookingid");   // store ID dynamically
        System.out.println(">>> Created booking id = " + bookingId);
    }

    // ---------- 3. GET (path parameter) ----------
    @Test(priority = 3, dependsOnMethods = "createBooking")
    public void getBooking() {
        Booking actual = given()
                .spec(requestSpec)
                .pathParam("id", bookingId)             // replaces {id} in the URL
                .when()
                .get("/booking/{id}")
                .then()
                .spec(successSpec)
                .body(matchesJsonSchemaInClasspath("schemas/booking-schema.json"))  // schema validation
                .extract().as(Booking.class);           // JSON -> POJO (deserialization)

        Assert.assertEquals(actual.getFirstname(), booking.getFirstname());
        Assert.assertEquals(actual.getLastname(), booking.getLastname());
        Assert.assertEquals(actual.getTotalprice(), booking.getTotalprice());
        Assert.assertEquals(actual.isDepositpaid(), booking.isDepositpaid());
        Assert.assertEquals(actual.getBookingdates().getCheckin(), booking.getBookingdates().getCheckin());
        Assert.assertEquals(actual.getBookingdates().getCheckout(), booking.getBookingdates().getCheckout());
        Assert.assertEquals(actual.getAdditionalneeds(), booking.getAdditionalneeds());
    }

    // ---------- 3b. GET with query parameters ----------
    @Test(priority = 4, dependsOnMethods = "createBooking")
    public void searchBookingByName() {
        given()
                .spec(requestSpec)
                .queryParam("firstname", booking.getFirstname())   // adds ?firstname=...
                .queryParam("lastname", booking.getLastname())     // adds &lastname=...
                .when()
                .get("/booking")
                .then()
                .spec(successSpec)
                .body("bookingid", hasItem(bookingId));            // our ID must be in the list
    }

    // ---------- 4. PUT (full update) ----------
    @Test(priority = 5, dependsOnMethods = "getBooking")
    public void updateBooking() {
        booking = TestDataGenerator.newBooking();       // brand new values; every field changes

        given()
                .spec(requestSpec)
                .cookie("token", token)                 // authorization cookie
                .pathParam("id", bookingId)
                .body(booking)
                .when()
                .put("/booking/{id}")
                .then()
                .spec(successSpec)
                .body("firstname", equalTo(booking.getFirstname()))
                .body("lastname", equalTo(booking.getLastname()))
                .body("totalprice", equalTo(booking.getTotalprice()))
                .body("depositpaid", equalTo(booking.isDepositpaid()))
                .body("bookingdates.checkin", equalTo(booking.getBookingdates().getCheckin()))
                .body("bookingdates.checkout", equalTo(booking.getBookingdates().getCheckout()))
                .body("additionalneeds", equalTo(booking.getAdditionalneeds()));
    }

    // ---------- 5. PATCH (partial update) ----------
    @Test(priority = 6, dependsOnMethods = "updateBooking")
    public void partialUpdateBooking() {
        // only send the fields we want to change
        Map<String, Object> changes = new HashMap<>();
        changes.put("firstname", "PatchedName");
        changes.put("totalprice", 999);

        given()
                .spec(requestSpec)
                .cookie("token", token)
                .pathParam("id", bookingId)
                .body(changes)
                .when()
                .patch("/booking/{id}")
                .then()
                .spec(successSpec)
                // updated fields
                .body("firstname", equalTo("PatchedName"))
                .body("totalprice", equalTo(999))
                // unchanged fields (still equal to the PUT values)
                .body("lastname", equalTo(booking.getLastname()))
                .body("depositpaid", equalTo(booking.isDepositpaid()))
                .body("bookingdates.checkin", equalTo(booking.getBookingdates().getCheckin()))
                .body("bookingdates.checkout", equalTo(booking.getBookingdates().getCheckout()))
                .body("additionalneeds", equalTo(booking.getAdditionalneeds()));
    }

    // ---------- 6. DELETE + verify ----------
    @Test(priority = 7, dependsOnMethods = "partialUpdateBooking")
    public void deleteBooking() {
        // This API returns 201 (Created) for a successful delete - an API quirk
        given()
                .spec(requestSpec)
                .cookie("token", token)
                .pathParam("id", bookingId)
                .when()
                .delete("/booking/{id}")
                .then()
                .statusCode(201);

        // Verify it is really gone
        given()
                .spec(requestSpec)
                .pathParam("id", bookingId)
                .when()
                .get("/booking/{id}")
                .then()
                .statusCode(404);
    }
}
