# 06 - Architecture

## 1. Purpose

This document defines the planned architecture for the **SRM Credit Engine**.

The architecture must support:

* financial calculation correctness;
* decimal-safe monetary operations;
* Strategy Pattern for receivable pricing;
* ACID settlement transactions;
* auditability;
* REST API clarity;
* database-level analytical queries;
* Angular frontend integration;
* maintainable code organization;
* testability;
* Docker-based local execution.

This specification must remain consistent with:

```text id="aei43d"
docs/specs/03-business-rules.md
docs/specs/04-api-contract.md
docs/specs/05-data-model.md
docs/adr/ADR-001-backend-stack.md
docs/adr/ADR-002-database-choice.md
docs/adr/ADR-003-money-precision.md
docs/adr/ADR-004-architecture-style.md
docs/adr/ADR-005-frontend-stack.md
```

## 2. Architectural Style

The system will use a **modular monolith with layered architecture**.

The backend will be organized into four main layers:

```text id="p6wv9q"
api
application
domain
infrastructure
```

The frontend will use a feature-oriented Angular architecture:

```text id="8ldzrl"
core
shared
features
models
```

The project intentionally avoids microservices, event-driven architecture and distributed infrastructure in the initial delivery.

## 3. High-Level System View

```text id="72zwhp"
+---------------------------+
|      Angular Frontend     |
|  Operator-facing SPA      |
+-------------+-------------+
              |
              | HTTP / JSON
              v
+---------------------------+
|     Spring Boot Backend   |
| REST API + Application    |
| Domain + Persistence      |
+-------------+-------------+
              |
              | JDBC
              v
+---------------------------+
|        MySQL 8.4 LTS      |
| Relational financial data |
+---------------------------+
```

## 4. Backend Architecture Overview

The backend is responsible for:

* official financial calculations;
* exchange-rate management;
* receivable pricing;
* settlement creation;
* transaction management;
* audit snapshot persistence;
* statement queries;
* API validation;
* structured error handling.

The backend must be the source of truth for all official pricing and settlement values.

## 5. Backend Layer Responsibilities

## 5.1 API Layer

### Purpose

Expose HTTP endpoints and translate HTTP concerns into application commands and queries.

### Responsibilities

The API layer may contain:

* REST controllers;
* request DTOs;
* response DTOs;
* validation annotations;
* OpenAPI annotations;
* HTTP status mapping;
* global exception handling;
* API-specific mappers.

### Must Not Contain

The API layer must not contain:

* pricing formula logic;
* spread selection logic;
* currency conversion logic;
* settlement transaction orchestration;
* direct SQL;
* direct business state mutation;
* financial rounding policy.

### Example Components

```text id="lay3pa"
ReferenceDataController
ExchangeRateController
PricingSimulationController
SettlementController
SettlementStatementController
GlobalExceptionHandler
ApiErrorResponse
```

### Recommended Package

```text id="k61jsm"
com.srm.creditengine.api
|-- controller
|-- request
|-- response
|-- error
`-- mapper
```

## 5.2 Application Layer

### Purpose

Represent use cases and orchestrate domain behavior, persistence access and transaction boundaries.

### Responsibilities

The application layer may contain:

* use case services;
* command/query objects;
* transaction boundaries;
* orchestration logic;
* application-level validation;
* coordination between domain and infrastructure;
* mapping between domain results and API-facing models.

### Must Not Contain

The application layer should not contain:

* HTTP-specific annotations;
* Angular/frontend concepts;
* low-level SQL details unless hidden behind a query/repository abstraction;
* hardcoded financial constants that belong to domain strategy or reference data;
* formatting logic for UI display.

### Example Components

```text id="ie4e76"
CreateExchangeRateService
GetLatestExchangeRateService
ListReferenceDataService
PricingSimulationService
CreateSettlementService
GetSettlementService
SettlementStatementService
```

### Recommended Package

```text id="5qjo1c"
com.srm.creditengine.application
|-- exchange
|-- pricing
|-- settlement
|-- statement
`-- reference
```

