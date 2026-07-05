@regression
Feature: Parent Management
  As a parent user
  I want to manage parent profiles in my account
  So that multiple parents can share the account

  @regression
  Scenario: Add a second parent and verify list
    Given I am signed in
    When I add a parent named "Bob"
    Then I should receive HTTP 201
    And the parent should be added to the account

    When I list all parents
    Then I should see at least 2 parents

  @regression
  Scenario: Signin shows parent selection when 2+ parents exist
    Given I am signed in
    When I add a parent named "Bob"
    Then I should receive HTTP 201

    When I sign in with the same credentials
    Then the response should indicate parent selection is required

  @regression
  Scenario: Update own parent name
    Given I am signed in
    When I update my name to "Alice Updated"
    Then I should receive HTTP 200
    And my name should be "Alice Updated"

  @regression
  Scenario: List parents without auth returns error
    When I list all parents
    Then I should receive HTTP 400

  @regression
  Scenario: Add parent without auth returns 400
    When I add a parent named "Bob"
    Then I should receive HTTP 400

  @regression
  Scenario: Delete a parent
    Given I am signed in
    When I add a parent named "Bob"
    Then I should receive HTTP 201
    When I delete the parent
    Then I should receive HTTP 204
    When I list all parents
    Then I should see at least 1 parents

  @regression
  Scenario: Delete parent without auth returns 400
    When I delete the parent
    Then I should receive HTTP 400
