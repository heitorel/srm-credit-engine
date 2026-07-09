# 01 — Backend Scaffold Prompt

## Purpose

Use this prompt to create the initial Spring Boot backend scaffold for the **SRM Credit Engine**.

This prompt must be used after the specification baseline is committed and before implementing business features such as pricing, currency exchange or settlement.

The goal is to create a clean backend foundation aligned with:

* Java 21;
* Spring Boot 4.1.x;
* Maven;
* layered architecture;
* Flyway;
* MySQL;
* OpenAPI/Swagger;
* validation;
* global exception handling;
* Docker support;
* test structure.

This prompt must not implement business features yet.

## Target Branch

Recommended branch:

```text
feature/backend-scaffold
```

Recommended commit message:

```text
feat: add backend scaffold
```

## Prompt

```text
You are working on the SRM Credit Engine repository.

This task is to create only the initial backend scaffold.

Read first:
- AGENTS.md
- README.md
- docs/specs/00-spec-index.md
- docs/specs/03-business-rules.md
- docs/specs/04-api-contract.md
- docs/specs/05-data-model.md
- docs/specs/06-architecture.md
- docs/specs/07-testing-strategy.md
- docs/adr/ADR-001-backend-stack.md
- docs/adr/ADR-002-database-choice.md
- docs/adr/ADR-003-money-precision.md
- docs/adr/ADR-004-architecture-style.md
- docker-compose.yml
- .env.example

Task:
Create the initial Spring Boot backend scaffold under backend/.

Scope:
You may create or modify only:
- backend/
- README.md only if a backend-specific setup note is strictly necessary
- AI_USAGE.md only if you materially use AI and need to log the interaction

Do not change:
- frontend/
- docs/specs/
- docs/adr/
- docs/diagrams/
- docs/prompts/
- docker-compose.yml
- .env.example

Technology requirements:
- Java 21
- Spring Boot 4.1.x
- Maven
- MySQL JDBC driver
- Flyway
- Spring Web
- Spring Validation
- Spring Data JPA
- OpenAPI/Swagger support
- JUnit 5
- Mockito
- AssertJ
- Testcontainers if useful for future integration tests

Architecture requirements:
Create a package structure aligned with the architecture spec.

Expected base package:
- com.srm.creditengine

Expected backend structure:
- api/
- api/controller/
- api/request/
- api/response/
- api/error/
- api/mapper/
- application/
- application/exchange/
- application/pricing/
- application/settlement/
- application/statement/
- application/reference/
- domain/
- domain/currency/
- domain/exchange/
- domain/pricing/
- domain/receivable/
- domain/settlement/
- domain/shared/
- infrastructure/
- infrastructure/persistence/
- infrastructure/repository/
- infrastructure/query/
- infrastructure/config/

Configuration requirements:
- Create application.yml.
- Create application-local.yml if useful.
- Configure datasource using environment variables.
- Configure Flyway.
- Configure JPA ddl-auto as validate.
- Configure OpenAPI/Swagger.
- Configure CORS origin from environment variable.
- Use UTC-oriented configuration where applicable.

Required initial components:
- Main Spring Boot application class.
- GlobalExceptionHandler skeleton.
- ApiErrorResponse model.
- Basic validation error handling.
- Basic business exception handling structure.
- Placeholder domain exception base class if useful.
- OpenAPI configuration if required by the selected dependency.
- CORS configuration.
- Optional health endpoint only if not using Actuator.
- Backend Dockerfile.

Do not implement yet:
- Pricing formula.
- PricingStrategy implementations.
- Exchange-rate endpoints.
- Settlement endpoints.
- Statement query.
- JPA entities.
- Flyway schema migrations.
- Angular/frontend code.

Financial safety constraints:
- Do not introduce double, float, Double or Float for financial values.
- If any placeholder financial type is needed, use BigDecimal.
- Do not create financial calculation code in this task.
- Do not add Math.pow-based financial calculation.
- Do not hardcode spreads or exchange rates in application code.

Testing requirements:
- Add a basic context load test.
- Add a simple GlobalExceptionHandler or ApiErrorResponse test only if practical.
- Ensure mvn test runs successfully.

Expected commands:
- cd backend
- mvn test

Expected output:
Return a concise report in this format:

Summary:
- ...

Files changed:
- ...

Tests:
- Command run:
- Result:

Validation:
- Confirm Java/Spring/Maven setup.
- Confirm package structure.
- Confirm no business features were implemented.
- Confirm no floating-point financial logic was introduced.

Risks or follow-ups:
- ...
```

## Expected AI Output

The AI agent should create a backend scaffold only.

Expected generated files may include:

```text
backend/
├── Dockerfile
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── srm/
│   │   │           └── creditengine/
│   │   │               ├── SrmCreditEngineApplication.java
│   │   │               ├── api/
│   │   │               │   ├── controller/
│   │   │               │   ├── request/
│   │   │               │   ├── response/
│   │   │               │   ├── error/
│   │   │               │   └── mapper/
│   │   │               ├── application/
│   │   │               ├── domain/
│   │   │               └── infrastructure/
│   │   └── resources/
│   │       ├── application.yml
│   │       └── application-local.yml
│   └── test/
│       └── java/
│           └── com/
│               └── srm/
│                   └── creditengine/
│                       └── SrmCreditEngineApplicationTests.java
```