## 5.3 Domain Layer

### Purpose

Represent core business concepts, financial rules and domain behavior.

### Responsibilities

The domain layer may contain:

* entities;
* value objects;
* domain services;
* pricing strategies;
* business rules;
* domain exceptions;
* calculation results.

### Must Not Depend On

The domain layer must not depend on:

* REST controllers;
* HTTP DTOs;
* Angular concepts;
* JPA repositories;
* SQL;
* framework-specific application services.

### Example Components

```text id="usul79"
Money
Rate
Term
CurrencyCode
Receivable
ReceivableType
Settlement
SettlementItem
ExchangeRate
PricingStrategy
MercantileDuplicatePricingStrategy
PostDatedCheckPricingStrategy
PricingStrategyResolver
PricingEngine
CurrencyConversionService
```

### Recommended Package

```text id="v3cqrx"
com.srm.creditengine.domain
|-- currency
|-- exchange
|-- pricing
|-- receivable
|-- settlement
`-- shared
```

## 5.4 Infrastructure Layer

### Purpose

Implement technical details such as persistence, database queries, migrations and external integrations.

### Responsibilities

The infrastructure layer may contain:

* JPA entities;
* Spring Data repositories;
* repository adapters;
* read projections;
* native SQL queries;
* Flyway migrations;
* database configuration;
* external/mock integrations;
* technical configuration.

### Must Not Contain

The infrastructure layer should not contain core business policy.

It may enforce persistence constraints, but business meaning must remain in domain/application layers.

### Example Components

```text id="4he9lt"
JpaExchangeRateRepository
JpaReceivableRepository
JpaSettlementRepository
SettlementStatementQueryRepository
ReferenceDataRepository
PersistenceConfig
```

### Recommended Package

```text id="op28vy"
com.srm.creditengine.infrastructure
|-- persistence
|-- repository
|-- query
|-- config
`-- integration
```

## 6. Backend Dependency Direction

Preferred dependency direction:

```text id="og8xgq"
api -> application -> domain
application -> infrastructure
infrastructure -> domain
```

Simplified rule:

```text id="jd7q90"
Outer layers may depend on inner business concepts.
Inner business layers should not depend on outer technical layers.
```

The domain layer should remain testable without running Spring Boot.

## 7. Backend Package Structure

Recommended initial package structure:

```text id="9gyoyo"
backend/
`-- src/
    |-- main/
    |   |-- java/
    |   |   `-- com/srm/creditengine/
    |   |       |-- SrmCreditEngineApplication.java
    |   |       |-- api/
    |   |       |   |-- controller/
    |   |       |   |-- request/
    |   |       |   |-- response/
    |   |       |   |-- error/
    |   |       |   `-- mapper/
    |   |       |-- application/
    |   |       |   |-- exchange/
    |   |       |   |-- pricing/
    |   |       |   |-- settlement/
    |   |       |   |-- statement/
    |   |       |   `-- reference/
    |   |       |-- domain/
    |   |       |   |-- currency/
    |   |       |   |-- exchange/
    |   |       |   |-- pricing/
    |   |       |   |-- receivable/
    |   |       |   |-- settlement/
    |   |       |   `-- shared/
    |   |       `-- infrastructure/
    |   |           |-- persistence/
    |   |           |-- repository/
    |   |           |-- query/
    |   |           |-- config/
    |   |           `-- integration/
    |   `-- resources/
    |       |-- application.yml
    |       |-- application-local.yml
    |       `-- db/
    |           `-- migration/
    `-- test/
        `-- java/
            `-- com/srm/creditengine/
```

## 8. Backend Module Responsibilities

## 8.1 Reference Data Module

### Purpose

Expose supported currencies and receivable types to the frontend.

### API

```text id="eb4ln2"
GET /api/reference-data/currencies
GET /api/reference-data/receivable-types
```

### Responsibilities

* read supported currencies;
* read supported receivable types;
* expose reference spreads;
* avoid frontend hardcoding.

### Architecture Flow

```text id="ecffdy"
ReferenceDataController
        |
        v
