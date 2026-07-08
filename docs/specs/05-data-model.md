# 05 - Data Model

## 1. Purpose

This document defines the initial relational data model for the **SRM Credit Engine**.

The data model must support:

* supported currencies;
* supported receivable types;
* exchange-rate history;
* assignors;
* receivables;
* settlement batches;
* settlement item calculation snapshots;
* duplicate settlement prevention;
* analytical settlement statement queries;
* Flyway-based schema migration;
* auditability of financial calculations.

This file is the source of truth for the first Flyway migrations and the ER diagram.

If the implementation changes table names, fields, relationships, constraints or indexes, this file and `docs/diagrams/er-diagram.md` must be updated.

## 2. Database Decision

The selected database is:

```text id="lskn9m"
MySQL 8.4 LTS
```

Schema management:

```text id="29lg1r"
Flyway migrations
```

Application persistence:

```text id="ju0snq"
Spring Data JPA / Hibernate
```

Analytical reads:

```text id="nxq0mi"
Repository projections, query builders or native SQL when useful
```

Hibernate auto-DDL must not be used as the final schema source.

Expected setting:

```properties id="q34o2p"
spring.jpa.hibernate.ddl-auto=validate
```

## 3. Modeling Principles

The database model follows these principles:

1. Use relational integrity for core business relationships.
2. Use `DECIMAL`, never floating-point types, for financial values.
3. Store historical calculation snapshots in settlement items.
4. Prevent duplicate settlement with both application logic and database constraints.
5. Store timestamps in UTC.
6. Use Flyway migrations for schema creation and evolution.
7. Add indexes for expected analytical filters.
8. Avoid recomputing historical settlement values from current reference data.
9. Keep reference data normalized.
10. Keep the initial model simple enough for a technical challenge, but robust enough for financial auditability.

## 4. Important Modeling Assumption

### DMA-001 - One Source Currency per Settlement Batch

A settlement batch may contain multiple receivables, but in the initial data model all receivables in the same settlement must share the same `source_currency_code`.

Rationale:

* `settlements.total_face_value` is meaningful only when all face values are in the same source currency;
* `settlements.total_present_value` is meaningful only when all present values are in the same source currency;
* `settlements.total_payment_value` is in the payment currency;
* allowing mixed source currencies would require grouped totals by currency or a separate aggregate table.

Therefore, the initial implementation must validate:

```text id="ey1m3c"
all settlement items in a settlement have the same sourceCurrency
```

The settlement may still be cross-currency:

```text id="l50q5n"
sourceCurrency = BRL
paymentCurrency = USD
```

Future enhancement:

```text id="of0m81"
Allow mixed-source-currency batches by introducing settlement currency total groups.
```

If this assumption changes, update:

```text id="6fech7"
docs/specs/03-business-rules.md
docs/specs/04-api-contract.md
docs/specs/05-data-model.md
docs/diagrams/er-diagram.md
```

## 5. Identifier Strategy

The initial model uses UUIDs represented as `CHAR(36)`.

Example:

```sql id="4sm3lq"
id CHAR(36) NOT NULL
```

Rationale:

* simple to inspect in database;
* easy to expose in REST APIs;
* avoids auto-increment enumeration;
* straightforward for a technical challenge.

Future optimization:

```text id="lsb91x"
Use BINARY(16) UUID storage for improved storage and index efficiency.
```

That optimization is intentionally out of scope for the initial delivery.

## 6. Timestamp Strategy

All timestamps are stored in UTC.

Recommended SQL type:

```sql id="dr8duj"
DATETIME(6)
```

Common timestamp columns:

```text id="qzp9mi"
created_at
updated_at
valid_at
settled_at
calculated_at
```

Rules:

* backend must generate timestamps in UTC;
* API should expose timestamps in ISO 8601 format;
* date-only values such as `due_date` must use `DATE`;
* due dates are not timestamps.

## 7. Decimal Precision Strategy

Financial columns must use `DECIMAL`.

Recommended types:

```text id="bz9j6o"
Monetary values: DECIMAL(19, 4)
Rates:           DECIMAL(19, 8)
Terms:           DECIMAL(19, 8)
```

Do not use:

```sql id="wz97dw"
FLOAT
DOUBLE
REAL
```

## 8. Initial Tables

The initial schema must contain the following tables:

```text id="d4qan5"
currencies
receivable_types
assignors
exchange_rates
receivables
settlements
settlement_items
```

## 9. Table: `currencies`

## 9.1 Purpose

Stores supported currencies.

Initial records:

```text id="328xcc"
BRL
USD
```

## 9.2 Columns

