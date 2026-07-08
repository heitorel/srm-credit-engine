# 03 - Business Rules

## 1. Purpose

This document defines the mandatory business rules for the **SRM Credit Engine**.

These rules guide the implementation of:

* exchange-rate management;
* receivable pricing;
* risk spread selection;
* cross-currency conversion;
* settlement batch processing;
* auditability;
* validation;
* duplicate settlement prevention;
* analytical settlement queries.

Business logic implemented in the backend must not contradict this document.

If a future implementation decision requires changing one of these rules, this file must be updated before or alongside the code change.

## 2. Rule Classification

Business rules are classified as:

| Type               | Meaning                                            |
| ------------------ | -------------------------------------------------- |
| Mandatory          | Must be implemented for the initial delivery       |
| Assumption         | Project interpretation used to resolve ambiguity   |
| Validation         | Input or state validation required by the backend  |
| Audit              | Rule required for traceability                     |
| Technical-business | Technical implementation rule with business impact |

## 3. Core Financial Principles

### BR-001 - Decimal-Safe Financial Arithmetic

**Type:** Mandatory
**Priority:** Critical

All monetary values, rates and financial calculation results must use decimal-safe representation.

In the Java backend, financial calculations must use `BigDecimal`.

Forbidden for financial calculations:

```text id="f9sbqa"
double
float
Double
Float
```

This applies to:

* face value;
* present value;
* discount value;
* payment value;
* base rate;
* spread;
* term;
* exchange rate;
* totals.

Rationale:

Financial systems must avoid binary floating-point rounding errors in monetary and rate calculations.

---

### BR-002 - Explicit Rounding

**Type:** Mandatory
**Priority:** Critical

The system must define explicit rounding behavior for monetary outputs.

Initial rule:

```text id="mve4js"
Monetary values: scale 2
Rates: scale 8
Intermediate calculations: scale 12 or higher when needed
Rounding mode: HALF_UP
```

Rules:

* intermediate calculations must preserve more precision than final monetary outputs;
* rounding must occur only at controlled boundaries;
* API responses for monetary values should expose values rounded to monetary scale;
* persisted audit snapshots must preserve enough precision to explain calculations.

Implementation notes:

* use a centralized financial math component or value object;
* avoid scattering rounding logic across controllers, services and mappers.

---

### BR-003 - Backend as Official Calculation Source

**Type:** Mandatory
**Priority:** Critical

The backend is the official source for financial calculation.

The frontend may collect inputs and display results, but it must not independently implement the official pricing or settlement formula.

Rules:

* pricing simulation must call the backend;
* settlement calculation must occur in backend domain/application logic;
* frontend must not duplicate official settlement calculation logic;
* frontend may format values for display only.

Rationale:

Duplicating the financial formula in the frontend can cause divergence between preview and settlement values.

---

## 4. Currency Rules

### BR-004 - Supported Currencies

**Type:** Mandatory
**Priority:** High

The initial supported currencies are:

```text id="sh6mo8"
BRL
USD
```

Rules:

* currency codes must be uppercase;
* unsupported currencies must be rejected;
* every monetary value must be associated with a currency;
* same-currency operations do not require exchange-rate conversion;
* cross-currency operations require an available exchange rate.

---

### BR-005 - Currency Code Validation

**Type:** Validation
**Priority:** High

Currency inputs must be validated.

A valid currency code must:

* be present;
* be uppercase;
* be supported by the system;
* be represented consistently in API, domain and database layers.

Invalid examples:

```text id="a9d0fr"
brl
REAL
US
EUR
null
""
```

Expected API behavior:

```text id="izlljr"
400 Bad Request
```

or:

```text id="8l7fmw"
422 Unprocessable Entity
```

depending on whether the error is treated as syntactic validation or business validation.

---

### BR-006 - Source Currency

**Type:** Mandatory
**Priority:** High

