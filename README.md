# Finance Data Processing and Access Control Backend

A backend system for a multi-role finance dashboard, built with Spring Boot 4 and PostgreSQL. The system handles financial record management, role-based access control, and aggregated analytics — designed with the intent that a real frontend team could pick this up and integrate against it without ambiguity.

Live API (Swagger UI): `{your-deployed-url}/swagger-ui/index.html#/`

---

## What This System Does

At its core, this is a backend for a finance dashboard where three types of users interact with financial data in very different ways. A **Viewer** sees only their own records. An **Analyst** oversees a set of assigned Viewers and can see aggregated or filtered data across that group. An **Admin** has unrestricted access and is the only role that can create, modify, or delete financial records and user accounts.

The interesting engineering challenge was not the CRUD layer — that part is straightforward. The interesting part was enforcing data boundaries dynamically at the query level, and making those boundaries extensible enough that a frontend could consume them without hardcoding role logic into the UI.

---

## Architecture

The project uses a **Package-by-Feature** structure, where each domain area (`auth`, `records`, `users`, `config`) is a self-contained vertical slice of the application rather than a horizontal layer across it. Each feature contains its own `data`, `domain`, and `presentation` sub-packages.

```
src/main/java/com/shanudevcodes/fdpacb/
 ├── common/                          # Shared utilities (ApiResponse, validators, error handler)
 ├── features/
 │    ├── auth/                       # JWT authentication, token refresh, capabilities
 │    ├── config/                     # Public metadata endpoints (enums for frontend)
 │    ├── records/                    # Financial records CRUD, filters, dashboard analytics
 │    └── users/                      # User management, profile updates, analyst assignments
 └── security/                        # JWT infrastructure, Spring Security config, RBAC roles
```

This structure was chosen over the traditional `controllers/services/repositories` layout because it makes the codebase grow without tangling. When you need to touch the records feature, every file you need is in one place. It is also the structure most large Spring Boot codebases migrate toward as they scale, so it felt like the right starting point.

---

## Getting Started

### Prerequisites

- Java 17
- A PostgreSQL database (local or hosted — the project is tested against Supabase)
- Gradle (the wrapper is included, so you do not need to install it globally)

### Environment Variables

The application reads all sensitive configuration from environment variables. You will need to set these before starting:

| Variable | Description | Example |
| :--- | :--- | :--- |
| `DB_URL` | Full JDBC connection string | `jdbc:postgresql://localhost:5432/fdpacb` |
| `DB_USERNAME` | Database user | `postgres` |
| `DB_PASSWORD` | Database password | `yourpassword` |
| `JWT_SECRET_BASE64` | Base64-encoded HMAC-SHA256 key (min 32 bytes) | `dGhpcyBpcyBhIHNlY3JldA==` |

To generate a valid JWT secret locally:
```bash
# On any system with openssl
openssl rand -base64 32
```

### Running the Application

```bash
./gradlew bootRun
```

The server starts on port `8080` by default. You can override this by setting a `PORT` environment variable.

Hibernate is configured with `ddl-auto=update`, so the schema will be created or updated automatically on first run. There is no migration script to run separately.

---

## API Reference

All endpoints return a consistent JSON envelope:

```json
{
  "status": "success | error",
  "message": "Human readable description",
  "data": { ... }
}
```

### Authentication

All auth endpoints are public (no token required).

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/v1/auth/signup` | Register a new user. All self-registered users are assigned the `VIEWER` role. |
| `POST` | `/api/v1/auth/login` | Authenticate and receive an access token (15 min) and refresh token (30 days). |
| `POST` | `/api/v1/auth/refresh` | Exchange a valid refresh token for a new token pair. Old refresh token is invalidated. |
| `GET` | `/api/v1/auth/me/capabilities` | Returns what the currently authenticated user is allowed to do in the UI. |

The capabilities endpoint was added specifically to enable the frontend to render the correct interface without embedding role logic in the client code. Example response for an Analyst:

```json
{
  "canCreateRecords": false,
  "canManageUsers": false,
  "canFilterByUsers": true,
  "allowedFilters": ["date", "category", "type", "target_user_id"]
}
```

### Financial Records

All record-writing operations are restricted to Admins. Reading records is available to Analysts and Admins.

| Method | Endpoint | Role | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/records/{userId}` | ADMIN | Create a financial record for a specified user. |
| `PUT` | `/api/v1/records/{recordId}` | ADMIN | Update an existing record. |
| `DELETE` | `/api/v1/records/{recordId}` | ADMIN | Soft-delete a record (sets `isDeleted=true`, never removed from DB). |
| `GET` | `/api/v1/records` | ADMIN, ANALYST | Retrieve records with pagination and optional filtering. |