| Column           | Type          | Nullable | Description            |
| ---------------- | ------------- | -------: | ---------------------- |
| `code`           | `CHAR(3)`     |       no | Currency code          |
| `name`           | `VARCHAR(64)` |       no | Currency name          |
| `decimal_places` | `TINYINT`     |       no | Display decimal places |
| `created_at`     | `DATETIME(6)` |       no | Creation timestamp     |
| `updated_at`     | `DATETIME(6)` |       no | Last update timestamp  |

## 9.3 Primary Key

```sql id="kxn0qu"
PRIMARY KEY (code)
```

## 9.4 Constraints

```sql id="nhsy8f"
CHECK (CHAR_LENGTH(code) = 3)
CHECK (decimal_places >= 0)
```

## 9.5 Seed Data

```text id="dch6g9"
BRL - Brazilian Real - 2
USD - US Dollar - 2
```

## 10. Table: `receivable_types`

## 10.1 Purpose

Stores supported receivable types and their reference monthly spreads.

Although settlement items persist spread snapshots, this table keeps current reference values for supported types.

## 10.2 Columns

| Column           | Type             | Nullable | Description                |
| ---------------- | ---------------- | -------: | -------------------------- |
| `code`           | `VARCHAR(64)`    |       no | Receivable type code       |
| `description`    | `VARCHAR(128)`   |       no | Human-readable description |
| `monthly_spread` | `DECIMAL(19, 8)` |       no | Current monthly spread     |
| `created_at`     | `DATETIME(6)`    |       no | Creation timestamp         |
| `updated_at`     | `DATETIME(6)`    |       no | Last update timestamp      |

## 10.3 Primary Key

```sql id="klpwzk"
PRIMARY KEY (code)
```

## 10.4 Constraints

```sql id="b3z2zi"
CHECK (monthly_spread >= 0)
```

## 10.5 Seed Data

```text id="g05jie"
MERCANTILE_DUPLICATE - Mercantile Duplicate - 0.01500000
POST_DATED_CHECK    - Post-Dated Check    - 0.02500000
```

## 11. Table: `assignors`

## 11.1 Purpose

Stores assignor companies that sell or assign receivables.

## 11.2 Columns

| Column       | Type           | Nullable | Description                     |
| ------------ | -------------- | -------: | ------------------------------- |
| `id`         | `CHAR(36)`     |       no | Assignor UUID                   |
| `name`       | `VARCHAR(255)` |       no | Assignor legal or display name  |
| `document`   | `VARCHAR(32)`  |      yes | Assignor document, such as CNPJ |
| `created_at` | `DATETIME(6)`  |       no | Creation timestamp              |
| `updated_at` | `DATETIME(6)`  |       no | Last update timestamp           |

## 11.3 Primary Key

```sql id="9kh63o"
PRIMARY KEY (id)
```

## 11.4 Constraints

```sql id="pjbxb1"
CHECK (name <> '')
```

## 11.5 Indexes

```sql id="g5egf7"
CREATE INDEX idx_assignors_document ON assignors (document);
CREATE INDEX idx_assignors_name ON assignors (name);
```

## 11.6 Notes

`document` is nullable in the initial version to keep the API flexible.

If `document` is provided, the application may reuse an existing assignor by document.

Strict CPF/CNPJ validation is out of scope for the initial delivery.

## 12. Table: `exchange_rates`

## 12.1 Purpose

Stores exchange rates between currency pairs.

The table supports manual exchange-rate registration and latest-rate lookup.

## 12.2 Columns

| Column                 | Type             | Nullable | Description        |
| ---------------------- | ---------------- | -------: | ------------------ |
| `id`                   | `CHAR(36)`       |       no | Exchange rate UUID |
| `source_currency_code` | `CHAR(3)`        |       no | Source currency    |
| `target_currency_code` | `CHAR(3)`        |       no | Target currency    |
| `rate`                 | `DECIMAL(19, 8)` |       no | Exchange rate      |
| `valid_at`             | `DATETIME(6)`    |       no | Validity timestamp |
| `created_at`           | `DATETIME(6)`    |       no | Creation timestamp |

## 12.3 Primary Key

```sql id="ef4lot"
PRIMARY KEY (id)
```

## 12.4 Foreign Keys

```sql id="2vrd7c"
FOREIGN KEY (source_currency_code) REFERENCES currencies(code)
FOREIGN KEY (target_currency_code) REFERENCES currencies(code)
```

## 12.5 Constraints

```sql id="zjsx03"
CHECK (rate > 0)
CHECK (source_currency_code <> target_currency_code)
```

## 12.6 Indexes

