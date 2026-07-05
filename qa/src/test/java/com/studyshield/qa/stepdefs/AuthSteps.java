package com.studyshield.qa.stepdefs;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Optional;
import java.util.UUID;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

public class AuthSteps {

    private final SharedState state;

    public AuthSteps(SharedState state) {
        this.state = state;
    }

    private String baseUrl() {
        return Optional.ofNullable(System.getProperty("api.base-url")).orElse("http://localhost:8080");
    }

    @Given("I generate a unique login ID")
    public void iGenerateUniqueLoginId() {
        state.loginId = "test-" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        state.password = "secure123";
    }

    @Given("I have an existing account")
    public void iHaveAnExistingAccount() {
        iGenerateUniqueLoginId();
        iSignUp(state.password, "Alice");
        state.response = null;
    }

    @When("I sign up with password {string} and name {string}")
    public void iSignUp(String password, String name) {
        if (state.loginId == null) {
            iGenerateUniqueLoginId();
        }
        state.password = password;
        String body = "{\"loginId\":\"" + state.loginId + "\",\"password\":\"" + password + "\"";
        if (name != null && !name.isEmpty()) {
            body += ",\"name\":\"" + name + "\"";
        }
        body += "}";
        state.response = given().baseUri(baseUrl())
                .contentType(ContentType.JSON)
                .body(body).when().post("/api/auth/signup");
        if (state.response.getStatusCode() == 201) {
            state.sessionId = state.response.jsonPath().getString("sessionId");
            state.parentId = state.response.jsonPath().getString("parentId");
            state.parentName = state.response.jsonPath().getString("parentName");
            state.accountId = state.response.jsonPath().getString("accountId");
        }
    }

    @When("I sign up with the same credentials")
    public void iSignUpSameCredentials() {
        String body = "{\"loginId\":\"" + state.loginId + "\",\"password\":\"" + state.password + "\"}";
        state.response = given().baseUri(baseUrl())
                .contentType(ContentType.JSON)
                .body(body).when().post("/api/auth/signup");
    }

    @When("I sign in with the same credentials")
    public void iSignInSameCredentials() {
        String body = "{\"loginId\":\"" + state.loginId + "\",\"password\":\"" + state.password + "\"}";
        state.response = given().baseUri(baseUrl())
                .contentType(ContentType.JSON)
                .body(body).when().post("/api/auth/signin");
        if (state.response.getStatusCode() == 200 && !state.response.jsonPath().getBoolean("requiresParentSelection")) {
            state.sessionId = state.response.jsonPath().getString("sessionId");
            state.parentId = state.response.jsonPath().getString("parentId");
            state.parentName = state.response.jsonPath().getString("parentName");
            state.accountId = state.response.jsonPath().getString("accountId");
        }
    }

    @When("I sign in with the same login ID and password {string}")
    public void iSignInWithPassword(String password) {
        String body = "{\"loginId\":\"" + state.loginId + "\",\"password\":\"" + password + "\"}";
        state.response = given().baseUri(baseUrl())
                .contentType(ContentType.JSON)
                .body(body).when().post("/api/auth/signin");
    }

    @When("I sign in with login ID {string} and password {string}")
    public void iSignInWithExplicitCredentials(String loginId, String password) {
        String body = "{\"loginId\":\"" + loginId + "\",\"password\":\"" + password + "\"}";
        state.response = given().baseUri(baseUrl())
                .contentType(ContentType.JSON)
                .body(body).when().post("/api/auth/signin");
    }

    @When("I sign out")
    public void iSignOut() {
        state.response = given().baseUri(baseUrl())
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + state.sessionId)
                .when().post("/api/auth/signout");
    }

    @When("I validate my session")
    public void iValidateSession() {
        if (state.sessionId == null) {
            state.response = given().baseUri(baseUrl())
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer invalid-session-id")
                    .when().post("/api/auth/validate");
            return;
        }
        state.response = given().baseUri(baseUrl())
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + state.sessionId)
                .when().post("/api/auth/validate");
    }

    @Then("I should receive HTTP {int}")
    public void iShouldReceiveHttpStatus(int statusCode) {
        assertEquals(statusCode, state.response.getStatusCode());
    }

    @Then("the response should contain a session ID")
    public void responseShouldContainSessionId() {
        String sid = state.response.jsonPath().getString("sessionId");
        assertNotNull(sid, "Response should contain a sessionId");
        assertTrue(!sid.isEmpty(), "sessionId should not be empty");
    }

    @Then("the response should indicate the session is valid")
    public void responseShouldIndicateSessionValid() {
        boolean valid = state.response.jsonPath().getBoolean("valid");
        assertTrue(valid, "Response should indicate session is valid");
    }

    @Then("the response should indicate parent selection is required")
    public void responseShouldIndicateParentSelectionRequired() {
        boolean requiresSelection = state.response.jsonPath().getBoolean("requiresParentSelection");
        assertTrue(requiresSelection, "Response should indicate parent selection is required");
    }

    @Then("should return HTTP {int}")
    public void shouldReturnHttpStatus(int statusCode) {
        assertEquals(statusCode, state.response.getStatusCode());
    }
}
