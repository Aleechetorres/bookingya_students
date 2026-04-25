Feature: Reservation Management
  As a platform user
  I want to manage reservations
  So that I can book, update and cancel rooms

  Background:
    Given a room with code "101" and name "Deluxe Room" exists in the system
    And a guest with identification "1234567890" and name "John Doe" exists in the system

  Scenario: Create a reservation successfully
    When I create a reservation for the room with check-in in 1 day and check-out in 3 days with 2 guests
    Then the reservation should be created successfully
    And the reservation should have 2 guests

  Scenario: Get a reservation by ID
    Given a reservation has been created for the room with check-in in 1 day and check-out in 3 days
    When I request the reservation by its ID
    Then the reservation should be returned successfully

  Scenario: Get all reservations
    Given a reservation has been created for the room with check-in in 1 day and check-out in 3 days
    When I request all reservations
    Then the response should contain at least 1 reservation

  Scenario: Update a reservation successfully
    Given a reservation has been created for the room with check-in in 1 day and check-out in 3 days
    When I update the reservation with check-in in 5 days and check-out in 7 days with 1 guest
    Then the reservation should be updated successfully
    And the reservation should have 1 guests

  Scenario: Delete a reservation successfully
    Given a reservation has been created for the room with check-in in 1 day and check-out in 3 days
    When I delete the reservation
    Then the reservation should be deleted successfully

  Scenario: Delete a non-existent reservation
    When I try to delete a reservation with a random non-existent ID
    Then the system should throw a not found exception
