# ADR-005 — Frontend Stack

## Status

Accepted

## Date

2026-07-07

## Context

The SRM Credit Engine requires a frontend application for operators to interact with the backend system.

The frontend must support:

* input of receivable data;
* pricing simulation through backend API;
* display of calculated values;
* exchange-rate interaction, if implemented in the UI;
* settlement history grid;
* dynamic filters;
* server-side pagination;
* clear validation feedback;
* clear error display;
* maintainable feature organization.

The technical challenge allows the use of a modern SPA framework such as React, Vue, Angular or Svelte.

The selected frontend stack must be mature, strongly typed, maintainable and suitable for a financial operations interface.

## Decision

The frontend will be implemented with:

| Concern             | Decision                                 |
| ------------------- | ---------------------------------------- |
| Framework           | Angular 22                               |
| Language            | TypeScript                               |
| UI library          | Angular Material                         |
| Forms               | Angular Reactive Forms                   |
| HTTP client         | Angular HttpClient                       |
| State management    | Angular Signals + services               |
| Routing             | Angular Router                           |
| Styling             | SCSS                                     |
| Build tool          | Angular CLI / Vite-based Angular tooling |
| Runtime target      | Modern evergreen browsers                |
| Container runtime   | Nginx serving production build           |
| Local orchestration | Docker Compose                           |

NgRx will not be introduced in the initial version.

## Selected Version Targets

Initial version targets:

```text id="20fukr"
Angular: 22.x
TypeScript: 6.0.x
RxJS: 7.x
Node.js: 22.x LTS-compatible
Angular Material: 22.x
```

The frontend should keep Angular packages aligned on the same major version.

Expected package family:

```text id="ikp553"
@angular/core
@angular/common
@angular/router
@angular/forms
@angular/platform-browser
@angular/material
@angular/cdk
```

## Rationale

### 1. Angular is suitable for structured enterprise frontends

Angular was selected because it provides an opinionated, batteries-included framework for building maintainable frontend applications.

The project benefits from Angular because it includes:

* TypeScript-first development;
* built-in routing;
* built-in form handling;
* built-in HTTP client;
* dependency injection;
* strong CLI tooling;
* scalable project conventions;
* mature ecosystem;
* good fit for enterprise-style applications.

This aligns with the backend stack and the financial-system nature of the challenge.

### 2. TypeScript supports safer API integration

The frontend must consume backend contracts for:

* pricing simulation;
* exchange-rate management;
* settlement creation;
* settlement statement queries.

Using TypeScript helps model request and response payloads explicitly.

This reduces the risk of:

* sending invalid payload shapes;
* misreading monetary fields;
* confusing source currency and payment currency;
* mishandling pagination metadata.

### 3. Angular Reactive Forms fit operator input screens

The product requires forms for:

* pricing simulation;
* exchange-rate registration;
* settlement statement filters;
* possibly settlement creation.

Angular Reactive Forms provide:

* explicit form models;
* synchronous validation;
* custom validators;
* predictable state;
* testability;
* clear integration with API calls.

### 4. Angular Material accelerates a professional UI

The frontend does not need a custom design system.

Angular Material is sufficient for:

* form fields;
* buttons;
* selects;
* date pickers;
* tables;
* pagination;
* cards;
* dialogs;
* loading states;
* error display.

This allows the project to focus on domain correctness and full-stack integration rather than custom UI engineering.

### 5. Signals and services are enough for this scope

The initial application scope is limited.

Expected state includes:

* form state;
* loading state;
* simulation result;
* statement filters;
* paginated statement response;
* API error state.

Angular Signals and feature services are sufficient for this state model.

NgRx would add unnecessary boilerplate and architectural weight for this challenge.

## Frontend Responsibilities

The frontend is responsible for:

* rendering the operator interface;
* collecting user inputs;
* performing usability-level validation;
* calling backend APIs;
* displaying backend calculation results;
* displaying settlement statement data;
* managing loading, empty and error states;
* formatting monetary and date values for display;
* triggering server-side pagination and filtering.

The frontend is not responsible for official financial calculation.

## Frontend Non-Responsibilities

The frontend must not:

* implement the official present value formula;
* calculate official settlement amounts;
* resolve pricing strategies;
* hardcode exchange rates;
* apply business-critical currency conversion;
* treat frontend validation as sufficient;
* paginate settlement history locally after loading all records.

The backend remains the source of truth for financial calculations.

## Architecture

The frontend will use a feature-oriented structure.

