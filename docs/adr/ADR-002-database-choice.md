# ADR-002 — Database Choice

## Status

Accepted

## Date

2026-07-07

## Context

The SRM Credit Engine must persist financial data related to:

* currencies;
* exchange rates;
* assignors;
* receivable types;
* receivables;
* settlements;
* settlement items;
* calculation snapshots;
* analytical settlement statements.

The technical challenge explicitly requires persistence with transactional integrity and emphasizes that financial transactions must respect ACID properties. It also requires a relational database preferably, auditability, DDL documentation, data modeling and analytical queries.

The system must support:

* atomic settlement batches;
* prevention of duplicate settlement;
* reliable historical audit records;
* decimal precision for monetary values and rates;
* relational integrity between financial entities;
* filtered and paginated settlement statements;
* deterministic local execution through Docker Compose.

## Decision

The project will use:

```text id="17drzr"
Database: MySQL 8.4 LTS
Migration tool: Flyway
Schema management: SQL migrations
Application access: Spring Data JPA / Hibernate for transactional write use cases
Analytical access: projections, query builders or native SQL where appropriate
```

The database will be executed locally through Docker Compose.

The final project must not rely on Hibernate auto-DDL for schema creation.

Expected backend setting:

```properties id="p98pfg"
spring.jpa.hibernate.ddl-auto=validate
```

Database schema changes must be managed through Flyway migrations.

## Selected Version

Initial target:

```text id="hze3s3"
MySQL 8.4 LTS
Docker image: mysql:8.4.10
```

The Docker Compose file may use either:

```text id="mx5gcp"
mysql:8.4.10
```

for patch-level reproducibility, or:

```text id="1n58u1"
mysql:8.4
```

to follow the latest patch in the 8.4 LTS line.

For this technical challenge, `mysql:8.4.10` is preferred because it makes local evaluation more reproducible.

## Rationale

### Relational Model Fit

The SRM Credit Engine domain is naturally relational.

Core relationships include:

```text id="93s6se"
assignor -> receivables
assignor -> settlements
receivable_type -> receivables
settlement -> settlement_items
receivable -> settlement_item
currency -> exchange_rates
currency -> settlements
```

A relational database is appropriate because the system needs:

* foreign keys;
* uniqueness constraints;
* indexed queries;
* transactional consistency;
* historical records;
* structured analytical filtering;
* deterministic schema evolution.

### ACID Requirements

Settlement creation is a critical operation.

A settlement batch must either:

```text id="eozkvy"
persist the full settlement and all settlement items
```

or:

```text id="vkpd79"
persist nothing
```

A relational database with transaction support is appropriate for this requirement.

MySQL with InnoDB supports transactional behavior required for:

* settlement atomicity;
* rollback on invalid item;
* duplicate settlement prevention;
* consistency between settlement header and items.

### Decimal Precision

Financial data must not be stored using floating-point database types.

The database will use `DECIMAL` columns for:

* face value;
* present value;
* discount value;
* payment value;
* settlement totals;
* base rate;
* spread;
* term;
* exchange rate.

Recommended types:

```sql id="qv9p9m"
DECIMAL(19, 4)  -- monetary values
DECIMAL(19, 8)  -- rates and exchange rates
DECIMAL(19, 8)  -- term in months
```

Exact precision and scale may be refined in `docs/specs/05-data-model.md`.

### Auditability

The system must preserve historical calculation data.

A settlement item must store the calculation snapshot used at settlement time, including:

* original face value;
* source currency;
* payment currency;
* base rate;
* spread;
* term;
* present value;
* discount;
* payment value;
* exchange rate snapshot, when applicable;
* calculation timestamp.

A relational model makes this explicit and easy to inspect.

### Query Performance

The settlement statement route must support filtering by:

* period;
* assignor;
* currency;
* receivable type.

MySQL supports indexes for these access patterns.

Expected indexes include:

```text id="2esdtt"
settlements.settled_at
settlements.assignor_id
settlements.payment_currency_code
settlement_items.receivable_type_code
settlement_items.source_currency_code
settlement_items.payment_currency_code
exchange_rates.source_currency_code, target_currency_code, valid_at
```

The reporting route may use optimized SQL, projections or query builders instead of loading large datasets into memory.

## Alternatives Considered

### PostgreSQL

PostgreSQL was strongly considered.

Advantages:

* excellent relational database;
* robust transactional behavior;
* strong indexing features;
* strong data integrity capabilities;
* excellent support for analytical queries;
* widely used in financial and enterprise systems.

Rejected for this project because:

* the selected implementation direction uses MySQL;
* MySQL is sufficient for the required scope;
* MySQL integrates well with Spring Boot, Flyway and Docker Compose;
* using MySQL demonstrates ability to design robust financial persistence without relying on PostgreSQL-specific features.

