# ADR-001 — Backend Stack

## Status

Accepted

## Date

2026-07-07

## Context

The SRM Credit Engine backend must support a financial domain where correctness, decimal precision, transaction consistency, auditability and maintainability are critical.

The technical challenge expects a backend suitable for a financial environment, with strong typing, mature frameworks, REST APIs, persistence, ACID transactions, robust validation, global exception handling, OpenAPI/Swagger documentation, Docker support and automated tests.

The backend must implement:

* exchange-rate management;
* pricing simulation;
* receivable-type pricing strategies;
* batch settlement;
* atomic persistence;
* settlement statement queries;
* structured error handling;
* database migrations;
* testable business logic.

The project targets a **Pleno / Mid-Level Software Engineer** delivery, so the stack must be mature, defensible and productive without introducing unnecessary complexity.

## Decision

The backend will be implemented with the following stack:

| Concern             | Decision                                                 |
| ------------------- | -------------------------------------------------------- |
| Language            | Java 21                                                  |
| Framework           | Spring Boot 4.1.x                                        |
| Build tool          | Maven 3.9.x                                              |
| API style           | REST                                                     |
| API documentation   | OpenAPI / Swagger                                        |
| Persistence         | Spring Data JPA / Hibernate                              |
| Database migration  | Flyway                                                   |
| Database            | MySQL 8.4 LTS                                            |
| Validation          | Jakarta Bean Validation                                  |
| Testing             | JUnit 5, Mockito, AssertJ                                |
| Integration testing | Testcontainers, when database behavior must be validated |
| Packaging           | Docker image                                             |
| Local orchestration | Docker Compose                                           |

## Selected Backend Version Targets

Initial version targets:

```text id="7jhdvf"
Java: 21
Spring Boot: 4.1.x
Maven: 3.9.x
MySQL: 8.4 LTS
Flyway: 12.x
JUnit: 5.x
```

The project should avoid manually pinning dependency versions managed by Spring Boot unless there is a clear reason.

Spring Boot dependency management should be the primary source for compatible versions of:

* Spring Framework;
* Spring Data JPA;
* Hibernate;
* Jackson;
* validation libraries;
* logging libraries;
* test dependencies.

## Rationale

### Java 21

Java 21 was selected because it is a Long-Term Support release and is widely suitable for enterprise backend systems.

Benefits:

* strong static typing;
* mature ecosystem;
* good support for financial systems;
* excellent compatibility with Spring Boot;
* modern language features;
* long-term maintainability.

Java 21 is preferred over newer non-LTS versions because this challenge values stability and defensibility more than using the newest runtime.

### Spring Boot 4.1.x

Spring Boot was selected because it is a mature, production-grade Java framework for backend systems.

Benefits:

* mature REST API development model;
* strong integration with validation;
* strong transaction support;
* mature persistence support;
* good testing support;
* strong ecosystem around observability, configuration and documentation;
* good fit for layered architecture.

Spring Boot also aligns well with the requirements for:

* RESTful APIs;
* global exception handlers;
* transaction boundaries;
* OpenAPI integration;
* Docker-friendly deployment;
* enterprise maintainability.

### Maven

Maven was selected as the build tool because it is explicit, stable and widely used in enterprise Java projects.

Benefits:

* predictable project structure;
* mature dependency management;
* simple CI/CD integration;
* good compatibility with Spring Boot;
* easy execution for evaluators.

Gradle was considered, but Maven was preferred for clarity and lower cognitive overhead in a technical challenge context.

### Spring Data JPA / Hibernate

Spring Data JPA with Hibernate was selected for transactional persistence and domain data management.

Benefits:

* mature ORM ecosystem;
* strong Spring integration;
* transaction management;
* repository abstraction;
* productive CRUD and aggregate persistence.

However, analytical settlement statement queries may use optimized SQL, projections or dedicated read repositories where ORM abstraction would be inefficient.

This is consistent with the requirement that reports may use query builders or native SQL for performance.

### Flyway

Flyway was selected for database schema migration.

Benefits:

* deterministic schema evolution;
* explicit DDL;
* versioned migrations;
* easy local reproducibility;
* compatibility with Docker Compose;
* better technical challenge evaluation than relying on Hibernate auto-DDL.