ListReferenceDataService
        |
        v
ReferenceDataRepository
```

## 8.2 Exchange Rate Module

### Purpose

Manage exchange rates used in cross-currency pricing and settlement.

### API

```text id="ur4jjf"
POST /api/exchange-rates
GET  /api/exchange-rates/latest
```

### Responsibilities

* validate currency pairs;
* validate positive rates;
* persist manual exchange rates;
* retrieve latest rate for exact currency direction;
* prevent silent inversion of exchange-rate pairs.

### Architecture Flow

```text id="pgq1pk"
ExchangeRateController
        |
        v
CreateExchangeRateService / GetLatestExchangeRateService
        |
        v
ExchangeRateRepository
        |
        v
MySQL
```

## 8.3 Pricing Module

### Purpose

Calculate pricing simulation results and provide reusable pricing logic for settlement.

### API

```text id="7y621r"
POST /api/pricing/simulations
```

### Responsibilities

* validate pricing input;
* resolve the effective base rate;
* calculate term;
* resolve pricing strategy;
* calculate present value;
* calculate discount;
* apply exchange conversion when needed;
* return calculation result;
* avoid persistence for simulations.

### Architecture Flow

```text id="dkqx62"
PricingSimulationController
        |
        v
PricingSimulationService
        |
        v
PricingEngine
        |
        +--> PricingStrategyResolver
        |       |
        |       +--> MercantileDuplicatePricingStrategy
        |       +--> PostDatedCheckPricingStrategy
        |
        +--> CurrencyConversionService
        |
        v
PricingResult
```

### Critical Rule

The Pricing Module must not use floating-point types for financial calculations.
The effective base rate must be resolved using: request `baseRate`, then `DEFAULT_BASE_RATE`, otherwise fail with a structured error.

## 8.4 Settlement Module

### Purpose

Create auditable settlement batches atomically.

### API

```text id="qpedkm"
POST /api/settlements
GET  /api/settlements/{id}
```

### Responsibilities

* receive settlement batches;
* validate assignor;
* validate receivables;
* enforce one source currency per settlement batch;
* resolve the effective base rate;
* prevent duplicate settlement;
* calculate each item;
* persist settlement header;
* persist settlement item snapshots;
* update receivable status;
* rollback completely on failure.

### Architecture Flow

```text id="3h7wac"
SettlementController
        |
        v
CreateSettlementService @Transactional
        |
        +--> AssignorRepository
        +--> ReceivableRepository
        +--> ExchangeRateRepository
        +--> PricingEngine
        +--> SettlementRepository
        |
        v
SettlementResponse
```

### Transaction Boundary

The transaction boundary belongs to:

```text id="p5arm4"
CreateSettlementService
```

The controller must not manage transactions.

## 8.5 Settlement Statement Module

### Purpose

Provide analytical settlement history with filters and server-side pagination.

### API

```text id="xf8nsn"
GET /api/settlements/statement
```

### Responsibilities

* validate filters;
* apply date range;
* apply assignor filters;
* apply currency filters;
* apply receivable type filters;
* paginate in database;
* sort deterministically;
* return settlement-level summaries.

### Architecture Flow

```text id="70asx0"
SettlementStatementController
        |
        v
SettlementStatementService
        |
        v
SettlementStatementQueryRepository
        |
        v
SQL query / projection
        |
        v
