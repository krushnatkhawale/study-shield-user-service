@regression
Feature: Authentication
  As a user of the platform
  I want to sign up, sign in, sign out, and validate sessions
  So that I can securely access my account

  @regression
  Scenario: Successful signup with name
    Given I generate a unique login ID
    When I sign up with password "secure123" and name "Alice"
    Then I should receive HTTP 201
    And the response should contain a session ID

  @regression
  Scenario: Successful signup without name (auto-generated)
    Given I generate a unique login ID
    When I sign up with password "secure123" and name ""
    Then I should receive HTTP 201
    And the response should contain a session ID

  @regression
  Scenario: Signup with duplicate login ID returns 409
    Given I have an existing account
    When I sign up with the same credentials
    Then I should receive HTTP 409

  @regression
  Scenario: Signup with weak password returns 400
    Given I generate a unique login ID
    When I sign up with password "abc" and name "Test"
    Then I should receive HTTP 400

  @regression
  Scenario: Signin with valid credentials returns session
    Given I have an existing account
    When I sign in with the same credentials
    Then I should receive HTTP 200
    And the response should contain a session ID

  @regression
  Scenario: Signin with wrong password returns 401
    Given I have an existing account
    When I sign in with the same login ID and password "wrongpassword"
    Then I should receive HTTP 401

  @regression
  Scenario: Signin with non-existent login ID returns 401
    When I sign in with login ID "nonexistent@test.com" and password "secure123"
    Then I should receive HTTP 401

  @regression
  Scenario: Full auth lifecycle (signup->signin->validate->signout->validate fails)
    Given I have an existing account
    When I sign in with the same credentials
    Then I should receive HTTP 200
    And the response should contain a session ID

    When I validate my session
    Then I should receive HTTP 200
    And the response should indicate the session is valid

    When I sign out
    Then I should receive HTTP 200

    When I validate my session
    Then I should receive HTTP 401

  @regression
  Scenario: Signout without session ID returns 400
    When I sign in with login ID "" and password ""
    Then I should receive HTTP 400
