@regression
Feature: Student Management
  As a parent user
  I want to manage student profiles in my account
  So that I can track my children's academic information

  @regression
  Scenario: Add a student
    Given I am signed in
    When I add a student named "Charlie"
    Then I should receive HTTP 201
    And the student should be created

  @regression
  Scenario: List students after adding
    Given I am signed in
    When I add a student named "Charlie"
    Then I should receive HTTP 201

    When I list all students
    Then I should see "Charlie" in the student list

  @regression
  Scenario: Update a student's name
    Given I am signed in
    When I add a student named "Charlie"
    Then I should receive HTTP 201

    When I update the student's name to "Charlie Updated"
    Then I should receive HTTP 200
    And the student's name should be "Charlie Updated"

  @regression
  Scenario: Delete a student
    Given I am signed in
    When I add a student named "Charlie"
    Then I should receive HTTP 201

    When I delete the student
    Then I should receive HTTP 204

    When I list all students
    Then I should not see "Charlie" in the student list

  @regression
  Scenario: Add student without auth returns 400
    When I add a student named "Charlie"
    Then I should receive HTTP 400

  @regression
  Scenario: List students without auth returns 400
    When I list all students
    Then I should receive HTTP 400
