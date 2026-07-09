# ADR-004 - Architecture Style

## Status

Accepted

## Date

2026-07-07

## Context

The SRM Credit Engine must implement a financial application with clear separation of responsibilities, reliable business rules, decimal-safe pricing, auditable settlement records and transactional consistency.

The technical challenge requires:

* layered backend architecture;
* separation between application logic, business logic and persistence logic;
* Strategy Pattern for receivable pricing rules;
* RESTful API design;
* relational persistence;
* ACID transaction guarantees;
* analytical settlement statement queries;
* frontend separation between presentation and business/state logic.

The system must be strong enough to demonstrate mid-level engineering maturity without introducing unnecessary distributed architecture.

## Decision

The project will use a **modular monolith with layered architecture**.

The backend will be organized into four main layers:

```text
api
application
domain
infrastructure
```

The frontend will be organized by feature modules, with separation between:

```text
pages
components
services
models
state
```

The backend will keep official financial calculation logic in the domain/application boundary.

The frontend will request simulations from the backend and display results, but it will not duplicate official settlement calculation logic.

## Architecture Summary

### Backend

```text
api
 `-- HTTP controllers, DTOs, validation annotations, OpenAPI annotations, exception mapping

application
 `-- use cases, orchestration, transaction boundaries, application workflows

domain
 `-- business entities, value objects, pricing strategies, domain services, domain exceptions

infrastructure
 `-- persistence, JPA mappings, repositories, Flyway migrations, SQL queries, external/mock integrations
```

### Frontend

```text
core
 `-- API configuration, interceptors, shared services

shared
 `-- reusable UI components, pipes, formatters

features
 |-- pricing-simulation
 |-- settlements
 `-- exchange-rates

models
 `-- TypeScript contracts and view models
```

## Rationale

### 1. The domain is not a simple CRUD

The system contains financial behavior that must be explicit and testable:

* present value calculation;
* spread selection by receivable type;
* cross-currency conversion;
* settlement atomicity;
* duplicate settlement prevention;
* audit snapshot persistence.

A simple controller-service-repository CRUD structure would be insufficient because it could hide important financial rules inside procedural service methods or controllers.

### 2. A modular monolith is sufficient for the challenge

The system does not require multiple deployable services in the initial version.

A modular monolith provides:

* clear code organization;
* lower operational complexity;
* easier local execution;
* simpler transaction management;
* easier evaluation;
* enough modularity for future extraction if needed.

Microservices, message brokers and distributed transactions are intentionally avoided in the initial delivery.

### 3. Layered architecture supports testability

Pricing rules and settlement behavior must be testable without requiring a running web server.

The domain layer should support unit tests for:

* pricing strategies;
* money/rate/term validation;
* currency conversion rules;
* discount calculation;
* cross-currency calculation order.

The application layer should support tests for:

* settlement orchestration;
* transaction behavior;
* duplicate prevention;
* error mapping.

### 4. Transaction boundaries belong in application services

Settlement is a use case that coordinates multiple operations:

* validate request;
* load assignor;
* load receivables;
* validate receivable status;
* fetch exchange rate when needed;
* calculate pricing;
* create settlement;
* create settlement items;
* update receivable status;
* commit or rollback.

This orchestration belongs in the application layer.

The domain layer defines rules and behavior, but it should not directly manage database transactions.

### 5. Reports can use a simpler read path

The technical challenge explicitly allows reports to be organized with fewer layers when business rules are not required.

Therefore, settlement statement queries may use:

```text
api -> application/read service -> infrastructure query
```

when justified by simplicity and performance.

Simplification must not bypass the application boundary.

However, report implementation must still preserve:

* input validation;
* pagination;
* database-level filtering;
* structured error handling;
* clear DTOs.

## Backend Layer Responsibilities

## 1. API Layer

### Purpose

Expose HTTP endpoints and translate HTTP concerns into application requests.

### Responsibilities

The API layer may contain:

* REST controllers;
* request DTOs;
* response DTOs;
* validation annotations;
* OpenAPI annotations;
* HTTP status mapping;
* global exception handlers.

### Must Not Contain

The API layer must not contain:

* financial calculation logic;
* pricing formula implementation;
* spread selection logic;
* settlement transaction orchestration;
* direct SQL or JPA implementation details;
* cross-currency calculation logic;
* business decisions hidden inside controllers.