Page<SettlementStatementRow>
```

### Reporting Exception

Reporting may use a simplified read path, but it must not bypass the application boundary.

Allowed:

```text id="p5u05h"
api -> application -> infrastructure query
```

For this project, simplification means a thin application read service with little or no domain orchestration.

Not allowed:

```text id="nq8rxw"
api -> infrastructure query
```

Reporting must still follow:

* backend validation;
* structured error handling;
* database-level filtering;
* server-side pagination;
* deterministic sorting.

## 9. Financial Calculation Architecture

## 9.1 Official Calculation Boundary

Official pricing and settlement calculations live in the backend.

The frontend must call backend endpoints.

The backend calculation components are:

```text id="pbtj9k"
PricingEngine
PricingStrategy
PricingStrategyResolver
CurrencyConversionService
FinancialMath
Money
Rate
Term
```

## 9.2 Value Objects

Recommended value objects:

```text id="kld4ql"
Money
Rate
Term
CurrencyCode
```

### Money

Represents amount and currency.

Expected invariants:

* amount is not null;
* currency is not null;
* amount uses `BigDecimal`;
* operations preserve currency consistency.

### Rate

Represents base rate, spread or exchange rate.

Expected invariants:

* value is not null;
* value uses `BigDecimal`;
* value is decimal, not percentage notation;
* value follows expected positivity rules.

### Term

Represents term in months.

Expected invariants:

* value is not null;
* value is greater than zero;
* value uses `BigDecimal`.

### CurrencyCode

Represents supported currency code.

Expected invariants:

* code is not blank;
* code is uppercase;
* code is supported.

## 9.3 Financial Math Component

A centralized financial math component should handle:

* scale;
* rounding;
* division;
* exponentiation strategy;
* monetary output rounding;
* rate normalization.

Recommended component:

```text id="g52tzt"
FinancialMath
```

Rules:

* do not scatter rounding logic across controllers and services;
* avoid hidden floating-point conversion;
* document any approximation if needed;
* test exponentiation behavior.

## 9.4 Strategy Pattern

Expected interface:

```text id="7ge2zp"
PricingStrategy
```

Expected methods may include:

```text id="5lz72i"
supports(receivableType)
spread()
```

or:

```text id="hw03da"
price(input)
```

Preferred design:

* `PricingEngine` owns the common present value calculation;
* strategies provide receivable-type risk behavior, especially spread;
* strategy resolver selects the correct implementation.

Expected implementations:

```text id="60dhad"
MercantileDuplicatePricingStrategy
PostDatedCheckPricingStrategy
```

## 10. Transaction Architecture

## 10.1 Transactional Use Cases

The following use cases modify state:

```text id="e95j1f"
CreateExchangeRateService
CreateSettlementService
```

`CreateSettlementService` is business-critical and must be transactional.

## 10.2 Settlement Transaction Steps

The settlement transaction must include:

```text id="ldmlsc"
1. Validate request shape.
2. Resolve or create assignor.
3. Validate batch size.
4. Validate one source currency per batch.
5. Resolve or create receivables.
6. Validate receivable availability.
7. Fetch exchange rate if cross-currency.
8. Calculate item pricing.
9. Create settlement header.
10. Create settlement item snapshots.
11. Update receivable statuses.
12. Commit.
```

If any step fails:

```text id="sh6d4w"
Rollback everything.
```

## 10.3 Transaction Annotation Placement

Expected placement:

```java id="b2mr3q"
@Transactional
public SettlementResult createSettlement(CreateSettlementCommand command) {
    ...
}
```

Not allowed:

```text id="zz280l"
Transaction boundary in controller.
```

## 10.4 Duplicate Settlement Protection

Duplicate settlement prevention must include:

```text id="2k1ktw"
1. Application validation.
2. Database unique constraint on settlement_items.receivable_id.
3. Optional optimistic locking on receivables.version.
```

## 10.5 Race Condition Handling

Potential race:

```text id="1zqxt2"
Two requests attempt to settle the same receivable concurrently.
```

Required mitigation:

* transactional use case;
* unique database constraint;
* structured handling of constraint violation;
* optional optimistic locking.

## 11. Data Architecture

## 11.1 Persistence Strategy

Use:

```text id="l3tuwm"
Spring Data JPA / Hibernate
```

for transactional write models.

Use:

```text id="nlmkd6"
SQL projections, query builders or native SQL
```

for analytical statement queries when more efficient.

## 11.2 Migration Strategy

Use Flyway migrations stored at:

```text id="ryj5fr"
backend/src/main/resources/db/migration
```

Expected initial migrations:

```text id="05jfze"
V1__create_initial_schema.sql
V2__seed_reference_data.sql
```

## 11.3 Schema Validation

Use:

```properties id="803j9m"
spring.jpa.hibernate.ddl-auto=validate
```

The application must validate against the schema created by Flyway.

## 11.4 Audit Snapshot Strategy

Settlement items store calculation snapshots.

Required snapshot fields include:

```text id="j8u7hr"
face_value
source_currency_code
payment_currency_code
base_rate
spread
term_in_months
present_value_source
discount_value
payment_value
exchange_rate
calculated_at
```

Historical statement and detail endpoints must use persisted values.

## 11.5 One Source Currency per Settlement

The application must enforce:

```text id="gubiei"
all receivables in the same settlement batch share the same source currency
```

This supports meaningful settlement header totals:

```text id="96n9ng"
total_face_value       in source_currency_code
total_present_value    in source_currency_code
total_payment_value    in payment_currency_code
```

## 12. API Architecture

## 12.1 Endpoint Groups

Expected API groups:

```text id="3w45hl"
/api/reference-data
/api/exchange-rates
/api/pricing
/api/settlements
```

## 12.2 API Layer Rules

Controllers must:

* expose endpoint mappings;
* validate request DTOs;
* call application services;
* map responses;
* document endpoints with OpenAPI annotations where useful.

Controllers must not:

* calculate financial values;
* choose pricing strategies;
* manage transactions;
* call low-level SQL directly;
* mutate domain state outside application services.

## 12.3 DTO Rules

Request DTOs and response DTOs must:

* represent API contracts;
* remain separate from domain objects;
* use `BigDecimal` for financial numeric fields;
* use ISO date/time types;
* use clear enum/string representations.

Recommended suffixes:

```text id="qdn5yq"
Request
Response
```

Examples:

```text id="pqorrm"
PricingSimulationRequest
PricingSimulationResponse
CreateSettlementRequest
SettlementResponse
ApiErrorResponse
```

## 12.4 Error Handling

The backend must provide global exception handling.

Expected component:

```text id="e4gc4o"
GlobalExceptionHandler
```

Expected error model:

```text id="xa52l4"
timestamp
status
error
message
path
details
```

Errors must not expose stack traces.

## 13. Frontend Architecture Overview

The frontend is an Angular SPA.

The frontend is responsible for:

* collecting operator input;
* calling backend APIs;
* displaying results;
* formatting values;
* showing validation and error states;
* triggering server-side pagination and filters.

The frontend is not responsible for official financial calculations.

## 14. Frontend Structure

Recommended structure:

```text id="gzwnoz"
frontend/
`-- src/
    `-- app/
        |-- core/
        |   |-- api/
        |   |-- config/
        |   |-- interceptors/
        |   `-- layout/
        |-- shared/
        |   |-- components/
        |   |-- pipes/
        |   |-- directives/
        |   `-- utils/
        |-- features/
        |   |-- pricing-simulation/
        |   |   |-- pages/
        |   |   |-- components/
        |   |   |-- services/
        |   |   `-- models/
        |   |-- settlements/
        |   |   |-- pages/
        |   |   |-- components/
        |   |   |-- services/
        |   |   `-- models/
        |   `-- exchange-rates/
        |       |-- pages/
        |       |-- components/
        |       |-- services/
        |       `-- models/
        |-- app.config.ts
        |-- app.routes.ts
        `-- app.component.ts
```

