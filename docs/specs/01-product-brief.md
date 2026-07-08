# 01 — Product Brief

## 1. Product Name

**SRM Credit Engine**

## 2. Product Summary

The SRM Credit Engine is a multi-currency credit assignment platform designed to support the pricing and settlement of receivables in a financial operation.

The system receives receivables from assignors, applies risk-based discount rules according to the receivable type, handles BRL and USD currency scenarios, and records settlement transactions in an auditable and consistent way.

The product must serve both as a functional application and as a technical demonstration of software engineering maturity for a mid-level Software Engineer role.

## 3. Business Context

SRM Asset operates in the investment fund market, especially with FIDCs, acquiring credit rights such as invoices, contracts and receivables from companies.

In this business model, assignors sell receivables to obtain liquidity before the original due date. The fund then prices these receivables by applying financial discounting rules based on risk, term, base rate and asset type.

With the addition of a multi-currency cash operation involving BRL and USD, the operation requires a system capable of:

* storing exchange rates;
* pricing receivables accurately;
* applying currency conversion when needed;
* settling receivable batches safely;
* keeping an auditable record of each calculation and settlement.

## 4. Business Problem

The operations desk needs a system that can receive receivable batches, calculate the discounted acquisition value, apply the correct risk spread, convert currencies when applicable, and register the settlement transaction reliably.

The main business risks addressed by the system are:

* incorrect pricing due to wrong spread application;
* monetary precision errors;
* inconsistent currency conversion;
* non-auditable settlement calculations;
* partially persisted settlement batches;
* duplicate settlement of the same receivable;
* inefficient access to historical settlement data.

## 5. Product Goals

The product must achieve the following goals:

1. Provide a reliable backend for receivable pricing and settlement.
2. Support BRL and USD currency scenarios.
3. Store and retrieve exchange rates.
4. Apply receivable-specific risk rules using Strategy Pattern.
5. Calculate present value using decimal-safe financial arithmetic.
6. Persist settlement transactions with auditable calculation snapshots.
7. Expose REST APIs with OpenAPI/Swagger documentation.
8. Provide an Angular frontend for operator interaction.
9. Support paginated and filtered settlement statement queries.
10. Demonstrate clean architecture, robust validation, testability and organized Git workflow.

## 6. Product Non-Goals

The following items are intentionally out of scope for the initial delivery:

1. Real integration with external exchange-rate providers.
2. Real banking payment execution.
3. Real authentication and authorization.
4. User profile management.
5. Production-grade cloud deployment.
6. Kubernetes deployment.
7. Event-driven processing with Kafka or similar tools.
8. Real-time market data streaming.
9. Advanced role-based access control.
10. Accounting ledger integration.
11. Regulatory reporting integration.
12. Multi-tenant support.

These items may be discussed as future improvements, but they are not required for the initial technical challenge delivery.

## 7. Target Users

### 7.1 Operations Desk Operator

Primary user responsible for simulating and registering receivable settlements.

Needs:

* input receivable data;
* simulate net settlement values;
* review pricing results;
* settle receivable batches;
* inspect previous transactions.

### 7.2 Backoffice / Financial Analyst

User responsible for reviewing historical transactions and validating settlement records.

Needs:

* filter settlement statements by date range;
* filter by assignor;
* filter by payment currency;
* filter by receivable type;
* inspect calculation details;
* verify audit information.

### 7.3 Technical Evaluator

Reviewer of the technical challenge.

Needs:

* understand the architecture;
* run the application locally;
* inspect code organization;
* inspect Git history;
* validate business rules;
* review tests;
* review documentation;
* assess AI usage transparency.

## 8. Core Product Capabilities

### 8.1 Exchange Rate Management

The system must allow exchange rates to be stored and retrieved.

Minimum capabilities:

* create a manual exchange rate;
* retrieve the latest exchange rate for a currency pair;
* validate that rates are positive;
* support BRL/USD and USD/BRL scenarios;
* persist exchange-rate metadata for auditability.

### 8.2 Pricing Simulation

The system must allow operators to simulate receivable pricing without creating a settlement transaction.

Minimum capabilities:

* receive face value;
* receive source currency;
* receive payment currency;
* receive base rate;
* receive receivable type;
* receive due date;
* calculate present value;
* apply receivable-type spread;
* apply currency conversion when applicable;
* return discount, present value and net payment value.

The simulation is not a settlement and must not create settlement records.

### 8.3 Settlement Batch Processing

The system must allow a batch of receivables to be settled atomically.

Minimum capabilities:

* receive assignor information;
* receive payment currency;
* receive one or more receivables;
* validate all receivables before settlement;
* calculate each settlement item;
* persist the settlement header;
* persist all settlement items;
* rollback the entire operation if one item fails;
* prevent duplicate settlement of the same receivable.

### 8.4 Settlement Statement

The system must provide analytical access to historical settlements.

Minimum capabilities:

* filter by date range;
* filter by assignor;
* filter by currency;
* filter by receivable type;
* paginate results server-side;
* return relevant settlement summary fields;
* avoid loading large datasets into memory for filtering.

### 8.5 Operator Frontend

The system must provide a frontend interface for operational usage.

Minimum capabilities:

* form for pricing simulation;
* real-time or near-real-time simulation through backend API;
* display of calculated values;
* settlement history grid;
* server-side pagination;
* dynamic filters;
* clear error feedback.

## 9. Functional Scope

The initial delivery must include the following functional modules:

```text id="p6xnnc"
1. Currency Engine
2. Pricing Engine
3. Settlement Engine
4. Settlement Statement Query
5. Operator Frontend
6. API Documentation
7. Database Migration and Seed Data
```

## 10. Currency Scope

The initial supported currencies are:

```text id="crkx7d"
BRL
USD
```

Currency behavior:

* same-currency operations do not require exchange-rate conversion;
* cross-currency operations require an available exchange rate;
* the exchange rate must be applied after present value calculation;
* the exchange rate used in a settlement must be persisted as an immutable snapshot.

## 11. Receivable Type Scope

The initial supported receivable types are:

```text id="o3q0he"
MERCANTILE_DUPLICATE
POST_DATED_CHECK
```

Initial monthly spreads:

```text id="zj8n4h"
MERCANTILE_DUPLICATE = 1.5% a.m.
POST_DATED_CHECK    = 2.5% a.m.
```

The pricing engine must support adding new receivable types or pricing strategies without modifying the core pricing flow excessively.

## 12. Pricing Scope

The base pricing formula is:

```text id="j7d9r7"
Present Value = Face Value / (1 + Base Rate + Spread) ^ Term
```

The calculation must consider:

* face value;
* base rate;
* receivable-type spread;
* term;
* source currency;
* payment currency;
* exchange rate when source currency differs from payment currency.

The backend is the official source of pricing results.

The frontend may display simulations, but it must call the backend simulation endpoint instead of independently implementing the official financial calculation.

## 13. Auditability Scope

Settlement records must be auditable.

A settlement item must preserve the calculation snapshot used at the time of settlement, including:

* face value;
* source currency;
* payment currency;
* base rate;
* spread;
* term;
* present value;
* discount value;
* payment value;
* exchange rate used, when applicable;
* receivable type;
* calculation timestamp.

Historical settlement values must not change when future rates, spreads or rules are updated.

## 14. Data Scope

The product requires a relational data model.

The initial data model must represent at least:

* currencies;
* exchange rates;
* assignors;
* receivable types;
* receivables;
* settlements;
* settlement items.

The model must support:

* relational integrity;
* financial auditability;
* settlement uniqueness;
* analytical filtering;
* migration-based schema evolution.

## 15. API Scope

The backend must expose REST APIs for:

* exchange-rate creation;
* latest exchange-rate lookup;
* pricing simulation;
* settlement creation;
* settlement detail retrieval;
* settlement statement filtering.

The API must use:

* semantic HTTP methods;
* semantic HTTP status codes;
* structured validation errors;
* structured business errors;
* OpenAPI/Swagger documentation.

## 16. Frontend Scope

The Angular frontend must include:

* an operator-facing pricing simulation screen;
* a settlement history screen;
* filters for settlement history;
* server-side pagination;
* integration with backend APIs;
* clear display of monetary and currency values.

The frontend should remain intentionally simple and focused on the challenge scope.

## 17. Non-Functional Requirements

### 17.1 Precision

Financial calculations must use decimal-safe arithmetic.

Floating-point types are not acceptable for money, rates or financial results.

### 17.2 Consistency

Settlement batch processing must respect ACID principles.

No settlement batch may be partially persisted.

### 17.3 Auditability

Every settlement must be explainable after persistence using stored calculation snapshots.

### 17.4 Resilience

Unexpected backend errors must be handled through global exception handling and structured responses.

### 17.5 Validation

Input validation must be enforced on the backend.

Frontend validation improves usability, but backend validation is mandatory.

### 17.6 Performance

Settlement statement queries must be designed for large historical datasets.

Filtering and pagination must occur at database level.

### 17.7 Maintainability

The codebase must be organized with clear separation of concerns.