### Example Components

```text
ExchangeRateController
PricingSimulationController
SettlementController
SettlementStatementController
GlobalExceptionHandler
ApiErrorResponse
```

### Example Package

```text
api/
|-- controller/
|-- request/
|-- response/
`-- error/
```

## 2. Application Layer

### Purpose

Coordinate use cases and define transaction boundaries.

### Responsibilities

The application layer may contain:

* use case services;
* transaction orchestration;
* request-to-domain coordination;
* domain service invocation;
* repository coordination;
* application-level validation;
* mapping from domain results to response models.

### Must Not Contain

The application layer should not contain:

* HTTP annotations;
* UI behavior;
* SQL query details for complex reports unless encapsulated behind a read port/repository;
* low-level persistence mapping concerns.

### Example Components

```text
CreateExchangeRateService
GetLatestExchangeRateService
PricingSimulationService
CreateSettlementService
GetSettlementService
SettlementStatementService
```

### Transaction Boundary Rule

Use cases that modify financial state must define transaction boundaries in the application layer.

Critical transactional service:

```text
CreateSettlementService
```

Settlement creation must be atomic.

### Example Package

```text
application/
|-- exchange/
|-- pricing/
|-- settlement/
`-- statement/
```

## 3. Domain Layer

### Purpose

Represent business concepts and enforce domain behavior.

### Responsibilities

The domain layer may contain:

* entities;
* value objects;
* domain services;
* pricing strategies;
* domain exceptions;
* business invariants;
* financial calculation logic.

### Must Not Contain

The domain layer must not depend on:

* controllers;
* HTTP request/response DTOs;
* JPA repositories;
* database-specific annotations when avoidable;
* Angular/frontend concepts;
* infrastructure integrations.

### Example Components

```text
Money
Rate
CurrencyCode
Term
Receivable
Settlement
SettlementItem
PricingStrategy
MercantileDuplicatePricingStrategy
PostDatedCheckPricingStrategy
PricingStrategyResolver
PricingEngine
CurrencyConversionService
```

### Strategy Pattern Rule

Receivable risk behavior must be modeled through Strategy Pattern.

Expected abstraction:

```text
PricingStrategy
```

Expected initial implementations:

```text
MercantileDuplicatePricingStrategy
PostDatedCheckPricingStrategy
```

The Strategy Pattern must not be replaced by a large conditional block in a controller or application service.

### Example Package

```text
domain/
|-- currency/
|-- pricing/
|-- receivable/
|-- settlement/
`-- shared/
```

## 4. Infrastructure Layer

### Purpose

Implement technical details and external boundaries.

### Responsibilities

The infrastructure layer may contain:

* JPA entities;
* Spring Data repositories;
* native SQL queries;
* read projections;
* Flyway migrations;
* database adapters;
* mock integrations;
* configuration classes.

### Must Not Contain

The infrastructure layer should not define business policy.

It may enforce persistence-level constraints, but domain meaning must remain in domain/application layers.

### Example Components

```text
JpaReceivableRepository
JpaSettlementRepository
JpaExchangeRateRepository
SettlementStatementQueryRepository
Flyway migrations
MySQL-specific SQL projections
```

### Example Package

```text
infrastructure/
|-- persistence/
|-- repository/
|-- query/
`-- config/
```

## Dependency Direction

The intended dependency direction is:

```text
api -> application -> domain
api -> application -> infrastructure
application -> domain
application -> infrastructure
infrastructure -> domain
```

The domain layer should not depend on API or infrastructure.

Simplified dependency rule:

```text
Outer layers may depend on inner layers.
Inner layers should not depend on outer layers.
```

## Backend Package Organization

Recommended initial package structure:

```text
backend/
`-- src/main/java/com/srm/creditengine/
    |-- SrmCreditEngineApplication.java
    |-- api/
    |   |-- controller/
    |   |-- request/
    |   |-- response/
    |   `-- error/
    |-- application/
    |   |-- exchange/
    |   |-- pricing/
    |   |-- settlement/
    |   `-- statement/
    |-- domain/
    |   |-- currency/
    |   |-- pricing/
    |   |-- receivable/
    |   |-- settlement/
    |   `-- shared/
    `-- infrastructure/
        |-- persistence/
        |-- repository/
        |-- query/
        `-- config/
```

