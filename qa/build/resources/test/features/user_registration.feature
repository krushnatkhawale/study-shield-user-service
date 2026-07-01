Feature: User Registration
  As a mobile app user
  I want to register with username and email
  So that I can get a unique user ID and auth token

  Scenario: Successful user registration with username and email
    Given I have a username "john_doe" and email "john@example.com"
    When I register the user
    Then I should receive a valid user ID
    And I should receive an authentication token
    And I should get a success message

  Scenario: User registration without email
    Given I have a username "jane_smith" and no email
    When I register the user
    Then I should receive a valid user ID
    And I should receive an authentication token
    And I should get a success message

  Scenario: User registration without username
    Given I have no username and email "bob@example.com"
    When I register the user
    Then I should receive a valid user ID
    And I should receive an authentication token
    And I should get a success message

  Scenario: Registration with duplicate user ID
    Given I have a username "duplicate_test" and email "test@example.com"
    When I register a user with the same details
    Then I should receive a unique user ID