## 15. Frontend Feature Responsibilities

## 15.1 Pricing Simulation Feature

Responsibilities:

* render simulation form;
* validate basic input;
* call `POST /api/pricing/simulations`;
* display backend result;
* show loading state;
* show backend validation/business errors.

Must not:

* implement official pricing formula;
* hardcode spreads;
* hardcode exchange rates.

## 15.2 Settlements Feature

Responsibilities:

* render settlement statement grid;
* collect filters;
* call `GET /api/settlements/statement`;
* request new pages from backend;
* display settlement summaries;
* optionally navigate to settlement detail.

Must not:

* load all settlement rows;
* paginate locally;
* filter locally after loading all data.

## 15.3 Exchange Rates Feature

Responsibilities:

* render exchange-rate form;
* call `POST /api/exchange-rates`;
* optionally call `GET /api/exchange-rates/latest`;
* display errors and success states.

Must not:

* treat frontend validation as authoritative;
* inject fake exchange rates into pricing calculations.

## 16. Frontend State Management

Initial state management:

```text id="m4kux8"
Angular Signals + services
```

Use cases:

* loading state;
* error state;
* form-derived state;
* simulation result;
* statement filters;
* paginated statement response.

NgRx is not part of the initial architecture.

## 17. Frontend API Integration

All HTTP communication must go through services.