The source currency is the currency in which the receivable is denominated.

Rules:

* each receivable must have one source currency;
* source currency must be supported;
* present value must initially be calculated in source currency.

---

### BR-007 - Payment Currency

**Type:** Mandatory
**Priority:** High

The payment currency is the currency used to settle the receivable.

Rules:

* each pricing simulation request must provide a payment currency;
* each settlement must have one payment currency;
* payment currency must be supported;
* if payment currency differs from source currency, cross-currency rules apply.

---

### BR-007A - One Source Currency per Settlement Batch

**Type:** Mandatory
**Priority:** High

All receivables in the same settlement batch must share the same source currency.

Rules:

* mixed-source-currency batches must be rejected;
* the common source currency becomes the settlement header source currency;
* this rule exists so settlement header totals remain meaningful in a single source currency;
* the initial expected API behavior for a mixed-source-currency batch is `422 Unprocessable Entity`.

---

## 5. Exchange Rate Rules

### BR-008 - Exchange Rate Registration

**Type:** Mandatory
**Priority:** High

The system must support manual exchange-rate registration.

An exchange rate must include:

* source currency;
* target currency;
* rate;
* validity timestamp;
* creation timestamp.

Rules:

* rate must be greater than zero;
* source currency and target currency must be supported;
* source currency and target currency must be different;
* exchange rates must not be hardcoded inside pricing logic.

---

### BR-009 - Exchange Rate Direction Is Explicit

**Type:** Mandatory
**Priority:** Critical

Exchange-rate direction must be explicit.

Example:

```text id="v2adpw"
USD -> BRL
```

is not the same stored pair as:

```text id="1dsq93"
BRL -> USD
```

Rules:

* the system must use the exchange rate for the exact required direction;
* if the exact pair is unavailable, the initial implementation must fail clearly;
* the initial implementation must not silently invert exchange rates unless this behavior is explicitly implemented, tested and documented.

Rationale:

Implicit inversion can create hidden financial risk if precision and rate source are not controlled.

---

### BR-010 - Latest Exchange Rate Lookup

**Type:** Mandatory
**Priority:** High

For pricing and settlement, the system must be able to retrieve the latest available exchange rate for a currency pair.

Rules:

* lookup must filter by source currency and target currency;
* lookup must return the most recent valid rate;
* if no rate is available for a required pair, the operation must fail;
* same-currency operations must not require exchange-rate lookup.

Expected API behavior for missing exchange rate:

* `GET /api/exchange-rates/latest` returns `404 Not Found` when the exact pair does not exist;
* pricing or settlement operations that depend on that exact pair return `422 Unprocessable Entity`.

---

### BR-011 - Exchange Rate Snapshot

**Type:** Audit
**Priority:** Critical

When a cross-currency settlement is created, the exchange rate used must be persisted as a snapshot in the settlement item.

Rules:

* historical settlement values must not change when new exchange rates are registered;
* each cross-currency settlement item must store the rate used;
* same-currency settlement items may store a null exchange rate;
* the snapshot must be sufficient to explain the historical calculation.

Rationale:

Auditability requires preserving the rate used at settlement time.

---

## 6. Receivable Type and Spread Rules

### BR-012 - Supported Receivable Types

**Type:** Mandatory
**Priority:** High

The initial supported receivable types are:

```text id="m9g4dy"
MERCANTILE_DUPLICATE
POST_DATED_CHECK
```

Unsupported receivable types must be rejected.

Expected API behavior:

```text id="yw6pli"
422 Unprocessable Entity
```

---

### BR-013 - Mercantile Duplicate Spread

**Type:** Mandatory
**Priority:** Critical

A receivable of type `MERCANTILE_DUPLICATE` must use the following monthly spread:

```text id="zroz4m"
1.5% a.m. = 0.01500000
```

Rules:

* spread must be represented as a decimal rate;
* spread must not be represented as `1.5`;
* spread must be persisted in settlement item snapshots.