The `GET /records` endpoint accepts the following query parameters:

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `page` | int | Page index (0-based, default 0) |
| `size` | int | Records per page (default 10) |
| `type` | String | Filter by `INCOME` or `EXPENSE` |
| `category` | String | Filter by category (e.g. `SALARY`, `FOOD`) |
| `assigned_userid` | UUID[] | Comma-separated list of user IDs to filter by (scoped per role) |

### Dashboard Analytics

| Method | Endpoint | Role | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/dashboard` | ALL | Returns aggregated financial summary for the caller's data scope. |

Optional query parameter: `target_user_id` (comma-separated UUID list) — narrows the dashboard to specific users. Access is enforced — an Analyst cannot query a user who is not assigned to them.

The dashboard response includes:

```json
{
  "totalIncome": 85000.00,
  "totalExpense": 34200.50,
  "netBalance": 50799.50,
  "categoryBreakdown": { "SALARY": 85000.00, "FOOD": 12000.00, ... },
  "typeBreakdown": { "INCOME": 85000.00, "EXPENSE": 34200.50 },
  "statusBreakdown": { "PENDING": 10000.00, "COMPLETED": 75000.00 },
  "recentTransactions": [ ... ]
}
```

### User Management

| Method | Endpoint | Role | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/users` | ADMIN | Paginated list of all users. |
| `PUT` | `/api/v1/users/{id}/roles` | ADMIN | Update the roles of any user. |
| `PUT` | `/api/v1/users/{id}/status` | ADMIN | Toggle user between ACTIVE and INACTIVE. |
| `PUT` | `/api/v1/users/{userId}/assign/{analystId}` | ADMIN | Assign a Viewer to an Analyst. |
| `GET` | `/api/v1/users/assigned` | ANALYST | Returns the list of Viewers assigned to the calling Analyst. |
| `PUT` | `/api/v1/users/name` | ALL | Update your own display name. |
| `PUT` | `/api/v1/users/email` | ALL | Update your own email (uniqueness is enforced). |
| `PUT` | `/api/v1/users/password` | ALL | Update your own password (old = new is rejected). |