```sql id="wv84pz"
CREATE INDEX idx_exchange_rates_pair_valid_at
    ON exchange_rates (source_currency_code, target_currency_code, valid_at DESC, created_at DESC);
```

## 12.7 Notes

Exchange-rate direction is explicit.

This table may contain both:

```text id="4ko1hn"
USD -> BRL
BRL -> USD
```

These are different rates and must not be treated as interchangeable unless explicitly implemented and tested.

The latest exchange rate is selected by:

```text id="zz0s8e"
source_currency_code
target_currency_code
valid_at DESC
created_at DESC
```

If an exact pair is not found for a cross-currency pricing or settlement operation, no placeholder exchange-rate snapshot should be persisted.

In the initial model, missing exchange rate is a validation/business failure handled before `settlements` and `settlement_items` are inserted.

## 13. Table: `receivables`

## 13.1 Purpose

Stores receivables available for settlement.

Receivables may be created directly through an optional receivable API or internally during settlement creation from inline request data.

## 13.2 Columns

| Column                 | Type             | Nullable | Description                   |
| ---------------------- | ---------------- | -------: | ----------------------------- |
| `id`                   | `CHAR(36)`       |       no | Receivable UUID               |
| `assignor_id`          | `CHAR(36)`       |       no | Assignor UUID                 |
| `external_reference`   | `VARCHAR(128)`   |       no | External receivable reference |
| `receivable_type_code` | `VARCHAR(64)`    |       no | Receivable type               |
| `face_value`           | `DECIMAL(19, 4)` |       no | Original receivable value     |
| `currency_code`        | `CHAR(3)`        |       no | Source currency               |
| `due_date`             | `DATE`           |       no | Receivable due date           |
| `status`               | `VARCHAR(32)`    |       no | Receivable status             |
| `version`              | `BIGINT`         |       no | Optimistic locking version    |
| `created_at`           | `DATETIME(6)`    |       no | Creation timestamp            |
| `updated_at`           | `DATETIME(6)`    |       no | Last update timestamp         |

## 13.3 Primary Key

```sql id="b41bqw"
PRIMARY KEY (id)
```

## 13.4 Foreign Keys

```sql id="tgdtmq"
FOREIGN KEY (assignor_id) REFERENCES assignors(id)
FOREIGN KEY (receivable_type_code) REFERENCES receivable_types(code)
FOREIGN KEY (currency_code) REFERENCES currencies(code)
```

## 13.5 Constraints

```sql id="ldhtp4"
CHECK (face_value > 0)
CHECK (status IN ('AVAILABLE', 'SETTLED', 'CANCELLED'))
CHECK (version >= 0)
UNIQUE (assignor_id, external_reference)
```

## 13.6 Indexes

```sql id="5b2o90"
CREATE INDEX idx_receivables_assignor ON receivables (assignor_id);
CREATE INDEX idx_receivables_type ON receivables (receivable_type_code);
CREATE INDEX idx_receivables_currency ON receivables (currency_code);
CREATE INDEX idx_receivables_status ON receivables (status);
CREATE INDEX idx_receivables_due_date ON receivables (due_date);
```

## 13.7 Status Values

Initial values:

```text id="uhx42n"
AVAILABLE
SETTLED
CANCELLED
```

Rules:

* only `AVAILABLE` receivables may be settled;
* settled receivables must not be settled again;
* cancelled receivables must not be settled.

## 13.8 Notes

The unique constraint on `(assignor_id, external_reference)` gives the application a stable business key for duplicate receivable detection.

The `version` field supports optimistic locking if implemented.

## 14. Table: `settlements`

## 14.1 Purpose

Stores settlement batch headers.

A settlement represents the atomic acquisition/liquidation of one or more receivables.

## 14.2 Columns

| Column                  | Type             | Nullable | Description                               |
| ----------------------- | ---------------- | -------: | ----------------------------------------- |
| `id`                    | `CHAR(36)`       |       no | Settlement UUID                           |
| `assignor_id`           | `CHAR(36)`       |       no | Assignor UUID                             |
| `source_currency_code`  | `CHAR(3)`        |       no | Common source currency for all items      |
| `payment_currency_code` | `CHAR(3)`        |       no | Payment currency                          |
| `status`                | `VARCHAR(32)`    |       no | Settlement status                         |
| `base_rate`             | `DECIMAL(19, 8)` |       no | Base rate used for the batch              |
| `item_count`            | `INT`            |       no | Number of items                           |
| `total_face_value`      | `DECIMAL(19, 4)` |       no | Sum of face values in source currency     |
| `total_present_value`   | `DECIMAL(19, 4)` |       no | Sum of present values in source currency  |
| `total_payment_value`   | `DECIMAL(19, 4)` |       no | Sum of payment values in payment currency |
| `settled_at`            | `DATETIME(6)`    |       no | Settlement timestamp                      |
| `created_at`            | `DATETIME(6)`    |       no | Creation timestamp                        |
| `updated_at`            | `DATETIME(6)`    |       no | Last update timestamp                     |