---

### BR-014 - Post-Dated Check Spread

**Type:** Mandatory
**Priority:** Critical

A receivable of type `POST_DATED_CHECK` must use the following monthly spread:

```text id="4rbply"
2.5% a.m. = 0.02500000
```

Rules:

* spread must be represented as a decimal rate;
* spread must not be represented as `2.5`;
* spread must be persisted in settlement item snapshots.

---

### BR-015 - Strategy Pattern for Risk Rules

**Type:** Mandatory
**Priority:** Critical

The system must use Strategy Pattern to select and apply receivable-type risk rules.

Expected abstraction:

```text id="4y16u5"
PricingStrategy
```

Expected initial implementations:

```text id="6nek6l"
MercantileDuplicatePricingStrategy
PostDatedCheckPricingStrategy
```

Rules:

* each supported receivable type must have a corresponding pricing strategy or risk strategy;
* strategy selection must be explicit and testable;
* large conditional blocks must not replace the Strategy Pattern as the main design;
* strategies must be covered by unit tests.

Acceptable implementation approach:

* keep the base present value formula in a shared pricing engine;
* use each strategy to provide spread and receivable-specific risk behavior.

---

## 7. Pricing Formula Rules

### BR-016 - Base Pricing Formula

**Type:** Mandatory
**Priority:** Critical

The system must use the following base formula:

```text id="iq3uz5"
Present Value = Face Value / (1 + Base Rate + Spread) ^ Term
```

Where:

| Variable   | Meaning                                  |
| ---------- | ---------------------------------------- |
| Face Value | Original nominal value of the receivable |
| Base Rate  | Monthly base rate                        |
| Spread     | Monthly receivable-type risk spread      |
| Term       | Time factor in months                    |

Rules:

* base rate and spread must use decimal format;
* term must be calculated consistently;
* present value must be calculated in the source currency;
* final values must be auditable.

---

### BR-017 - Base Rate

**Type:** Assumption
**Priority:** High

The base rate is treated as a monthly decimal rate.

Initial implementation rule:

```text id="tk07uh"
Effective base rate resolution order:
1. Use baseRate from the request when it is explicitly provided.
2. Otherwise use the server-side DEFAULT_BASE_RATE configuration.
3. If neither is available, fail the operation with a structured error.
```

Rules:

* request-provided `baseRate` has precedence over server-side fallback;
* base rate must be greater than or equal to zero;
* base rate must be represented in decimal format;
* `1%` must be represented as `0.01000000`;
* the effective base rate used in the calculation must be returned in API responses;
* base rate must be persisted in each settlement item snapshot.

Rationale:

The challenge defines the formula using base rate but does not define a full base-rate management module. Allowing an explicit request value with a documented server-side fallback keeps the implementation explicit while preserving operational flexibility.

Future enhancement:

* base rates may be managed in a dedicated table by currency and validity period.

---

### BR-018 - Term Calculation

**Type:** Assumption
**Priority:** Critical

The term used in the pricing formula is calculated in commercial months.

Initial rule:

```text id="6bzfiw"
termInMonths = daysBetween(pricingDate, dueDate) / 30
```

Rules:

* `pricingDate` is the current application date at calculation time;
* due date must be in the future;
* past-due receivables must be rejected;
* same-day due dates must be rejected in the initial version;
* term must be greater than zero;
* calculated term must be persisted in settlement item snapshots.

Rationale:

The challenge specifies monthly spreads but does not define the exact calendar convention. The project uses a simple commercial-month assumption to keep the implementation deterministic and documented.

---

### BR-019 - Pricing Date

**Type:** Assumption
**Priority:** Medium

The pricing date is the application date used as the starting point for term calculation.

Rules:

* simulation uses the current application date at simulation time;
* settlement uses the current application date at settlement time;
* settlement item must store calculation timestamp;
* tests may use a fixed clock to make term calculation deterministic.