PostgreSQL would also be a valid choice for this challenge.

### H2

H2 was considered only as a possible test database.

Rejected as the primary database because:

* it is not representative enough for MySQL behavior;
* SQL dialect differences may hide production issues;
* transactional and constraint behavior can differ from the target database;
* the challenge expects a realistic persistence setup.

For integration tests, Testcontainers with MySQL is preferred over H2 when database behavior matters.

### MongoDB / NoSQL

NoSQL was considered inappropriate for the initial version.

Rejected because:

* the domain has strong relational structure;
* settlement consistency depends on transactional boundaries;
* foreign keys and constraints are useful;
* analytical queries depend on structured filtering;
* the challenge preferably expects relational persistence.

NoSQL could be used for auxiliary future concerns such as event logs or document exports, but not as the primary transactional database.

### In-Memory Database Only

Rejected because:

* it would not satisfy realistic persistence expectations;
* it would not demonstrate ACID persistence design;
* it would weaken evaluation of schema, DDL, migrations and relational modeling.

## Consequences

### Positive Consequences

Using MySQL 8.4 LTS provides:

* mature relational persistence;
* ACID transactions with InnoDB;
* `DECIMAL` support for financial values;
* foreign keys and uniqueness constraints;
* indexing for analytical queries;
* compatibility with Spring Data JPA;
* compatibility with Flyway;
* simple Docker Compose setup;
* evaluator-friendly local execution.

### Negative Consequences

The choice also introduces:

* need to design indexes explicitly;
* need to handle MySQL-specific SQL behavior;
* care around `DECIMAL` precision and scale;
* care around timezone handling;
* possible complexity when using JPA for write models and SQL projections for reads;
* less advanced native analytical capabilities than some PostgreSQL setups.

### Mitigations

Mitigations:

* define schema with Flyway;
* avoid Hibernate auto-DDL as source of truth;
* use `DECIMAL`, not floating-point numeric types;
* store timestamps in UTC;
* add indexes for settlement statement filters;
* use database constraints for duplicate settlement prevention;
* use integration tests with MySQL Testcontainers where persistence behavior matters;
* use read projections or native queries for analytical endpoints.

## Data Modeling Principles

The database model must follow these principles:

### 1. Financial Values Are Explicit

Every monetary amount must include or be associated with a currency.

Examples:

```text id="28nol7"
face_value + source_currency_code
payment_value + payment_currency_code
total_payment_value + payment_currency_code
```

### 2. Calculation Snapshots Are Persisted

Settlement items must store the values used during calculation.

Historical settlements must not be recalculated dynamically from current exchange rates or current spreads.

### 3. Referential Integrity Is Enforced

Foreign keys should be used for:

* assignors;
* currencies;
* receivable types;
* receivables;
* settlements;
* settlement items.

### 4. Duplicate Settlement Is Prevented

The same receivable must not appear in more than one settlement item.

Recommended constraint:

```sql id="kgyy9v"
UNIQUE (receivable_id)
```

on `settlement_items`.

### 5. Analytical Access Patterns Are Indexed

The schema must support filtered and paginated statement queries.

Indexes must be defined for expected filters and sorting.

## Initial Table Set

The initial database schema must include at least:

```text id="dse3go"
currencies
exchange_rates
assignors
receivable_types
receivables
settlements
settlement_items
```

## Expected Table Responsibilities

### currencies

Stores supported currencies.

Expected records:

```text id="bnwke7"
BRL
USD
```

### exchange_rates

Stores exchange rates between currency pairs.

Expected responsibilities:

* source currency;
* target currency;
* rate;
* validity timestamp;
* creation timestamp.

### assignors

Stores companies assigning receivables.

Expected responsibilities:

* assignor identity;
* name;
* optional document.

### receivable_types

Stores supported receivable types and reference spread information.

Expected records:

```text id="4q54ln"
MERCANTILE_DUPLICATE
POST_DATED_CHECK
```

### receivables

Stores receivables available for settlement.

Expected responsibilities:

* assignor;
* receivable type;
* face value;
* source currency;
* due date;
* status;
* optimistic locking version.

### settlements

Stores settlement headers.

Expected responsibilities:

* assignor;
* payment currency;
* status;
* totals;
* settlement timestamp.

### settlement_items

Stores individual calculation snapshots.

Expected responsibilities:

* settlement reference;
* receivable reference;
* face value;
* source currency;
* payment currency;
* base rate;
* spread;
* term;
* present value;
* discount value;
* payment value;
* exchange-rate snapshot;
* calculation timestamp.

## Migration Strategy

Flyway migrations must be stored at:

```text id="vd4sp6"
backend/src/main/resources/db/migration
```

Expected initial migrations:

```text id="uz26y7"
V1__create_initial_schema.sql
V2__seed_reference_data.sql
```

Optional later migrations:

```text id="yxxcbp"
V3__add_statement_query_indexes.sql
V4__add_audit_columns.sql
```

Rules:

* migrations must be deterministic;
* migrations must be committed to the repository;
* schema must be reproducible from scratch;
* migrations must match `docs/specs/05-data-model.md`;
* the ER diagram must match the migrations.

## Transaction Strategy

Settlement creation must be executed inside a transaction.

Expected application boundary:

```text id="otxgf2"
CreateSettlementService
```

Rules:

* validate settlement request;
* load required receivables;
* check availability;
* calculate pricing;
* persist settlement;
* persist settlement items;
* update receivable statuses;
* commit only if the full operation succeeds.

If any step fails:

```text id="416l94"
rollback everything
```

## Concurrency Strategy

The system must reduce the risk of concurrent duplicate settlement.

Required protections:

```text id="krmwnw"
1. Application-level validation
2. Database-level unique constraint on settlement_items.receivable_id
```

Recommended protection:

```text id="kydeqc"
3. Optimistic locking on receivables.version
```

If optimistic locking is implemented, concurrent settlement conflicts should return:

```text id="db2a31"
409 Conflict
```

## Timezone Strategy

Database timestamps must be stored in UTC.

Rules:

* application should use UTC internally;
* Docker Compose should configure MySQL timezone as UTC;
* API responses should use ISO 8601 timestamps;
* date-only values such as due date should remain date-only.

Rationale:

Financial audit records must be consistent regardless of evaluator machine timezone.

## Character Set and Collation

The database should use:

```text id="6dnzjm"
utf8mb4
utf8mb4_unicode_ci
```

Rationale:

This supports broad Unicode text safely and avoids legacy charset issues.

## Development and Test Strategy

### Local Development

Local development uses MySQL through Docker Compose.

Expected command:

```bash id="iaivb0"
docker compose up mysql
```

### Backend Integration Tests

When database-specific behavior matters, use Testcontainers with MySQL.

Examples:

* Flyway migration validation;
* unique constraint behavior;
* transaction rollback;
* statement query filters;
* repository behavior.

### Avoid H2 for Critical Persistence Tests

H2 may behave differently from MySQL.

For this project, H2 should not be used to validate financial persistence behavior, constraints or SQL queries.

## Security Considerations

Database credentials must be provided through environment variables.

Rules:

* no real secrets in repository;
* `.env.example` may contain safe sample values;
* `.env` must be ignored by Git;
* Docker Compose must support overriding credentials;
* application properties must not hardcode production credentials.

## Performance Considerations

The settlement statement endpoint must be designed for large datasets.

Database-level requirements:

* indexed filtering;
* deterministic sorting;
* server-side pagination;
* projection-based reads where appropriate;
* avoidance of in-memory filtering.

Initial expected default sort:

```text id="31eq1f"
settled_at DESC
```

Initial expected pagination:

```text id="3bgumo"
default page size: 20
maximum page size: 100
```

## Audit Considerations

Auditability is a core reason for choosing relational persistence.

The database must support reconstructing the calculation context of a settlement without relying on current reference values.

For example:

* if a new exchange rate is registered, old settlements keep their original exchange-rate snapshot;
* if a spread changes in the future, old settlements keep their original spread;
* if a base-rate module is introduced later, old settlements keep their original base rate.

## Decision Validation

This decision is valid if the implementation demonstrates:

* MySQL starts successfully through Docker Compose;
* Flyway migrations create the schema from scratch;
* application starts with `ddl-auto=validate`;
* schema includes required tables and relationships;
* monetary columns use `DECIMAL`;
* duplicate settlement prevention exists at database level;
* statement queries are paginated and filtered in the database;
* integration tests validate critical database behavior where feasible.

## Related Documents

```text id="oztffp"
README.md
AGENTS.md
.env.example
docker-compose.yml
docs/specs/03-business-rules.md
docs/specs/05-data-model.md
docs/specs/06-architecture.md
docs/specs/07-testing-strategy.md
docs/diagrams/er-diagram.md
docs/adr/ADR-001-backend-stack.md
docs/adr/ADR-003-money-precision.md
```

## Review Notes

This ADR may be revisited if:

* MySQL 8.4 LTS image availability changes;
* the implementation requires database features better supported by another relational database;
* the evaluator environment has constraints around MySQL execution;
* future requirements introduce scale or analytical needs beyond the initial challenge scope.

Any database change must update:

* this ADR;
* `docker-compose.yml`;
* `.env.example`;
* `docs/specs/05-data-model.md`;
* `docs/diagrams/er-diagram.md`;
* Flyway migrations;
* relevant tests.