Business logic must not be placed directly in controllers or UI components.

### 17.8 Testability

Business-critical rules must be covered by automated tests.

Pricing strategy tests are mandatory.

### 17.9 Local Reproducibility

The final project must be runnable locally through Docker Compose.

### 17.10 Documentation

The repository must provide enough documentation for a technical evaluator to understand:

* how to run the project;
* what the system does;
* which decisions were made;
* how the architecture is organized;
* how AI was used.

## 18. Delivery Scope by Seniority

The target delivery level is **Pleno / Mid-Level**, with selected senior-level documentation where it is low-risk and valuable.

### 18.1 Required for Pleno-Level Delivery

The delivery must include:

* functional backend;
* functional frontend;
* Docker Compose;
* global exception handling;
* robust input validation;
* unit tests for pricing rules;
* Strategy Pattern for pricing;
* Conventional Commits;
* simulated Pull Requests;
* clear README;
* normalized relational database;
* ER diagram;
* DDL or Flyway migrations;
* OpenAPI/Swagger documentation;
* AI usage documentation.

### 18.2 Controlled Differentials

The delivery may also include:

* C4 context diagram;
* C4 container diagram;
* Git release tag;
* GitHub Actions pipeline;
* structured logging;
* optimistic locking for settlement safety.

These items should not compromise the completeness or quality of the required Pleno-level scope.

## 19. Success Metrics

The project will be considered successful if:

1. The backend correctly calculates pricing for supported receivable types.
2. Cross-currency conversion is applied in the correct order.
3. Monetary calculations avoid unsafe floating-point arithmetic.
4. Settlement batches are persisted atomically.
5. Settlement records are auditable.
6. The API is documented and usable.
7. The frontend allows simulation and history consultation.
8. Statement queries support server-side filtering and pagination.
9. The project runs locally through Docker Compose.
10. The code is organized, testable and documented.
11. Git history demonstrates incremental development.
12. AI usage is transparently documented.

## 20. Risks and Mitigations

### 20.1 Risk: Treating the Product as CRUD

Mitigation:

* keep pricing, settlement and auditability as core domain concerns;
* test business rules explicitly;
* document financial assumptions.

### 20.2 Risk: Incorrect Monetary Precision

Mitigation:

* use decimal-safe types;
* forbid floating-point financial calculations;
* define scale and rounding rules;
* test rounding behavior.

### 20.3 Risk: Incorrect Currency Conversion

Mitigation:

* apply conversion only after present value calculation;
* persist exchange-rate snapshot;
* test cross-currency scenarios.

### 20.4 Risk: Partial Settlement Persistence

Mitigation:

* use transactional application services;
* validate settlement batches;
* rollback on item failure;
* test atomicity.

### 20.5 Risk: Superficial Strategy Pattern

Mitigation:

* define explicit pricing strategy abstraction;
* create one implementation per receivable risk rule;
* cover strategy behavior with unit tests.

### 20.6 Risk: Inefficient Statement Query

Mitigation:

* filter at database level;
* use pagination;
* add indexes;
* avoid in-memory filtering.

### 20.7 Risk: Frontend and Backend Calculation Divergence

Mitigation:

* official calculation stays in backend;
* frontend calls simulation endpoint;
* frontend does not duplicate financial formula for settlement.

### 20.8 Risk: Overengineering

Mitigation:

* prioritize Pleno-level deliverables;
* document future architecture options instead of implementing unnecessary infrastructure;
* avoid distributed components unless required.

## 21. Out-of-Scope Future Enhancements

Potential future enhancements include:

* authentication and authorization;
* user roles;
* real exchange-rate provider integration;
* approval workflow for settlement;
* export of settlement statements;
* event-driven settlement processing;
* audit event stream;
* advanced observability with metrics and tracing;
* cloud deployment;
* Kubernetes manifests;
* infrastructure as code;
* sharding strategy for high-volume transaction processing.

## 22. Product Assumptions

The initial implementation assumes:

1. The system is used internally by an operations team.
2. Currency support is limited to BRL and USD.
3. Exchange rates are manually registered or mock-integrated.
4. Receivable types and spreads are known at implementation time.
5. The backend owns all official financial calculations.
6. The frontend is an operational interface, not a financial calculation authority.
7. Local execution through Docker Compose is sufficient for the technical challenge.
8. Authentication is not part of the initial scope.
9. Settlement records must remain historically explainable even if future rules change.

## 23. Current Project Phase

```text id="uqs8w7"
Phase: Specification phase
Branch: develop
Goal: consolidate documentation, architecture decisions, diagrams and AI prompts before implementation
```
