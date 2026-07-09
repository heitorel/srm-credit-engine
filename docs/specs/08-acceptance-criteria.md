# 08 - Acceptance Criteria

## 1. Purpose

This document defines the acceptance criteria for the **SRM Credit Engine**.

The criteria describe the expected behavior and delivery quality required for the initial technical challenge implementation.

The acceptance criteria cover:

* functional behavior;
* financial correctness;
* API behavior;
* frontend behavior;
* persistence and auditability;
* transaction consistency;
* validation and error handling;
* performance expectations;
* documentation;
* Git workflow;
* AI usage transparency;
* local execution.

A feature should not be considered complete unless the relevant acceptance criteria are satisfied.

## 2. Acceptance Criteria Format

Most criteria follow this structure:

```text
Given <initial context>
When <action happens>
Then <expected result>
```

Each criterion includes:

* ID;
* title;
* category;
* priority;
* acceptance scenario;
* validation method.

Priority levels:

| Priority | Meaning                                          |
| -------- | ------------------------------------------------ |
| Critical | Required for valid delivery                      |
| High     | Required for strong Pleno-level delivery         |
| Medium   | Important but not delivery-blocking if justified |
| Low      | Nice to have                                     |

## 3. Functional Acceptance Criteria

## AC-001 - Local Stack Starts with Docker Compose

**Category:** Local execution
**Priority:** Critical

### Scenario

Given a clean local environment with Docker available,
when the evaluator runs:

```bash
docker compose up --build
```

then the following services must start successfully:

* MySQL;
* backend;
* frontend.

### Expected result

* MySQL container becomes healthy.
* Backend starts on port `8080`.
* Frontend starts on port `4200`.
* Backend connects to MySQL.
* Flyway migrations execute successfully.
* Swagger/OpenAPI is accessible.
* Frontend is accessible in the browser.

### Validation method

Manual validation and final README instructions.

---

## AC-002 - Flyway Creates the Database Schema

**Category:** Database
**Priority:** Critical

### Scenario

Given an empty MySQL database,
when the backend starts,
then Flyway must create the database schema and seed reference data.

### Expected result

The database contains at least:

```text
currencies
receivable_types
assignors
exchange_rates
receivables
settlements
settlement_items
```

Seeded data includes:

```text
BRL
USD
MERCANTILE_DUPLICATE
POST_DATED_CHECK
```

### Validation method

Integration test or manual database inspection.

---

## AC-003 - Hibernate Validates the Schema

**Category:** Database
**Priority:** High

### Scenario

Given the database schema created by Flyway,
when the Spring Boot application starts,
then Hibernate must validate the schema instead of creating it automatically.

### Expected result

The application uses:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

The application starts successfully only if JPA mappings match the Flyway schema.

### Validation method

Configuration review and startup validation.

---

## AC-004 - Supported Currencies Are Exposed

**Category:** Reference data
**Priority:** High

### Scenario

Given the backend is running,
when the frontend or evaluator calls:

```http
GET /api/reference-data/currencies
```

then the API must return the supported currencies.

### Expected result

Response includes:

```text
BRL
USD
```

### Validation method

API test or manual Swagger/cURL validation.

---

## AC-005 - Supported Receivable Types Are Exposed

**Category:** Reference data
**Priority:** High

### Scenario

Given the backend is running,
when the frontend or evaluator calls:

```http
GET /api/reference-data/receivable-types
```

then the API must return the supported receivable types and spreads.

### Expected result

Response includes:

```text
MERCANTILE_DUPLICATE = 0.01500000
POST_DATED_CHECK = 0.02500000
```

### Validation method

API test or manual Swagger/cURL validation.

---

## AC-006 - Exchange Rate Can Be Registered

**Category:** Currency Engine
**Priority:** Critical

### Scenario

Given a valid exchange-rate request,
when the operator calls:

```http
POST /api/exchange-rates
```

then the system must persist the exchange rate.

### Expected result

The API returns:

```http
201 Created
```

Response includes:

* id;
* source currency;
* target currency;
* rate;
* validity timestamp;
* creation timestamp.

### Validation method

API test and database verification.

---

## AC-007 - Exchange Rate Direction Is Explicit

**Category:** Currency Engine
**Priority:** Critical

### Scenario

Given only a `USD -> BRL` exchange rate exists,
when the system searches for `BRL -> USD`,
then the system must not silently invert the existing rate.