Recommended services:

```text id="h31xai"
ReferenceDataApiService
ExchangeRateApiService
PricingSimulationApiService
SettlementApiService
SettlementStatementApiService
```

API models must match `docs/specs/04-api-contract.md`.

## 18. Cross-Cutting Concerns

## 18.1 Validation

Backend validation is mandatory.

Frontend validation improves usability only.

Validation must cover:

```text id="ivfz2d"
required fields
positive monetary values
supported currencies
supported receivable types
future due dates
positive exchange rates
batch size
pagination limits
date range consistency
```

## 18.2 Error Handling

Backend:

* global exception handler;
* structured errors;
* no exposed stack traces.

Frontend:

* display field-level validation errors where possible;
* display business errors clearly;
* display fallback message for unexpected failures.

## 18.3 Logging

Backend should log:

* application startup;
* exchange-rate creation;
* settlement creation attempts;
* settlement failures;
* unexpected errors.

Logs must not expose secrets.

## 18.4 Configuration

Configuration must come from environment variables and profile-specific application config.

Expected variables:

```text id="xmt7fw"
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
CORS_ALLOWED_ORIGINS
DEFAULT_BASE_RATE (optional fallback)
ANGULAR_API_BASE_URL
```

Base-rate rule:

* `DEFAULT_BASE_RATE` is an optional server-side fallback;
* request `baseRate` takes precedence when provided;
* if neither request value nor configured fallback exists, the operation must fail with a structured error.

## 18.5 Security

Initial scope excludes authentication and authorization.

Still required:

* no secrets committed;
* explicit CORS configuration;
* backend validation;
* structured error handling;
* no stack traces in API responses;
* no raw SQL string concatenation with user input.

## 18.6 CORS

Allowed local frontend origin:

```text id="6nu9xo"
http://localhost:4200
```

CORS configuration must be environment-driven.

Avoid unrestricted wildcard CORS in final local delivery unless explicitly justified.

## 19. Docker Architecture

Expected Docker Compose services:

```text id="te3qll"
mysql
backend
frontend
```

## 19.1 MySQL Service

Responsibilities:

* run MySQL 8.4 LTS;
* expose port 3306 for local development;
* use persistent Docker volume;
* use UTF-8 compatible charset/collation;
* run in UTC.

## 19.2 Backend Service

Responsibilities:

* run Spring Boot application;
* connect to MySQL through Docker network;
* run Flyway migrations on startup;
* expose port 8080;
* serve Swagger/OpenAPI.

## 19.3 Frontend Service

Responsibilities:

* serve Angular production build;
* expose port 4200 mapped to container port 80;
* communicate with backend API through browser-accessible base URL.

## 20. Runtime View

## 20.1 Local Development

Backend:

```bash id="c0yexm"
cd backend
mvn spring-boot:run
```

