@smoke
Feature: Health Check
  As an API consumer
  I want to verify the API is running
  So that I can trust downstream requests

  Scenario: API is healthy
    Given the API is running
    When I check the health endpoint
    Then I should receive HTTP 200
    Then the response body should contain "UP"