This structure may be refined during implementation if documentation and specs are updated accordingly.

## Data Flow Examples

## 1. Pricing Simulation Flow

```text
Angular Pricing Simulation Screen
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
        v
PricingStrategyResolver
        |
        v
PricingStrategy
        |
        v
CurrencyConversionService
        |
        v
PricingSimulationResponse
```

Rules:

* no settlement is persisted;
* exchange rate may be read when cross-currency conversion is required;
* the same domain pricing logic must be reusable by settlement.

## 2. Settlement Creation Flow

```text
POST /api/settlements
        |
        v
SettlementController
        |
        v
CreateSettlementService  @Transactional
        |
        |-- Load assignor
        |-- Load receivables
        |-- Validate receivable status
        |-- Resolve exchange rates when needed
        |-- Calculate pricing through domain pricing engine
        |-- Create settlement
        |-- Create settlement items
        |-- Update receivable status
        `-- Commit or rollback
```

Rules:

* settlement must be atomic;
* all calculation snapshots must be persisted;
* duplicate settlement must be prevented;
* no partial persistence is acceptable.

## 3. Settlement Statement Flow

```text
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
SettlementStatementResponse
```

Rules:

* filter in database;
* paginate in database;
* avoid loading all data into memory;
* optimized SQL or projections are acceptable.

## Frontend Architecture

## 1. Frontend Style

The Angular frontend will use a feature-oriented structure.

Recommended structure:

```text
frontend/
`-- src/
    `-- app/
        |-- core/
        |   |-- api/
        |   |-- config/
        |   `-- interceptors/
        |-- shared/
        |   |-- components/
        |   |-- pipes/
        |   `-- utils/
        |-- features/
        |   |-- pricing-simulation/
        |   |-- settlements/
        |   `-- exchange-rates/
        |-- models/
        `-- app.config.ts
