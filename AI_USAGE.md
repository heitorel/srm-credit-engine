# AI_USAGE.md

## 1. Purpose

This document describes how Artificial Intelligence tools were used during the development of the **SRM Credit Engine** technical challenge.

The goal is to keep AI usage transparent, auditable and aligned with the project requirement that AI should be used as a co-pilot, not as a substitute for engineering ownership.

All AI-assisted outputs must be reviewed, validated and understood by the project author before being committed.

## 2. AI Usage Policy for This Project

AI tools may be used to support:

* interpretation of the technical challenge;
* identification of risks and hidden requirements;
* specification writing;
* architecture planning;
* development task breakdown;
* scaffolding suggestions;
* code generation for small scoped tasks;
* unit test generation;
* documentation review;
* refactoring suggestions;
* debugging support;
* final delivery review.

AI tools must not be used to bypass understanding of:

* financial calculation rules;
* monetary precision;
* transaction consistency;
* database constraints;
* security validation;
* concurrency handling;
* API behavior;
* architectural trade-offs.

The project author remains responsible for:

* final code correctness;
* final architecture decisions;
* security implications;
* test coverage;
* Git history;
* documentation accuracy;
* delivery quality.

## 3. AI Tools Used

| Tool    | Usage Status | Purpose                                                                                     |
| ------- | -----------: | ------------------------------------------------------------------------------------------- |
| ChatGPT |         Used | Challenge interpretation, risk analysis, specification structure and documentation drafting |
| Codex   |         Used | Scoped implementation, test generation, review and refactoring during feature development    |

## 4. Current Project Stage

The project started in the **Specification-Driven Development** phase and is now progressing through the first implementation increments.

The current AI usage includes:

* analyzing the challenge statement;
* identifying potential traps and evaluation risks;
* defining the repository documentation structure;
* drafting initial specification and governance files;
* planning the Codex workflow;
* generating the initial backend scaffold under controlled scope.

## 5. Usage Log

### 2026-07-07 - Challenge Risk Analysis

**Tool used:** ChatGPT

**Prompt summary:**

The technical challenge statement was provided and the AI was asked to identify possible traps, hidden requirements and implementation risks that could compromise the project.

**AI contribution:**

The AI helped identify the following risk areas:

* treating the project as a simple CRUD instead of a financial domain system;
* using floating-point types for monetary values;
* not documenting how the pricing term is calculated;
* applying currency conversion before present value calculation;
* ignoring batch settlement requirements;
* failing to provide ACID guarantees;
* implementing Strategy Pattern only superficially;
* hardcoding exchange rates;
* implementing analytical reports with inefficient ORM-only queries;
* duplicating official financial calculation logic in the frontend;
* not documenting AI usage;
* having poor Git history;
* omitting required diagrams, DDL or acceptance criteria.

**Author review:**

The findings were reviewed and accepted as relevant to the project.

**Decisions derived from this interaction:**

* Monetary values, rates and financial calculations will use decimal-safe types.
* The backend will be the official calculation source.
* The exchange rate used in settlement will be persisted as an audit snapshot.
* Settlement batches must be atomic.
* The project will follow Specification-Driven Development.
* AI usage will be documented continuously in this file.

**Corrections applied:**

No code was generated in this step.

**Known limitations:**

The analysis was based on the challenge statement and engineering interpretation. Final correctness will depend on implementation, tests and manual review.

---

### 2026-07-08 - Backend Scaffold

**Tool used:** Codex

**Prompt summary:**

Execute `docs/prompts/01-backend-scaffold.md` after reading the required specs, ADRs, `README.md`, `docker-compose.yml` and `.env.example`, creating only the initial Spring Boot backend scaffold under `backend/`.

**AI contribution:**

The AI generated and adjusted:

* the Maven Spring Boot 4.1.0 project scaffold under `backend/`;
* the Maven Wrapper files so the backend can be built without a host Maven installation;
* the layered package structure aligned with the architecture spec;
* `application.yml` and `application-local.yml` with environment-driven datasource, Flyway, JPA, Springdoc and CORS settings;
* a global exception handling skeleton with structured API error responses;
* placeholder domain business exception types;
* infrastructure configuration for UTC clock, CORS and OpenAPI;
* a multi-stage backend `Dockerfile`;
* a context load test and a focused exception handler test.

**Author review:**

The generated scaffold was reviewed against:

* `AGENTS.md`;
* `docs/specs/03-business-rules.md`;
* `docs/specs/04-api-contract.md`;
* `docs/specs/05-data-model.md`;
* `docs/specs/06-architecture.md`;
* `docs/specs/07-testing-strategy.md`;
* `docs/adr/ADR-001-backend-stack.md`;
* `docs/adr/ADR-002-database-choice.md`;
* `docs/adr/ADR-003-money-precision.md`;
* `docs/adr/ADR-004-architecture-style.md`.

The dependency set, package organization and configuration properties were also checked against current official Spring Boot, Springdoc and Flyway compatibility documentation before finalizing the scaffold.

**Accepted changes:**

* Spring Boot 4.1.0 Maven project scaffold limited to `backend/`;
* official package base `com.srm.creditengine`;
* no business endpoints, pricing logic, JPA entities or Flyway schema migrations;
* no floating-point financial code introduced;
* Dockerfile and Maven Wrapper added for local and container execution.

**Rejected or corrected AI output:**

* removed unnecessary generated files from the default Spring Initializr template;
* corrected test-time auto-configuration exclusions to Spring Boot 4 package names so the context test does not require a live MySQL database;
* removed optional Testcontainers dependencies from this increment after dependency resolution failed and because no integration test in this scaffold required them;
* avoided introducing any pricing, settlement or exchange-rate feature implementation ahead of scope.

**Tests or validation performed:**

* `cd backend`
* `.\mvnw.cmd test`
* verified `BUILD SUCCESS`
* verified the scaffold keeps `BigDecimal`-safe boundaries by not introducing financial calculation code at this stage

**Known limitations:**

* no Flyway schema migrations were created yet by design;
* no business endpoints were implemented yet by design;
* `mvn` is not installed globally in the current machine context, so validation was executed with the Maven Wrapper (`mvnw.cmd`) instead.

---

### 2026-07-07 - Specification-Driven Development Planning

**Tool used:** ChatGPT

**Prompt summary:**

The AI was asked how to organize the project for AI-assisted development using Codex and Specification-Driven Development.

**AI contribution:**

The AI proposed an initial documentation and repository structure:

* `AGENTS.md`;
* `AI_USAGE.md`;
* `README.md`;
* `docs/specs/`;
* `docs/adr/`;
* `docs/diagrams/`;
* `docs/prompts/`;
* `backend/`;
* `frontend/`.

The AI also suggested a development flow where Codex should work from scoped prompts referencing specific specs and ADRs.

**Author review:**

The proposed structure was reviewed and accepted with adjustments.

**Decisions derived from this interaction:**

* The project will use explicit specs before implementation.
* Codex tasks will be small and scoped.
* Prompts will point to specific files.
* Each feature will be implemented from a feature branch created from `develop`.
* Features will be integrated into `develop` through simulated Pull Requests.
* Final delivery will merge `develop` into `main` and create a release tag.

**Corrections applied:**

The initial ADR list was expanded to include frontend stack and AI-assisted development decisions.

**Known limitations:**

The structure is a planning artifact. Its effectiveness depends on keeping specs updated as implementation evolves.

---

### 2026-07-07 - Initial README Draft

**Tool used:** ChatGPT

**Prompt summary:**

The AI was asked to draft the initial `README.md` for the project based on the selected stack and delivery workflow.

**AI contribution:**

The AI drafted a README covering:

* project overview;
* challenge context;
* selected backend and frontend stack;
* business assumptions;
* planned architecture;
* repository organization;
* documentation layout;
* Git workflow;
* implementation strategy;
* AI usage policy;
* planned final delivery.