### Expected result

The lookup for the inverse pair fails unless that exact pair exists.

### Validation method

Unit or integration test.

---

## AC-008 - Latest Exchange Rate Can Be Retrieved

**Category:** Currency Engine
**Priority:** High

### Scenario

Given multiple exchange rates exist for the same currency pair,
when the evaluator calls:

```http
GET /api/exchange-rates/latest-sourceCurrency=USD&targetCurrency=BRL
```

then the system must return the latest valid exchange rate.

### Expected result

The selected rate is ordered by:

```text
validAt descending
createdAt descending
```

### Validation method

Repository/integration test and API test.

---

## AC-009 - Invalid Exchange Rate Is Rejected

**Category:** Validation
**Priority:** High

### Scenario

Given an exchange-rate request with invalid data,
when the API receives the request,
then the request must be rejected.

### Invalid examples

* rate equal to zero;
* negative rate;
* unsupported source currency;
* unsupported target currency;
* same source and target currency.

### Expected result

The API returns a structured error response with appropriate status.

Expected statuses:

```text
400 Bad Request
422 Unprocessable Entity
```

depending on validation type.

### Validation method

API validation tests.

---

## AC-010 - Same-Currency Pricing Simulation Works

**Category:** Pricing Engine
**Priority:** Critical

### Scenario

Given a valid BRL receivable paid in BRL,
when the operator calls:

```http
POST /api/pricing/simulations
```

then the system must calculate pricing without requiring an exchange rate.

### Expected result

Response includes:

* face value;
* source currency;
* payment currency;
* present value in source currency;
* discount value;
* net payment value;
* base rate;
* spread;
* term in months;
* `exchangeRate = null`;
* calculation timestamp.

For same-currency simulation:

```text
netPaymentValue = presentValueInSourceCurrency
```

after monetary rounding.

### Validation method

Domain unit test and API test.

---

## AC-010A - Pricing Base Rate Resolution Is Explicit

**Category:** Pricing Engine / Configuration
**Priority:** High

### Scenario

Given a pricing or settlement operation needs a base rate,
when the backend resolves the effective base rate,
then it must follow the documented precedence.

### Expected result

The effective base rate is resolved in this order:

```text
1. request baseRate, when explicitly provided
2. server-side DEFAULT_BASE_RATE
3. structured error if neither exists
```

Request-provided `baseRate` must override the server-side fallback.

### Validation method

Application test, API test and configuration review.

---

## AC-011 - Cross-Currency Pricing Simulation Works

**Category:** Pricing Engine
**Priority:** Critical

### Scenario

Given a valid BRL receivable paid in USD and a valid `BRL -> USD` exchange rate,
when the operator calls:

```http
POST /api/pricing/simulations
```

then the system must calculate present value in BRL first and convert the result to USD at the end.

### Expected result

Response includes:

* present value in source currency;
* final net payment value in payment currency;
* exchange rate used;
* discount value in source currency.

### Validation method

Domain unit test and API test.

---

## AC-012 - Cross-Currency Conversion Order Is Correct

**Category:** Financial correctness
**Priority:** Critical

### Scenario

Given a cross-currency pricing input,
when the pricing engine performs the calculation,
then it must apply currency conversion after present value calculation.

### Correct order

```text
1. Calculate present value in source currency.
2. Convert present value to payment currency.
3. Return payment value.
```

### Incorrect order

```text
1. Convert face value.
2. Calculate present value after conversion.
```

### Expected result

A regression test must fail if conversion is applied before discounting.

### Validation method

Domain unit test.

---

## AC-013 - Pricing Uses Strategy Pattern

**Category:** Design / Pricing Engine
**Priority:** Critical

### Scenario

Given a receivable type,
when the pricing engine calculates spread,
then the risk rule must be selected through a Strategy Pattern implementation.

### Expected result

The codebase includes equivalent components to:

```text
PricingStrategy
MercantileDuplicatePricingStrategy
PostDatedCheckPricingStrategy
PricingStrategyResolver
```

### Validation method

Code review and unit tests.

---

## AC-014 - Mercantile Duplicate Spread Is Correct

**Category:** Financial correctness
**Priority:** Critical

### Scenario

Given a receivable of type `MERCANTILE_DUPLICATE`,
when pricing is calculated,
then the monthly spread must be:

```text
0.01500000
```

### Expected result

The system must not represent `1.5%` as `1.5`.

### Validation method

Strategy unit test.

---

## AC-015 - Post-Dated Check Spread Is Correct

**Category:** Financial correctness
**Priority:** Critical

### Scenario

Given a receivable of type `POST_DATED_CHECK`,
when pricing is calculated,
then the monthly spread must be:

```text
0.02500000
```

### Expected result

The system must not represent `2.5%` as `2.5`.

### Validation method

Strategy unit test.

---

## AC-016 - Financial Calculations Do Not Use Floating-Point Types

**Category:** Financial precision
**Priority:** Critical

### Scenario

Given any backend financial calculation,
when code is reviewed or tested,
then monetary values, rates and financial results must not use floating-point types.

### Forbidden types

```text
double
float
Double
Float
```

### Expected result

Financial calculations use:

```text
BigDecimal
```

### Validation method

Code review, unit tests and optional static check.

---

## AC-017 - Pricing Rejects Invalid Due Dates

**Category:** Validation
**Priority:** Critical

### Scenario

Given a pricing request with a past or same-day due date,
when the API receives the request,
then the system must reject the request.

### Expected result

The API returns:

```http
422 Unprocessable Entity
```

with a structured error response.

### Validation method

Domain test and API test.

---

## AC-018 - Pricing Rejects Missing Exchange Rate

**Category:** Currency Engine / Pricing
**Priority:** Critical

### Scenario

Given a cross-currency simulation request and no exchange rate exists for the exact pair,
when pricing is requested,
then the system must reject the simulation.

### Expected result

The API returns:

```http
422 Unprocessable Entity
```

No settlement record is created.

### Validation method

Application test and API test.

---

## AC-019 - Pricing Simulation Is Non-Persistent

**Category:** Pricing Engine
**Priority:** High

### Scenario

Given a valid simulation request,
when the simulation endpoint is called,
then the system must return a pricing result without creating settlement records.

### Expected result

No rows are inserted into:

```text
settlements
settlement_items
```

### Validation method

Application or integration test.

---

## AC-020 - Settlement Batch Can Be Created

**Category:** Settlement Engine
**Priority:** Critical

### Scenario

Given a valid settlement batch,
when the operator calls:

```http
POST /api/settlements
```

then the system must create a settlement and its settlement items.

### Expected result

The API returns:

```http
201 Created
```

Response includes:

* settlement id;
* assignor;
* source currency;
* payment currency;
* status;
* effective base rate used;
* totals;
* item count;
* settlement timestamp;
* settlement item snapshots.

### Validation method

API test and integration test.

---

## AC-021 - Settlement Batch Is Atomic

**Category:** Settlement Engine / ACID
**Priority:** Critical

### Scenario

Given a settlement batch with at least one invalid item,
when settlement creation is requested,
then the system must persist nothing from that settlement.

### Expected result

No partial records exist in:

```text
settlements
settlement_items
```

Receivable statuses must not be partially updated.

### Validation method

Integration test.

---

## AC-022 - Settlement Rejects Empty Batch

**Category:** Validation
**Priority:** High

### Scenario

Given a settlement request with an empty `receivables` list,
when the API receives the request,
then it must reject the request.

### Expected result

The API returns:

```http
400 Bad Request
```

with a structured validation error.

### Validation method

API validation test.

---

## AC-023 - Settlement Enforces Maximum Batch Size

**Category:** Validation / Performance safety
**Priority:** Medium

### Scenario

Given a settlement request with more than 100 receivables,
when the API receives the request,
then it must reject the request.

### Expected result

The API returns:

```http
400 Bad Request
```

with a structured validation error.

### Validation method

API validation test.

---

## AC-024 - Settlement Enforces One Source Currency per Batch

**Category:** Financial correctness / Data model
**Priority:** High

### Scenario

Given a settlement batch containing receivables with different source currencies,
when settlement creation is requested,
then the system must reject the batch.

### Expected result

The API returns:

```http
422 Unprocessable Entity
```

No settlement is persisted.

### Rationale

Settlement header totals are meaningful only when all face values and present values share the same source currency.

### Validation method

Application or API test.

---

## AC-025 - Settlement Stores Calculation Snapshots

**Category:** Auditability
**Priority:** Critical