```

## 2. Frontend Responsibilities

The frontend is responsible for:

* collecting operator input;
* validating basic UI constraints;
* calling backend APIs;
* displaying simulation results;
* displaying settlement statement data;
* handling loading and error states;
* formatting values for display.

## 3. Frontend Must Not

The frontend must not:

* implement official pricing formula;
* calculate final settlement values independently;
* hardcode exchange rates;
* paginate settlement history locally after loading all records;
* bypass backend validation assumptions.

## 4. State Management

Initial state management decision:

```text
Angular Signals + services
```

NgRx is not part of the initial architecture.

Rationale:

* application scope is limited;
* server data is fetched through API services;
* feature-level state is enough;
* NgRx would add unnecessary complexity for the challenge.

## 5. API Integration

Angular services should wrap backend API calls.

Recommended services:

```text
PricingSimulationApiService
ExchangeRateApiService
SettlementApiService
SettlementStatementApiService
```

Frontend models should mirror API contracts defined in:

```text
docs/specs/04-api-contract.md
```

## Cross-Cutting Concerns

## 1. Validation

Validation must exist at backend API boundary.

Frontend validation is useful for usability but not sufficient.

Backend validation should cover:

* required fields;
* positive monetary values;
* valid currency codes;
* valid receivable types;
* future due dates;
* positive exchange rates;
* pagination limits;
* date range consistency;
* batch size limits.

## 2. Error Handling

The backend must use global exception handling.

Expected error structure:

```text
timestamp
status
error
message
path
details
```

Frontend must display errors clearly.

## 3. Transactions

Financial write use cases must be transactional.

Critical transactional boundary:

```text
CreateSettlementService
```

The system must not persist half of a settlement.

## 4. Auditability

Settlement item snapshots must preserve the calculation inputs and outputs used at settlement time.

The architecture must not depend on recalculating old settlement values from current reference data.

## 5. Logging

The initial architecture should support structured and useful logs.

Minimum logging expectations:

* application startup;
* major settlement creation events;
* controlled error scenarios;
* unexpected exception handling.

Logs must not expose secrets.

## 6. Configuration

Configuration must come from environment variables or profile-specific application configuration.

Examples:

```text
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
CORS_ALLOWED_ORIGINS
DEFAULT_BASE_RATE
```

`DEFAULT_BASE_RATE` is an optional server-side fallback. Request `baseRate` takes precedence, and operations must fail with a structured error if neither source is available.

No real secrets should be committed.

## 7. Documentation

Architecture decisions must remain aligned with:

```text
README.md
AGENTS.md
docs/specs/*
docs/adr/*
docs/diagrams/*
```

When architecture changes, update the related documentation.

## Alternatives Considered

## 1. Simple MVC / Controller-Service-Repository

### Description

A traditional structure where controllers call services, and services call repositories.

### Advantages

* simple;
* fast to implement;
* familiar to many Spring developers.

### Rejected As Primary Style

Reason:

* may hide business logic inside large services;
* does not clearly distinguish application orchestration from domain behavior;
* can lead to an anemic domain;
* weaker demonstration of design maturity;
* risk of mixing transaction orchestration, pricing rules and persistence.

A simplified MVC structure may still appear in small peripheral features, but critical financial logic must follow the selected layered model.

## 2. Strict Clean Architecture / Hexagonal Architecture

### Description

An architecture with strict port/adapters boundaries and dependency inversion around all external details.

### Advantages

* strong separation;
* high testability;
* excellent long-term maintainability.

### Not Fully Adopted

Reason:

* may add too much ceremony for a 3-4 day technical challenge;
* strict ports for every dependency may reduce delivery speed;
* the evaluation target is mid-level, not principal-level architecture.

The selected architecture borrows useful ideas from Clean Architecture and Hexagonal Architecture without implementing excessive ceremony.

## 3. Microservices

### Description

Split the system into independently deployable services, such as:

```text
currency-service
pricing-service
settlement-service
statement-service
frontend
```

### Rejected

Reason:

* unnecessary operational complexity;
* harder local execution;
* distributed transaction concerns;
* more infrastructure than the challenge requires;
* increased delivery risk;
* core challenge is domain correctness, not service distribution.

Microservices may be discussed as a future scalability direction but not implemented initially.

## 4. Event-Driven Architecture

### Description

Use events and messaging to process settlements asynchronously.

### Rejected for Initial Delivery

Reason:

* not required for the core challenge;
* adds operational dependencies;
* complicates audit and consistency;
* may distract from required Pleno-level deliverables.

Event-driven architecture can be documented as a future option for high-scale scenarios.

## 5. Full CQRS

### Description

Separate write model and read model completely.

### Not Adopted Initially

Reason:

* unnecessary for the current scope;
* increases complexity;
* not needed to satisfy the challenge.

However, the project will use a **light CQRS-like approach** for analytical settlement statements:

* transactional write use cases use domain/application flow;
* report queries may use optimized read projections.

## Consequences

## Positive Consequences

The selected architecture provides:

* clear separation of responsibilities;
* testable domain logic;
* explicit transaction boundaries;
* easier auditability;
* better maintainability than simple CRUD;
* lower complexity than microservices;
* strong fit for a mid-level technical challenge;
* ability to evolve into stricter architecture later.

## Negative Consequences

The selected architecture also introduces:

* more files and packages than simple MVC;
* need to avoid overengineering;
* need for discipline to keep layers clean;
* possible duplication between domain objects, DTOs and persistence entities;
* need for clear mapping strategy.

## Mitigations

Mitigations:

* keep use cases small;
* avoid unnecessary abstractions;
* only create ports/adapters when there is a clear benefit;
* keep DTOs simple;
* centralize financial calculations;
* use tests to protect domain rules;
* keep documentation updated;
* review AI-generated changes against `AGENTS.md`.

## Implementation Rules

## 1. Controllers

Controllers must:

* receive HTTP requests;
* validate DTOs;
* call application services;
* return response DTOs;
* expose OpenAPI documentation.

Controllers must not:

* calculate present value;
* resolve pricing strategies;
* directly manipulate JPA entities for business workflows;
* contain transaction orchestration.

## 2. Application Services

Application services must:

* represent use cases;
* orchestrate domain and persistence;
* define transaction boundaries where needed;
* handle application-level decisions;
* return application results.

Application services must not:

* expose HTTP concerns;
* implement UI formatting;
* hide financial formulas that belong in domain components.

## 3. Domain Services and Strategies

Domain services and strategies must:

* contain financial and business behavior;
* be independently testable;
* avoid framework dependencies where practical;
* expose clear business methods.

## 4. Repositories

Repositories must:

* abstract persistence;
* support transactional use cases;
* support optimized read queries when needed;
* avoid hiding business policy inside query methods.

## 5. DTOs

DTOs must:

* represent API contracts;
* not be reused as domain entities;
* contain validation annotations when useful;
* remain stable and documented.

## 6. Entities

Persistence entities may differ from domain entities if separation becomes useful.

Initial implementation may use pragmatic mapping, but domain behavior must remain testable and not depend on controllers or HTTP DTOs.

## 7. Reports

Settlement statement reporting may use projections or native SQL.

Reports must:

* filter at database level;
* paginate at database level;
* sort deterministically;
* avoid loading all rows into memory.

## Transaction Design

## Settlement Transaction Boundary

The settlement transaction must include:

```text
1. Request validation
2. Receivable loading
3. Receivable availability check
4. Exchange-rate lookup
5. Pricing calculation
6. Settlement creation
7. Settlement item creation
8. Receivable status update
9. Commit
```

If any step fails:

```text
Rollback
```

## Recommended Annotation Placement

Use Spring transaction management at application service level:

```java
@Transactional
public SettlementResult createSettlement(CreateSettlementCommand command) {
    ...
}
```

Avoid placing transaction boundaries in controllers.

## Read-Only Transactions

Read services may use read-only transaction hints when useful:

```java
@Transactional(readOnly = true)
```

## API Design Impact

The architecture supports the following API groups:

```text
/api/exchange-rates
/api/pricing/simulations
/api/settlements
/api/settlements/statement
```

The API layer must remain thin.

The API contract is defined in:

```text
docs/specs/04-api-contract.md
```

## Testing Impact

The architecture enables the following test distribution:

### Domain Unit Tests

Used for:

* money value object;
* rate value object;
* term calculation;
* pricing strategy;
* pricing engine;
* currency conversion;
* discount calculation.

### Application Tests

Used for:

* pricing simulation orchestration;
* settlement creation flow;
* duplicate settlement behavior;
* missing exchange-rate behavior.

### Infrastructure Tests

Used for:

* Flyway migration;
* repository behavior;
* SQL statement query;
* database constraints.

### API Tests

Used for:

* request validation;
* error responses;
* OpenAPI-visible behavior;
* HTTP status codes.

### Frontend Tests

Used for:

* form validation;
* API service behavior;
* settlement grid behavior;
* error display.

## AI-Assisted Development Impact

AI tools must follow the architecture rules defined in this ADR.

AI-generated code must be reviewed for:

* business logic placed in controllers;
* financial logic using floating-point types;
* report queries filtering in memory;
* frontend duplicating backend calculation;
* transaction boundaries placed incorrectly;
* unnecessary abstractions;
* broad unrelated changes.

Relevant AI instructions are documented in:

```text
AGENTS.md
docs/specs/10-ai-workflow.md
AI_USAGE.md
```

## Decision Validation

This decision is valid if the implementation demonstrates:

* thin controllers;
* application services representing use cases;
* domain-level pricing strategy implementations;
* transaction boundary in settlement application service;
* infrastructure repositories and migrations separated from domain logic;
* backend calculation as official source;
* frontend calling backend for simulation;
* server-side statement filtering and pagination;
* tests for domain and application behavior;
* documentation consistent with implementation.

## Related Documents

```text
README.md
AGENTS.md
docs/specs/01-product-brief.md
docs/specs/02-domain-glossary.md
docs/specs/03-business-rules.md
docs/specs/04-api-contract.md
docs/specs/05-data-model.md
docs/specs/06-architecture.md
docs/specs/07-testing-strategy.md
docs/adr/ADR-001-backend-stack.md
docs/adr/ADR-002-database-choice.md
docs/adr/ADR-003-money-precision.md
docs/adr/ADR-005-frontend-stack.md
docs/diagrams/c4-context.md
docs/diagrams/c4-container.md
```

## Review Notes

This ADR may be revisited if:

* implementation becomes overly complex;
* the package structure harms delivery speed;
* stricter dependency inversion becomes necessary;
* new integration boundaries appear;
* future requirements introduce asynchronous processing;
* authentication and authorization become part of the scope.

Any architecture change must update:

* this ADR;
* `docs/specs/06-architecture.md`;
* relevant diagrams;
* relevant prompts;
* `AGENTS.md`;
* README if the change affects project understanding.