Hibernate auto-DDL must not be used as the final source of schema creation.

Expected production-like setting:

```properties id="qolbr9"
spring.jpa.hibernate.ddl-auto=validate
```

### MySQL 8.4 LTS

MySQL 8.4 LTS was selected as the relational database for this project.

The database choice itself is documented in:

```text id="1pnfgb"
docs/adr/ADR-002-database-choice.md
```

From the backend perspective, MySQL provides:

* relational integrity;
* ACID transactions;
* decimal numeric types;
* indexing for analytical filters;
* compatibility with Flyway and Spring Data JPA.

### OpenAPI / Swagger

OpenAPI / Swagger will be used to document the REST API.

Benefits:

* evaluator can inspect endpoints quickly;
* frontend integration becomes clearer;
* request and response contracts are visible;
* validation and error responses can be documented.

### JUnit 5, Mockito and AssertJ

These tools were selected for backend automated testing.

Expected use:

* JUnit 5 for test execution;
* Mockito for mocking collaborators when appropriate;
* AssertJ for expressive assertions;
* Testcontainers for database integration tests where needed.

Pricing rules must be covered by unit tests.

Settlement atomicity and persistence behavior should be covered by integration tests when feasible.

## Alternatives Considered

### Kotlin + Spring Boot

Kotlin was considered because it provides concise syntax, null-safety and strong interoperability with Spring.

Rejected for this project because:

* Java is more universally familiar to technical evaluators;
* Java 21 is sufficient for clear domain modeling;
* the challenge benefits from lower toolchain complexity;
* Java reduces risk of Kotlin/JPA pitfalls in a short delivery window.

Kotlin remains a valid alternative, but Java was selected for predictability.

### Node.js / NestJS

NestJS was considered because it supports structured backend development with TypeScript.

Rejected because:

* the financial calculation domain benefits from Java's mature decimal and enterprise ecosystem;
* Spring transaction management is more conventional for this kind of backend;
* the role context benefits from demonstrating Java/Spring backend maturity.

### .NET / C#

.NET was considered as a strong typed enterprise stack.

Rejected because:

* the selected implementation direction is Java/Spring;
* Spring Boot aligns well with the expected backend profile;
* Java ecosystem is sufficient for all challenge requirements.

### Quarkus or Micronaut

Quarkus and Micronaut were considered as modern Java frameworks.

Rejected because:

* Spring Boot is more widely recognized;
* Spring Boot has a broader ecosystem and evaluator familiarity;
* the challenge prioritizes clarity and maturity over startup-time optimization.

### Gradle

Gradle was considered as an alternative build tool.

Rejected because:

* Maven is simpler to inspect;
* Maven is common in enterprise Java repositories;
* Maven is sufficient for this challenge;
* Maven configuration is usually easier for evaluators to follow.

## Consequences

### Positive Consequences

The selected stack provides:

* strong typing;
* mature financial-system-friendly backend foundation;
* robust transaction management;
* clear validation support;
* mature persistence model;
* straightforward OpenAPI documentation;
* good Docker and CI/CD compatibility;
* strong testing ecosystem;
* high evaluator familiarity.

### Negative Consequences

The selected stack also introduces:

* more boilerplate than some alternatives;
* careful handling required for `BigDecimal`;
* potential JPA pitfalls if domain and persistence concerns are not separated cleanly;
* need for explicit transaction boundary design;
* possible complexity when mixing ORM write models with optimized report queries.

### Mitigations

To mitigate the negative consequences:

* keep financial calculations in domain services/value objects;
* do not place business logic in controllers;
* use application services for transaction boundaries;
* use Flyway for schema control;
* use projections/native SQL for analytical reads when appropriate;
* cover pricing rules with unit tests;
* cover critical persistence behavior with integration tests where feasible;
* document assumptions in specs and ADRs.

## Architecture Impact

This decision supports the backend layered architecture:

```text id="m9d9je"
api
 └── controllers, DTOs, validation, OpenAPI annotations, exception mapping

application
 └── use cases, orchestration, transaction boundaries

domain
 └── entities, value objects, business rules, pricing strategies

infrastructure
 └── persistence, repositories, migrations, SQL queries, integrations
```