Implementation recommendation:

* inject a `Clock` in backend services instead of directly calling the system clock in business logic.

---

### BR-020 - Face Value Validation

**Type:** Validation
**Priority:** Critical

Face value must be valid.

Rules:

* face value is required;
* face value must be greater than zero;
* face value must use a supported monetary scale;
* face value must be associated with a currency.

Invalid examples:

```text id="1gntc4"
null
0
-100.00
```

Expected API behavior:

```text id="mt74hz"
400 Bad Request
```

or:

```text id="ba6nbu"
422 Unprocessable Entity
```

depending on validation layer.

---

### BR-021 - Present Value Must Not Exceed Face Value in Normal Positive-Rate Scenarios

**Type:** Validation
**Priority:** Medium

When base rate and spread are greater than or equal to zero and term is greater than zero, present value should not exceed face value in the source currency.

Rules:

* if calculated present value exceeds face value under normal positive-rate conditions, treat it as a calculation defect;
* unit tests must cover typical scenarios where present value is lower than face value.

---

### BR-022 - Discount Calculation

**Type:** Mandatory
**Priority:** High

Discount must be calculated as:

```text id="wglcu7"
Discount = Face Value - Present Value
```

Rules:

* discount is calculated in the source currency;
* discount must be greater than or equal to zero for normal positive-rate scenarios;
* discount must be persisted in settlement item snapshots.

---

## 8. Cross-Currency Rules

### BR-023 - Cross-Currency Conversion Order

**Type:** Mandatory
**Priority:** Critical

For cross-currency operations, conversion must be applied after present value calculation.

Correct order:

```text id="iltot8"
1. Calculate present value in source currency.
2. Convert present value to payment currency.
3. Return or persist final payment value.
```

Incorrect order:

```text id="kxwmri"
1. Convert face value to payment currency.
2. Calculate present value after conversion.
```

Rules:

* the backend must enforce the correct order;
* tests must cover cross-currency conversion order;
* settlement item must persist both source-currency present value and payment-currency value.

---

### BR-024 - Cross-Currency Requires Exchange Rate

**Type:** Mandatory
**Priority:** Critical

If source currency differs from payment currency, a valid exchange rate must be available.

Rules:

* missing exchange rate must fail the operation;
* settlement must not be persisted when required exchange rate is missing;
* simulation must return a structured error when required exchange rate is missing.

Expected API behavior:

```text id="gkwhqa"
422 Unprocessable Entity
```

---

### BR-025 - Same-Currency Does Not Require Exchange Rate

**Type:** Mandatory
**Priority:** High

If source currency and payment currency are the same, exchange-rate conversion is not required.

Rules:

* same-currency operation must not fail because of missing exchange rate;
* payment value equals present value after monetary rounding;
* exchange-rate snapshot may be null.

---

## 9. Pricing Simulation Rules

### BR-026 - Simulation Is Non-Persistent

**Type:** Mandatory
**Priority:** High

Pricing simulation must calculate and return pricing results without creating settlement records.

Rules:

* simulation must not create a settlement;
* simulation must not create settlement items;
* simulation may read exchange rates;
* simulation must use the same pricing domain logic used by settlement;
* simulation must return enough calculation details for operator review.

---

### BR-027 - Simulation Input Requirements

**Type:** Validation
**Priority:** High

A pricing simulation request must include:

* face value;
* source currency;
* payment currency;
* receivable type;
* base rate;
* due date.

Rules:

* all fields are required;
* invalid values must return structured validation errors;
* cross-currency simulations require exchange-rate lookup.

---

### BR-028 - Simulation Output Requirements

**Type:** Mandatory
**Priority:** Medium

A pricing simulation response must include at least:

* face value;
* source currency;
* payment currency;
* present value in source currency;
* net payment value in payment currency;
* discount value;
* base rate;
* spread;
* term in months;
* exchange rate used, when applicable;
* calculation timestamp.

