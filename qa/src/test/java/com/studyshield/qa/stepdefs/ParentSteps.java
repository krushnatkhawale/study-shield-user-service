package com.studyshield.qa.stepdefs;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import java.util.Optional;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

public class ParentSteps {

    private final SharedState state;

    public ParentSteps(SharedState state) {
        this.state = state;
    }

    private String baseUrl() {
        return Optional.ofNullable(System.getProperty("api.base-url")).orElse("http://localhost:8080");
    }

    @Given("I am signed in")
    public void iAmSignedIn() {
        if (state.sessionId == null) {
            state.loginId = "test-" + java.util.UUID.randomUUID().toString().substring(0, 8) + "@example.com";
            state.password = "secure123";
            String signupBody = "{\"loginId\":\"" + state.loginId + "\",\"password\":\"secure123\",\"name\":\"Alice\"}";
            var signupResp = given().baseUri(baseUrl())
                    .contentType(ContentType.JSON)
                    .body(signupBody)
                    .when().post("/api/auth/signup");
            state.sessionId = signupResp.jsonPath().getString("sessionId");
            state.parentId = signupResp.jsonPath().getString("parentId");
            state.parentName = signupResp.jsonPath().getString("parentName");
            state.accountId = signupResp.jsonPath().getString("accountId");
        }
    }

    @When("I add a parent named {string}")
    public void iAddParent(String name) {
        String body = "{\"name\":\"" + name + "\"}";
        state.response = given().baseUri(baseUrl())
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + state.sessionId)
                .body(body).when().post("/api/parents");
        if (state.response.getStatusCode() == 201) {
            state.parentId = state.response.jsonPath().getString("parentId");
            state.parentName = state.response.jsonPath().getString("name");
        }
    }

    @When("I list all parents")
    public void iListParents() {
        state.response = given().baseUri(baseUrl())
                .header("Authorization", "Bearer " + state.sessionId)
                .when().get("/api/parents");
    }

    @When("I update my name to {string}")
    public void iUpdateMyName(String newName) {
        String body = "{\"name\":\"" + newName + "\"}";
        state.response = given().baseUri(baseUrl())
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + state.sessionId)
                .body(body).when().put("/api/parents/me");
        if (state.response.getStatusCode() == 200) {
            state.parentName = state.response.jsonPath().getString("name");
        }
    }

    @Then("the parent should be added to the account")
    public void parentShouldBeAdded() {
        String pid = state.response.jsonPath().getString("parentId");
        assertTrue(pid != null && !pid.isEmpty(), "Parent should have a parentId");
        String name = state.response.jsonPath().getString("name");
        assertTrue(name != null && !name.isEmpty(), "Parent should have a name");
    }

    @Then("I should see at least {int} parents")
    public void iShouldSeeAtLeastParents(int count) {
        var parents = state.response.jsonPath().getList("$");
        assertTrue(parents.size() >= count, "Should have at least " + count + " parents");
    }

    @When("I delete the parent")
    public void iDeleteParent() {
        String pid = state.parentId != null ? state.parentId : "00000000-0000-0000-0000-000000000000";
        state.response = given().baseUri(baseUrl())
                .header("Authorization", "Bearer " + state.sessionId)
                .when().delete("/api/parents/" + pid);
    }

    @Then("my name should be {string}")
    public void myNameShouldBe(String expectedName) {
        assertEquals(expectedName, state.response.jsonPath().getString("name"));
    }
}
