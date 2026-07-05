package com.studyshield.qa.stepdefs;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import java.util.List;
import java.util.Optional;

import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

public class StudentSteps {

    private final SharedState state;

    public StudentSteps(SharedState state) {
        this.state = state;
    }

    private String baseUrl() {
        return Optional.ofNullable(System.getProperty("api.base-url")).orElse("http://localhost:8080");
    }

    @When("I add a student named {string}")
    public void iAddStudent(String name) {
        String body = "{\"name\":\"" + name + "\",\"gender\":\"MALE\",\"birthYear\":2018,\"studentClass\":\"1st\"}";
        state.response = given().baseUri(baseUrl())
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + state.sessionId)
                .body(body).when().post("/api/students");
        if (state.response.getStatusCode() == 201) {
            state.studentId = state.response.jsonPath().getString("studentId");
        }
    }

    @When("I list all students")
    public void iListStudents() {
        state.response = given().baseUri(baseUrl())
                .header("Authorization", "Bearer " + state.sessionId)
                .when().get("/api/students");
    }

    @When("I update the student's name to {string}")
    public void iUpdateStudent(String newName) {
        String body = "{\"name\":\"" + newName + "\",\"gender\":\"MALE\",\"birthYear\":2018,\"studentClass\":\"1st\"}";
        state.response = given().baseUri(baseUrl())
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + state.sessionId)
                .body(body).when().put("/api/students/" + state.studentId);
    }

    @When("I delete the student")
    public void iDeleteStudent() {
        state.response = given().baseUri(baseUrl())
                .header("Authorization", "Bearer " + state.sessionId)
                .when().delete("/api/students/" + state.studentId);
    }

    @Then("the student should be created")
    public void studentShouldBeCreated() {
        String sid = state.response.jsonPath().getString("studentId");
        assertTrue(sid != null && !sid.isEmpty(), "Student should have a studentId");
        String name = state.response.jsonPath().getString("name");
        assertTrue(name != null && !name.isEmpty(), "Student should have a name");
    }

    @Then("I should see {string} in the student list")
    public void iShouldSeeStudentInList(String name) {
        List<String> names = state.response.jsonPath().getList("name");
        assertTrue(names.contains(name), "Student list should contain: " + name);
    }

    @Then("I should not see {string} in the student list")
    public void iShouldNotSeeStudentInList(String name) {
        List<String> names = state.response.jsonPath().getList("name");
        assertFalse(names.contains(name), "Student list should not contain: " + name);
    }

    @Then("the student's name should be {string}")
    public void studentNameShouldBe(String expectedName) {
        assertEquals(expectedName, state.response.jsonPath().getString("name"));
    }

    @Then("the student list should be empty")
    public void studentListShouldBeEmpty() {
        List<?> students = state.response.jsonPath().getList("$");
        assertTrue(students == null || students.isEmpty(), "Student list should be empty");
    }
}