**Author review:**

The document will be reviewed before commit to ensure it reflects the final intended project direction.

**Corrections applied:**

Pending manual review.

**Known limitations:**

The initial README describes the planned architecture and delivery flow. Some execution instructions will only become final after backend, frontend and Docker Compose are implemented.

---

### 2026-07-07 - AGENTS.md Draft

**Tool used:** ChatGPT

**Prompt summary:**

The AI was asked to draft `AGENTS.md` to guide future AI agents and Codex sessions during implementation.

**AI contribution:**

The AI drafted instructions covering:

* project context;
* backend architecture rules;
* frontend architecture rules;
* financial calculation constraints;
* database and migration rules;
* API rules;
* error handling;
* testing expectations;
* Docker expectations;
* Git rules;
* AI-assisted development rules;
* task completion checklist.

**Author review:**

The document will be reviewed before commit to ensure the rules are strict enough to prevent unsafe generated code.

**Corrections applied:**

Pending manual review.

**Known limitations:**

The file defines expected behavior for AI-assisted development, but it does not guarantee correctness. Every generated change must still be reviewed manually.

---

### 2026-07-07 - AI_USAGE.md Draft

### 2026-07-07 - Documentation Consistency Review and Correction

**Tool used:** Codex

**Prompt summary:**

The AI was asked to review the repository documentation and specs for inconsistencies, then update the affected documents to align business rules, API contract, glossary and architecture rules.

**AI contribution:**

The AI identified and corrected documentation divergences involving:

* the one-source-currency-per-settlement-batch rule;
* the missing settlement header `sourceCurrency` field in the API contract;
* glossary naming drift between `source_currency_code` and `currency_code`;
* explicit treatment for missing exchange rates in lookup versus dependent business operations;
* excessive architectural flexibility that allowed bypassing the application boundary for reporting;
* undocumented server-side fallback behavior for `baseRate`.

**Author review:**

The proposed changes were reviewed for consistency across business rules, API contract, glossary, architecture and testing strategy before acceptance.

**Accepted changes:**

* Added the batch-level source currency rule to the business rules.
* Updated the API contract for settlement responses and base-rate fallback behavior.
* Adjusted glossary terminology and naming guidance.
* Tightened the reporting architecture rule to keep the application boundary mandatory.
* Extended testing guidance to cover server-side base-rate fallback scenarios.

**Rejected or corrected AI output:**

* Avoided leaving the base-rate fallback only in architecture/configuration docs without reflecting it in business and API specs.
* Avoided keeping the reporting exception broad enough to permit `api -> infrastructure` bypass.

**Tests or validation performed:**

* Cross-read `docs/specs/02-domain-glossary.md`, `03-business-rules.md`, `04-api-contract.md`, `06-architecture.md`, `07-testing-strategy.md` and `docs/adr/ADR-004-architecture-style.md`.
* Reviewed the resulting diff for internal consistency.

**Known limitations:**

The review focused on the documentation set and not on implementation code, so code-level behavior still needs to follow the updated specs during development.

---

### 2026-07-07 - SDD Roadmap and Prompt Restructuring

**Tool used:** Codex

**Category:** Documentation

**Prompt summary:**

The AI was asked to inspect the current specs and prompts, identify workflow misalignments and turn the proposed SDD implementation sequence into concrete repository documentation.

**AI contribution:**

The AI restructured the documented prompt inventory and implementation roadmap to match the actual dependency chain:

* backend scaffold;
* database migrations;
* currency engine;
* pricing engine;
* settlement flow;
* settlement detail;
* statement query;
* frontend scaffold;
* frontend simulation;
* frontend statement grid;
* frontend exchange-rate screen;
* Docker and delivery finalization;
* final review.

It also updated references across README, Git workflow, AI workflow, delivery checklist and ADRs to point to the new prompt set.

**Author review:**

