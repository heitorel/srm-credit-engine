# ADR-003 — Money Precision

## Status

Accepted

## Date

2026-07-07

## Context

The SRM Credit Engine operates in a financial domain where monetary precision directly affects pricing, settlement, auditability and trust.

The system must calculate:

* face value;
* present value;
* discount value;
* net payment value;
* settlement totals;
* base rate;
* spread;
* term;
* exchange-rate conversion.

The technical challenge explicitly requires safe decimal precision and reliable financial calculations.

The project must avoid precision defects caused by binary floating-point arithmetic.

## Decision

The project will use decimal-safe arithmetic for all financial calculations.

In the Java backend, the selected type is:

```text
BigDecimal
```

This applies to:

* money;
* rates;
* exchange rates;
* spreads;
* terms;
* financial totals;
* intermediate calculation results.

The project must not use the following types for financial calculations:

```text
double
float
Double
Float
```

The database must use `DECIMAL` columns for monetary values and rates.

Recommended initial database scales:

```text
Money:          DECIMAL(19, 4)
Rates:          DECIMAL(19, 8)
Exchange rates: DECIMAL(19, 8)
Terms:          DECIMAL(19, 8)
```

Recommended application scales:

```text
Monetary API output scale: 2
Persisted monetary scale: 4
Rate scale: 8
Intermediate calculation scale: 12 or higher when needed
Rounding mode: HALF_UP
```

## Rationale

### 1. Financial calculations require decimal precision

Binary floating-point types are not appropriate for monetary calculations because many decimal values cannot be represented exactly in binary.

Example risk:

```java
double value = 0.1 + 0.2;
```

This kind of operation may produce unexpected decimal representations.

In a credit-pricing system, small rounding defects can affect:

* present value;
* discount;
* currency conversion;
* settlement totals;
* historical audit records.

### 2. BigDecimal is the standard Java choice for decimal financial arithmetic

`BigDecimal` supports explicit decimal representation, scale and rounding behavior.

This is important because the system must calculate values such as:

```text
Present Value = Face Value / (1 + Base Rate + Spread) ^ Term
```

and persist the calculation snapshot used during settlement.

### 3. Rounding must be explicit

Financial code must not rely on default or implicit rounding behavior.

Every operation that may require rounding must specify:

* target scale;
* rounding mode.

The selected initial rounding mode is:

```text
HALF_UP
```

This decision is simple, explicit and easy to explain in the context of a technical challenge.

If a different rounding standard is later required, this ADR and the business rules must be updated.

### 4. Intermediate precision must be higher than final output precision

The system should avoid rounding too early.

Example:

```text
1. Calculate present value with high intermediate precision.
2. Apply currency conversion with high intermediate precision.
3. Round final monetary output at the boundary.
```

Premature rounding may distort results, especially in cross-currency scenarios.

## Financial Types

### Money

Represents a monetary amount associated with a currency.

Recommended domain representation:

```java
public record Money(BigDecimal amount, CurrencyCode currency) {
}
```

Rules:

* amount must not be null;
* currency must not be null;
* amount must not use floating-point types;
* monetary operations should preserve currency consistency;
* cross-currency operations must go through explicit conversion logic.

### Rate

Represents decimal rates such as base rate, spread and exchange rate.

Recommended domain representation:

```java
public record Rate(BigDecimal value) {
}
```

Rules:

* value must not be null;
* value must be decimal;
* value must not be represented as a percentage number.

Correct:

```text
1.5% = 0.01500000
```

Incorrect:

```text
1.5% = 1.5
```

### Term

Represents the time factor used in the present value formula.

Recommended domain representation:

```java
public record Term(BigDecimal months) {
}
```

Rules:

* term must be greater than zero;
* term is represented in commercial months;
* term should use decimal precision because it may be fractional.

## Business Rule Alignment

This ADR supports the following business rules:

```text
BR-001 — Decimal-Safe Financial Arithmetic
BR-002 — Explicit Rounding
BR-013 — Mercantile Duplicate Spread
BR-014 — Post-Dated Check Spread
BR-016 — Base Pricing Formula
BR-017 — Base Rate
BR-018 — Term Calculation
BR-022 — Discount Calculation
BR-023 — Cross-Currency Conversion Order
BR-031 — Settlement Must Be Auditable
BR-032 — Settlement Header Totals
BR-055 — Historical Calculation Immutability
BR-063 — Financial Calculation Tests Are Mandatory
```

## Calculation Rules

### Present Value Calculation

Formula:

```text
Present Value = Face Value / (1 + Base Rate + Spread) ^ Term
```

Rules:

* face value must be represented as `BigDecimal`;
* base rate must be represented as decimal `BigDecimal`;
* spread must be represented as decimal `BigDecimal`;
* term must be represented as decimal `BigDecimal`;
* present value must be calculated in source currency;
* conversion, when required, must happen after present value calculation.

