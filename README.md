# Cafe Seating System

[![CI](https://github.com/Satvik-09/CAFE-BOOKING/actions/workflows/ci.yml/badge.svg)](https://github.com/Satvik-09/CAFE-BOOKING/actions/workflows/ci.yml)
A backend-focused cafe seating and reservation management system built with Java and Spring Boot. The project aims to simplify seat reservations, manage booking operations, and prevent conflicting bookings through reliable backend logic.

> **Status:** Under Development

## Tech Stack

- **Language:** Java 21
- **Backend Framework:** Spring Boot 4.1.1
- **Database:** MySQL
- **ORM / Persistence:** Spring Data JPA, Hibernate
- **Security:** Spring Security, JWT
- **Validation:** Jakarta Bean Validation
- **Build Tool:** Maven
- **Application Server:** Embedded Apache Tomcat
- **Development Environment:** IntelliJ IDEA

## Planned Features

- Seat and reservation management
- Create and manage cafe bookings
- Reservation cancellation
- RESTful API endpoints
- Authentication and authorization using JWT
- Request validation and error handling
- Database persistence using MySQL and JPA
- Reliable booking operations and conflict handling

## Project Goals

- Apply backend development principles using Java and Spring Boot.
- Design REST APIs for real-world reservation workflows.
- Understand database relationships and transaction management.
- Explore concurrency control for conflicting seat reservations.
- Build a maintainable backend with appropriate testing and documentation.

## Getting Started

### Prerequisites

- Java 21
- Maven
- MySQL Server
- IntelliJ IDEA or another Java IDE

### Setup

1. Clone the repository:

   ```bash
   git clone <your-repository-url>
   cd <your-project-folder>
   ```

2. Create a MySQL database for the application.

3. Configure the database URL, username, and password in `src/main/resources/application.properties`.

4. Run the application through your IDE or using Maven:

   ```bash
   mvn spring-boot:run
   ```

The application uses the default Spring Boot port `8080`, unless configured otherwise.

## Testing

JUnit testing and automated CI are planned as part of the project's development workflow.

## Future Improvements

- Unit and integration tests
- Docker-based application setup
- GitHub Actions CI pipeline
- Repository governance and contribution templates
- Application deployment and monitoring

## Project Status

This project is currently under development. Features, testing, and deployment will be updated as implementation progresses.