The updated documentation should be reviewed for naming consistency and realistic feature scope before code implementation begins.

**Accepted changes:**

* Added a dedicated database-migrations prompt.
* Moved currency ahead of pricing in the documented prompt order.
* Split frontend work into scaffold, simulation, statement grid and exchange-rate prompts.
* Split settlement detail from the settlement write-flow prompt.
* Added a dedicated Docker and delivery prompt.
* Updated workflow documents to reference the new prompt inventory.

**Rejected or corrected AI output:**

* Avoided preserving pricing before the currency engine.
* Avoided keeping a single broad frontend prompt despite the narrower branch plan.
* Avoided leaving settlement detail coupled to the settlement write flow.

**Tests or validation performed:**

* Cross-read `README.md`, `docs/specs/09-git-workflow.md`, `docs/specs/10-ai-workflow.md`, `docs/specs/11-delivery-checklist.md`, `docs/adr/ADR-005-frontend-stack.md`, `docs/adr/ADR-006-git-workflow.md`, `docs/adr/ADR-007-ai-assisted-development.md` and `docs/prompts/*`.
* Verified the final prompt inventory under `docs/prompts/`.
* Searched the repository for stale references to the old prompt names.

**Known limitations:**

This work adjusts the development workflow and prompt structure only. Future implementation may still require prompt scope refinements if the codebase reveals new dependency constraints.

**Tool used:** ChatGPT

**Prompt summary:**

The AI was asked to draft the initial `AI_USAGE.md` for transparent reporting of AI usage.

**AI contribution:**

The AI created the initial structure for this document, including:

* purpose;
* AI usage policy;
* tools used;
* project stage;
* usage log;
* critical analysis;
* ongoing update rules.

**Author review:**

The document will be updated continuously as Codex and other AI tools are used during implementation.

**Corrections applied:**

Pending manual review.

**Known limitations:**

This document must be kept current. If it is not updated during implementation, it will no longer accurately represent the project history.

## 6. Planned Codex Usage

Codex is planned to be used during implementation with strict scope control.

Each Codex task should include:

* relevant specs to read;
* relevant ADRs to read;
* allowed files or directories;
* forbidden changes;
* expected tests;
* commands to run;
* expected output format.

Codex should not be asked to implement the entire project at once.

Preferred task pattern:

```text
Read:
- AGENTS.md
- docs/specs/<relevant-spec>.md
- docs/adr/<relevant-adr>.md

Task:
Implement only <specific feature>.

Scope:
You may change <allowed paths>.
Do not change <forbidden paths>.

Requirements:
- <requirement 1>
- <requirement 2>
- <requirement 3>

Tests:
Add or update tests for <cases>.
Run <commands>.

Constraints:
- Do not use floating-point types for financial calculations.
- Do not put business logic in controllers.
- Keep the diff small.
- Explain assumptions before implementing.

Expected output:
- summary of changes;
- tests executed;
- files changed;
- risks or follow-ups.
```

## 7. Known AI Risk Areas

The following areas require extra manual review because AI-generated code may be especially risky:

### 7.1 Financial Precision

AI may generate calculations using `double`, `float`, `Double`, `Float` or Java `Math.pow`.

This is not acceptable for financial calculations in this project.

Expected approach:

* use `BigDecimal`;
* define explicit scale;
* define explicit rounding;
* avoid uncontrolled binary floating-point operations;
* test boundary and rounding cases.

### 7.2 Currency Conversion

AI may apply currency conversion before discounting the receivable.

This is not acceptable for this project.

Expected order:

1. calculate present value in source currency;
2. apply currency conversion at the end;
3. persist the exchange rate snapshot used.

### 7.3 Transaction Atomicity

AI may generate persistence code that saves a settlement header before all items are validated.

This is risky.

Expected behavior:

* validate the full batch;
* execute settlement inside a transaction;
* rollback the entire operation if any item fails;
* prevent duplicate settlement of the same receivable.

### 7.4 Strategy Pattern