### Cross-Currency Calculation

Correct order:

```text
1. Calculate present value in source currency.
2. Convert present value to payment currency.
3. Round final payment value at the defined boundary.
4. Persist the exchange-rate snapshot used.
```

Incorrect order:

```text
1. Convert face value to payment currency.
2. Calculate present value after conversion.
```

### Discount Calculation

Formula:

```text
Discount = Face Value - Present Value
```

Rules:

* discount is calculated in source currency;
* discount must use decimal-safe arithmetic;
* discount must be persisted in the settlement item snapshot.

### Totals Calculation

Settlement totals must be derived from settlement items.

Rules:

* total face value is the sum of item face values;
* total present value is the sum of item present values;
* total payment value is the sum of item payment values;
* totals must use `BigDecimal`;
* totals must not use stream operations that reduce to floating-point values.

## BigDecimal Construction Rules

### Allowed

Use string-based construction when creating constants:

```java
BigDecimal spread = new BigDecimal("0.01500000");
BigDecimal baseRate = new BigDecimal("0.01000000");
```

Use `BigDecimal.valueOf(long)` or `BigDecimal.valueOf(double)` only when the input is already controlled and understood.

Preferred for fixed financial constants:

```java
new BigDecimal("0.01500000")
```

### Forbidden

Do not create financial `BigDecimal` values from floating-point literals:

```java
new BigDecimal(0.015)
```

This may preserve the binary floating-point approximation inside the `BigDecimal`.

## Rounding Policy

### Initial Rounding Mode

The initial rounding mode is:

```text
RoundingMode.HALF_UP
```

### Initial Scales

Recommended scales:

```text
MONEY_OUTPUT_SCALE = 2
MONEY_STORAGE_SCALE = 4
RATE_SCALE = 8
CALCULATION_SCALE = 12
```

### Rounding Boundaries

Rounding should occur at controlled boundaries:

* before API output;
* before persistence if database scale requires it;
* after final conversion;
* after totals are calculated.

Rounding should not occur unnecessarily between every internal step.

## Database Mapping Rules

### Monetary Columns

Recommended type:

```sql
DECIMAL(19, 4)
```

Use for:

* face value;
* present value;
* discount value;
* payment value;
* settlement totals.

### Rate Columns

Recommended type:

```sql
DECIMAL(19, 8)
```

Use for:

* base rate;
* spread;
* exchange rate;
* term in months.

### Forbidden Database Types

Do not use the following for financial columns:

```sql
FLOAT
DOUBLE
REAL
```

## API Representation Rules

API payloads should represent financial values as JSON numbers or strings consistently.

Initial project decision:

```text
Use JSON numbers for monetary and rate values.
Parse them into BigDecimal in the backend.
```

Examples:

```json
{
  "faceValue": 10000.00,
  "baseRate": 0.01000000,
  "exchangeRate": 5.25000000
}
```

Rules:

* backend DTOs must use `BigDecimal`;
* validation must reject invalid values;
* API documentation must show rates as decimal values;
* examples must not show percentage-style values such as `1.5` for `1.5%`.

## Validation Rules

### Money Validation

A monetary amount must:

* be present;
* be greater than zero when used as face value;
* use supported scale;
* be associated with a supported currency.

### Rate Validation

A rate must:

* be present when required;
* be greater than or equal to zero for base rate and spread;
* be greater than zero for exchange rate;
* use decimal representation.

### Term Validation

A term must:

* be greater than zero;
* be calculated consistently from due date;
* be persisted in settlement item snapshots.

## Testing Requirements

The implementation must include tests for monetary precision.

Minimum test cases:

### 1. Spread representation

Verify that:

```text
1.5% = 0.01500000
2.5% = 0.02500000
```

### 2. Same-currency pricing

Verify present value and discount calculation for a BRL receivable paid in BRL.

### 3. Cross-currency pricing

Verify that conversion is applied after present value calculation.

### 4. Rounding behavior

Verify final monetary rounding with expected scale and rounding mode.

### 5. Invalid floating-point regression

Review code and tests to ensure financial calculations do not use:

```text
double
float
Double
Float
```

### 6. Settlement totals

Verify totals are calculated from `BigDecimal` item values.

### 7. Exchange-rate snapshot

Verify persisted settlement item keeps the exchange rate used during calculation.

## Alternatives Considered

### double / float

Rejected.

Reason:

* unsafe for decimal financial calculations;
* can introduce invisible rounding defects;
* does not satisfy the precision requirements of the domain.

### Long cents

Considered.

This approach stores money as minor units, such as cents.

Advantages:

* avoids decimal floating-point problems;
* simple for single-currency money;
* efficient arithmetic.

Rejected for this project because:

* the system also handles rates, terms and exchange rates;
* cross-currency conversion requires decimal rate calculations;
* `BigDecimal` provides clearer expression of financial formulas;
* using long cents would still require decimal handling for rates.

### BigDecimal everywhere without value objects

Considered.

Advantages:

* simple;
* fast to implement.

Rejected as the preferred design because:

* primitive obsession can spread financial rules across the codebase;
* value objects improve validation and readability;
* value objects help prevent currency and scale mistakes.

However, direct `BigDecimal` usage inside DTOs and persistence mappings is acceptable.

Domain logic should prefer explicit concepts such as:

```text
Money
Rate
Term
CurrencyCode
```

### External money library

Considered.

Examples include Java money libraries.

Rejected for the initial delivery because:

* the challenge scope is small enough for explicit value objects;
* adding a library increases cognitive overhead;
* the evaluator can more easily inspect custom financial logic;
* `BigDecimal` is sufficient for the required calculations.

## Consequences

### Positive Consequences

This decision provides:

* explicit decimal precision;
* predictable rounding behavior;
* easier financial auditability;
* safer cross-currency calculations;
* clearer tests;
* better alignment with financial-domain expectations.

### Negative Consequences

This decision introduces:

* more verbose arithmetic code;
* need for careful scale and rounding management;
* need to handle exponentiation with `BigDecimal`;
* need for utility methods or domain value objects;
* risk of accidental `double` usage if not reviewed.

### Mitigations

Mitigations:

* centralize financial math operations;
* use value objects for money, rate and term;
* prohibit floating-point types in financial code;
* add unit tests for pricing edge cases;
* review generated AI code carefully;
* document rounding behavior;
* include financial precision rules in `AGENTS.md`.

## BigDecimal Exponentiation Strategy

The formula includes an exponent:

```text
(1 + Base Rate + Spread) ^ Term
```

Because `BigDecimal` does not provide a simple built-in method for arbitrary fractional exponentiation, the implementation must choose a controlled approach.

Initial implementation decision:

```text
Term will be calculated as a decimal number of months.
The pricing engine must use a documented, tested exponentiation strategy.
```

Acceptable approaches:

### Option A — Integer month term

Use integer commercial months.

Pros:

* easier `BigDecimal.pow(int)` usage;
* fully decimal-safe;
* simple to test.

Cons:

* less precise for due dates that do not align with full months.

### Option B — Fractional term with controlled math utility

Use a dedicated financial math component for fractional exponentiation.

Pros:

* preserves the documented commercial-month rule;
* handles fractional terms.

Cons:

* Java standard library may require careful handling;
* must avoid uncontrolled conversion to `double`;
* needs explicit tests and documentation.

### Initial Preferred Approach

For the initial technical challenge, the preferred approach is:

```text
Use integer commercial days converted to BigDecimal months for the API and audit snapshot.
For exponentiation, use a controlled financial math utility with explicit documentation and tests.
```

If the implementation temporarily restricts term to integer months to avoid unsafe fractional exponentiation, this must be documented in:

```text
docs/specs/03-business-rules.md
docs/specs/04-api-contract.md
docs/specs/07-testing-strategy.md
AI_USAGE.md
```

No hidden approximation is acceptable.

## AI-Assisted Development Risk

AI tools may generate unsafe financial code.

Common unsafe outputs:

```java
double presentValue = faceValue / Math.pow(1 + baseRate + spread, term);
```

or:

```java
BigDecimal spread = new BigDecimal(0.015);
```

Both patterns are not acceptable.

AI-generated code must be reviewed for:

* floating-point usage;
* implicit rounding;
* wrong percentage representation;
* wrong currency conversion order;
* missing snapshot persistence;
* missing precision tests.

## Decision Validation

This decision is valid if the implementation demonstrates:

* no floating-point types in financial calculation code;
* `BigDecimal` DTO fields for financial API payloads;
* `DECIMAL` database columns;
* centralized scale and rounding policy;
* tests for pricing precision;
* tests for cross-currency conversion order;
* persisted settlement snapshots;
* documented assumptions around term and exponentiation.

## Related Documents

```text
README.md
AGENTS.md
AI_USAGE.md
docs/specs/03-business-rules.md
docs/specs/04-api-contract.md
docs/specs/05-data-model.md
docs/specs/07-testing-strategy.md
docs/adr/ADR-001-backend-stack.md
docs/adr/ADR-002-database-choice.md
```

## Review Notes

This ADR must be revisited if:

* a different rounding standard is required;
* a dedicated money library is introduced;
* term calculation changes;
* base-rate management changes;
* the project expands to currencies with different decimal places;
* fractional exponentiation strategy changes.

Any change to money precision must update:

* this ADR;
* business rules;
* data model;
* API contract;
* tests;
* AI prompts;
* documentation examples.
