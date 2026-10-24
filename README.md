                                            # Cafe Booking & Seating System

[![CI](https://github.com/Satvik-09/CAFE-BOOKING/actions/workflows/ci.yml/badge.svg)](https://github.com/Satvik-09/CAFE-BOOKING/actions/workflows/ci.yml)
![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)
![Spring
Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?logo=springboot&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-Database-4479A1?logo=mysql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)
![REST API](https://img.shields.io/badge/API-REST-7952B3)
![Security](https://img.shields.io/badge/Security-JWT-000000)
![Status](https://img.shields.io/badge/Status-In%20Development-yellow)

A Java Spring Boot backend for cafe reservations and customer orders.
The project focuses on REST API design, request validation,
JWT-protected endpoints, role-based access control, JPA persistence, and
business-rule enforcement for reservation and order workflows.

> **Current scope:** Reservation APIs, order creation, order status
> transitions, and customer cancellation have been implemented and
> exercised through the API regression script. The latest reported API
> regression run passed **37 tests with 0 failures**. GitHub Actions
> also shows successful workflow runs in the evidence screenshot below.
> Check the Actions page for the latest live CI status.

## Contents

-   [Features](#features)
-   [Tech Stack](#tech-stack)
-   [Architecture](#architecture)
-   [Reservation Workflow](#reservation-workflow)
-   [Order Workflow](#order-workflow)
-   [API Overview](#api-overview)
-   [Validation and HTTP Responses](#validation-and-http-responses)
-   [Testing and CI](#testing-and-ci)
-   [Run Locally](#run-locally)
-   [Development Workflow](#development-workflow)
-   [Current Limitations and Roadmap](#current-limitations-and-roadmap)

## Features

### Reservations

-   Create reservations with request validation.
-   Reject overlapping reservations with `409 Conflict`.
-   Allow back-to-back bookings when time slots do not overlap.
-   Validate party size, time range, date, and table existence.
-   List a customer's reservations.
-   Enforce reservation ownership during cancellation.
-   Allow a freed time slot to be booked again.

### Orders

-   Create an order containing multiple order items.
-   Validate order lines before persisting the order.
-   Calculate order totals with `BigDecimal`.
-   Reject empty orders, zero quantities, missing menu items, and
    unavailable items.
-   Enforce order status transitions:
    -   `PLACED` → `PREPARING` or `CANCELLED`
    -   `PREPARING` → `SERVED` or `CANCELLED`
    -   `SERVED` and `CANCELLED` → no further transitions
-   Allow an administrator to update order status.
-   Allow customers to cancel their own orders only while status is
    `PLACED`.
-   Check ownership before customer cancellation.
-   Reject invalid state transitions with `409 Conflict`.
-   Restrict administrative status changes from customer accounts.

### Engineering Practices

-   Layered Spring Boot structure: controllers, services, DTOs,
    repositories, and entities.
-   Request validation using Jakarta Bean Validation.
-   JWT-protected endpoints and role-based authorization.
-   MySQL persistence using Spring Data JPA/Hibernate.
-   API regression tests covering success and failure scenarios.
-   GitHub Actions CI workflow and issue-linked pull-request workflow.
-   Docker Compose configuration for local containerized development.

## Tech Stack

  Area                     Technology
  ------------------------ -------------------------------------
  Language                 Java 21
  Backend                  Spring Boot, Spring Web
  Persistence              Spring Data JPA, Hibernate
  Database                 MySQL
  Security                 Spring Security, JWT
  Validation               Jakarta Bean Validation
  Build                    Maven Wrapper
  API regression testing   PowerShell script and HTTP requests
  CI                       GitHub Actions
  IDE                      IntelliJ IDEA
  Containers               Docker Compose

> Update the Spring Boot badge from `3.x` to the exact version used in
> the project if you want the badge to state a precise version. The CI
> badge assumes the workflow file is `.github/workflows/ci.yml`; update
> its URL if your workflow filename differs.

## Architecture

``` text
Client
  |
  v
Spring Security / JWT authentication and authorization
  |
  v
REST Controllers
  |
  v
Service Layer
  |  - business validation
  |  - order status rules
  |  - transaction boundaries
  v
Spring Data JPA Repositories
  |
  v
MySQL
```

-   **Controllers** expose HTTP endpoints and accept request DTOs.
-   **DTOs** define request and response shapes without exposing
    persistence entities directly.
-   **Services** implement reservation, order creation, status-update,
    and cancellation rules.
-   **Repositories** access persisted data through Spring Data JPA.
-   **Transactions** are used for operations that require consistent
    database updates.
-   **Spring Security** controls access to customer and administrator
    operations.

## Reservation Workflow

The API regression suite covers these reservation scenarios:

  -----------------------------------------------------------------------
  Scenario                                              Expected response
  ------------------------------ ----------------------------------------
  Valid reservation                                         `201 Created`

  Overlapping reservation                                  `409 Conflict`

  Back-to-back reservation                                  `201 Created`

  Party size too large                                  `400 Bad Request`

  End time before start time                            `400 Bad Request`

  Past reservation date                                 `400 Bad Request`

  Unknown table                                           `404 Not Found`

  Missing authentication              `403 Forbidden` in the current test
                                                            configuration

  List own reservations                                          `200 OK`

  Cancel as non-owner                                     `403 Forbidden`

  Cancel as owner                                                `200 OK`

  Rebook a freed time slot                                  `201 Created`
  -----------------------------------------------------------------------

## Order Workflow

### Order Creation

Order lines are validated before the order is persisted. The total is
calculated using decimal arithmetic:

``` text
total = Σ(unit price × quantity)
```

`BigDecimal` is used for monetary values. The order and its items should
be persisted consistently within a transaction, with cascade behavior
configured for associated order items.

### Order Status Lifecycle

  Current status   Requested status   Result
  ---------------- ------------------ ----------
  `PLACED`         `PREPARING`        Allowed
  `PLACED`         `CANCELLED`        Allowed
  `PREPARING`      `SERVED`           Allowed
  `PREPARING`      `CANCELLED`        Allowed
  `PREPARING`      `PLACED`           Rejected
  `SERVED`         Any transition     Rejected
  `CANCELLED`      Any transition     Rejected

### Customer Cancellation

`POST /orders/{id}/cancel` checks that the order belongs to the
authenticated customer and is still `PLACED`. Attempts to cancel another
customer's order are rejected, and cancellation after the order has
moved to `PREPARING` is not allowed.

## API Overview

The following routes are covered by the current API tests. Confirm exact
mappings in the controllers if they change.

  -----------------------------------------------------------------------------
  Method                  Endpoint                      Purpose
  ----------------------- ----------------------------- -----------------------
  `GET`                   `/menu`                       Retrieve menu

  `POST`                  `/orders`                     Place an order

  `GET`                   `/orders`                     List the authenticated
                                                        customer's orders

  `PATCH`                 `/admin/orders/{id}/status`   Administrator updates
                                                        order status

  `POST`                  `/orders/{id}/cancel`         Customer cancels their
                                                        own eligible order
  -----------------------------------------------------------------------------

Reservation routes are implemented as well. Consult the reservation
controllers for the exact request mappings and payloads.

## Validation and HTTP Responses

  -----------------------------------------------------------------------
  Scenario                                                Expected status
  ------------------------------ ----------------------------------------
  Successful order creation                                 `201 Created`

  Empty order or quantity `0`                           `400 Bad Request`

  Missing menu item or unknown                            `404 Not Found`
  order                          

  Unavailable menu item                                    `409 Conflict`

  Invalid status transition                                `409 Conflict`

  Customer attempts admin status                          `403 Forbidden`
  update                         

  Customer cancels own eligible                                  `200 OK`
  order                          

  Customer attempts to cancel                             `404 Not Found`
  another customer's order       

  Customer cancels after status                            `409 Conflict`
  advances                       
  -----------------------------------------------------------------------

Authentication failures may be represented as `401` or `403` depending
on the Spring Security configuration. The current regression script
expects `403` for its tested missing-token cases.

## Testing and CI

### Latest API Regression Result

**37 passed · 0 failed** in the latest reported run.

Coverage includes menu access and validation, reservation conflict and
ownership rules, order creation and totals, invalid order lines, order
status transitions, admin authorization, and customer cancellation.

### GitHub Actions Evidence

The screenshot below is a historical snapshot of successful GitHub
Actions workflow runs for order-related pull requests and merges. It
complements, but does not replace, the live Actions page.

![GitHub Actions workflow runs for Cafe
Booking](docs/images/ci-actions-cafe.png)

Live workflow status: [GitHub
Actions](https://github.com/Satvik-09/CAFE-BOOKING/actions)

### Run API Regression Tests

Ensure the application is running and the test credentials and test data
expected by the script are configured. From the repository root in
IntelliJ Terminal on Windows:

``` powershell
Test-Path .\api-tests\run-tests.ps1
powershell -ExecutionPolicy Bypass -File .\api-tests\run-tests.ps1
```

The script must exist at that path. Review it before execution and never
commit real tokens or passwords.

### Run Java Tests

Run test classes in IntelliJ using the green run icon or the Maven tool
window. Alternatively, from the repository root on Windows:

``` powershell
.\mvnw.cmd test
```

### CI Badge

The CI badge at the top assumes the workflow file is
`.github/workflows/ci.yml`. If the workflow has another filename,
replace `ci.yml` in both the badge URL and link with the actual workflow
filename.

## Run Locally

### Prerequisites

-   Java 21
-   MySQL
-   Git
-   IntelliJ IDEA
-   Docker Desktop and Docker Compose, if using containers

### 1. Clone the Repository

``` bash
git clone https://github.com/Satvik-09/CAFE-BOOKING.git
cd CAFE-BOOKING
```

### 2. Configure MySQL

Configure the datasource and credentials in the appropriate Spring Boot
configuration file or profile. Do not commit real database passwords,
JWT signing keys, or production secrets. Prefer environment variables
for local and deployed environments.

### 3. Start the Application

Open the project in IntelliJ, allow dependencies to resolve, and run the
class annotated with `@SpringBootApplication`.

Alternatively, from the repository root on Windows:

``` powershell
.\mvnw.cmd spring-boot:run
```

On macOS/Linux:

``` bash
./mvnw spring-boot:run
```

The default Spring Boot port is typically `8080`, unless overridden by
configuration.

### 4. Docker Compose (If Configured)

Review `docker-compose.yml` and its environment variables before
starting containers:

``` bash
docker compose up --build
```

Stop containers without deleting persisted volumes:

``` bash
docker compose down
```

`docker compose down -v` deletes named volumes and stored database data.
Use it only when intentionally resetting local data.

## Development Workflow

Development uses issue-linked pull requests:

1.  Create or select a GitHub issue.
2.  Create a feature branch from the updated `main`.
3.  Implement the change and test it.
4.  Commit and push the branch.
5.  Link the issue in the pull request description using `Closes #123`.
6.  Merge after review and required CI checks pass.

## Current Limitations and Roadmap

The current scope focuses on reservation and order workflows. Further
work is planned before describing the system as production-ready:

-   Add and verify concurrency protection for simultaneous reservations
    and orders.
-   Add idempotency for order creation to protect against duplicate
    client retries.
-   Expand integration testing against MySQL.
-   Add structured logging, health checks, and operational diagnostics.
-   Consider Redis only for a specific use case such as rate limiting or
    menu caching, with clear expiration and invalidation behavior.
-   Add a deployed demo and API documentation if deployment is
    completed.

## Repository

[github.com/Satvik-09/CAFE-BOOKING](https://github.com/Satvik-09/CAFE-BOOKING)

## Author

**Satvik** · Java Backend Development