### Scenario

Given a valid settlement batch,
when settlement is created,
then each settlement item must store the calculation inputs and outputs used at the time of settlement.

### Required snapshot fields

* receivable id;
* external reference;
* receivable type;
* face value;
* source currency;
* payment currency;
* base rate;
* spread;
* term in months;
* present value in source currency;
* discount value;
* payment value;
* exchange rate, when applicable;
* calculation timestamp.

### Validation method

Integration test and settlement detail response inspection.

---

## AC-026 - Historical Settlement Values Are Immutable

**Category:** Auditability
**Priority:** Critical

### Scenario

Given a settlement was created using a specific exchange rate,
when a new exchange rate is later registered,
then the historical settlement detail and statement must still show the original persisted values.

### Expected result

Historical settlement values do not change after reference data changes.

### Validation method

Integration test.

---

## AC-027 - Duplicate Settlement Is Prevented

**Category:** Settlement Engine / Concurrency safety
**Priority:** Critical

### Scenario

Given a receivable has already been settled,
when another settlement attempts to settle the same receivable again,
then the system must reject the second settlement.

### Expected result

The API returns:

```http
409 Conflict
```

The database prevents duplicate settlement through a unique constraint on `settlement_items.receivable_id`.

### Validation method

Integration test and database constraint verification.

---

## AC-028 - Receivable Status Is Updated After Settlement

**Category:** Settlement Engine
**Priority:** High

### Scenario

Given a receivable is available,
when it is successfully settled,
then its status must change to:

```text
SETTLED
```

### Expected result

The receivable cannot be settled again.

### Validation method

Integration test.

---

## AC-029 - Settlement Detail Returns Audit Data

**Category:** Settlement API
**Priority:** High

### Scenario

Given an existing settlement,
when the evaluator calls:

```http
GET /api/settlements/{id}
```

then the API must return settlement details and item-level calculation snapshots.

### Expected result

The response includes persisted values, not recalculated values.

### Validation method

API test.

---

## AC-030 - Unknown Settlement Returns Not Found

**Category:** Settlement API
**Priority:** Medium

### Scenario

Given a settlement id does not exist,
when the evaluator calls:

```http
GET /api/settlements/{id}
```

then the API must return:

```http
404 Not Found
```

### Validation method

API test.

---

## AC-031 - Settlement Statement Supports Filters

**Category:** Analytical queries
**Priority:** Critical

### Scenario

Given settlement records exist,
when the evaluator calls:

```http
GET /api/settlements/statement
```

with filters,
then the API must return only matching settlement rows.

### Required filters

* period;
* assignor;
* payment currency;
* source currency;
* receivable type;
* settlement status.

### Validation method

Integration test or API test.

---

## AC-032 - Settlement Statement Uses Server-Side Pagination

**Category:** Performance / Frontend integration
**Priority:** Critical

### Scenario

Given many settlements exist,
when the frontend requests a statement page,
then the backend must return only the requested page.

### Expected result

Response includes:

* content;
* page;
* size;
* total elements;
* total pages;
* first;
* last.

The frontend must not fetch all rows and paginate locally.

### Validation method

API test and frontend code review.

---

## AC-033 - Settlement Statement Uses Database-Level Filtering

**Category:** Performance
**Priority:** Critical

### Scenario

Given a statement query with filters,
when the backend processes the request,
then filtering must happen in the database.

### Forbidden implementation

```text
repository.findAll().stream().filter(...)
```

### Expected result

Implementation uses database query, projection, query builder or native SQL.

### Validation method

Code review and integration test.

---

## AC-034 - Statement Rejects Invalid Date Range

**Category:** Validation
**Priority:** High

### Scenario

Given `from` is greater than `to`,
when the statement endpoint is called,
then the system must reject the request.

### Expected result

The API returns:

```http
400 Bad Request
```

with a structured error response.

### Validation method

API test.

---

## AC-035 - Statement Enforces Pagination Limits

**Category:** Validation / Performance
**Priority:** High

### Scenario

Given a page size greater than 100,
when the statement endpoint is called,
then the system must reject or clamp the request according to the documented behavior.

### Initial expected behavior

Reject the request.

### Expected result

The API returns:

```http
400 Bad Request
```

### Validation method

API test.

---

## AC-036 - API Errors Are Structured

**Category:** Error handling
**Priority:** Critical

