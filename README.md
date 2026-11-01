# Cafe Booking API

![CI](https://github.com/Satvik-09/CAFE-BOOKING/actions/workflows/ci.yml/badge.svg)

A backend for a cafe: customers browse the menu, book tables and place orders, and staff move orders through a controlled lifecycle. The project is built to practise the problems real backends face: transactions, concurrency, idempotency and role-based security.

> **Status:** the core backend is complete (authentication, menu, reservations, orders, order lifecycle, transactions, double-booking prevention, idempotent requests). Packaging the app in Docker, caching, deployment and a front end are still planned. See [Roadmap](#roadmap).

## Highlights

- **JWT authentication** with `USER` and `ADMIN` roles, BCrypt password hashing and stateless sessions
- **No double bookings:** the table row is locked (`SELECT ... FOR UPDATE`) before the overlap check, proven by a test that fires 8 parallel requests for 10 rounds
- **Idempotent POSTs:** `POST /orders` and `POST /reservations` accept an `Idempotency-Key` header, so a retried request returns the original result instead of creating a duplicate
- **Order lifecycle as a state machine:** only legal status moves are possible, covered by a 16-case unit test
- **Transactional order placement:** an order and its lines are saved together or not at all, with tests for rollback rules and propagation
- **Ownership checks:** a customer cannot see or cancel someone else's order (the API answers 404, so it does not reveal that the order exists)

## Tech stack

| Area | Technology |
|---|---|
| Language and framework | Java 21, Spring Boot 4 |
| Persistence | Spring Data JPA, Hibernate, MySQL 8 |
| Security | Spring Security, JWT (jjwt), BCrypt |
| Build and tests | Maven, JUnit 5, Spring Boot Test |
| Local infrastructure | Docker Compose (MySQL) |
| CI | GitHub Actions |

## Architecture

```
Client
  -> Controller        HTTP, validation, status codes (no business logic)
  -> Service           business rules, transaction boundary (@Transactional)
  -> Repository        Spring Data JPA queries
  -> MySQL
```

- DTOs (Java records) are used for every request and response, and entities never leave the service layer.
- Each service method is one transaction, so lazy loading and dirty checking happen inside it.
- Security runs as a filter before the controllers: the JWT's role claim becomes a `ROLE_*` authority, and `/admin/**` requires `ROLE_ADMIN`.

### Database Schema

    USERS ||--o{ RESERVATIONS : makes
    USERS ||--o{ ORDERS : places
    CAFE_TABLES ||--o{ RESERVATIONS : "is booked in"
    ORDERS ||--|{ ORDER_ITEMS : contains
    MENU_ITEMS ||--o{ ORDER_ITEMS : "is ordered as"

    USERS {
        bigint id PK
        varchar username UK
        varchar password "BCrypt hash"
        varchar role "USER or ADMIN"
    }

    MENU_ITEMS {
        bigint id PK
        varchar name
        varchar category
        decimal price
    }

    CAFE_TABLES {
        bigint id PK
        int capacity
    }

    RESERVATIONS {
        bigint id PK
        bigint user_id FK
        bigint table_id FK
        date date
        time start_time
        time end_time
        int party_size
    }

    ORDERS {
        bigint id PK
        bigint user_id FK
        varchar status "PLACED, PREPARING, SERVED, CANCELLED"
        decimal total
        datetime created_at
    }

    ORDER_ITEMS {
        bigint id PK
        bigint order_id FK
        bigint menu_item_id FK
        int quantity
        decimal unit_price "price snapshot"
    }

    IDEMPOTENCY_RECORDS {
        bigint id PK
        varchar username
        varchar idempotency_key
        varchar endpoint
        varchar request_hash
        bigint resource_id
    }
```

## Features and status

| Feature | Status | Notes |
|---|---|---|
| Register and login (JWT) | Done | Roles `USER` and `ADMIN`; admins are promoted in the database, never through the API |
| Menu: list with pagination, admin CRUD | Done | Validation on every request |
| Table reservations | Done | Overlap validation, capacity check, cancel (owner only) |
| Orders | Done | Price snapshot per line, `BigDecimal` totals, cascade saves |
| Order lifecycle | Done | `PLACED -> PREPARING -> SERVED`, with `CANCELLED` allowed before `SERVED` |
| Customer cancel | Done | Only while `PLACED` |
| Transactions | Done | Demonstrated and tested (rollback rules, propagation, self-invocation) |
| Concurrency control | Done | Pessimistic row lock plus a parallel test |
| Idempotency keys | Done | Orders and reservations |
| Admin order list with filters | Planned | Open issue |
| App Dockerfile and full compose stack | Planned | |
| Redis menu cache | Planned | |
| AWS deployment and load test | Planned | |

## API reference

| Method | Path | Access | Description |
|---|---|---|---|
| POST | `/auth/register` | Public | Create an account |
| POST | `/auth/login` | Public | Returns a JWT as plain text |
| GET | `/menu` | Public | Paginated menu; optional `category`, `page`, `size`, `sort` |
| POST | `/admin/menu` | Admin | Create a menu item |
| PUT | `/admin/menu/{id}` | Admin | Update a menu item |
| DELETE | `/admin/menu/{id}` | Admin | Delete a menu item |
| POST | `/reservations` | User | Book a table (supports `Idempotency-Key`) |
| GET | `/reservations/my` | User | List my reservations |
| DELETE | `/reservations/{id}` | Owner | Cancel a reservation |
| POST | `/orders` | User | Place an order (supports `Idempotency-Key`) |
| GET | `/orders/my` | User | List my orders |
| POST | `/orders/{id}/cancel` | Owner | Cancel my order while it is `PLACED` |
| PATCH | `/admin/orders/{id}/status` | Admin | Move an order to the next status |

Status codes used: `200` and `201` success, `400` invalid input, `403` not allowed, `404` not found (also used when the resource belongs to someone else), `409` conflict with current state (for example an illegal status move or a booked slot), `422` idempotency key reused with a different request.

### Example requests

```bash
# register and log in (the response body is the token)
curl -X POST http://localhost:8080/auth/register -H "Content-Type: application/json" \
     -d '{"username":"alice","password":"pass12345"}'
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login -H "Content-Type: application/json" \
     -d '{"username":"alice","password":"pass12345"}')

# book a table, safely retryable
curl -X POST http://localhost:8080/reservations \
     -H "Authorization: Bearer $TOKEN" -H "Idempotency-Key: $(uuidgen)" \
     -H "Content-Type: application/json" \
     -d '{"tableId":1,"date":"2027-01-15","startTime":"18:00","endTime":"19:00","partySize":2}'
```

## Design decisions

**State machine for order status.** The allowed moves live in `OrderStatus.canMoveTo`, an exhaustive `switch`. Adding a status later makes the compiler flag the method. The service answers an illegal move with `409`, because the request is valid but conflicts with the order's current state.

**Preventing double bookings.** Checking for an overlap and then saving is a check-then-act sequence, so two simultaneous requests can both pass the check. `create()` therefore locks the table row first (`@Lock(PESSIMISTIC_WRITE)`), then checks and saves. The lock must be the first read in the transaction: under MySQL's default `REPEATABLE READ`, an earlier plain read would fix a stale snapshot. A unique constraint cannot protect free-form time ranges, which is why a lock is used.

**Idempotency.** The client sends an `Idempotency-Key`. The server inserts a record with a unique key on `(username, key, endpoint)` in the same transaction as the work. A retry hits the unique key and returns the original resource, the same key with a different body returns `422`, and parallel duplicates are settled by the database (the second insert waits and then fails as a duplicate).

**Transactions.** Order placement is one transaction. The `TransactionRulesTest` class documents the rules with real rows: runtime exceptions roll back, checked exceptions do not unless `rollbackFor` is set, calling a method on `this` bypasses the proxy, and `REQUIRES_NEW` survives an outer rollback.

**Security.** Passwords use BCrypt. The JWT carries the role. Ownership is checked on every object access, and someone else's order answers `404` rather than `403`.

## Testing

| What | Proves | Command |
|---|---|---|
| `OrderStatusTest` | All 16 status transitions | `mvn test` |
| `TransactionRulesTest` | Rollback rules, propagation, self-invocation | `mvn test` |
| `DoubleBookingRaceTest` | 8 parallel bookings of one slot give exactly one booking | `mvn test` |
| `api-tests/run-tests.ps1` | End-to-end API checks (menu, reservations, orders, lifecycle, cancel, idempotency) | see below |

The Spring tests are integration tests and need the MySQL container to be running.

```bash
docker compose up -d
mvn test
powershell -ExecutionPolicy Bypass -File api-tests\run-tests.ps1   # app must be running
```

The script wipes `orders`, `order_items` and `reservations` each run, so use it only against a development database.

## Running locally

Requirements: Java 21, Maven, Docker Desktop.

```bash
git clone https://github.com/Satvik-09/CAFE-BOOKING.git
cd CAFE-BOOKING
docker compose up -d        # starts MySQL on port 3306
mvn spring-boot:run         # or run BookingApplication from your IDE
```

To create an admin, register a user and then run:

```sql
UPDATE users SET role = 'ADMIN' WHERE username = 'your-username';
```

## Configuration

| Setting | Where | Default (development only) |
|---|---|---|
| Database URL, user, password | `application.properties` | `jdbc:mysql://localhost:3306/cafe_db`, `root`, `rootpass` |
| JWT signing secret | `jwt.secret` (set the `JWT_SECRET` environment variable) | A development placeholder, never use it in production |

## Project structure

```
src/main/java/com/cafe/booking
  Filter/                  JWT filter
  Service/                 business logic and transaction boundaries
  controller/              REST controllers
  dto/                     request and response records
  entity/                  JPA entities (and enums)
  exception/               global exception handling
  repository/              Spring Data repositories
  securityConfiguration/   Spring Security setup
  util/                    JWT utilities
src/test/java/...          unit, transaction and concurrency tests
api-tests/                 end-to-end API script
docker-compose.yml         MySQL for local development
.github/workflows/ci.yml   continuous integration
```

## Known limitations

- Idempotency records are not purged yet (they should expire after about 24 hours)
- A replayed idempotent request returns the resource's current state, not a byte-for-byte copy of the first response
- Two admins changing the same order's status at the same moment can overwrite each other (an optimistic-locking `@Version` on `Order` would fix it)
- No rate limiting, refresh tokens or token revocation yet
- No front end; the database and JWT secret defaults are for local development only

## Roadmap

- [x] Authentication and roles
- [x] Menu, reservations, orders
- [x] Order lifecycle and customer cancel
- [x] Transactions, concurrency control, idempotency
- [x] README ER diagram and screenshots
- [ ] Security hardening (sanitised errors, input limits, rate limiting)
- [ ] App Dockerfile and a one-command compose stack
- [ ] Admin order list with filters and pagination
- [ ] Redis menu cache
- [ ] AWS deployment and load test
- [ ] Ideas: QR code per table, kitchen dashboard, OTP for takeaway orders

## Git workflow

Every change goes through an issue, a branch, a pull request with a green CI check, and a squash merge. Commits follow `type(scope): summary (#issue)`.