Frontend:

```bash id="u179ta"
cd frontend
npm install
npm start
```

Database:

```bash id="h9vxsx"
docker compose up mysql
```

## 20.2 Full Local Stack

Expected final command:

```bash id="p2a1j6"
docker compose up --build
```

Expected URLs:

```text id="ldo2bn"
Frontend: http://localhost:4200
Backend:  http://localhost:8080
Swagger:  http://localhost:8080/swagger-ui.html
MySQL:    localhost:3306
```

## 21. Main Business Flows

## 21.1 Pricing Simulation Flow

```text id="oo9o2j"
Operator
  |
  v
Angular Pricing Simulation Screen
  |
  v
PricingSimulationApiService
  |
  v
POST /api/pricing/simulations
  |
  v
PricingSimulationController
  |
  v
PricingSimulationService
  |
  v
PricingEngine
  |
  +--> PricingStrategyResolver
  +--> CurrencyConversionService
  |
  v
PricingSimulationResponse
  |
  v
Angular Result Panel
```

Rules:

* no settlement persistence;
* backend calculates official result;
* frontend only displays result.

## 21.2 Exchange Rate Registration Flow

```text id="v9cjrk"
Operator
  |
  v
Angular Exchange Rate Form
  |
  v
POST /api/exchange-rates
  |
  v
ExchangeRateController
  |
  v
CreateExchangeRateService
  |
  v
ExchangeRateRepository
  |
  v
MySQL
```

Rules:

* rate must be positive;
* source and target currencies must differ;
* direction is explicit.

## 21.3 Settlement Creation Flow

```text id="3svl8l"
Operator
  |
  v
POST /api/settlements
  |
  v
SettlementController
  |
  v
CreateSettlementService @Transactional
  |
  +--> resolve/create assignor
  +--> resolve/create receivables
  +--> validate one source currency per batch
  +--> validate receivable availability
  +--> fetch exchange rate if needed
  +--> calculate pricing for each item
  +--> persist settlement
  +--> persist settlement items
  +--> update receivable statuses
  |
  v
SettlementResponse
```

Rules:

* all-or-nothing persistence;
* no partial settlement;
* calculation snapshots persisted;
* duplicate settlement prevented.

## 21.4 Statement Query Flow

```text id="f210as"
Operator
  |
  v
Angular Settlement Grid
  |
  v
GET /api/settlements/statement
  |
  v
SettlementStatementController
  |
  v
SettlementStatementService
  |
  v
SettlementStatementQueryRepository
  |
  v
Database-level filtering and pagination
  |
  v
Paginated Statement Response
```

Rules:

* filters are applied in database;
* pagination is server-side;
* frontend does not load all records.

## 22. Testing Architecture

## 22.1 Domain Unit Tests

Target:

```text id="kgy37g"
domain/
```

Cover:

* Money;
* Rate;
* Term;
* PricingStrategy;
* PricingStrategyResolver;
* PricingEngine;
* CurrencyConversionService.

## 22.2 Application Tests

Target:

```text id="w3hjvt"
application/
```

Cover:

* pricing simulation use case;
* settlement creation orchestration;
* missing exchange rate;
* invalid batch;
* duplicate settlement;
* rollback behavior.

## 22.3 Infrastructure Tests

Target:

```text id="22rqoq"
infrastructure/
```

Cover:

* Flyway migrations;
* repository behavior;
* unique constraints;
* statement query filtering;
* pagination.

## 22.4 API Tests

Target:

```text id="9d9jhq"
api/
```

Cover:

* request validation;
* status codes;
* structured error responses;
* request/response contracts.

## 22.5 Frontend Tests

Target:

```text id="37kp02"
frontend/
```

Cover:

* form validation;
* API service calls;
* grid pagination behavior;
* error display.

## 23. Observability

Initial delivery should include practical logs.

Minimum backend logging expectations:

```text id="v93o35"
application startup
exchange-rate creation
pricing simulation business failures
settlement creation success
settlement creation rollback/failure
unexpected exceptions
```

Optional senior-level additions:

```text id="m0x7ng"
structured JSON logs
basic metrics
request correlation ID
```

These additions are optional and must not compromise required delivery.

## 24. Performance Architecture

## 24.1 Statement Queries

Statement queries must:

* use database-level filtering;
* use indexes;
* use pagination;
* avoid in-memory filtering;
* return projection rows instead of full aggregate graphs where appropriate.

## 24.2 Pricing

Pricing is CPU-light for the challenge scope.

Priority is correctness over micro-optimization.

## 24.3 Batch Size

Initial maximum settlement batch size:

```text id="90m6gd"
100 items
```

Rationale:

* prevents accidental oversized requests;
* keeps transaction size reasonable;
* is sufficient to demonstrate batch behavior.

## 25. Scalability Considerations

The initial implementation is a modular monolith.

Future scaling options may include:

* read replicas for reporting;
* caching reference data;
* asynchronous settlement processing;
* outbox pattern;
* event-driven architecture;
* sharding by assignor or settlement date;
* separate read model for analytics.

These are future design considerations, not initial implementation requirements.

## 26. Architecture Constraints

The following constraints must not be violated:

```text id="ttvj29"
- Backend owns official financial calculations.
- Financial calculations use BigDecimal.
- Controllers remain thin.
- Settlement transaction boundary is in application service.
- Settlement item snapshots are persisted.
- Cross-currency conversion happens after present value calculation.
- Statement queries use database-level filtering.
- Frontend does not duplicate pricing formula.
- Flyway controls schema.
- Docker Compose runs the local stack.
```

## 27. AI-Assisted Development Architecture Rules

AI-generated code must be reviewed against this architecture.

Common AI mistakes to reject:

```text id="kmwcch"
- business logic in controllers
- double/float financial calculations
- frontend pricing formula implementation
- in-memory statement filtering
- missing transaction boundary
- missing settlement snapshot
- hardcoded exchange rates
- broad unrelated changes
```

Codex prompts must reference this file when generating implementation code for architecture-sensitive features.

## 28. Architecture Validation Checklist

The implementation is consistent with this architecture if:

```text id="tamw2v"
- Backend packages follow the layered organization.
- Controllers are thin.
- Application services orchestrate use cases.
- Domain contains pricing strategies and financial rules.
- Infrastructure contains persistence and query details.
- Settlement creation is transactional.
- Settlement item snapshots are persisted.
- Statement query is database-paginated.
- Angular frontend calls backend for simulation.
- Frontend grid uses server-side pagination.
- Docker Compose contains mysql, backend and frontend.
- Tests cover domain and application behavior.
```

## 29. Related Documents

```text id="ncty3b"
README.md
AGENTS.md
docs/specs/01-product-brief.md
docs/specs/02-domain-glossary.md
docs/specs/03-business-rules.md
docs/specs/04-api-contract.md
docs/specs/05-data-model.md
docs/specs/07-testing-strategy.md
docs/specs/08-acceptance-criteria.md
docs/adr/ADR-001-backend-stack.md
docs/adr/ADR-002-database-choice.md
docs/adr/ADR-003-money-precision.md
docs/adr/ADR-004-architecture-style.md
docs/adr/ADR-005-frontend-stack.md
docs/diagrams/c4-context.md
docs/diagrams/c4-container.md
```

## 30. Change Policy

When architecture changes, update:

```text id="t71zi4"
docs/specs/06-architecture.md
docs/adr/ADR-004-architecture-style.md
docs/diagrams/c4-context.md
docs/diagrams/c4-container.md
AGENTS.md
README.md
relevant Codex prompts
```

Architecture changes that affect business rules must also update:

```text id="knejex"
docs/specs/03-business-rules.md
docs/specs/04-api-contract.md
docs/specs/05-data-model.md
docs/specs/07-testing-strategy.md
```