## 10. Settlement Rules

### BR-029 - Settlement Batch Is Mandatory

**Type:** Mandatory
**Priority:** Critical

Settlement must support batch processing.

A settlement request must contain one or more receivables.

Rules:

* empty batches must be rejected;
* batch size must be validated;
* all items must be validated before successful persistence;
* the settlement must be persisted as a single atomic operation.

Initial batch size limit:

```text id="3a2j8n"
Maximum items per settlement batch: 100
```

Rationale:

The limit prevents accidental oversized requests in the initial implementation while still demonstrating batch behavior.

---

### BR-030 - Settlement Atomicity

**Type:** Mandatory
**Priority:** Critical

Settlement batch processing must be atomic.

Rules:

* if all items are valid, persist the settlement and all settlement items;
* if any item is invalid, persist nothing;
* no settlement may be left halfway completed;
* rollback must occur for business or persistence failures during settlement.

Implementation expectation:

```text id="s9nkpc"
CreateSettlementService must define the transaction boundary.
```

---

### BR-031 - Settlement Must Be Auditable

**Type:** Audit
**Priority:** Critical

Every settlement item must persist the calculation snapshot used at settlement time.

Required snapshot fields:

* receivable identifier;
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
* exchange rate used, when applicable;
* calculation timestamp.

Rules:

* historical values must not be recalculated from current exchange rates or current spreads;
* statement queries must use persisted settlement values;
* audit snapshots must remain readable after business rules evolve.

---

### BR-032 - Settlement Header Totals

**Type:** Mandatory
**Priority:** High

A settlement must store aggregated totals.

Recommended totals:

* total face value;
* total present value;
* total payment value;
* item count.

Rules:

* totals must be derived from settlement items;
* totals must use decimal-safe arithmetic;
* totals must be persisted for statement efficiency and auditability.

---

### BR-033 - Settlement Status

**Type:** Mandatory
**Priority:** Medium

A settlement must have a status.

Initial supported statuses:

```text id="t0y2o2"
PENDING
SETTLED
FAILED
CANCELLED
```

Initial implementation rule:

* successfully created settlements are stored as `SETTLED`;
* invalid settlement attempts should not create partial records;
* `FAILED` may be used only if the implementation intentionally persists failed attempts, which is not required in the initial version.

---

### BR-034 - Settlement Timestamp

**Type:** Audit
**Priority:** High

A settlement must store the date and time when it was created or settled.

Recommended field:

```text id="xn0him"
settledAt
```

Rules:

* timestamps must be stored in UTC;
* API may format timestamps according to ISO 8601;
* statement queries must filter using persisted settlement timestamp.

---

## 11. Duplicate Settlement and Concurrency Rules

### BR-035 - Receivable Cannot Be Settled Twice

**Type:** Mandatory
**Priority:** Critical

The same receivable must not be settled more than once.

Rules:

* only available receivables can be settled;
* settled receivables must not be settled again;
* duplicate settlement attempts must fail;
* duplicate prevention must exist in application logic;
* duplicate prevention should also exist through database constraints.

Expected API behavior:

```text id="jqp5g6"
409 Conflict
```

---

### BR-036 - Database Constraint for Duplicate Prevention

**Type:** Technical-business
**Priority:** Critical

The database must support duplicate settlement prevention.

Recommended constraint:

```text id="3y6iui"
unique(receivable_id) on settlement_items
```

Rules:

* the application should validate before insert;
* the database constraint acts as a final safety net;
* constraint violations must be mapped to structured API errors when possible.

---

### BR-037 - Receivable Versioning

**Type:** Technical-business
**Priority:** Medium

Receivables should include a version field to support optimistic locking.

Recommended field:

```text id="v0xm6h"
version
```

Rules:

* versioning helps avoid concurrent updates to the same receivable;
* optimistic locking is recommended as a controlled senior-level differential;
* if implemented, concurrency conflicts should return a conflict response.

Expected API behavior for optimistic-lock conflict:

```text id="ad1um7"
409 Conflict
```

---

### BR-038 - Race Condition Protection

**Type:** Mandatory
**Priority:** High

Settlement logic must be designed to avoid race conditions.

Risk scenario:

```text id="9prjqs"
Two requests attempt to settle the same receivable at the same time.
```

Required mitigations:

* transactional settlement service;
* duplicate settlement validation;
* database uniqueness constraint;
* optional optimistic locking.

## 12. Receivable Rules

### BR-039 - Receivable Must Belong to an Assignor

**Type:** Mandatory
**Priority:** High

Every persisted receivable must be associated with an assignor.

Rules:

* assignor must exist;
* settlement must be associated with the same assignor context;
* statement queries must support filtering by assignor.

---

### BR-040 - Receivable Due Date Must Be Future

**Type:** Validation
**Priority:** Critical

A receivable due date must be in the future relative to pricing date.

Invalid due dates:

* past dates;
* same-day due date in the initial version.

Expected API behavior:

```text id="2k5jb5"
422 Unprocessable Entity
```

---

### BR-041 - Receivable Status Validation

**Type:** Validation
**Priority:** High

Only receivables with status `AVAILABLE` may be settled.

Rules:

* settled receivables cannot be settled again;
* cancelled receivables cannot be settled;
* invalid status transitions must fail.

Initial statuses:

```text id="lsr9v5"
AVAILABLE
SETTLED
CANCELLED
```

## 13. Assignor Rules

### BR-042 - Assignor Is Required for Settlement

**Type:** Mandatory
**Priority:** High

A settlement must be associated with an assignor.

Rules:

* assignor identifier is required;
* assignor must exist or be created according to the implementation design;
* settlement statement must support filtering by assignor.

Initial implementation option:

```text id="e0of6g"
Create assignors through seed data or simple persistence support as part of settlement flow.
```

The final implementation must document which option was selected.

---

### BR-043 - Assignor Document

**Type:** Validation
**Priority:** Medium

Assignor may have a document field for identification.

Initial rule:

* document is stored as text;
* advanced CPF/CNPJ validation is not mandatory for the initial version;
* if document validation is implemented, it must be documented and tested.

## 14. Analytical Statement Rules

### BR-044 - Statement Query Filters

**Type:** Mandatory
**Priority:** High

The settlement statement route must support filters for:

* period;
* assignor;
* payment currency;
* receivable type.

Additional optional filters:

* settlement status;
* source currency;
* minimum amount;
* maximum amount.

---

### BR-045 - Period Filter

**Type:** Mandatory
**Priority:** High

Statement queries must support filtering by date period.

Rules:

* `from` date is optional;
* `to` date is optional;
* if both are provided, `from` must be less than or equal to `to`;
* filtering must use settlement timestamp;
* invalid ranges must return validation errors.

Expected API behavior:

```text id="rkiunw"
400 Bad Request
```

---

### BR-046 - Server-Side Pagination

**Type:** Mandatory
**Priority:** Critical

Settlement statement queries must use server-side pagination.

Rules:

* API must accept page and size parameters;
* database query must limit returned records;
* frontend must not load all records and paginate locally;
* response must include pagination metadata.

Initial pagination limits:

```text id="h8yvop"
Default page size: 20
Maximum page size: 100
```

---

### BR-047 - Database-Level Filtering

**Type:** Mandatory
**Priority:** Critical

Statement query filtering must happen at database level.

Forbidden approach:

```text id="mb43tg"
Load all settlements into memory and filter in Java.
```

Required approach:

* SQL filtering;
* query builder;
* repository projections;
* native query if useful for performance.

---

### BR-048 - Statement Query Sorting

**Type:** Mandatory
**Priority:** Medium