The stack must be used in a way that preserves layer responsibilities.

Spring-specific annotations should be concentrated in outer layers where possible.

The domain layer should remain testable without requiring a running Spring application.

## Dependency Rules

### Allowed

The backend may use:

* Spring Boot starters;
* Spring Web;
* Spring Validation;
* Spring Data JPA;
* Flyway;
* MySQL JDBC driver;
* Springdoc OpenAPI;
* JUnit 5;
* Mockito;
* AssertJ;
* Testcontainers;
* Lombok only if explicitly justified before use.

### Avoid

The backend should avoid:

* unnecessary code generation frameworks;
* unnecessary mapping complexity;
* premature messaging infrastructure;
* premature distributed architecture;
* direct use of floating-point numeric types in financial logic;
* framework leakage into core domain objects when avoidable.

### Lombok Position

Lombok is not part of the default decision.

If introduced, it must be justified because it can reduce boilerplate but may hide code behavior from reviewers.

For this challenge, explicit Java code is preferred unless the implementation becomes excessively verbose.

## Package Organization Guidance

Recommended package structure:

```text id="y4u95i"
backend/
└── src/main/java/<base-package>/
    ├── api/
    │   ├── controller/
    │   ├── request/
    │   ├── response/
    │   └── error/
    ├── application/
    │   ├── exchange/
    │   ├── pricing/
    │   ├── settlement/
    │   └── statement/
    ├── domain/
    │   ├── currency/
    │   ├── pricing/
    │   ├── receivable/
    │   ├── settlement/
    │   └── shared/
    └── infrastructure/
        ├── persistence/
        ├── repository/
        ├── migration/
        └── query/
```

This structure may be adjusted during implementation if the documentation is updated accordingly.

## Testing Impact

The selected stack enables the following testing strategy:

### Unit Tests

Used for:

* pricing formula;
* strategy resolution;
* rate and money value objects;
* currency conversion;
* domain validation.

### Application Tests

Used for:

* settlement orchestration;
* transactional behavior;
* exception mapping from domain to application.

### Integration Tests

Used for:

* Flyway migration validation;
* repository behavior;
* duplicate settlement prevention;
* settlement rollback;
* statement query filtering.

### API Tests

Used for:

* validation errors;
* expected status codes;
* request/response contract verification.

## Operational Impact

The backend must be executable in two ways:

### Local Development

```bash id="yab68e"
cd backend
mvn spring-boot:run
```

### Docker Compose

```bash id="esvpws"
docker compose up --build
```

The final README must document both options.

## Security Impact

This stack supports:

* backend-side validation;
* controlled exception handling;
* environment-based configuration;
* no hardcoded secrets;
* explicit CORS configuration;
* structured API errors.

Security-sensitive items such as authentication and authorization are outside the initial scope, but the application structure should not make them difficult to add later.

## Observability Impact

The initial Pleno-level delivery will focus on:

* structured application logs;
* clear error messages;
* traceable settlement records.

Advanced metrics, tracing, Prometheus or Grafana may be documented as future enhancements or added only if they do not compromise core delivery quality.

## Decision Validation

This decision is considered valid if the implementation demonstrates:

* successful Spring Boot application startup;
* REST endpoints documented through OpenAPI/Swagger;
* MySQL connectivity;
* Flyway migrations running successfully;
* validation errors handled consistently;
* pricing rules tested with JUnit;
* settlement transaction behavior implemented through Spring transaction management;
* Docker Compose execution.

## Related Documents

```text id="pmr97t"
README.md
AGENTS.md
docs/specs/03-business-rules.md
docs/specs/04-api-contract.md
docs/specs/05-data-model.md
docs/specs/06-architecture.md
docs/specs/07-testing-strategy.md
docs/adr/ADR-002-database-choice.md
docs/adr/ADR-003-money-precision.md
docs/adr/ADR-004-architecture-style.md
```

## Review Notes

This ADR may be revisited if implementation constraints reveal that a selected dependency version is incompatible with another project decision.

Any change to the backend stack must update:

* this ADR;
* `README.md`;
* `AGENTS.md`;
* `.env.example`;
* `docker-compose.yml`;
* relevant specs;
* implementation prompts.