### Scenario

Given any validation or business error,
when an API endpoint returns an error,
then the response must follow the common error structure.

### Expected fields

```text
timestamp
status
error
message
path
details
```

### Validation method

API tests.

---

## AC-037 - Global Exception Handling Is Implemented

**Category:** Error handling
**Priority:** High

### Scenario

Given an unexpected backend error occurs,
when the API returns a response,
then the response must be controlled and must not expose stack traces.

### Expected result

The API returns a structured error response.

### Validation method

Code review and API test where feasible.

---

## AC-038 - Backend Validation Is Mandatory

**Category:** Security / Validation
**Priority:** Critical

### Scenario

Given the frontend sends invalid input or a direct API call bypasses the frontend,
when the backend receives the request,
then the backend must validate the input.

### Expected result

Invalid input does not reach business-critical calculation or persistence logic.

### Validation method

API validation tests.

---

## AC-039 - No Secrets Are Committed

**Category:** Security
**Priority:** Critical

### Scenario

Given the final repository,
when files are reviewed,
then no real credentials, tokens, private keys or production secrets must be committed.

### Expected result

Only `.env.example` with safe example values is committed.

`.env` is ignored.

### Validation method

Repository review.

---

## AC-040 - CORS Is Explicit

**Category:** Security / Local integration
**Priority:** Medium

### Scenario

Given the Angular frontend runs at:

```text
http://localhost:4200
```

when it calls the backend,
then the backend must allow that origin through explicit configuration.

### Expected result

CORS origin is configurable and not hardcoded as unrestricted wildcard in final local delivery.

### Validation method

Manual frontend integration test and configuration review.

---

## AC-041 - Swagger/OpenAPI Is Available

**Category:** API documentation
**Priority:** High

### Scenario

Given the backend is running,
when the evaluator opens the Swagger UI,
then API documentation must be available.

### Expected URL

```text
http://localhost:8080/swagger-ui.html
```

or equivalent Springdoc URL.

### Expected result

Swagger documents the main endpoints, request schemas and response schemas.

### Validation method

Manual validation.

---

## AC-042 - OpenAPI Documents Main Endpoints

**Category:** API documentation
**Priority:** Medium

### Scenario

Given Swagger/OpenAPI is available,
when the evaluator inspects the API,
then the core endpoints must be documented.

### Required endpoints

```text
POST /api/exchange-rates
GET  /api/exchange-rates/latest
GET  /api/reference-data/currencies
GET  /api/reference-data/receivable-types
POST /api/pricing/simulations
POST /api/settlements
GET  /api/settlements/{id}
GET  /api/settlements/statement
```

### Validation method

Manual Swagger inspection.

---

## AC-043 - Angular Frontend Loads

**Category:** Frontend
**Priority:** High

### Scenario

Given the frontend service is running,
when the evaluator opens:

```text
http://localhost:4200
```

then the Angular application must load successfully.

### Expected result

The application renders the operator interface without console-breaking runtime errors.

### Validation method

Manual browser validation.

---

## AC-044 - Frontend Pricing Simulation Form Works

**Category:** Frontend
**Priority:** High

### Scenario

Given the operator fills a valid pricing simulation form,
when the simulation is submitted,
then the frontend must call the backend simulation endpoint and display the returned values.

### Expected result

The frontend displays:

* present value;
* discount;
* net payment value;
* source currency;
* payment currency;
* exchange rate when applicable.

### Validation method

Manual UI validation or frontend test.

---

## AC-045 - Frontend Does Not Own Official Calculation

**Category:** Frontend / Financial correctness
**Priority:** Critical

### Scenario

Given the frontend displays pricing simulation values,
when the code is reviewed,
then the frontend must not implement the official financial formula.

### Expected result

The frontend calls:

```http
POST /api/pricing/simulations
```

and displays backend results.

### Validation method

Frontend code review.

---

## AC-046 - Frontend Statement Grid Uses Server-Side Pagination

**Category:** Frontend / Performance
**Priority:** Critical

### Scenario

Given the operator changes the settlement grid page,
when the page changes,
then the frontend must request the selected page from the backend.

### Expected result

The frontend calls:

```http
GET /api/settlements/statement-page=<page>&size=<size>
```

The frontend does not fetch all records and paginate locally.

### Validation method

Frontend code review, browser network inspection or frontend test.