### App Configuration

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/config/enums` | None | Returns all enum values (categories, types, roles, statuses) for frontend dropdowns. |

---

## Access Control Design

This is probably the most deliberate part of the system and worth explaining clearly.

### Role Definitions

Access in this system goes beyond simply checking "does this user have the right role to call this endpoint." The more interesting problem is: which data records is each user allowed to see?

| Role | Record Access | Dashboard | User Management |
| :--- | :--- | :--- | :--- |
| **VIEWER** | Own records only | Own data only | None |
| **ANALYST** | Assigned viewers + self | Assigned viewers + self | Read assigned viewers |
| **ADMIN** | All records | All data | Full management |

### Data Scoping

Rather than implementing data scoping at the controller or service level with if-else chains, the data boundaries are resolved to a `List<UUID>` before any database query runs. This list is then passed directly into the JPQL query as an `IN` clause.

For example, when an Analyst requests the dashboard with `target_user_id=uuid1,uuid2`, the backend first validates that both UUIDs belong to that Analyst's set of assigned Viewers. If even one UUID fails that check, the entire request is rejected with a `403`. If validation passes, the IDs are forwarded directly to the database — no filtering happens in memory.

This design means the data scoping logic is centralized in `RecordAnalyticsService` and `RecordService`. If the scoping rules ever change, there is exactly one place to update them.

### Analyst-Viewer Relationship

Viewers are assigned to Analysts via a proper `@ManyToOne` JPA relationship (`assigned_analyst_id` foreign key in the `users` table). This was deliberately chosen over storing a raw UUID field, because it allows Hibernate to handle relationship integrity and enables straightforward JPQL traversal like `WHERE u.assignedAnalyst.id = :analystId`.

---

## Data Model

### Financial Record

Each record belongs to one user and contains:

- `amount` — stored as `BigDecimal` with 15-digit precision and 2 decimal places
- `type` — `INCOME` or `EXPENSE`
- `category` — strongly typed and validated against type (a `SALARY` record must be `INCOME`, `FOOD` must be `EXPENSE`, etc.)
- `paymentMethod` — e.g. `CASH`, `CARD`, `UPI`
- `transactionDate` — defaults to the current date if not provided
- `status` — defaults to `PENDING` on creation
- `isRecurring` — flag for recurring transactions
- `currency` — defaults to `INR`
- `isDeleted` — soft delete flag

The category-to-type constraint is enforced at the service layer before persistence. If you try to create an `EXPENSE` record with a `SALARY` category, the request is rejected with a `400` before it reaches the database. This prevents data inconsistency that would otherwise silently corrupt analytics.

### Database Indexes

The `RecordsModel` table has composite indexes on `(user_id, transactionDate)`, `(user_id, type)`, and `(user_id, category)`. These make the most common query patterns — filtering a user's records by date or type — efficient even at larger data volumes.

---

## Authentication Implementation

Authentication uses stateless JWT with a distinct access/refresh token pair.

- **Access tokens** expire in 15 minutes and carry the user's ID, email, and roles.
- **Refresh tokens** expire in 30 days and are stored in the database as a SHA-256 hash (the raw token is never persisted). When a refresh is requested, the incoming token is hashed and compared against the stored hash. On success, the old token is deleted and a new pair is issued.

The refresh token rotation approach (delete old, issue new) prevents token reuse even if a token is intercepted after being used once. It is not a full refresh token rotation with replay detection, but it is meaningfully more secure than simply validating expiry.

The `JWTAuthFilter` validates the access token on every request, loads the full user entity from the database (to pick up any permission changes since the token was issued), and populates the Spring Security context. If a user is deactivated (`status = INACTIVE`), Spring Security rejects them at the `isAccountNonLocked()` check — no custom code required.

---

## Validation and Error Handling

Input validation uses Jakarta Bean Validation (`@NotNull`, `@DecimalMin`, custom `@ValidEnum`). The project includes a custom `EnumValidator` that validates string inputs against enum constants at the constraint level — so invalid values like `"FOOOD"` or `"income"` are caught before reaching the service layer.

All exceptions are handled by a single `@RestControllerAdvice` class (`GlobalExceptionHandler`) that maps common exception types to consistent JSON error responses:

| Exception Type | HTTP Status |
| :--- | :--- |
| `MethodArgumentNotValidException` | 400 Bad Request |
| `ResponseStatusException` | Matches the set status |
| `AccessDeniedException` | 403 Forbidden |
| `DataAccessException` | 500 Internal Server Error |
| `Exception` (fallback) | 500 Internal Server Error |

Spring Security's 401/403 responses are handled by `JwtAuthenticationEntryPoint` and `JwtAccessDeniedHandler` respectively, so those also return the same JSON format as everything else.

---

## Key Assumptions and Tradeoffs

**Viewers cannot read their own raw records in a list view.** The `GET /records` endpoint is restricted to Analysts and Admins. The reasoning is that the assignment described the Viewer as a "dashboard consumer," not a record browser. Viewers can access the dashboard (which includes recent transactions), which satisfies the spirit of read access without exposing the raw paginated record endpoint.

**Admin creates records, not individual users.** Financial records in this system are admin-managed entries. Users do not submit their own transactions. This fits a scenario where the finance data comes from an external system and Admins are responsible for importing or entering it.

**Soft delete is the only delete.** Records are never removed from the database. The `isDeleted` flag filters them out of all queries. This is intentional — financial audit trails should never be physically deleted in any real system.

**Refresh tokens are per-session, not per-device.** A new login always generates a new refresh token and stores it. Multiple active sessions are technically possible, but the refresh operation invalidates the specific token used, not all tokens for that user. Full multi-device session management was considered out of scope for this assignment.

**Schema is managed by Hibernate.** `spring.jpa.hibernate.ddl-auto=update` handles schema creation and evolution automatically. In a production system, this would be replaced by a proper migration tool like Flyway. For this assignment, it removes friction from the setup process.

---

## Tech Stack

| Layer | Technology |
| :--- | :--- |
| Runtime | Java 17 |
| Framework | Spring Boot 4.0.5 |
| Security | Spring Security 7, JWT (jjwt 0.11.5) |
| Persistence | Spring Data JPA, Hibernate 7, PostgreSQL |
| Validation | Jakarta Bean Validation, Hibernate Validator |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Utilities | Lombok |
| Build | Gradle (Kotlin DSL) |