Statement query results must be sorted deterministically.

Initial rule:

```text id="fki8ln"
Default sort: settledAt descending
```

Rules:

* newest settlements should appear first;
* stable ordering must be preserved across pages.

## 15. API Validation and Error Rules

### BR-049 - Structured Error Responses

**Type:** Mandatory
**Priority:** High

API errors must be returned in a structured format.

Expected fields:

```text id="mjrzvq"
timestamp
status
error
message
path
details
```

Rules:

* validation errors must include field-level information when possible;
* business errors must have clear messages;
* internal stack traces must not be exposed.

---

### BR-050 - Global Exception Handling

**Type:** Mandatory
**Priority:** High

The backend must implement global exception handling.

Rules:

* validation exceptions must be mapped consistently;
* domain exceptions must be mapped consistently;
* unexpected exceptions must be handled safely;
* API consumers must receive predictable error responses.

---

### BR-051 - Input Validation Is Backend Responsibility

**Type:** Mandatory
**Priority:** Critical

Backend validation is mandatory.

Rules:

* frontend validation is not sufficient;
* all public API inputs must be validated server-side;
* invalid values must not reach financial calculation logic.

## 16. Data Persistence and Audit Rules

### BR-052 - Relational Persistence

**Type:** Mandatory
**Priority:** High

The system must use a relational database.

Selected database:

```text id="vd696e"
MySQL 8.4 LTS
```

Rules:

* schema changes must be versioned through Flyway;
* final project must not rely on Hibernate auto-DDL for schema creation;
* relationships must use foreign keys where appropriate.

---

### BR-053 - Flyway Migrations

**Type:** Mandatory
**Priority:** High

Database schema must be managed through Flyway migrations.

Expected location:

```text id="lsh7uf"
backend/src/main/resources/db/migration
```

Expected initial migrations:

```text id="8vj4jl"
V1__create_initial_schema.sql
V2__seed_reference_data.sql
```

Rules:

* migration scripts must be deterministic;
* schema must match `05-data-model.md`;
* migration scripts represent the project DDL.

---

### BR-054 - Created and Updated Timestamps

**Type:** Audit
**Priority:** Medium

Persisted business records should contain timestamps.

Recommended fields:

```text id="wjyacp"
created_at
updated_at
```

Rules:

* settlement-specific timestamps must also be persisted;
* timestamps should use UTC;
* audit fields must support traceability.

---

### BR-055 - Historical Calculation Immutability

**Type:** Audit
**Priority:** Critical

Historical settlement calculation data must not change when reference data changes.

Examples of reference data changes:

* new exchange rate registered;
* new spread configured in future version;
* new base-rate management feature added.

Rules:

* settlement items must store calculation snapshots;
* statement queries must read persisted values;
* historical values must not be recomputed dynamically for display.

## 17. Frontend Business Behavior Rules

### BR-056 - Frontend Simulation Behavior

**Type:** Mandatory
**Priority:** High

The frontend pricing simulation screen must request backend calculation.

Rules:

* user inputs are collected through the form;
* frontend sends request to backend simulation endpoint;
* frontend displays returned values;
* frontend may debounce requests for better usability;
* frontend must display validation and business errors clearly.

---

### BR-057 - Frontend Settlement Grid Behavior

**Type:** Mandatory
**Priority:** High

The frontend settlement grid must use server-side pagination and filtering.

Rules:

* page changes must request backend data;
* filter changes must request backend data;
* frontend must not load the entire settlement history;
* loading and empty states should be visible.

---

### BR-058 - Frontend Monetary Display

**Type:** Mandatory
**Priority:** Medium

Frontend monetary values must be displayed with currency information.

Rules:

* display currency code or symbol consistently;
* do not hide source currency and payment currency in cross-currency scenarios;
* display decimal places according to monetary scale.

## 18. Security and Safety Rules

### BR-059 - No Secrets in Repository