Recommended structure:

```text id="g23d9r"
frontend/
└── src/
    └── app/
        ├── core/
        │   ├── api/
        │   ├── config/
        │   ├── interceptors/
        │   └── layout/
        ├── shared/
        │   ├── components/
        │   ├── pipes/
        │   ├── directives/
        │   └── utils/
        ├── features/
        │   ├── pricing-simulation/
        │   │   ├── pages/
        │   │   ├── components/
        │   │   ├── services/
        │   │   └── models/
        │   ├── settlements/
        │   │   ├── pages/
        │   │   ├── components/
        │   │   ├── services/
        │   │   └── models/
        │   └── exchange-rates/
        │       ├── pages/
        │       ├── components/
        │       ├── services/
        │       └── models/
        ├── app.config.ts
        ├── app.routes.ts
        └── app.component.ts
```

## Feature Modules

### Pricing Simulation

Purpose:

* collect receivable pricing inputs;
* call backend simulation endpoint;
* display calculated values.

Expected UI elements:

* face value input;
* source currency select;
* payment currency select;
* receivable type select;
* base rate input;
* due date picker;
* simulation result panel;
* validation and error messages.

Expected backend endpoint:

```text id="ywlr4n"
POST /api/pricing/simulations
```

Rules:

* calculation must be performed by backend;
* frontend displays returned values;
* frontend may debounce simulation calls or use an explicit simulate button.

### Settlements

Purpose:

* display historical settlement data;
* support filters and server-side pagination;
* optionally display settlement details.

Expected UI elements:

* settlement grid;
* date range filters;
* assignor filter;
* currency filter;
* receivable type filter;
* paginator;
* empty state;
* loading state;
* error state.

Expected backend endpoint:

```text id="wvza0h"
GET /api/settlements/statement
```

Rules:

* pagination must be server-side;
* filters must be sent to the backend;
* frontend must not load all records and filter locally.

### Exchange Rates

Purpose:

* create manual exchange rates;
* optionally display latest exchange rate for a pair.

Expected UI elements:

* source currency select;
* target currency select;
* rate input;
* validity timestamp input;
* submit button;
* latest rate display.

Expected backend endpoints:

```text id="yp50io"
POST /api/exchange-rates
GET /api/exchange-rates/latest
```

Rules:

* exchange rates must be validated by backend;
* frontend validation improves usability only;
* backend remains authoritative.

## State Management

### Decision

Use:

```text id="8j9jfi"
Angular Signals + services
```

### Expected State Types

Examples:

```text id="7v0g31"
loading
error
simulationResult
statementFilters
statementPage
selectedSettlement
latestExchangeRate
```

### Service Responsibilities

Services should:

* call backend APIs;
* expose typed methods;
* map API errors where useful;
* provide feature-level state when appropriate.

Services should not:

* implement official pricing formula;
* contain hidden business rules;
* hardcode financial values;
* replace backend validation.

### Why Not NgRx Initially

NgRx was rejected for the initial delivery because:

* state requirements are modest;
* most data is server-owned;
* feature services are sufficient;
* NgRx would increase boilerplate;
* the challenge has higher-priority backend/domain concerns;
* overengineering could reduce delivery quality.

NgRx may be revisited if the frontend grows into a larger application with complex shared state, offline workflows or advanced event coordination.

## API Integration Rules

All backend communication must go through Angular services.

Recommended service names:

```text id="abkpmf"
PricingSimulationApiService
ExchangeRateApiService
SettlementApiService
SettlementStatementApiService
```

Recommended model names:

```text id="d39czu"
PricingSimulationRequest
PricingSimulationResponse
CreateExchangeRateRequest
ExchangeRateResponse
SettlementStatementFilters
SettlementStatementResponse
PageResponse<T>
ApiErrorResponse
```

Frontend API models should match:

```text id="6hdqsc"
docs/specs/04-api-contract.md
```

If the backend API changes, frontend models and API contract documentation must be updated together.

## Form Validation Rules

The frontend must provide user-friendly validation, but backend validation remains mandatory.

Expected frontend validations:

### Pricing Simulation Form

Validate:

* face value required;
* face value greater than zero;
* source currency required;
* payment currency required;
* receivable type required;
* base rate optional;
* base rate greater than or equal to zero when provided;
* due date required;
* due date in the future.

### Exchange Rate Form

Validate:

* source currency required;
* target currency required;
* source and target currencies different;
* rate required;
* rate greater than zero;
* validity timestamp required.

