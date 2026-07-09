# 04 - Pricing Engine Prompt

## Purpose

Use this prompt to implement the pricing engine and pricing simulation endpoint for the **SRM Credit Engine**.

This prompt focuses on the core financial calculation flow:

* value objects for financial concepts;
* receivable-type pricing strategies;
* Strategy Pattern resolver;
* present value calculation;
* discount calculation;
* same-currency pricing;
* cross-currency pricing;
* exchange-rate lookup;
* pricing simulation API;
* financial calculation tests.

This is a business-critical task.

All generated code must be reviewed carefully before commit.

## Target Branch

Recommended branch:

```text
feature/pricing-engine
```

Recommended commit messages:

```text
feat: implement pricing strategy engine
test: cover pricing calculation rules
```

## Prerequisites

Before running this prompt, the following features should already exist or be clearly available:

```text
[ ] Backend scaffold exists.
[ ] Maven project is configured.
[ ] Base package structure exists.
[ ] Global exception handling exists.
[ ] Flyway migrations exist or data model is available.
[ ] Supported currencies are seeded or represented.
[ ] Supported receivable types are seeded or represented.
[ ] Exchange-rate lookup exists.
```

If the exchange-rate module is not implemented yet, stop and report the missing prerequisite instead of introducing a temporary production abstraction that bypasses the documented implementation order.

## Prompt

```text
You are working on the SRM Credit Engine repository.

This task is to implement only the pricing engine and the pricing simulation endpoint.

Read first:
- AGENTS.md
- README.md
- AI_USAGE.md
- docs/specs/02-domain-glossary.md
- docs/specs/03-business-rules.md
- docs/specs/04-api-contract.md
- docs/specs/06-architecture.md
- docs/specs/07-testing-strategy.md
- docs/specs/08-acceptance-criteria.md
- docs/adr/ADR-001-backend-stack.md
- docs/adr/ADR-003-money-precision.md
- docs/adr/ADR-004-architecture-style.md
- docs/adr/ADR-007-ai-assisted-development.md

Task:
Implement the pricing engine and POST /api/pricing/simulations.

Scope:
You may change:
- backend/src/main/java
- backend/src/test/java
- backend/pom.xml only if a test or implementation dependency is strictly required
- AI_USAGE.md only if you materially use AI and need to log the interaction

Do not change:
- frontend/
- docs/specs/
- docs/adr/
- docs/diagrams/
- docs/prompts/
- docker-compose.yml
- .env.example
- database migrations unless a missing required field blocks the implementation

Functional requirements:
- Implement pricing simulation endpoint:
  POST /api/pricing/simulations
- Implement request and response DTOs according to docs/specs/04-api-contract.md.
- Implement backend validation for pricing request fields.
- Implement pricing calculation in backend domain/application code.
- Implement Strategy Pattern for receivable type spread resolution.
- Implement Mercantile Duplicate strategy.
- Implement Post-Dated Check strategy.
- Implement strategy resolver.
- Calculate present value using:
  Present Value = Face Value / (1 + Base Rate + Spread) ^ Term
- Calculate discount as:
  Discount = Face Value - Present Value
- Calculate present value in source currency first.
- For same-currency operation, return netPaymentValue equal to presentValueInSourceCurrency after monetary rounding.
- For cross-currency operation, retrieve exact exchange-rate pair and convert present value after discounting.
- Do not persist settlement records during simulation.
- Return all calculation details required by the API contract.

Financial requirements:
- Use BigDecimal for all monetary values, rates, terms and calculation results.
- Do not use double, float, Double or Float for financial values.
- Do not construct BigDecimal from floating-point literals.
- Use string-based BigDecimal constants for spreads.
- Use explicit scale and rounding.
- Use RoundingMode.HALF_UP unless a spec says otherwise.
- Centralize financial math behavior in a component such as FinancialMath.
- Keep monetary output scale and rate scale explicit.

Required spread values:
- MERCANTILE_DUPLICATE = 0.01500000
- POST_DATED_CHECK = 0.02500000

Term requirements:
- Calculate term in commercial months.
- Use daysBetween(pricingDate, dueDate) / 30.
- Use a fixed/injectable Clock for testability.
- Reject past due dates.
- Reject same-day due dates.
- Persisting term is not needed in simulation, but response must include termInMonths.

Currency requirements:
- Supported currencies are BRL and USD.
- Same-currency pricing must not require exchange-rate lookup.
- Cross-currency pricing must require an exact exchange-rate pair.
- Do not silently invert exchange rates.
- Do not hardcode exchange rates.
- If required exchange rate is missing, return a structured 422 error.

Architecture requirements:
- Controller must be thin.
- Controller must not calculate present value.
- Controller must not resolve spreads directly.
- Controller must not perform currency conversion.
- Application service should orchestrate pricing simulation.
- Domain components should own pricing rules.
- Infrastructure should own exchange-rate persistence/lookup details if already implemented.
- Keep the implementation aligned with docs/specs/06-architecture.md.

Expected components:
- PricingSimulationController
- PricingSimulationRequest
- PricingSimulationResponse
- PricingSimulationService
- PricingEngine
- PricingStrategy
- MercantileDuplicatePricingStrategy
- PostDatedCheckPricingStrategy
- PricingStrategyResolver
- PricingResult
- Money
- Rate
- Term
- CurrencyCode
- FinancialMath
- MissingExchangeRateException or equivalent business exception
- UnsupportedReceivableTypeException or equivalent business exception
- InvalidDueDateException or equivalent business exception

Tests:
Add or update tests for:
- Mercantile Duplicate strategy spread.
- Post-Dated Check strategy spread.
- Strategy resolver behavior.
- Unsupported receivable type.
- Same-currency pricing.
- Cross-currency pricing.
- Cross-currency conversion order.
- Missing exchange rate.
- Past due date.
- Same-day due date.
- Discount calculation.
- Rounding behavior.
- Simulation does not persist settlement records, if persistence components already exist.
- API validation errors for invalid pricing request.

Expected commands:
- cd backend
- mvn test

Constraints:
- Do not implement settlement creation in this task.
- Do not implement statement query in this task.
- Do not implement frontend in this task.
- Do not introduce unnecessary dependencies.
- Do not introduce Lombok unless it already exists in the project.
- Do not move business logic into controllers.
- Do not use in-memory fake exchange rates as production behavior.
- Do not change API contracts without reporting the mismatch first.

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
- Confirm BigDecimal usage.
- Confirm no double/float financial code.
- Confirm Strategy Pattern implementation.
- Confirm same-currency behavior.
- Confirm cross-currency conversion order.
- Confirm missing exchange-rate behavior.
- Confirm controller remains thin.

Risks or follow-ups:
- ...
```