**Type:** Mandatory
**Priority:** Critical

Real credentials and secrets must not be committed.

Rules:

* `.env.example` may contain safe example values;
* `.env` must be ignored by Git;
* Docker and application configs must use environment variables for credentials.

---

### BR-060 - No Stack Trace Exposure

**Type:** Mandatory
**Priority:** High

API responses must not expose stack traces or internal exception details.

Rules:

* log internal errors server-side;
* return controlled error responses to the client;
* avoid leaking database details in public API errors.

---

### BR-061 - CORS Must Be Explicit

**Type:** Mandatory
**Priority:** Medium

CORS configuration must be explicit for local development.

Initial allowed origin:

```text id="p4wwcg"
http://localhost:4200
```

Rules:

* avoid wildcard CORS in final configuration;
* allowed origins should come from environment configuration.

## 19. Testing Rules Derived from Business Rules

### BR-062 - Pricing Strategy Tests Are Mandatory

**Type:** Mandatory
**Priority:** Critical

Automated tests must cover pricing strategies.

Minimum required cases:

* Mercantile Duplicate spread;
* Post-Dated Check spread;
* unsupported receivable type;
* strategy resolver behavior.

---

### BR-063 - Financial Calculation Tests Are Mandatory

**Type:** Mandatory
**Priority:** Critical

Automated tests must cover financial calculation behavior.

Minimum required cases:

* same-currency present value calculation;
* cross-currency conversion applied after present value calculation;
* discount calculation;
* rounding behavior;
* invalid face value;
* invalid base rate;
* invalid due date.

---

### BR-064 - Settlement Atomicity Tests Are Recommended

**Type:** Mandatory for robust delivery
**Priority:** High

Automated tests should cover settlement atomicity.

Minimum expected case:

```text id="jojcvi"
Given a settlement batch with one valid item and one invalid item,
when settlement is requested,
then no settlement or settlement item is persisted.
```

---

### BR-065 - Duplicate Settlement Tests Are Recommended

**Type:** Mandatory for robust delivery
**Priority:** High

Automated tests should cover duplicate settlement prevention.

Minimum expected case:

```text id="62gen9"
Given a receivable already settled,
when another settlement attempts to include it,
then the request fails with conflict.
```

## 20. Business Rule Change Policy

Business rules must be treated as versioned project knowledge.

When a business rule changes:

1. update this file;
2. update API contract if affected;
3. update data model if affected;
4. update ADR if the decision is architectural;
5. update tests;
6. update prompts if Codex tasks depend on the changed rule;
7. update `AI_USAGE.md` if AI assisted the change.

## 21. Summary of Critical Rules

The following rules are critical and must not be violated:

```text id="lh0h5g"
BR-001 - Decimal-Safe Financial Arithmetic
BR-002 - Explicit Rounding
BR-003 - Backend as Official Calculation Source
BR-009 - Exchange Rate Direction Is Explicit
BR-011 - Exchange Rate Snapshot
BR-013 - Mercantile Duplicate Spread
BR-014 - Post-Dated Check Spread
BR-015 - Strategy Pattern for Risk Rules
BR-016 - Base Pricing Formula
BR-018 - Term Calculation
BR-023 - Cross-Currency Conversion Order
BR-024 - Cross-Currency Requires Exchange Rate
BR-029 - Settlement Batch Is Mandatory
BR-030 - Settlement Atomicity
BR-031 - Settlement Must Be Auditable
BR-035 - Receivable Cannot Be Settled Twice
BR-036 - Database Constraint for Duplicate Prevention
BR-046 - Server-Side Pagination
BR-047 - Database-Level Filtering
BR-051 - Input Validation Is Backend Responsibility
BR-055 - Historical Calculation Immutability
BR-059 - No Secrets in Repository
BR-062 - Pricing Strategy Tests Are Mandatory
BR-063 - Financial Calculation Tests Are Mandatory
```