---

## AC-047 - Frontend Displays Backend Errors Clearly

**Category:** Frontend / Usability
**Priority:** High

### Scenario

Given the backend returns a validation or business error,
when the frontend receives the error,
then the frontend must show a clear message to the operator.

### Expected result

The operator can understand what failed, such as:

* missing exchange rate;
* invalid due date;
* invalid face value;
* duplicate settlement.

### Validation method

Manual UI validation or frontend test.

---

## AC-048 - UI Provides Basic Usability

**Category:** Usability
**Priority:** Medium

### Scenario

Given the operator uses the frontend,
when interacting with forms and grids,
then the UI must provide basic usability support.

### Expected result

The UI includes:

* labels for form fields;
* clear required fields;
* validation messages;
* loading states;
* empty states;
* readable monetary values;
* readable date values.

### Validation method

Manual UI review.

---

## AC-049 - Monetary Values Display Currency Context

**Category:** Usability / Financial clarity
**Priority:** High

### Scenario

Given the frontend displays monetary values,
when values are rendered,
then currency context must be visible.

### Expected result

Examples:

```text
BRL 10,000.00
USD 1,850.45
```

Cross-currency results must show both source and payment currencies where relevant.

### Validation method

Manual UI review.

---

## AC-050 - Backend Tests Cover Pricing Strategies

**Category:** Testing
**Priority:** Critical

### Scenario

Given the test suite exists,
when backend tests run,
then pricing strategy behavior must be covered.

### Required cases

* Mercantile Duplicate spread;
* Post-Dated Check spread;
* strategy resolver;
* unsupported receivable type.

### Validation method

Run:

```bash
cd backend
mvn test
```

---

## AC-051 - Backend Tests Cover Financial Calculations

**Category:** Testing
**Priority:** Critical

### Scenario

Given the test suite exists,
when backend tests run,
then financial calculation behavior must be covered.

### Required cases

* same-currency pricing;
* cross-currency pricing;
* conversion order;
* request base rate overrides configured fallback;
* configured fallback is used when request base rate is omitted;
* missing request base rate and missing fallback fails clearly;
* discount calculation;
* rounding behavior;
* invalid due date;
* missing exchange rate.

### Validation method

Run:

```bash
cd backend
mvn test
```

---

## AC-052 - Backend Tests Cover Settlement Safety

**Category:** Testing
**Priority:** High

### Scenario

Given the test suite exists,
when backend tests run,
then settlement safety behavior must be covered.

### Required cases

* atomic rollback;
* duplicate settlement prevention;
* missing exchange rate during settlement;
* invalid item in batch.

### Validation method

Run backend tests or integration tests.

---

## AC-053 - Backend Tests Cover Statement Query

**Category:** Testing
**Priority:** High

### Scenario

Given statement query functionality exists,
when tests run,
then filters and pagination must be covered.

### Required cases

* date range filter;
* currency filter;
* receivable type filter;
* pagination;
* invalid date range.

### Validation method

Integration or API tests.

---

## AC-054 - Frontend Build Succeeds

**Category:** Frontend / Build
**Priority:** High

### Scenario

Given frontend dependencies are installed,
when the evaluator runs:

```bash
cd frontend
npm run build
```

then the Angular application must build successfully.

### Validation method

Build command.

---

## AC-055 - Backend Build Succeeds

**Category:** Backend / Build
**Priority:** Critical

### Scenario

Given backend dependencies are available,
when the evaluator runs:

```bash
cd backend
mvn test
```

or:

```bash
cd backend
mvn verify
```

then the backend must compile and tests must pass.

### Validation method

Build/test command.

---

## AC-056 - README Explains How to Run the Project

**Category:** Documentation
**Priority:** Critical

### Scenario

Given the evaluator opens `README.md`,
when reading setup instructions,
then they must be able to run the project locally.

### Expected content

README includes:

* prerequisites;
* Docker Compose command;
* backend local command;
* frontend local command;
* environment variables;
* Swagger URL;
* frontend URL;
* known assumptions.

### Validation method

Documentation review.

---

## AC-057 - README Explains Architecture and Decisions

**Category:** Documentation
**Priority:** High

### Scenario

Given the evaluator opens `README.md`,
when reading project documentation,
then they must understand the architecture and main decisions.

### Expected content

README includes:

* stack;
* architecture summary;
* business assumptions;
* data model reference;
* testing strategy;
* Git workflow;
* AI usage reference.

### Validation method

Documentation review.

---

## AC-058 - ER Diagram Exists

**Category:** Documentation / Data modeling
**Priority:** Critical

### Scenario

Given the evaluator opens the repository,
when inspecting documentation,
then an ER diagram must exist.

### Expected file

```text
docs/diagrams/er-diagram.md
```

### Expected result

The ER diagram matches the Flyway schema and data model spec.

### Validation method

Documentation review.

---

## AC-059 - C4 Diagrams Exist

**Category:** Documentation / Architecture
**Priority:** Medium

### Scenario

Given the evaluator opens the repository,
when inspecting architecture documentation,
then C4 context and container diagrams should exist.

### Expected files

```text
docs/diagrams/c4-context.md
docs/diagrams/c4-container.md
```

### Validation method

Documentation review.

---

## AC-060 - DDL or Flyway Migrations Are Provided

**Category:** Documentation / Database
**Priority:** Critical

### Scenario

Given the evaluator inspects the repository,
when looking for database schema definition,
then they must find SQL DDL through Flyway migrations.

### Expected location

```text
backend/src/main/resources/db/migration
```

### Validation method

Repository review.

---

## AC-061 - AI Usage Is Documented

**Category:** AI transparency
**Priority:** Critical

### Scenario

Given the evaluator opens `AI_USAGE.md`,
when reading it,
then they must understand how AI was used in the project.

### Expected content

AI usage documentation includes:

* tools used;
* prompt summaries;
* AI contribution;
* author review;
* corrected or rejected AI output;
* critical analysis;
* final ownership statement.

### Validation method

Documentation review.

---

## AC-062 - AI-Generated Output Was Reviewed

**Category:** AI transparency / Quality
**Priority:** High

### Scenario

Given AI materially contributed to implementation or documentation,
when `AI_USAGE.md` is reviewed,
then it must describe how the author reviewed the output.

### Expected result

The document should not merely say:

```text
Used AI to help.
```

It must explain review and corrections.

### Validation method

Documentation review.

---

## AC-063 - Git History Uses Conventional Commits

**Category:** Git workflow
**Priority:** Critical

### Scenario

Given the evaluator inspects Git history,
when reading commit messages,
then commits must follow Conventional Commits after the initial documentation baseline.

### Expected examples

```text
feat: implement pricing strategy engine
test: cover settlement rollback
docs: finalize delivery documentation
```

### Validation method

Git history review.

---

## AC-064 - Feature Work Uses Branches and PRs

**Category:** Git workflow
**Priority:** High

### Scenario

Given the evaluator inspects repository activity,
when reviewing project workflow,
then feature work should have been developed in feature branches and integrated through simulated Pull Requests.

### Expected result

Features are traceable by branch and PR description.

### Validation method

Repository and PR review.

---

## AC-065 - Final Release Is Tagged

**Category:** Git workflow / Release
**Priority:** Medium

### Scenario

Given final delivery is complete,
when the project is merged into `main`,
then the final version should be tagged.

### Expected tag

```text
v1.0.0
```

### Validation method

Git tag review.

---

## AC-066 - Project Avoids Unnecessary Overengineering

**Category:** Scope control
**Priority:** High

### Scenario

Given the project targets Pleno-level delivery,
when implementation is reviewed,
then required functionality must be complete before optional senior-level infrastructure is added.

### Expected result

The implementation does not introduce unnecessary components such as:

* Kafka;
* Kubernetes;
* full microservices;
* full CQRS;
* event sourcing;
* NgRx without need.

### Validation method

Code and architecture review.

---

## AC-067 - Project Documents Future Scalability Considerations

**Category:** Scalability
**Priority:** Medium

### Scenario

Given the system is a modular monolith,
when scalability is discussed in documentation,
then future options should be documented without overcomplicating the initial implementation.

### Expected topics

Possible future enhancements:

* caching reference data;
* read replicas;
* asynchronous settlement processing;
* outbox pattern;
* event-driven architecture;
* sharding;
* analytics read model.

### Validation method

README or architecture documentation review.

---

## AC-068 - Analytical Query Design Considers Large Data Volumes

**Category:** Performance / Scalability
**Priority:** High

### Scenario