## 14.3 Primary Key

```sql id="yqa297"
PRIMARY KEY (id)
```

## 14.4 Foreign Keys

```sql id="ewb1n3"
FOREIGN KEY (assignor_id) REFERENCES assignors(id)
FOREIGN KEY (source_currency_code) REFERENCES currencies(code)
FOREIGN KEY (payment_currency_code) REFERENCES currencies(code)
```

## 14.5 Constraints

```sql id="e91q2b"
CHECK (status IN ('PENDING', 'SETTLED', 'FAILED', 'CANCELLED'))
CHECK (base_rate >= 0)
CHECK (item_count > 0)
CHECK (total_face_value >= 0)
CHECK (total_present_value >= 0)
CHECK (total_payment_value >= 0)
```

## 14.6 Indexes

```sql id="z78m8r"
CREATE INDEX idx_settlements_assignor ON settlements (assignor_id);
CREATE INDEX idx_settlements_source_currency ON settlements (source_currency_code);
CREATE INDEX idx_settlements_payment_currency ON settlements (payment_currency_code);
CREATE INDEX idx_settlements_status ON settlements (status);
CREATE INDEX idx_settlements_settled_at ON settlements (settled_at);
CREATE INDEX idx_settlements_statement_default ON settlements (settled_at DESC, id);
```

## 14.7 Status Values

Initial values:

```text id="7skrw1"
PENDING
SETTLED
FAILED
CANCELLED
```

Initial implementation expectation:

* successful settlement creation stores status `SETTLED`;
* invalid settlement attempts should not persist partial settlement records;
* missing exchange rate must not create persisted `FAILED` placeholder rows in the initial version;
* `FAILED` is reserved for future workflows where failed attempts are intentionally persisted.

## 14.8 Notes

`source_currency_code` is included to make settlement header totals meaningful.

It is also the authoritative header-level source currency returned by settlement APIs for the batch.

For the initial version:

```text id="bxs7cu"
total_face_value and total_present_value are in source_currency_code
total_payment_value is in payment_currency_code
```

`base_rate` stores the effective base rate resolved by the backend for the batch.

Resolution order:

```text id="j2ru4f"
1. Request-provided baseRate
2. Server-side DEFAULT_BASE_RATE fallback
3. Fail the operation if neither source is available
```

The initial schema intentionally persists only the effective decimal value, not its provenance.

Future enhancement:

```text id="0voh4w"
Add base_rate_source or equivalent audit metadata if the origin of the effective base rate must be reported directly from persistence.
```

## 15. Table: `settlement_items`

## 15.1 Purpose

Stores item-level calculation snapshots for each receivable in a settlement.

This is the core audit table of the system.

Settlement item records must preserve all calculation inputs and outputs used at settlement time.

## 15.2 Columns

| Column                  | Type             | Nullable | Description                               |
| ----------------------- | ---------------- | -------: | ----------------------------------------- |
| `id`                    | `CHAR(36)`       |       no | Settlement item UUID                      |
| `settlement_id`         | `CHAR(36)`       |       no | Settlement UUID                           |
| `receivable_id`         | `CHAR(36)`       |       no | Receivable UUID                           |
| `external_reference`    | `VARCHAR(128)`   |       no | Snapshot of receivable external reference |
| `receivable_type_code`  | `VARCHAR(64)`    |       no | Snapshot of receivable type               |
| `face_value`            | `DECIMAL(19, 4)` |       no | Snapshot of face value                    |
| `source_currency_code`  | `CHAR(3)`        |       no | Source currency                           |
| `payment_currency_code` | `CHAR(3)`        |       no | Payment currency                          |
| `base_rate`             | `DECIMAL(19, 8)` |       no | Base rate used                            |
| `spread`                | `DECIMAL(19, 8)` |       no | Spread used                               |
| `term_in_months`        | `DECIMAL(19, 8)` |       no | Term used in formula                      |
| `present_value_source`  | `DECIMAL(19, 4)` |       no | Present value in source currency          |
| `discount_value`        | `DECIMAL(19, 4)` |       no | Face value minus present value            |
| `payment_value`         | `DECIMAL(19, 4)` |       no | Final amount in payment currency          |
| `exchange_rate`         | `DECIMAL(19, 8)` |      yes | Exchange-rate snapshot                    |
| `calculated_at`         | `DATETIME(6)`    |       no | Calculation timestamp                     |
| `created_at`            | `DATETIME(6)`    |       no | Creation timestamp                        |