### Statement Filter Form

Validate:

* valid date range;
* page size within allowed range;
* supported currency values;
* supported receivable type values.

Frontend validation must not replace backend validation.

## Error Handling

The frontend must handle structured backend errors.

Expected backend error model:

```text id="2qnr7g"
timestamp
status
error
message
path
details
```

Frontend behavior:

* display validation errors near fields when possible;
* display business errors in visible alert areas;
* display generic errors for unexpected failures;
* preserve enough information for the operator to understand what failed;
* avoid exposing raw stack traces.

## Monetary Display Rules

The frontend must display monetary values with currency context.

Rules:

* always show currency code or symbol;
* do not hide source currency in cross-currency scenarios;
* do not hide payment currency in cross-currency scenarios;
* format values consistently;
* avoid changing numeric values beyond display formatting.

If backend supports `DEFAULT_BASE_RATE`, the frontend may omit `baseRate` from the request instead of inventing a client-side fallback.

Examples:

```text id="jqnfxm"
BRL 10,000.00
USD 1,850.45
```

For audit-style views, prefer explicit currency codes over only symbols.

## Date and Time Display Rules

Rules:

* due date is date-only;
* calculation timestamp and settlement timestamp are date-time values;
* backend should persist timestamps in UTC;
* frontend may display timestamps in local browser timezone if clearly formatted;
* API contracts should use ISO 8601.

## Server-Side Pagination Rules

Settlement grids must use server-side pagination.

Expected query parameters:

```text id="59i9rd"
page
size
sort
from
to
assignorId
paymentCurrency
receivableType
```

Expected response metadata:

```text id="bdmeut"
content
page
size
totalElements
totalPages
```

Rules:

* page change triggers backend request;
* filter change triggers backend request;
* size change triggers backend request;
* frontend must not fetch all rows and paginate in memory.

## Styling Decision

Use SCSS with Angular component styles.

Rules:

* prefer component-scoped styles;
* use Angular Material theme configuration;
* avoid global CSS except layout, variables and resets;
* keep visual design clean and functional.

The UI should prioritize clarity over visual complexity.

## Accessibility Considerations

The frontend should use accessible Angular Material components where possible.

Expected practices:

* form fields with labels;
* visible validation messages;
* keyboard-accessible controls;
* clear button text;
* sufficient contrast;
* semantic table usage;
* no critical information conveyed only by color.

This is not a full accessibility certification scope, but the UI should not ignore basic accessibility.

## Testing Strategy

Frontend tests are useful but secondary to backend financial tests.

Minimum useful frontend tests:

* simulation form validation;
* API service request construction;
* settlement statement filters;
* server-side pagination behavior.

Testing tools may follow Angular CLI defaults unless changed during implementation.

Recommended test focus:

```text id="mgltbw"
1. Forms produce the expected API payload.
2. API services call expected endpoints.
3. Statement grid requests data when filters or page change.
4. Error states are displayed.
```

## Build and Runtime

### Local Development

Expected command:

```bash id="n0vovu"
cd frontend
npm install
npm start
```

or:

```bash id="akdykn"
ng serve
```

Expected local URL:

```text id="lpcx45"
http://localhost:4200
```

### Docker Runtime

The production build should be served by Nginx.

Expected flow:

```text id="ulb243"
1. Build Angular app.
2. Copy static files to Nginx image.
3. Expose frontend through port 80 inside container.
4. Map container port to FRONTEND_PORT in Docker Compose.
```

Docker Compose local URL:

```text id="s2gvzp"
http://localhost:4200
```

## Environment Configuration

The frontend must support API base URL configuration.

Expected environment variable:

```text id="o2vnfk"
ANGULAR_API_BASE_URL
```

For local browser usage:

```text id="rd7p4v"
http://localhost:8080/api
```

The exact runtime injection approach may be finalized during implementation.

Acceptable approaches:

* build-time environment replacement;
* runtime config JSON loaded on app startup;
* Docker build argument for local challenge delivery.

The selected approach must be documented in the final README.

## Alternatives Considered

### React

React was considered.

Advantages:

* widely adopted;
* flexible ecosystem;
* strong TypeScript support;
* good developer experience.

Rejected for this project because:

* Angular provides more built-in structure;
* Angular aligns well with enterprise application patterns;
* Angular Reactive Forms are suitable for operator workflows;
* Angular reduces architectural decision overhead for this challenge.

React would also be a valid choice, but Angular was selected for stronger conventions.

