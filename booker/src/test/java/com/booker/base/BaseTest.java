package com.booker.base;

import com.booker.utils.ConfigReader;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.filter.log.LogDetail;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.lessThan;


public class BaseTest {

    // Request spec: base URL + JSON headers + log the request to the console
    protected RequestSpecification requestSpec = new RequestSpecBuilder()
            .setBaseUri(ConfigReader.get("base.url"))
            .setContentType(ContentType.JSON)     // we send JSON
            .setAccept("application/json")        // the API's teapot bug rejects the expanded JSON media type list
            .log(LogDetail.ALL)                   // logging: print full request
            .build();

    // Response spec for successful calls: status 200 + JSON + fast response + log the response
    protected ResponseSpecification successSpec = new ResponseSpecBuilder()
            .expectStatusCode(200)
            .expectContentType(ContentType.JSON)
            .expectResponseTime(lessThan(10000L), TimeUnit.MILLISECONDS)
            .log(LogDetail.ALL)                   // logging: print full response
            .build();
}
