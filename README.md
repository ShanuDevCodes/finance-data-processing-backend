# Finance Data Processing and Access Control Backend

A production-grade backend for a multi-role finance dashboard, built with **Spring Boot 4**, **PostgreSQL**, and **stateless JWT authentication**. The system handles financial record management, strict role-based access control, and aggregated analytics — designed so that a real frontend team could pick it up and integrate without ambiguity.

**Live API (Swagger UI):** *(URL will be updated after deployment)*

**Local API Docs (Swagger UI):** `http://localhost:8081/swagger-ui/index.html#/`

> All endpoints, request bodies, and response shapes are documented interactively in Swagger. No Postman collection is required to explore the API.

---

## How This Matches the Assignment

| Assignment Requirement | How It Is Implemented |
| :--- | :--- |
| User and Role Management | `UserController` + `UserService` — create users, assign roles, toggle ACTIVE/INACTIVE status, manage Analyst↔Viewer hierarchy |
| Financial Records Management | `RecordController` → `RecordService` — full CRUD with soft delete, pagination, and multi-field filtering |
| Dashboard Summary APIs | `DashboardController` → `RecordAnalyticsService` — 7-field aggregated response (income, expense, net balance, category/type/status breakdowns, recent transactions) |
| Access Control Logic | `@PreAuthorize` annotations on all endpoints + dynamic data scoping at the query level (not the service level) |
| Validation and Error Handling | Jakarta Bean Validation + custom `@ValidEnum` + `GlobalExceptionHandler` mapping every exception type to a consistent JSON response |
| Data Persistence | PostgreSQL via Spring Data JPA — Viewer data never deleted, only soft-flagged |
| **Optional: Authentication** | Stateless JWT — access tokens (15 min) + refresh tokens (30 days) with SHA-256 hashing and rotation |
| **Optional: Pagination** | `Pageable` support on `/records` and `/users` |
| **Optional: Soft Delete** | `isDeleted` flag — records are flagged, never physically removed |
| **Optional: Rate Limiting** | Bucket4j Token Bucket — 100 requests/min per IP, returns `429 Too Many Requests` with JSON body |
| **Optional: Unit Tests** | 13 tests across 6 test classes — pure Mockito, no Spring context, no database required to run |
| **Optional: API Documentation** | SpringDoc OpenAPI (Swagger UI) at `/swagger-ui/index.html#/` |

---

## What Makes This System Interesting

Standard CRUD is the easy part. The deliberate engineering decisions are:

1. **Dynamic data scoping at the query level.** Every DB query receives a pre-resolved `List<UUID>` representing the exact user IDs the caller is permitted to see. There are no in-memory filters. Analysts literally cannot receive a record that is outside their assigned scope, not because of an if-else check after the query, but because the query itself only asks for the right records.

2. **Frontend decoupling via metadata APIs.** The `/config/enums` endpoint returns all valid enum values for dropdowns. The `/auth/me/capabilities` endpoint returns what the currently authenticated user is *allowed to do* so the frontend can render the correct UI without embedding role logic in client code.

3. **Refresh token security.** Refresh tokens are stored as SHA-256 hashes. The raw token is never persisted. On use, the old token is deleted before issuing a new pair — preventing replay after interception.

4. **Single source of truth for public paths.** `PublicPaths.java` is read by both `SecurityConfig` and `JWTAuthFilter`, so there is exactly one place where unauthenticated endpoints are defined. Configuration drift is structurally impossible.

---

## Architecture

The project uses **Package-by-Feature** (also called Vertical Slice Architecture). Each domain area owns its complete stack — no horizontal `controllers/services/repositories` folders that force you to jump across the codebase:

```
src/main/java/com/shanudevcodes/fdpacb/
 ├── common/                          # Shared: ApiResponse, GlobalExceptionHandler, validators
 ├── features/
 │    ├── auth/                       # JWT auth, signup, login, refresh, capabilities
 │    ├── config/                     # Public metadata endpoint (enums for frontend dropdowns)
 │    ├── records/                    # Financial records CRUD, filters, dashboard analytics
 │    └── users/                      # User management, profile updates, analyst assignments
 └── security/                        # JWTAuthFilter, SecurityConfig, RateLimitFilter, PublicPaths
```

---

## Getting Started

### Prerequisites

- **Java 17** (or 21)
- **PostgreSQL** — local instance or a hosted database (tested against Supabase)
- **Gradle** — the wrapper `./gradlew` is included, no global installation needed

### Environment Variables

Set these four environment variables before starting the application. The app will fail to start without them.