AI may replace Strategy Pattern with simple conditional logic.

This is not sufficient for this project.

Expected approach:

* explicit strategy interface;
* one implementation per receivable type or risk rule;
* resolver/factory to select the proper strategy;
* unit tests for strategy behavior.

### 7.5 Report Queries

AI may generate report endpoints using `findAll()` and in-memory filtering.

This is not acceptable for analytical routes.

Expected approach:

* database-side filtering;
* server-side pagination;
* indexes supporting period, assignor, currency and receivable type filters;
* projections or DTOs for report responses.

### 7.6 Frontend Calculation

AI may duplicate the backend financial formula in Angular.

This is risky because the frontend could diverge from the backend.

Expected approach:

* frontend requests official simulations from backend;
* frontend displays returned values;
* final settlement values are generated only by backend.

### 7.7 Overengineering

AI may suggest unnecessary distributed architecture, event streaming, Kubernetes or complex state management.

This project prioritizes a strong mid-level delivery.

Expected approach:

* modular monolith;
* clean layered architecture;
* Docker Compose;
* focused tests;
* clear documentation;
* optional senior-level documentation only when it does not compromise delivery quality.

## 8. AI Review Checklist

Before accepting AI-generated output, verify:

* the change matches the requested scope;
* relevant specs were followed;
* relevant ADRs were followed;
* no financial calculation uses floating-point types;
* no business logic was placed in controllers;
* no official financial calculation was moved to frontend;
* no exchange rate was hardcoded in pricing logic;
* settlement logic remains atomic;
* tests were added or updated;
* relevant tests pass;
* error handling remains structured;
* documentation was updated when needed;
* no secrets were introduced;
* no unrelated files were modified.

## 9. Prompt Log Template

Future AI interactions should be logged using this format:

```md
### YYYY-MM-DD - <Short description>

**Tool used:** <ChatGPT | Codex | Other>

**Prompt summary:**

<Describe the prompt without necessarily pasting the entire prompt if it is too long.>

**AI contribution:**

<List what the AI produced or suggested.>

**Author review:**

<Explain how the result was reviewed.>

**Accepted changes:**

<List accepted changes.>

**Rejected or corrected AI output:**

<List hallucinations, unsafe code, wrong assumptions or rejected suggestions.>

**Tests or validation performed:**

<List commands, tests or manual checks.>

**Known limitations:**

<List any remaining risk or follow-up.>
```

## 10. Critical Analysis

### 10.1 Where AI Helped

At the current stage, AI helped mainly with:

* identifying hidden risks in the challenge;
* organizing the repository documentation structure;
* defining an SDD workflow;
* drafting initial governance documents;
* proposing scope control rules for future Codex usage.

This reduced planning time and helped expose areas that require extra care, especially monetary precision, transaction atomicity, exchange-rate snapshotting and Git workflow.

### 10.2 Where AI Could Be Harmful

AI could harm the project if used without review in the following ways:

* generating financial code with unsafe numeric types;
* inventing business assumptions without documenting them;
* producing code that passes simple tests but fails edge cases;
* hiding complexity inside controllers or services;
* generating excessive architecture for the available delivery time;
* creating broad diffs that are hard to review;
* generating documentation that does not match the implemented behavior.

### 10.3 Current Mitigation Strategy

The project mitigates AI risks by:

* using Specification-Driven Development;
* maintaining `AGENTS.md`;
* requiring small scoped Codex tasks;
* documenting AI usage in this file;
* adding tests for business-critical logic;
* keeping architecture decisions in ADRs;
* reviewing all generated output before commit.

## 11. Final Ownership Statement

All AI-assisted outputs are treated as drafts.

The final responsibility for the delivered system belongs to the project author, including:

* business rule correctness;
* financial calculation precision;
* security and validation;
* transaction consistency;
* database model;
* frontend behavior;
* test coverage;
* documentation accuracy;
* Git history quality.

No AI-generated code, documentation or design decision should be accepted without review.