## 15.3 Primary Key

```sql id="6q03ep"
PRIMARY KEY (id)
```

## 15.4 Foreign Keys

```sql id="pz0v9l"
FOREIGN KEY (settlement_id) REFERENCES settlements(id)
FOREIGN KEY (receivable_id) REFERENCES receivables(id)
FOREIGN KEY (receivable_type_code) REFERENCES receivable_types(code)
FOREIGN KEY (source_currency_code) REFERENCES currencies(code)
FOREIGN KEY (payment_currency_code) REFERENCES currencies(code)
```

## 15.5 Constraints

```sql id="d9g69u"
CHECK (face_value > 0)
CHECK (base_rate >= 0)
CHECK (spread >= 0)
CHECK (term_in_months > 0)
CHECK (present_value_source >= 0)
CHECK (discount_value >= 0)
CHECK (payment_value >= 0)
CHECK (exchange_rate IS NULL OR exchange_rate > 0)
UNIQUE (receivable_id)
```

## 15.6 Indexes

```sql id="xxaj9a"
CREATE INDEX idx_settlement_items_settlement ON settlement_items (settlement_id);
CREATE INDEX idx_settlement_items_receivable_type ON settlement_items (receivable_type_code);
CREATE INDEX idx_settlement_items_source_currency ON settlement_items (source_currency_code);
CREATE INDEX idx_settlement_items_payment_currency ON settlement_items (payment_currency_code);
CREATE INDEX idx_settlement_items_calculated_at ON settlement_items (calculated_at);
```

## 15.7 Critical Audit Fields

The following fields are intentionally duplicated from related or calculated data:

```text id="0d1fwf"
external_reference
receivable_type_code
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

Rationale:

Historical settlement records must remain explainable even if reference data changes later.

`base_rate` in each item is the effective base rate actually applied during calculation, regardless of whether it came from the request or the server-side fallback.

## 15.8 Exchange Rate Nullability

`exchange_rate` is nullable.

Rules:

```text id="q0f9m3"
same-currency operation: exchange_rate may be null
cross-currency operation: exchange_rate must be present
```

This rule is enforced primarily by application/domain validation.

A database-level conditional constraint may be added if implementation complexity remains acceptable.

If a required cross-currency exchange rate is missing, the operation must fail before any `settlement_items` row is persisted.

## 16. Relationships Summary

## 16.1 One-to-Many Relationships

```text id="orkz7i"
currencies.code -> exchange_rates.source_currency_code
currencies.code -> exchange_rates.target_currency_code
currencies.code -> receivables.currency_code
currencies.code -> settlements.source_currency_code
currencies.code -> settlements.payment_currency_code
currencies.code -> settlement_items.source_currency_code
currencies.code -> settlement_items.payment_currency_code

receivable_types.code -> receivables.receivable_type_code
receivable_types.code -> settlement_items.receivable_type_code

assignors.id -> receivables.assignor_id
assignors.id -> settlements.assignor_id