| Variable | Description | Example |
| :--- | :--- | :--- |
| `DB_URL` | Full JDBC connection string | `jdbc:postgresql://localhost:5432/fdpacb` |
| `DB_USERNAME` | Database user | `postgres` |
| `DB_PASSWORD` | Database password | `yourpassword` |
| `JWT_SECRET_BASE64` | Base64-encoded HMAC-SHA256 signing key (minimum 32 bytes after decoding) | See below |

Generate a valid JWT secret:
```bash
openssl rand -base64 32
```

### Running Locally

```bash
./gradlew bootRun
```

The server starts on port **8081**. Hibernate is configured with `ddl-auto=update` — the schema is created or updated automatically on first run. No migration script is needed.

Open Swagger UI: **`http://localhost:8081/swagger-ui/index.html#/`**

---

## Deployment (Render)

The project includes a multi-stage `Dockerfile` optimised for Render's Docker-based deployment.

### Steps to Deploy on Render

1. **Push this repository to GitHub** (or GitLab).

2. **Go to [render.com](https://render.com)** → New → Web Service → connect your repository.

3. **Select "Docker"** as the runtime (Render auto-detects the `Dockerfile`).

4. **Set the following Environment Variables** in the Render dashboard (under the "Environment" tab):

   | Key | Value |
   | :--- | :--- |
   | `DB_URL` | Your Supabase / PostgreSQL JDBC URL |
   | `DB_USERNAME` | Your DB username |
   | `DB_PASSWORD` | Your DB password |
   | `JWT_SECRET_BASE64` | Output of `openssl rand -base64 32` |

5. **Deploy.** Render injects a `$PORT` environment variable automatically — the Dockerfile reads it via `-Dserver.port=${PORT:-8081}`.

6. Once deployed, your Swagger UI will be available at:
   ```
   https://<your-render-subdomain>.onrender.com/swagger-ui/index.html#/
   ```

### Building the Docker Image Locally

```bash
# Build the image
docker build -t fdpacb .

# Run it (pass your env vars inline)
docker run -p 8081:8081 \
  -e DB_URL=jdbc:postgresql://host:5432/db \
  -e DB_USERNAME=user \
  -e DB_PASSWORD=password \
  -e JWT_SECRET_BASE64=your_base64_secret \
  fdpacb
```

---

## API Reference

Every endpoint returns the same JSON envelope:

```json
{
  "status": "success | error",
  "message": "Human readable description",
  "data": {}
}
```

### Authentication — `/api/v1/auth`

All authentication endpoints are public (no token required).

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/v1/auth/signup` | Register a new user. All self-registered users receive the `VIEWER` role. |
| `POST` | `/api/v1/auth/login` | Authenticate. Returns an access token (15 min TTL) and a refresh token (30 day TTL). |
| `POST` | `/api/v1/auth/refresh` | Exchange a valid refresh token for a new token pair. The used token is invalidated immediately. |
| `GET` | `/api/v1/auth/me/capabilities` | Returns what the authenticated user is allowed to do — for use by a frontend to render the correct UI. |

**Capabilities response example (Analyst):**
```json
{
  "canCreateRecords": false,
  "canManageUsers": false,
  "canFilterByUsers": true,
  "allowedFilters": ["date", "category", "type", "target_user_id"]
}
```

---

### Financial Records — `/api/v1/records`

Write operations are restricted to `ADMIN`. Read operations are available to `ADMIN` and `ANALYST`.

| Method | Endpoint | Role | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/records/{userId}` | ADMIN | Create a financial record for a specific user. |
| `PUT` | `/api/v1/records/{recordId}` | ADMIN | Update an existing record's fields. |
| `DELETE` | `/api/v1/records/{recordId}` | ADMIN | Soft-delete a record (`isDeleted = true`). Never physically removed. |
| `GET` | `/api/v1/records` | ADMIN, ANALYST | Paginated records with optional filters. Analyst results are automatically scoped to assigned Viewers. |

**GET `/api/v1/records` query parameters:**

| Parameter | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `page` | `int` | `0` | Page index (0-based) |
| `size` | `int` | `10` | Records per page |
| `type` | `String` | — | `INCOME` or `EXPENSE` |
| `category` | `String` | — | `SALARY`, `FOOD`, `RENT`, etc. |
| `assigned_userid` | `UUID[]` | — | Filter by specific user IDs (Analyst: must be assigned; Admin: unrestricted) |

---

### Dashboard Analytics — `/api/v1/dashboard`

| Method | Endpoint | Role | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/dashboard` | ALL | Returns financial summary scoped to the caller's permitted data. |

**Optional query parameter:** `target_user_id` (one or more UUIDs) — narrows the summary to specific users. Analysts are blocked from querying users not assigned to them (`403`).

**Response:**
```json
{
  "totalIncome": 85000.00,
  "totalExpense": 34200.50,
  "netBalance": 50799.50,
  "categoryBreakdown": { "SALARY": 85000.00, "FOOD": 12000.00 },
  "typeBreakdown": { "INCOME": 85000.00, "EXPENSE": 34200.50 },
  "statusBreakdown": { "PENDING": 10000.00, "COMPLETED": 75000.00 },
  "recentTransactions": []
}
```

---

### User Management — `/api/v1/users`

| Method | Endpoint | Role | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/users` | ADMIN | Paginated user list. Filterable by `role`, `status`, and `analystId`. |
| `PUT` | `/api/v1/users/{id}/roles` | ADMIN | Replace a user's roles. |
| `PUT` | `/api/v1/users/{id}/status` | ADMIN | Toggle a user between `ACTIVE` and `INACTIVE`. |
| `PUT` | `/api/v1/users/{userId}/assign/{analystId}` | ADMIN | Assign a Viewer user to an Analyst. |
| `GET` | `/api/v1/users/assigned` | ANALYST | Returns the list of Viewers assigned to the currently authenticated Analyst. |
| `PUT` | `/api/v1/users/name` | ALL | Update own display name. |
| `PUT` | `/api/v1/users/email` | ALL | Update own email. Uniqueness is enforced. |
| `PUT` | `/api/v1/users/password` | ALL | Update own password. Old = new is rejected. |

---

### Config Metadata — `/api/v1/config`

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/config/enums` | None (public) | Returns all valid enum values for frontend dropdowns (roles, record types, categories, statuses). |

---

## Access Control Design

### Role Matrix

| Role | Record Access | Dashboard | User Management |
| :--- | :--- | :--- | :--- |
| **VIEWER** | Own records only | Own data only | None |
| **ANALYST** | Assigned Viewers + self | Assigned Viewers + self | Read own assigned Viewers |
| **ADMIN** | All records | All data | Full management |

### How Data Scoping Works

Access control goes further than "does this role have permission to call this endpoint." The system enforces *which records* each user can actually see.

Before any database query executes, the caller's permitted user IDs are resolved to a `List<UUID>`. This list is passed directly into a JPQL `IN (:userIds)` clause. No records outside that list can ever be returned — not because of a post-query filter, but because the query structurally cannot fetch them.

When an Analyst passes `target_user_id` values, those IDs are validated against the Analyst's assigned Viewers. Any ID that does not belong to that Analyst is silently stripped from the query — it does not cause a `403`, it simply disappears from the result set. (When *all* IDs are invalid, an empty result is returned.)

This means the data boundary enforcement is centralized in one place (`RecordService` and `RecordAnalyticsService`). Changing the scoping rules requires changing exactly one method.

### Analyst–Viewer Relationship

Viewers are linked to Analysts via a `@ManyToOne` JPA relationship (`assigned_analyst_id` FK in the `users` table). This is a first-class database relationship, not a raw UUID field, which means relationship integrity is enforced by the database and Hibernate can traverse it in JPQL queries: `WHERE u.assignedAnalyst.id = :analystId`.

---

## Data Model

### Financial Record (`records` table)

| Field | Type | Notes |
| :--- | :--- | :--- |
| `id` | UUID | Auto-generated |
| `amount` | BigDecimal | 15 digits, 2 decimal places |
| `type` | Enum | `INCOME` or `EXPENSE` |
| `category` | Enum | Validated against type (see below) |
| `paymentMethod` | Enum | `CASH`, `CARD`, `UPI`, etc. |
| `transactionDate` | LocalDate | Defaults to today if not provided |
| `status` | Enum | Defaults to `PENDING` |
| `currency` | String | Defaults to `INR` |
| `isRecurring` | boolean | Flags recurring transactions |
| `isDeleted` | boolean | Soft delete flag (never truly deleted) |
| `userId` | UUID (FK) | The user this record belongs to |

**Category-Type constraint:** The service layer validates that the category is consistent with the type before persisting. For example, `SALARY` is only valid as an `INCOME` record, and `FOOD` is only valid as an `EXPENSE` record. A mismatch returns a `400 Bad Request` before touching the database. This prevents silent data corruption in analytics.

**Composite indexes** on `(user_id, transactionDate)`, `(user_id, type)`, and `(user_id, category)` ensure that the most common query patterns (filtering a user's records by date, type, or category) stay efficient at scale.

---

## Security Implementation

### JWT Authentication

- **Access tokens** — 15-minute TTL. Carry `userId`, `email`, and `roles` as claims.
- **Refresh tokens** — 30-day TTL. Stored in the database as SHA-256 hashes. The raw token is never persisted.
- **Rotation** — on every refresh call, the used token hash is deleted from the database and a new pair is issued. A stolen token used after the legitimate owner has refreshed will be rejected.
- **Inactive users** — if an Admin deactivates a user (`status = INACTIVE`), the user is rejected by Spring Security's `isAccountNonLocked()` check on every subsequent request, even if they hold a valid access token.

### Rate Limiting

A `RateLimitFilter` (Bucket4j Token Bucket algorithm) protects all API endpoints:

- **Limit:** 100 requests per minute per IP address
- **Exceeded:** `HTTP 429 Too Many Requests` with JSON body
- **Excluded:** Swagger UI and OpenAPI spec paths are exempt

---

## Unit Tests

Tests run without a Spring context and without a database — pure Mockito unit tests that execute in milliseconds.

```bash
./gradlew test
```

| Test Class | Tests | What Is Verified |
| :--- | :---: | :--- |
| `RecordServiceTest` | 2 | **Analyst data scoping security** — proves that when an Analyst queries records, only their assigned Viewers' IDs reach the database query, and injecting an unassigned UUID is silently stripped |
| `JWTServiceTest` | 2 | Token claims (`subject = userId`, `email`, `roles`, `type = ACCESS/REFRESH`) |
| `AuthControllerTest` | 4 | All 4 auth endpoints delegate correctly to `AuthService` |
| `RecordControllerTest` | 3 | create, getAll, delete — correct service delegation and HTTP status codes |
| `DashboardControllerTest` | 1 | getDashboard — correct service delegation and response wrapping |
| `UserControllerTest` | 5 | All 5 user management endpoints delegate correctly to `UserService` |
| `ConfigControllerTest` | 1 | getEnums — correct metadata envelope returned |

**Total: 18 tests**

The `RecordServiceTest` is the most security-critical. It verifies that an Analyst requesting a Viewer's data they are not assigned to will have that ID stripped from the database query — the scoping layer cannot be bypassed.

---

## Validation and Error Handling

Input is validated at the HTTP layer before reaching the service layer:

- `@NotNull`, `@DecimalMin`, `@Size`, `@Email` — standard Jakarta Bean Validation
- `@ValidEnum` — custom constraint that validates string inputs against enum constants (rejects `"FOOOD"` or `"income"` at the boundary layer)

All exceptions are handled by `GlobalExceptionHandler` (`@RestControllerAdvice`):

| Exception | HTTP Status |
| :--- | :--- |
| `MethodArgumentNotValidException` | `400 Bad Request` |
| `ResponseStatusException` | Status set on the exception |
| `AccessDeniedException` | `403 Forbidden` |
| `DataAccessException` | `500 Internal Server Error` |
| Unhandled `Exception` | `500 Internal Server Error` |

Spring Security's authentication and authorization failures also return the same JSON format via `JwtAuthenticationEntryPoint` and `JwtAccessDeniedHandler`.

---

## Assumptions and Tradeoffs

**Viewers cannot browse raw records in a list view.** `GET /records` is restricted to Analyst and Admin. Viewers access their data through the Dashboard endpoint, which includes recent transactions. This fits the "dashboard consumer" framing from the assignment — Viewers consume summaries, not raw paginated data.

**Admins create records, not users themselves.** Financial records are admin-managed entries, not user-submitted transactions. This matches a realistic scenario where data originates from an external source and Admins are responsible for importing it.

**Soft delete only.** Records are never physically removed. Financial audit trails must remain intact — this is standard in any real finance system.

**Refresh tokens are per-session, not per-device.** A new login always issues a new refresh token. Multiple concurrent sessions work, but the refresh operation only invalidates the specific token used, not all sessions for that user. Full multi-device session management was considered out of scope.

**Schema managed by Hibernate.** `ddl-auto=update` handles schema evolution automatically. In a production system, this would be Flyway or Liquibase. For this assignment, it removes friction from the setup process completely.

---

## Tech Stack

| Layer | Technology |
| :--- | :--- |
| Runtime | Java 17 |
| Framework | Spring Boot 4.0.5 |
| Security | Spring Security 7, JWT (jjwt 0.11.5) |
| Persistence | Spring Data JPA, Hibernate 7, PostgreSQL |
| Rate Limiting | Bucket4j (Token Bucket) |
| Validation | Jakarta Bean Validation, Hibernate Validator |
| API Docs | SpringDoc OpenAPI 2 (Swagger UI) |
| Testing | JUnit 5, Mockito |
| Utilities | Lombok |
| Build | Gradle 8 (Kotlin DSL) |
