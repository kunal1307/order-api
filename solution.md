# Solution Overview

This document explains how the assignment requirements were interpreted and how the solution was implemented.

---

## 1. Problem Statement (Understanding)

The assignment required building an Order API with the following core responsibilities:

- Create an order for a product using a customer email
- Validate the customer against an external user system (ReqRes API)
- Prevent duplicate orders for the same email and product
- Persist orders in a relational database
- Expose REST endpoints for creating and retrieving orders
- Provide meaningful HTTP responses and error handling
- Ensure the solution is testable and production-ready

---

## 2. Contract-First Approach

The API was designed using a Contract-First approach.

An OpenAPI 3.0 specification (`openapi.yaml`) was created upfront to define:
- Endpoints and HTTP methods
- Request and response schemas
- Validation constraints
- Error responses and status codes

The OpenAPI contract serves as the single source of truth and is:
- Used to generate API interfaces and DTOs via OpenAPI Generator
- Exposed through Swagger UI for interactive documentation
- Used as a reference for implementation and testing


## 3. Architecture Overview

The application is implemented as a Spring Boot REST service with a layered architecture:

- **Controller layer**  
  Exposes HTTP endpoints and handles request/response mapping

- **Service layer**  
  Contains business logic, validation, and orchestration

- **Persistence layer (JPA + PostgreSQL)**  
  Stores orders and enforces data integrity

- **Integration layer**  
  Communicates with the external ReqRes user API

- **Infrastructure tooling**
    - Flyway for database migrations
    - Docker / Testcontainers for environment parity
    - WireMock for external API simulation in tests

---

## 4. Order Creation Flow

1. Client sends a `POST /api/orders` request
2. Request is validated (email format, required fields)
3. A fast pre-check verifies if the order already exists
4. The external ReqRes API is queried to validate the email
5. Order is persisted in PostgreSQL
6. A unique database constraint guarantees idempotency
7. Proper HTTP status codes are returned

Duplicate protection is implemented in two layers:

- **Application-level pre-check**  
  Provides fast feedback and avoids unnecessary external API calls

- **Database-level unique constraint**  
  Acts as the final authority and guarantees correctness under concurrency

The database constraint is intentionally relied upon as the source of truth.
The application-level check is an optimization, not a guarantee.


---

## 5. Database Design

- Orders are stored in a single `orders` table
- A **composite unique constraint** on `(email, product_id)` prevents duplicates
- `UUID` is used as the primary key
- Timestamps are generated automatically

Schema evolution is managed using **Flyway migrations**, ensuring:
- Versioned changes
- Repeatable and safe database initialization
- Identical behavior across local, test, and containerized environments

---

## 6. External Integration (ReqRes)

- The ReqRes API is accessed using Spring `WebClient`
- Pagination is handled transparently
- Failures are translated into domain-specific exceptions
- Timeouts are explicitly configured to avoid hanging requests

The service does not assume ReqRes reliability and fails gracefully when unavailable.

---

## 7. Error Handling Strategy

All exceptions are mapped centrally using `@RestControllerAdvice`:

- Validation errors -> `400 Bad Request`
- Missing external user -> `422 Unprocessable Entity`
- Duplicate order -> `409 Conflict`
- External service failure -> `502 Bad Gateway`
- Unexpected failures -> `500 Internal Server Error`

This keeps controllers clean and responses consistent.

---

## 8. Testing Strategy

### Integration Tests

The project uses **real integration tests**, not mocks:

- PostgreSQL is started using **Testcontainers**
- The application runs on a random port
- ReqRes API is simulated using **WireMock**
- Flyway migrations are executed against the test database

This setup verifies:
- HTTP layer
- Database constraints
- Transaction boundaries
- External integration behavior

### Why Testcontainers

- Eliminates "works on my machine" issues
- Matches production database behavior
- Ensures migrations and constraints are actually validated

---

## 9. Docker Support

The application can be run using Docker:

- Application container
- PostgreSQL container
- Environment-based configuration
- Flyway migrations run automatically on startup

This enables easy local setup and CI/CD readiness.

---

## 10. Key Design Decisions

- **Flyway over Hibernate DDL**  
  Chosen for explicit schema control and repeatable migrations

- **Database as the final source of truth**  
  Unique constraints enforce correctness under concurrency

- **WireMock instead of Mockito for integration tests** 
  Tests real HTTP behavior instead of mocked method calls

- **UUID identifiers**  
  Avoids sequencing issues and simplifies distributed use

---

## 11. Result

The final solution delivers:

- Clean separation of concerns
- Strong data consistency guarantees
- Realistic integration testing
- Production-ready database migrations
- Clear error handling and API behavior

The system behaves deterministically under failure, concurrency, and external dependency issues.

---

## 11. Possible Improvements

- Add pagination for `GET /api/orders`
- Introduce retry/backoff for external API calls
- Add observability (metrics, tracing)
- Introduce contract tests for external APIs

---

