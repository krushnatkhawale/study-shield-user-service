package com.studyshield.qa.stepdefs;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class HealthCheckSteps {

    private final SharedState state;

    public HealthCheckSteps(SharedState state) {
        this.state = state;
    }

    private String baseUrl() {
        return Optional.ofNullable(System.getProperty("api.base-url")).orElse("http://localhost:8080");
    }

    @Given("the API is running")
    public void theApiIsRunning() {
    }

    @When("I check the health endpoint")
    public void iCheckTheHealthEndpoint() {
        state.response = given().baseUri(baseUrl()).when().get("/actuator/health");
    }

    @Then("the response body should contain {string}")
    public void theResponseBodyShouldContain(String expectedContent) {
        String body = state.response.getBody().asString();
        assertTrue(body.contains(expectedContent), "Response body should contain: " + expectedContent);
    }
}