settlements.id -> settlement_items.settlement_id
```

## 16.2 One-to-One Business Constraint

```text id="6d39qa"
receivables.id -> settlement_items.receivable_id
```

A receivable may appear in at most one settlement item.

This is enforced by:

```sql id="x13dr8"
UNIQUE (receivable_id)
```

## 17. Analytical Query Support

The settlement statement endpoint must support filters by:

```text id="gs6stt"
period
assignor
payment currency
source currency
receivable type
status
```

## 17.1 Main Query Tables

Expected query path:

```text id="ol5kji"
settlements
JOIN assignors
LEFT JOIN settlement_items
```

Depending on filters:

* filtering by settlement-level fields uses `settlements`;
* filtering by `receivable_type_code` or source currency may require `settlement_items`;
* response rows are settlement-level summaries.

## 17.2 Required Index Support

Minimum indexes:

```text id="spzhak"
settlements.settled_at
settlements.assignor_id
settlements.payment_currency_code
settlements.source_currency_code
settlements.status
settlement_items.settlement_id
settlement_items.receivable_type_code
settlement_items.source_currency_code
settlement_items.payment_currency_code
```

## 17.3 Sorting

Default sort:

```text id="gyv5hi"
settlements.settled_at DESC
```

Tie-breaker:

```text id="h266j7"
settlements.id
```

## 17.4 Pagination

Statement queries must use database-level pagination.

Expected SQL pattern:

```sql id="t7dv2a"
ORDER BY s.settled_at DESC, s.id
LIMIT :size OFFSET :offset
```

The application must not load all settlements and filter them in memory.

## 18. Data Integrity Rules

## 18.1 Financial Integrity

Rules:

* monetary values must be non-negative except face value, which must be positive;
* rates must be non-negative;
* exchange rates must be positive;
* term must be positive;
* `DECIMAL` must be used for all financial numeric columns.

## 18.2 Settlement Integrity

Rules:

* settlement must have at least one item;
* item count must match persisted items;
* totals must be calculated from items;
* a receivable must not be settled twice;
* all items in a settlement must share the same `source_currency_code` as the settlement header;
* settlement and items must be persisted atomically.

Some rules are enforced by application logic because they span multiple rows.

## 18.3 Historical Integrity

Rules:

* settlement item snapshots must not be updated after settlement creation;
* statement queries must use persisted values;
* historical values must not be recomputed from current exchange rates or current spreads.
* persisted `base_rate` values must reflect the effective rate used at calculation time.

## 19. Migration Plan

## 19.1 Initial Migration

Expected file:

```text id="eoagfr"
backend/src/main/resources/db/migration/V1__create_initial_schema.sql
```

Responsibilities:

* create all tables;
* create primary keys;
* create foreign keys;
* create constraints;
* create indexes.

## 19.2 Reference Data Seed

Expected file:

```text id="2wc2ks"
backend/src/main/resources/db/migration/V2__seed_reference_data.sql
```

Responsibilities:

* insert supported currencies;
* insert supported receivable types.

Seed data:

```text id="mcdss4"
currencies:
- BRL
- USD

receivable_types:
- MERCANTILE_DUPLICATE, 0.01500000
- POST_DATED_CHECK, 0.02500000
```

## 19.3 Optional Additional Migration

Expected file, if separated:

```text id="reinkm"
backend/src/main/resources/db/migration/V3__add_statement_query_indexes.sql
```

Responsibilities:

* add or refine indexes for statement query performance.

For the initial delivery, indexes may be included directly in `V1__create_initial_schema.sql`.

## 20. Initial DDL Draft

The exact Flyway migration may refine formatting, but it must remain equivalent to the model below.

```sql id="vr9abf"
CREATE TABLE currencies (
    code CHAR(3) NOT NULL,
    name VARCHAR(64) NOT NULL,
    decimal_places TINYINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (code),
    CHECK (CHAR_LENGTH(code) = 3),
    CHECK (decimal_places >= 0)
);

CREATE TABLE receivable_types (
    code VARCHAR(64) NOT NULL,
    description VARCHAR(128) NOT NULL,
    monthly_spread DECIMAL(19, 8) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (code),
    CHECK (monthly_spread >= 0)
);

CREATE TABLE assignors (
    id CHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    document VARCHAR(32) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CHECK (name <> '')
);

CREATE INDEX idx_assignors_document ON assignors (document);
CREATE INDEX idx_assignors_name ON assignors (name);

CREATE TABLE exchange_rates (
    id CHAR(36) NOT NULL,
    source_currency_code CHAR(3) NOT NULL,
    target_currency_code CHAR(3) NOT NULL,
    rate DECIMAL(19, 8) NOT NULL,
    valid_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_exchange_rates_source_currency
        FOREIGN KEY (source_currency_code) REFERENCES currencies(code),
    CONSTRAINT fk_exchange_rates_target_currency
        FOREIGN KEY (target_currency_code) REFERENCES currencies(code),
    CHECK (rate > 0),
    CHECK (source_currency_code <> target_currency_code)
);

CREATE INDEX idx_exchange_rates_pair_valid_at
    ON exchange_rates (source_currency_code, target_currency_code, valid_at DESC, created_at DESC);

CREATE TABLE receivables (
    id CHAR(36) NOT NULL,
    assignor_id CHAR(36) NOT NULL,
    external_reference VARCHAR(128) NOT NULL,
    receivable_type_code VARCHAR(64) NOT NULL,
    face_value DECIMAL(19, 4) NOT NULL,
    currency_code CHAR(3) NOT NULL,
    due_date DATE NOT NULL,
    status VARCHAR(32) NOT NULL,
    version BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_receivables_assignor
        FOREIGN KEY (assignor_id) REFERENCES assignors(id),
    CONSTRAINT fk_receivables_type
        FOREIGN KEY (receivable_type_code) REFERENCES receivable_types(code),
    CONSTRAINT fk_receivables_currency
        FOREIGN KEY (currency_code) REFERENCES currencies(code),
    CONSTRAINT uk_receivables_assignor_external_reference
        UNIQUE (assignor_id, external_reference),
    CHECK (face_value > 0),
    CHECK (status IN ('AVAILABLE', 'SETTLED', 'CANCELLED')),
    CHECK (version >= 0)
);