### Vue

Vue was considered.

Advantages:

* approachable;
* productive;
* good ecosystem;
* simpler learning curve.

Rejected because:

* Angular provides stricter structure and stronger enterprise conventions;
* the selected project direction favors Angular's built-in form, HTTP, routing and dependency injection model.

### Svelte

Svelte was considered.

Advantages:

* lightweight;
* excellent developer experience;
* less boilerplate.

Rejected because:

* less conventional for enterprise financial applications;
* smaller enterprise ecosystem compared with Angular;
* the challenge benefits from a more conventional SPA framework.

### NgRx

NgRx was considered as a state management option.

Rejected for initial delivery because:

* state scope is small;
* server state is simple;
* Angular Signals and services are sufficient;
* NgRx would introduce unnecessary ceremony.

NgRx may be reconsidered in a larger future version.

### Custom UI Without Component Library

Rejected because:

* it would increase UI delivery time;
* it would distract from business logic and backend quality;
* Angular Material is sufficient for a clean operations interface.

## Consequences

### Positive Consequences

The selected frontend stack provides:

* strong typing;
* mature SPA framework;
* clear project structure;
* excellent form support;
* standard HTTP integration;
* professional UI components;
* maintainable feature organization;
* good fit with enterprise evaluation expectations.

### Negative Consequences

The selected stack introduces:

* more boilerplate than lightweight frameworks;
* Angular-specific conventions to follow;
* need to manage Material imports and theme configuration;
* possible complexity around runtime environment configuration;
* need for discipline to avoid overengineering.

### Mitigations

Mitigations:

* use standalone components and feature folders;
* avoid NgRx initially;
* keep UI simple;
* use Angular Material components;
* keep financial logic in backend;
* keep API services typed and focused;
* document environment configuration;
* prioritize business-critical backend tests over extensive UI test coverage.

## Security Considerations

The frontend must not store secrets.

Rules:

* no API secrets in Angular environment files;
* no database credentials in frontend;
* no sensitive configuration committed;
* backend validation is mandatory;
* frontend must not be trusted as a security boundary.

## Performance Considerations

The frontend must avoid inefficient data access.

Rules:

* settlement grid must use server-side pagination;
* filters must be sent to the backend;
* avoid loading large datasets into browser memory;
* use loading indicators for API requests;
* debounce simulation requests if real-time simulation is implemented.

## AI-Assisted Development Considerations

AI tools may generate Angular code during implementation.

AI-generated frontend code must be reviewed for:

* duplicated financial calculation logic;
* hardcoded exchange rates;
* local-only pagination;
* untyped API responses;
* missing error handling;
* oversized components;
* unnecessary NgRx introduction;
* inconsistent API models.

Codex prompts for frontend work must reference:

```text id="9m2lp0"
AGENTS.md
docs/specs/04-api-contract.md
docs/specs/06-architecture.md
docs/specs/08-acceptance-criteria.md
docs/adr/ADR-005-frontend-stack.md
```

## Decision Validation

This decision is valid if the final frontend demonstrates:

* Angular 22 application scaffold;
* TypeScript API models;
* Angular Reactive Forms;
* Angular Material UI components;
* pricing simulation screen calling backend API;
* settlement statement grid using server-side pagination;
* clear loading, empty and error states;
* no official financial formula duplicated in frontend;
* Docker-compatible production build.

## Related Documents

```text id="q3wrmo"
README.md
AGENTS.md
.env.example
docker-compose.yml
docs/specs/01-product-brief.md
docs/specs/02-domain-glossary.md
docs/specs/03-business-rules.md
docs/specs/04-api-contract.md
docs/specs/06-architecture.md
docs/specs/07-testing-strategy.md
docs/specs/08-acceptance-criteria.md
docs/adr/ADR-004-architecture-style.md
docs/prompts/08-frontend-scaffold.md
docs/prompts/09-frontend-simulation.md
docs/prompts/10-frontend-statement-grid.md
docs/prompts/11-frontend-exchange-rates.md
```

## Review Notes

This ADR may be revisited if:

* Angular version compatibility changes;
* implementation constraints require a different frontend setup;
* the project scope expands significantly;
* state management becomes more complex;
* frontend testing strategy changes;
* runtime configuration approach changes.

Any frontend stack change must update:

* this ADR;
* `README.md`;
* `AGENTS.md`;
* `.env.example`;
* `docker-compose.yml`;
* `docs/specs/06-architecture.md`;
* relevant frontend prompts.