Exact file layout may differ slightly if justified, but it must preserve the layered architecture.

## Required `pom.xml` Expectations

The Maven configuration should include or support:

```text
Java 21
Spring Boot parent or dependency management
spring-boot-starter-web
spring-boot-starter-validation
spring-boot-starter-data-jpa
mysql-connector-j
flyway-core
flyway-mysql, if required by the selected Flyway version
springdoc-openapi starter
spring-boot-starter-test
mockito
assertj
testcontainers, if included
```

Avoid unnecessary dependencies.

Do not add Lombok unless explicitly justified.

## Required Configuration Expectations

The backend configuration should support environment variables from `.env.example`.

Expected properties include:

```text
server.port
spring.profiles.active
spring.datasource.url
spring.datasource.username
spring.datasource.password
spring.jpa.hibernate.ddl-auto
spring.jpa.show-sql
spring.flyway.enabled
spring.flyway.locations
springdoc.swagger-ui.enabled
springdoc.api-docs.enabled
cors.allowed-origins
```

The application should not contain real secrets.

## Required Dockerfile Expectations

The backend Dockerfile should:

```text
- build or run the Spring Boot application;
- use a Java 21-compatible image;
- expose port 8080;
- be compatible with docker-compose.yml;
- not require local secrets;
- keep the image reasonably simple for the technical challenge.
```

A multi-stage build is preferred but not mandatory if the implementation remains clear and runnable.

## Acceptance Criteria

This scaffold task is acceptable when:

```text
[ ] backend/pom.xml exists.
[ ] backend/src/main/java contains the application entry point.
[ ] Backend package structure matches the architecture spec.
[ ] application.yml exists.
[ ] JPA ddl-auto is configured as validate.
[ ] Flyway is configured.
[ ] OpenAPI dependency/configuration exists.
[ ] Global exception handling skeleton exists.
[ ] ApiErrorResponse exists.
[ ] CORS configuration exists.
[ ] Backend Dockerfile exists.
[ ] mvn test passes.
[ ] No business feature was implemented.
[ ] No financial calculation was implemented.
[ ] No floating-point financial code was introduced.
```

## Review Checklist for Author

After Codex completes the task, verify:

```text
[ ] Did it only change backend files?
[ ] Did it avoid implementing pricing, settlement or exchange-rate features?
[ ] Did it create a clean package structure?
[ ] Did it use Java 21?
[ ] Did it use Spring Boot 4.1.x?
[ ] Did it include Flyway?
[ ] Did it include MySQL driver?
[ ] Did it include validation?
[ ] Did it include OpenAPI/Swagger?
[ ] Did it configure ddl-auto=validate?
[ ] Did it avoid secrets?
[ ] Did mvn test pass?
[ ] Did it avoid double/float financial code?
[ ] Did it avoid unnecessary dependencies?
```

## Common AI Mistakes to Reject

Reject or correct the output if the AI:

```text
- implements pricing formula in this scaffold task;
- creates exchange-rate endpoints too early;
- creates settlement endpoints too early;
- uses H2 as the primary database;
- configures Hibernate ddl-auto=create or update as the final default;
- omits Flyway;
- omits validation;
- omits OpenAPI/Swagger;
- places business logic in controllers;
- creates financial fields using double or float;
- adds unnecessary libraries;
- commits real secrets;
- changes frontend or docs unexpectedly.
```

## Suggested PR Description

````md
## Summary

Adds the initial Spring Boot backend scaffold for SRM Credit Engine.

## Specs Covered

- docs/specs/06-architecture.md
- docs/specs/07-testing-strategy.md
- docs/adr/ADR-001-backend-stack.md
- docs/adr/ADR-004-architecture-style.md

## Changes

- Created Maven Spring Boot backend project.
- Added base package structure.
- Added application configuration.
- Added OpenAPI/Swagger support.
- Added validation and global exception handling skeleton.
- Added backend Dockerfile.
- Added context load test.

## Tests

- [x] Unit/context test added
- [x] Manual validation performed

Commands executed:

```bash
cd backend
mvn test
````

## Risks and Trade-offs

* Business features are intentionally not implemented in this PR.
* Flyway migrations will be added in a separate database migration feature.

## AI Usage

AI was used for:

* scaffolding support

AI_USAGE.md:

* [ ] Updated
* [ ] Not applicable

````

## Related Documents

```text
AGENTS.md
README.md
AI_USAGE.md
docs/specs/03-business-rules.md
docs/specs/04-api-contract.md
docs/specs/05-data-model.md
docs/specs/06-architecture.md
docs/specs/07-testing-strategy.md
docs/adr/ADR-001-backend-stack.md
docs/adr/ADR-002-database-choice.md
docs/adr/ADR-003-money-precision.md
docs/adr/ADR-004-architecture-style.md
docker-compose.yml
.env.example
````