## Expected AI Output

The AI agent should implement a focused pricing feature.

Expected backend additions may include files similar to:

```text
backend/src/main/java/com/srm/creditengine/api/controller/PricingSimulationController.java
backend/src/main/java/com/srm/creditengine/api/request/PricingSimulationRequest.java
backend/src/main/java/com/srm/creditengine/api/response/PricingSimulationResponse.java
backend/src/main/java/com/srm/creditengine/application/pricing/PricingSimulationService.java
backend/src/main/java/com/srm/creditengine/domain/pricing/PricingEngine.java
backend/src/main/java/com/srm/creditengine/domain/pricing/PricingStrategy.java
backend/src/main/java/com/srm/creditengine/domain/pricing/MercantileDuplicatePricingStrategy.java
backend/src/main/java/com/srm/creditengine/domain/pricing/PostDatedCheckPricingStrategy.java
backend/src/main/java/com/srm/creditengine/domain/pricing/PricingStrategyResolver.java
backend/src/main/java/com/srm/creditengine/domain/pricing/PricingResult.java
backend/src/main/java/com/srm/creditengine/domain/shared/Money.java
backend/src/main/java/com/srm/creditengine/domain/shared/Rate.java
backend/src/main/java/com/srm/creditengine/domain/shared/Term.java
backend/src/main/java/com/srm/creditengine/domain/currency/CurrencyCode.java
backend/src/main/java/com/srm/creditengine/domain/shared/FinancialMath.java
```

Exact names may vary if they remain aligned with the specs and architecture.

## Required API Contract

The endpoint must follow this contract:

```http
POST /api/pricing/simulations
```

Request example:

```json
{
  "faceValue": 10000.00,
  "sourceCurrency": "BRL",
  "paymentCurrency": "USD",
  "baseRate": 0.01000000,
  "receivableType": "MERCANTILE_DUPLICATE",
  "dueDate": "2026-09-07"
}
```

Response example:

```json
{
  "faceValue": 10000.00,
  "sourceCurrency": "BRL",
  "paymentCurrency": "USD",
  "presentValueInSourceCurrency": 9509.18,
  "netPaymentValue": 1811.27,
  "discountValue": 490.82,
  "baseRate": 0.01000000,
  "spread": 0.01500000,
  "termInMonths": 2.06666667,
  "exchangeRate": 5.25000000,
  "calculatedAt": "2026-07-07T13:20:00Z"
}
```

Same-currency response must use:

```json
{
  "exchangeRate": null
}
```

## Financial Calculation Requirements

## 1. Present Value

Formula:

```text
Present Value = Face Value / (1 + Base Rate + Spread) ^ Term
```

Rules:

```text
- Face value is in source currency.
- Base rate is monthly.
- Spread is monthly.
- Term is expressed in commercial months.
- Present value is calculated in source currency.
```

## 2. Discount

Formula:

```text
Discount = Face Value - Present Value
```

Rules:

```text
- Discount is in source currency.
- Discount must not be negative in normal positive-rate scenarios.
```

## 3. Cross-Currency Conversion

Correct order:

```text
1. Calculate present value in source currency.
2. Convert present value to payment currency.
3. Return net payment value.
```

Incorrect order:

```text
1. Convert face value to payment currency.
2. Calculate present value after conversion.
```

The tests must protect this order.

## BigDecimal Rules

Allowed:

```java
new BigDecimal("0.01500000")
new BigDecimal("0.02500000")
BigDecimal.ZERO
BigDecimal.ONE
```

Forbidden:

```java
new BigDecimal(0.015)
new BigDecimal(0.025)
double presentValue
float exchangeRate
Math.pow(1 + baseRate, term)
```

If an exponentiation helper requires approximation, the implementation must be isolated, documented and tested. Hidden `double` conversion spread across business code is not acceptable.

## Exponentiation Guidance

The present value formula uses exponentiation:

```text
(1 + Base Rate + Spread) ^ Term
```

Because Java `BigDecimal` does not directly support arbitrary fractional exponents, the implementation must choose a controlled strategy.

Preferred options:

```text
Option A:
Use BigDecimal.pow(int) if term is intentionally rounded or constrained to integer months.
This must be documented if chosen.

Option B:
Use a dedicated FinancialMath component for decimal exponentiation.
Any unavoidable approximation must be isolated, documented and tested.

Option C:
Use commercial days with an explicitly tested approximation strategy.
The strategy must not leak double-based arithmetic across the codebase.
```

Do not silently implement a hidden approximation.

If the AI cannot implement fractional exponentiation safely, it must stop and report the trade-off instead of inventing unsafe financial code.

## Required Tests

## 1. Strategy Tests

Required cases:

```text
shouldReturnMercantileDuplicateSpread
shouldReturnPostDatedCheckSpread
shouldResolveMercantileDuplicateStrategy
shouldResolvePostDatedCheckStrategy
shouldFailForUnsupportedReceivableType
```

## 2. Same-Currency Pricing Tests

Required cases:

```text
shouldCalculatePresentValueForSameCurrencyOperation
shouldCalculateDiscountAsFaceValueMinusPresentValue
shouldUsePresentValueAsPaymentValueWhenSameCurrency
shouldNotRequireExchangeRateForSameCurrencyPricing
```

## 3. Cross-Currency Pricing Tests

Required cases:

```text
shouldApplyExchangeConversionAfterPresentValueCalculation
shouldFailCrossCurrencyPricingWhenExchangeRateIsMissing
shouldNotInvertExchangeRateAutomatically
shouldReturnExchangeRateUsedInPricingResult
```

## 4. Due Date and Term Tests

Required cases:

```text
shouldCalculateCommercialMonthTermFromDueDate
shouldRejectPastDueDate
shouldRejectSameDayDueDate
shouldUseFixedClockForDeterministicTermCalculation
```

## 5. API Tests

Required cases, if API test infrastructure exists:

```text
shouldSimulateSameCurrencyPricing
shouldSimulateCrossCurrencyPricing
shouldRejectInvalidFaceValue
shouldRejectPastDueDate
shouldReturnUnprocessableEntityWhenExchangeRateIsMissing
shouldReturnUnprocessableEntityForUnsupportedReceivableType
```

## Acceptance Criteria

This task is acceptable when:

```text
[ ] POST /api/pricing/simulations exists.
[ ] Request DTO matches API contract.
[ ] Response DTO matches API contract.
[ ] Backend validation exists.
[ ] PricingEngine exists.
[ ] PricingStrategy abstraction exists.
[ ] MercantileDuplicatePricingStrategy exists.
[ ] PostDatedCheckPricingStrategy exists.
[ ] PricingStrategyResolver exists.
[ ] MERCANTILE_DUPLICATE spread is 0.01500000.
[ ] POST_DATED_CHECK spread is 0.02500000.
[ ] Present value formula is implemented.
[ ] Discount calculation is implemented.
[ ] Same-currency operation works without exchange rate.
[ ] Cross-currency operation requires exchange rate.
[ ] Cross-currency conversion happens after present value calculation.
[ ] Missing exchange rate returns structured 422 error.
[ ] Past due date is rejected.
[ ] Same-day due date is rejected.
[ ] Financial calculations use BigDecimal.
[ ] No financial calculation uses double or float.
[ ] Controller remains thin.
[ ] Tests cover strategy behavior.
[ ] Tests cover pricing calculation behavior.
[ ] mvn test passes.
```

## Review Checklist for Author

After Codex completes the task, verify:

```text
[ ] Did it avoid changing unrelated files?
[ ] Did it keep business logic out of controllers?
[ ] Did it use BigDecimal everywhere in financial logic?
[ ] Did it avoid new BigDecimal(double)?
[ ] Did it avoid double/float financial calculations?
[ ] Did it avoid hidden Math.pow-based financial logic?
[ ] Did it implement Strategy Pattern clearly?
[ ] Did it use correct spread values?
[ ] Did it calculate term according to specs?
[ ] Did it reject invalid due dates?
[ ] Did it apply FX conversion after present value?
[ ] Did it avoid silent exchange-rate inversion?
[ ] Did it return structured errors?
[ ] Did it add meaningful tests?
[ ] Did mvn test pass?
[ ] Does AI_USAGE.md need an update?
```

## Common AI Mistakes to Reject

Reject or correct the output if the AI:

```text
- uses double, float, Double or Float for financial values;
- uses Math.pow directly in business logic with doubles;
- uses new BigDecimal(0.015);
- represents 1.5% as 1.5;
- represents 2.5% as 2.5;
- converts face value before discounting;
- silently inverts exchange rates;
- hardcodes exchange rates;
- places pricing logic in the controller;
- skips Strategy Pattern and uses only if/else blocks;
- implements settlement creation in this task;
- creates frontend code in this task;
- changes database schema without a clear reason;
- writes tests that only confirm the wrong implementation;
- omits missing exchange-rate behavior;
- omits due-date validation.
```

## Suggested PR Description

````md
## Summary

Implements the pricing engine and pricing simulation endpoint.

## Specs Covered

- docs/specs/03-business-rules.md
- docs/specs/04-api-contract.md
- docs/specs/06-architecture.md
- docs/specs/07-testing-strategy.md
- docs/specs/08-acceptance-criteria.md
- docs/adr/ADR-003-money-precision.md
- docs/adr/ADR-004-architecture-style.md

## Changes

- Added financial value objects.
- Added pricing strategy abstraction.
- Added Mercantile Duplicate strategy.
- Added Post-Dated Check strategy.
- Added strategy resolver.
- Added pricing engine.
- Added pricing simulation application service.
- Added POST /api/pricing/simulations.
- Added tests for pricing strategies and calculation rules.

## Tests

- [x] Unit tests added/updated
- [x] API/application tests added/updated, if available
- [x] Manual validation performed

Commands executed:

```bash
cd backend
mvn test
````

## Risks and Trade-offs

* Review exponentiation strategy and rounding behavior carefully.
* Confirm exchange-rate lookup integration if currency engine was implemented separately.

## AI Usage

AI was used for:

* implementation support
* test generation support

AI_USAGE.md:

* [ ] Updated
* [ ] Not applicable

````

## Related Documents

```text
AGENTS.md
AI_USAGE.md
docs/specs/02-domain-glossary.md
docs/specs/03-business-rules.md
docs/specs/04-api-contract.md
docs/specs/06-architecture.md
docs/specs/07-testing-strategy.md
docs/specs/08-acceptance-criteria.md
docs/adr/ADR-003-money-precision.md
docs/adr/ADR-004-architecture-style.md
docs/adr/ADR-007-ai-assisted-development.md
````