Given settlement statement queries may process historical data,
when implementation is reviewed,
then query design must support large datasets better than in-memory filtering.

### Expected result

The implementation uses:

* database filters;
* indexes;
* pagination;
* projections or optimized queries.

### Validation method

Code review and integration tests.

---

## AC-069 - Logs Are Useful and Safe

**Category:** Observability
**Priority:** Medium

### Scenario

Given the backend runs and operations occur,
when logs are reviewed,
then logs should help diagnose behavior without exposing secrets.

### Expected logs

Useful events include:

* application startup;
* exchange-rate creation;
* settlement creation;
* settlement failure;
* unexpected errors.

### Validation method

Manual log review.

---

## AC-070 - Final Delivery Checklist Is Complete

**Category:** Delivery readiness
**Priority:** Critical

### Scenario

Given implementation is complete,
when the evaluator or author checks final delivery readiness,
then `docs/specs/11-delivery-checklist.md` must be complete and aligned with the delivered project.

### Expected result

The checklist covers:

* backend;
* frontend;
* database;
* Docker;
* tests;
* documentation;
* Git;
* AI usage;
* release.

### Validation method

Checklist review.

## 4. Minimum Acceptance Set for Pleno Delivery

The following criteria are considered the minimum required set for a strong Pleno-level delivery:

```text
AC-001 - Local Stack Starts with Docker Compose
AC-002 - Flyway Creates the Database Schema
AC-006 - Exchange Rate Can Be Registered
AC-010 - Same-Currency Pricing Simulation Works
AC-011 - Cross-Currency Pricing Simulation Works
AC-012 - Cross-Currency Conversion Order Is Correct
AC-013 - Pricing Uses Strategy Pattern
AC-016 - Financial Calculations Do Not Use Floating-Point Types
AC-020 - Settlement Batch Can Be Created
AC-021 - Settlement Batch Is Atomic
AC-025 - Settlement Stores Calculation Snapshots
AC-027 - Duplicate Settlement Is Prevented
AC-031 - Settlement Statement Supports Filters
AC-032 - Settlement Statement Uses Server-Side Pagination
AC-033 - Settlement Statement Uses Database-Level Filtering
AC-036 - API Errors Are Structured
AC-038 - Backend Validation Is Mandatory
AC-041 - Swagger/OpenAPI Is Available
AC-043 - Angular Frontend Loads
AC-044 - Frontend Pricing Simulation Form Works
AC-045 - Frontend Does Not Own Official Calculation
AC-046 - Frontend Statement Grid Uses Server-Side Pagination
AC-050 - Backend Tests Cover Pricing Strategies
AC-051 - Backend Tests Cover Financial Calculations
AC-052 - Backend Tests Cover Settlement Safety
AC-056 - README Explains How to Run the Project
AC-058 - ER Diagram Exists
AC-060 - DDL or Flyway Migrations Are Provided
AC-061 - AI Usage Is Documented
AC-063 - Git History Uses Conventional Commits
```

## 5. Final Acceptance Rule

The project is acceptable for delivery when:

```text
1. Critical criteria are satisfied.
2. High-priority criteria are satisfied or explicitly justified.
3. Backend and frontend run locally.
4. Financial calculation rules are tested.
5. Settlement persistence is atomic and auditable.
6. Statement queries are paginated and database-filtered.
7. Documentation explains setup, architecture and trade-offs.
8. AI usage is transparently documented.
9. Git history demonstrates organized development.
10. Final delivery is merged into main and tagged when ready.
```

## 6. Related Documents

```text
README.md
AGENTS.md
AI_USAGE.md
docs/specs/01-product-brief.md
docs/specs/02-domain-glossary.md
docs/specs/03-business-rules.md
docs/specs/04-api-contract.md
docs/specs/05-data-model.md
docs/specs/06-architecture.md
docs/specs/07-testing-strategy.md
docs/specs/09-git-workflow.md
docs/specs/10-ai-workflow.md
docs/specs/11-delivery-checklist.md
docs/adr/ADR-001-backend-stack.md
docs/adr/ADR-002-database-choice.md
docs/adr/ADR-003-money-precision.md
docs/adr/ADR-004-architecture-style.md
docs/adr/ADR-005-frontend-stack.md
docs/adr/ADR-006-git-workflow.md
docs/adr/ADR-007-ai-assisted-development.md
```