CREATE INDEX idx_receivables_assignor ON receivables (assignor_id);
CREATE INDEX idx_receivables_type ON receivables (receivable_type_code);
CREATE INDEX idx_receivables_currency ON receivables (currency_code);
CREATE INDEX idx_receivables_status ON receivables (status);
CREATE INDEX idx_receivables_due_date ON receivables (due_date);

CREATE TABLE settlements (
    id CHAR(36) NOT NULL,
    assignor_id CHAR(36) NOT NULL,
    source_currency_code CHAR(3) NOT NULL,
    payment_currency_code CHAR(3) NOT NULL,
    status VARCHAR(32) NOT NULL,
    base_rate DECIMAL(19, 8) NOT NULL,
    item_count INT NOT NULL,
    total_face_value DECIMAL(19, 4) NOT NULL,
    total_present_value DECIMAL(19, 4) NOT NULL,
    total_payment_value DECIMAL(19, 4) NOT NULL,
    settled_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_settlements_assignor
        FOREIGN KEY (assignor_id) REFERENCES assignors(id),
    CONSTRAINT fk_settlements_source_currency
        FOREIGN KEY (source_currency_code) REFERENCES currencies(code),
    CONSTRAINT fk_settlements_payment_currency
        FOREIGN KEY (payment_currency_code) REFERENCES currencies(code),
    CHECK (status IN ('PENDING', 'SETTLED', 'FAILED', 'CANCELLED')),
    CHECK (base_rate >= 0),
    CHECK (item_count > 0),
    CHECK (total_face_value >= 0),
    CHECK (total_present_value >= 0),
    CHECK (total_payment_value >= 0)
);

CREATE INDEX idx_settlements_assignor ON settlements (assignor_id);
CREATE INDEX idx_settlements_source_currency ON settlements (source_currency_code);
CREATE INDEX idx_settlements_payment_currency ON settlements (payment_currency_code);
CREATE INDEX idx_settlements_status ON settlements (status);
CREATE INDEX idx_settlements_settled_at ON settlements (settled_at);
CREATE INDEX idx_settlements_statement_default ON settlements (settled_at DESC, id);

CREATE TABLE settlement_items (
    id CHAR(36) NOT NULL,
    settlement_id CHAR(36) NOT NULL,
    receivable_id CHAR(36) NOT NULL,
    external_reference VARCHAR(128) NOT NULL,
    receivable_type_code VARCHAR(64) NOT NULL,
    face_value DECIMAL(19, 4) NOT NULL,
    source_currency_code CHAR(3) NOT NULL,
    payment_currency_code CHAR(3) NOT NULL,
    base_rate DECIMAL(19, 8) NOT NULL,
    spread DECIMAL(19, 8) NOT NULL,
    term_in_months DECIMAL(19, 8) NOT NULL,
    present_value_source DECIMAL(19, 4) NOT NULL,
    discount_value DECIMAL(19, 4) NOT NULL,
    payment_value DECIMAL(19, 4) NOT NULL,
    exchange_rate DECIMAL(19, 8) NULL,
    calculated_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_settlement_items_settlement
        FOREIGN KEY (settlement_id) REFERENCES settlements(id),
    CONSTRAINT fk_settlement_items_receivable
        FOREIGN KEY (receivable_id) REFERENCES receivables(id),
    CONSTRAINT fk_settlement_items_receivable_type
        FOREIGN KEY (receivable_type_code) REFERENCES receivable_types(code),
    CONSTRAINT fk_settlement_items_source_currency
        FOREIGN KEY (source_currency_code) REFERENCES currencies(code),
    CONSTRAINT fk_settlement_items_payment_currency
        FOREIGN KEY (payment_currency_code) REFERENCES currencies(code),
    CONSTRAINT uk_settlement_items_receivable
        UNIQUE (receivable_id),
    CHECK (face_value > 0),
    CHECK (base_rate >= 0),
    CHECK (spread >= 0),
    CHECK (term_in_months > 0),
    CHECK (present_value_source >= 0),
    CHECK (discount_value >= 0),
    CHECK (payment_value >= 0),
    CHECK (exchange_rate IS NULL OR exchange_rate > 0)
);

