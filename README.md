# DWP Cinema Tickets Exercise

[![CircleCI](https://circleci.com/gh/brightonsbox/dwp-cinema-tickets/tree/main.svg?style=svg)](https://circleci.com/gh/brightonsbox/dwp-cinema-tickets)

Thank you for reviewing my submission!

## Building and running unit tests
Prerequisites, Java 17 and Maven, I use [SDKMan](https://sdkman.io/) to manage my dependencies locally.

To run the unit tests, at the root of the project directory, run in a command line:

```bash
mvn test
```

I am also using CircleCI to build and test code before merging branches into the `main` branch.

## Questions
Some additional questions that I would want to ask the business:

* Is one adult allowed to have multiple infants sat on their lap? Is there a reasonable limit?
* What should the behaviour be for a request for zero tickets? Currently my implementation requires at least one adult.
* Can `InvalidPurchaseExeption` be modified so that a message can be added to the exception to describe in more detail what the issue was?

<br>
<details>
<summary>Initial instructions</summary>

### Objective

This is a coding exercise which will allow you to demonstrate how you code and your approach to a given problem.

You will be assessed on:

- Your ability to write clean, well-tested and reusable code.
- How you have ensured the following business rules are correctly met.

### Business Rules

- There are 3 types of tickets i.e. Infant, Child, and Adult.
- The ticket prices are based on the type of ticket (see table below).
- The ticket purchaser declares how many and what type of tickets they want to buy.
- Multiple tickets can be purchased at any given time.
- Only a maximum of 20 tickets that can be purchased at a time.
- Infants do not pay for a ticket and are not allocated a seat. They will be sitting on an Adult's lap.
- Child and Infant tickets cannot be purchased without purchasing an Adult ticket.

|   Ticket Type    |     Price   |
| ---------------- | ----------- |
|    INFANT        |    £0       |
|    CHILD         |    £10      |
|    ADULT         |    £20      |

- There is an existing `TicketPaymentService` responsible for taking payments.
- There is an existing `SeatReservationService` responsible for reserving seats.

### Constraints

- The TicketService interface CANNOT be modified. (For Java solution only)
- The code in the thirdparty.* packages CANNOT be modified.
- The `TicketTypeRequest` SHOULD be an immutable object.

### Assumptions

You can assume:

- All accounts with an id greater than zero are valid. They also have sufficient funds to pay for any no of tickets.
- The `TicketPaymentService` implementation is an external provider with no defects. You do not need to worry about how the actual payment happens.
- The payment will always go through once a payment request has been made to the `TicketPaymentService`.
- The `SeatReservationService` implementation is an external provider with no defects. You do not need to worry about how the seat reservation algorithm works.
- The seat will always be reserved once a reservation request has been made to the `SeatReservationService`.

### Your Task

Provide a working implementation of a `TicketService` that:

- Considers the above objective, business rules, constraints & assumptions.
- Calculates the correct amount for the requested tickets and makes a payment request to the `TicketPaymentService`.
- Calculates the correct no of seats to reserve and makes a seat reservation request to the `SeatReservationService`.
- Rejects any invalid ticket purchase requests. It is up to you to identify what should be deemed as an invalid purchase request.”
</details>