CREATE INDEX idx_settlement_items_settlement ON settlement_items (settlement_id);
CREATE INDEX idx_settlement_items_receivable_type ON settlement_items (receivable_type_code);
CREATE INDEX idx_settlement_items_source_currency ON settlement_items (source_currency_code);
CREATE INDEX idx_settlement_items_payment_currency ON settlement_items (payment_currency_code);
CREATE INDEX idx_settlement_items_calculated_at ON settlement_items (calculated_at);
```

Application note:

```text id="u3jlwm"
The initial DDL does not encode cross-row equality between settlements.source_currency_code and settlement_items.source_currency_code.
This invariant must be enforced by application/domain validation and covered by tests.
```

## 21. Seed Data Draft

```sql id="t4bv18"
INSERT INTO currencies (
    code,
    name,
    decimal_places,
    created_at,
    updated_at
) VALUES
    ('BRL', 'Brazilian Real', 2, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
    ('USD', 'US Dollar', 2, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6));

INSERT INTO receivable_types (
    code,
    description,
    monthly_spread,
    created_at,
    updated_at
) VALUES
    ('MERCANTILE_DUPLICATE', 'Mercantile Duplicate', 0.01500000, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
    ('POST_DATED_CHECK', 'Post-Dated Check', 0.02500000, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6));
```

## 22. JPA Mapping Guidance

## 22.1 IDs

Use UUID in application code.

Possible implementation:

```java id="qz4dug"
UUID id
```

Persisted as:

```text id="yuvf0z"
CHAR(36)
```

Alternative:

```java id="rv7v1n"
String id
```

Using `UUID` is preferred if mapping remains simple.

## 22.2 Monetary and Rate Fields

Use:

```java id="eupizb"
BigDecimal
```

for:

* face value;
* present value;
* discount value;
* payment value;
* totals;
* base rate;
* spread;
* exchange rate;
* term.

Do not use:

```java id="dbntoa"
double
float
Double
Float
```

## 22.3 Optimistic Locking

`receivables.version` may be mapped with:

```java id="8vac41"
@Version
```

if optimistic locking is implemented.

This is recommended for concurrency safety.

## 22.4 Enumerations

Status values and type codes may be represented as Java enums.

Recommended mapping:

```java id="8vkd97"
@Enumerated(EnumType.STRING)
```

Do not use ordinal enum persistence.

## 22.5 Entity Update Rules

Settlement item calculation snapshots should be immutable after creation.

Implementation may enforce this by:

* not exposing update methods;
* avoiding update endpoints;
* keeping fields set only at creation time;
* using application-level discipline and tests.

## 23. Data Model Validation Checklist

The implementation is consistent with this model if:

```text id="2quax1"
- Flyway creates all required tables.
- Hibernate validates schema successfully.
- Monetary database columns use DECIMAL.
- Financial Java fields use BigDecimal.
- Currencies are seeded.
- Receivable types are seeded.
- Exchange-rate direction is explicit.
- Receivable external reference is unique per assignor.
- A receivable cannot appear in more than one settlement item.
- Settlement item stores calculation snapshots.
- Statement query filters are supported by indexes.
- Timestamps are stored in UTC.
- ER diagram matches the schema.
```

## 24. Known Trade-Offs

## 24.1 CHAR(36) UUID

Accepted for readability and simplicity.

Trade-off:

* less storage efficient than `BINARY(16)`.

## 24.2 Snapshot Duplication in Settlement Items

Accepted for auditability.

Trade-off:

* duplicates some reference data;
* increases storage;
* avoids historical recalculation errors.

## 24.3 One Source Currency per Settlement

Accepted for initial aggregate simplicity.

Trade-off:

* prevents mixed-source-currency batches;
* simplifies settlement totals;
* keeps API and statement responses easier to understand.

## 24.4 Reference Spread in `receivable_types`

Accepted for transparency.

Trade-off:

* strategy classes also encode spread behavior;
* must keep seed data and strategy behavior consistent.

Mitigation:

* tests must validate strategy spread values;
* reference endpoint must return expected spreads.

## 25. Future Data Model Enhancements

Potential future enhancements:

```text id="2w3f7e"
base_rates
settlement_currency_totals
audit_events
users
roles
approval_workflows
exchange_rate_sources
settlement_status_history
outbox_events
```

These are intentionally out of scope for the initial delivery.

## 26. Related Documents

```text id="ve7tr3"
README.md
AGENTS.md
docs/specs/02-domain-glossary.md
docs/specs/03-business-rules.md
docs/specs/04-api-contract.md
docs/specs/06-architecture.md
docs/specs/07-testing-strategy.md
docs/diagrams/er-diagram.md
docs/adr/ADR-002-database-choice.md
docs/adr/ADR-003-money-precision.md
docs/adr/ADR-004-architecture-style.md
```
