# 09 — Git Workflow

## 1. Purpose

This document defines the Git workflow for the **SRM Credit Engine** project.

The goal is to demonstrate a professional development process, even though the project is implemented individually.

The Git workflow must show:

* organized planning;
* feature isolation;
* readable commit history;
* traceability between requirements and implementation;
* Conventional Commits;
* simulated Pull Requests;
* final release discipline;
* controlled use of AI-assisted development.

Git history is part of the technical evaluation.

## 2. Workflow Summary

The project uses a simplified Git Flow model:

```text
main
└── develop
    ├── feature/*
    ├── docs/*
    ├── fix/*
    └── chore/*
```

Branch responsibilities:

| Branch      | Purpose                        |
| ----------- | ------------------------------ |
| `main`      | Stable release branch          |
| `develop`   | Integration branch             |
| `feature/*` | Isolated feature work          |
| `docs/*`    | Documentation-specific changes |
| `fix/*`     | Bug fixes                      |
| `chore/*`   | Tooling and maintenance        |

The final delivery will be merged from `develop` into `main` and tagged as:

```text
v1.0.0
```

## 3. Branches

## 3.1 `main`

### Purpose

`main` represents the stable delivery branch.

### Rules

* Do not implement directly on `main`.
* Keep `main` stable.
* Merge `develop` into `main` only for final delivery.
* Create the final release tag from `main`.

### Expected final state

At the end of the challenge, `main` must contain:

* backend implementation;
* frontend implementation;
* Docker Compose setup;
* Flyway migrations;
* specs;
* ADRs;
* diagrams;
* prompts;
* README;
* AI usage documentation;
* final delivery tag.

## 3.2 `develop`

### Purpose

`develop` is the integration branch.

### Rules

* Feature branches must be created from `develop`.
* Feature branches must merge back into `develop`.
* `develop` receives the initial specification baseline.
* After the initial specification commit, avoid direct implementation commits on `develop`.
* Use Pull Requests, even if simulated, to merge feature work.

### Current initial context

The repository started with an almost empty `main`, containing only a minimal `README.md` and `.gitignore`.

A `develop` branch was created from `main`.

The initial specification phase is being consolidated directly in `develop`.

## 3.3 `feature/*`

### Purpose

Feature branches isolate implementation work.

### Pattern

```text
feature/<feature-name>
```

### Examples

```text
feature/backend-scaffold
feature/database-migrations
feature/currency-engine
feature/pricing-engine
feature/settlement-flow
feature/settlement-detail
feature/statement-query
feature/frontend-scaffold
feature/frontend-simulation
feature/frontend-statement-grid
feature/frontend-exchange-rates
```

### Rules

* Create from `develop`.
* Keep scope narrow.
* Include tests when business logic is added or changed.
* Update documentation when behavior changes.
* Open a simulated Pull Request into `develop`.
* Avoid unrelated changes.

## 3.4 `docs/*`

### Purpose

Documentation branches isolate documentation-only work.

### Pattern

```text
docs/<topic>
```

### Examples

```text
docs/final-delivery
docs/update-api-contract
docs/review-diagrams
docs/update-ai-usage
```

### Rules

* Use for meaningful documentation work.
* Documentation changes related to a feature may be included in the feature branch.
* Final README and delivery checklist review should use a dedicated docs branch.

## 3.5 `fix/*`

### Purpose

Fix branches isolate bug fixes.

### Pattern

```text
fix/<bug-description>
```

### Examples

```text
fix/cross-currency-rounding
fix/duplicate-settlement-conflict
fix/statement-date-filter
```

### Rules

* Create from `develop` before final release.
* Include regression tests when applicable.
* Keep the fix narrow.
* Update docs if behavior changes.

## 3.6 `chore/*`

### Purpose

Chore branches isolate tooling, configuration or maintenance changes.

### Pattern

```text
chore/<task-description>
```

### Examples

```text
chore/configure-ci
chore/update-docker-compose
chore/configure-formatting
```

### Rules

* Do not mix chore changes with domain feature changes.
* Keep tooling changes reviewable.
* Document commands or setup changes in README when relevant.

## 4. Initial Specification Baseline

The first meaningful commit on `develop` will establish the project baseline.

It includes:

* root configuration files;
* `README.md`;
* `AGENTS.md`;
* `AI_USAGE.md`;
* `.editorconfig`;
* `.gitignore`;
* `.env.example`;
* `docker-compose.yml`;
* specifications under `docs/specs/`;
* ADRs under `docs/adr/`;
* diagrams under `docs/diagrams/`;
* prompts under `docs/prompts/`.

Recommended commit message:

```text
docs: define project specifications and architecture decisions
```

This commit is intentionally larger than a normal feature commit because it creates the initial specification foundation.

After this baseline commit, work must be divided into smaller feature branches and commits.

## 5. Conventional Commits

All commits after the initial specification baseline must follow Conventional Commits.

## 5.1 Allowed Types

```text
feat
fix
test
docs
refactor
chore
build
ci
style
perf
```

## 5.2 Commit Format

```text
<type>: <short imperative description>
```

Examples:

```text
feat: implement pricing strategy engine
fix: correct cross-currency conversion order
test: cover settlement rollback behavior
docs: update API contract for settlement response
refactor: extract financial math component
chore: configure editor settings
build: add backend Maven dependencies
ci: add backend test workflow
```

## 5.3 Type Usage

### `feat`

Use for new functionality.

Examples:

```text
feat: add exchange rate management
feat: add pricing simulation endpoint
feat: implement atomic settlement flow
feat: add settlement statement query
feat: add pricing simulation screen
```

### `fix`

Use for bug fixes.

Examples:

```text
fix: prevent duplicate settlement persistence
fix: correct missing exchange rate status code
fix: handle invalid statement date range
```

### `test`

Use for test-only changes.

Examples:

```text
test: cover pricing strategy resolution
test: add settlement rollback integration test
test: cover statement query pagination
```

### `docs`

Use for documentation-only changes.

Examples:

```text
docs: update business rules
docs: finalize setup instructions
docs: document AI-assisted development
```

### `refactor`

Use for behavior-preserving code changes.

Examples:

```text
refactor: isolate pricing domain service
refactor: simplify statement query mapper
```

### `chore`

Use for maintenance or repository housekeeping.

Examples:

```text
chore: update gitignore
chore: configure editor settings
```

### `build`

Use for dependency or build configuration changes.

Examples:

```text
build: configure Spring Boot project
build: add Angular Material dependencies
```

### `ci`

Use for CI/CD changes.

Examples:

```text
ci: add backend test workflow
ci: validate frontend build on pull request
```

### `style`

Use for formatting-only changes.

Examples:

```text
style: format backend source files
```

### `perf`

Use for performance improvements.

Examples:

```text
perf: optimize settlement statement query
```

## 6. Commit Quality Rules

Commits must be:

* atomic;
* focused;
* readable;
* logically ordered;
* traceable to a feature, fix or documentation update.

Avoid vague messages:

```text
finalizado
ajustes
teste
wip
arrumando
commit final
subindo projeto
```

Avoid mixed-scope commits:

```text
feat: add pricing engine, frontend grid, Docker and README
```

Prefer splitting into smaller commits:

```text
feat: implement pricing strategy engine
test: cover pricing calculation rules
docs: update pricing API contract
```

## 7. Pull Request Simulation

Even though this is an individual project, feature branches must be integrated through simulated Pull Requests.

The purpose is to demonstrate:

* review discipline;
* clear work summaries;
* traceability;
* risk communication;
* test evidence;
* AI usage transparency.

## 7.1 PR Title Format

Recommended format:

```text
<type>: <short summary>
```

Examples:

```text
feat: implement pricing strategy engine
feat: add settlement statement grid
docs: finalize delivery documentation
```

## 7.2 PR Description Template

Use this template for feature Pull Requests:

````md
## Summary

Describe what this PR implements.

## Specs Covered

- docs/specs/<file>.md
- docs/adr/<file>.md

## Changes

- Change 1
- Change 2
- Change 3

## Tests

- [ ] Unit tests added/updated
- [ ] Integration tests added/updated
- [ ] API tests added/updated
- [ ] Frontend tests added/updated
- [ ] Manual validation performed

Commands executed:

```bash
<commands>
````

## Screenshots or Evidence

Add screenshots for frontend changes or Swagger/API evidence for backend changes.

## Risks and Trade-offs

Describe known risks, assumptions or follow-ups.

## AI Usage

AI was used for:

* <planning | scaffolding | implementation | testing | review | documentation | not applicable>

AI_USAGE.md:

* [ ] Updated
* [ ] Not applicable

````

## 7.3 PR Review Checklist

Before merging into `develop`, verify:

```text
- The PR scope is focused.
- Relevant specs were followed.
- Relevant ADRs were followed.
- Business rules were not bypassed.
- Financial calculations do not use floating-point types.
- Backend validation exists for new API inputs.
- Tests were added or updated when needed.
- Tests pass locally.
- Documentation was updated when behavior changed.
- AI_USAGE.md was updated when AI materially contributed.
- No secrets were committed.
- No unrelated files were changed.
````

## 8. Merge Strategy

Preferred strategy:

```text
Squash and merge
```

Rationale:

* keeps `develop` history readable;
* groups each feature into one coherent commit;
* reduces noisy WIP commits;
* makes final review easier.

Alternative acceptable strategy:

```text
Rebase and merge
```

Use only when branch commits are already clean and meaningful.

Avoid unnecessary merge commits unless intentionally preserving branch history for clarity.

## 9. Recommended Implementation Plan

After the initial specification baseline, use the following branch order.

## 9.1 Backend Scaffold

Branch:

```text
feature/backend-scaffold
```

Expected work:

* Spring Boot project scaffold;
* Maven configuration;
* package structure;
* application config;
* OpenAPI setup;
* global exception handler skeleton;
* backend Dockerfile.

Expected commit:

```text
feat: add backend scaffold
```

## 9.2 Database Migrations

Branch:

```text
feature/database-migrations
```

Expected work:

* Flyway configuration;
* initial schema migration;
* seed currencies;
* seed receivable types;
* indexes;
* constraints;
* JPA entity mappings if scoped here.

Expected commits:

```text
feat: add initial database migrations
test: validate database schema migrations
```

## 9.3 Currency Engine

Branch:

```text
feature/currency-engine
```

Expected work:

* create exchange-rate endpoint;
* latest exchange-rate endpoint;
* validation;
* exact direction lookup;
* persistence;
* tests.

Expected commits:

```text
feat: add exchange rate management
test: cover exchange rate validation
```

## 9.4 Pricing Engine

Branch:

```text
feature/pricing-engine
```

Expected work:

* value objects;
* `PricingStrategy`;
* strategy implementations;
* strategy resolver;
* pricing engine;
* financial math;
* simulation endpoint;
* tests.

Expected commits:

```text
feat: implement pricing strategy engine
test: cover pricing calculation rules
```

## 9.5 Settlement Flow

Branch:

```text
feature/settlement-flow
```

Expected work:

* settlement creation endpoint;
* transactional application service;
* assignor/receivable handling;
* settlement item snapshots;
* duplicate settlement prevention;
* rollback behavior;
* tests.

Expected commits:

```text
feat: implement atomic settlement flow
test: cover settlement rollback and duplicate prevention
```

## 9.6 Settlement Detail

Branch:

```text
feature/settlement-detail
```

Expected work:

* settlement detail endpoint;
* persisted snapshot response;
* audit-focused response mapping;
* tests proving no historical recalculation.

Expected commits:

```text
feat: add settlement detail query
test: cover settlement detail audit response
```

## 9.7 Statement Query

Branch:

```text
feature/statement-query
```

Expected work:

* settlement statement endpoint;
* filters;
* server-side pagination;
* database-level query;
* deterministic sorting;
* tests.

Expected commits:

```text
feat: add settlement statement query
test: cover statement query filters
```

## 9.8 Frontend Scaffold

Branch:

```text
feature/frontend-scaffold
```

Expected work:

* Angular project scaffold;
* Angular Material;
* routing;
* layout;
* API base configuration;
* frontend Dockerfile.

Expected commit:

```text
feat: add angular frontend scaffold
```

## 9.9 Frontend Pricing Simulation

Branch:

```text
feature/frontend-simulation
```

Expected work:

* simulation form;
* backend API service;
* result panel;
* validation;
* loading/error states.

Expected commit:

```text
feat: add pricing simulation screen
```

## 9.10 Frontend Statement Grid

Branch:

```text
feature/frontend-statement-grid
```

Expected work:

* statement grid;
* filters;
* server-side pagination;
* backend API integration;
* empty/loading/error states.

Expected commit:

```text
feat: add settlement statement grid
```

## 9.11 Frontend Exchange Rates

Branch:

```text
feature/frontend-exchange-rates
```

Expected work:

* exchange-rate registration screen;
* reference data integration;
* success and error states;
* optional latest-rate lookup UI.

Expected commit:

```text
feat: add exchange rate screen
```

## 9.12 Final Documentation and Delivery

Branch:

```text
chore/docker-compose-final
```

or:

```text
docs/final-delivery
```

Expected work:

* final Docker Compose adjustments;
* final README;
* final AI_USAGE update;
* diagram review;
* delivery checklist;
* setup instructions;
* evidence for local execution.

Expected commits:

```text
chore: finalize local runtime setup
docs: finalize technical challenge delivery
```

## 10. Release Flow

## 10.1 Final Merge

Final delivery flow:

```bash
git checkout develop
git pull origin develop

# run final checks

git checkout main
git pull origin main
git merge --no-ff develop
```

If using platform Pull Requests, open a final PR:

```text
develop -> main
```

## 10.2 Final Tag

Create final tag from `main`:

```bash
git tag -a v1.0.0 -m "Release v1.0.0"
git push origin main
git push origin v1.0.0
```

## 10.3 GitHub/GitLab Release

If using GitHub or GitLab releases, create a release for:

```text
v1.0.0
```

Release notes should include:

* project summary;
* implemented features;
* setup instructions;
* test commands;
* known limitations;
* AI usage reference.

## 11. Optional Pre-Release Tags

Optional tags may be used if helpful:

```text
v0.1.0-specs
v0.5.0-backend
v0.8.0-frontend
```

For this challenge, only `v1.0.0` is required.

Do not create unnecessary tags if they do not add clarity.

## 12. Hotfix / Crisis Simulation

A hotfix simulation is not required for Pleno-level delivery.

If time allows and the implementation is already complete, a small documented hotfix flow may be added as a senior-level differential.

Example branch:

```text
hotfix/cross-currency-rounding
```

Example flow:

```bash
git checkout main
git checkout -b hotfix/cross-currency-rounding

# apply fix and tests

git commit -m "fix: correct cross-currency rounding"

git checkout main
git merge --no-ff hotfix/cross-currency-rounding
git tag -a v1.0.1 -m "Release v1.0.1"
```

Do not prioritize this over core Pleno-level requirements.

## 13. Git Hooks

Git hooks are optional.

If implemented, they may run:

* formatting;
* backend tests;
* frontend tests;
* linting;
* commit message validation.

Possible tools:

```text
pre-commit
Husky
Maven test phase
npm scripts
```

Hooks are a senior-level differential and should not block core delivery.

## 14. CI/CD Relationship

The Git workflow should support a basic CI pipeline.

Recommended checks:

```text
backend: mvn test
frontend: npm run build
```

Optional checks:

```text
backend: mvn verify
frontend: npm test
docker compose config
```

If CI is implemented, use a branch/PR workflow that runs checks before merge.

CI configuration changes should use:

```text
ci: add backend and frontend validation workflow
```

## 15. AI Usage and Git

AI-assisted work must be transparent in Git workflow.

## 15.1 AI in PR Descriptions

If AI materially helped with a feature, the PR must mention it.

Example:

```md
## AI Usage

AI was used for:
- generating initial unit test cases;
- reviewing edge cases around cross-currency conversion.

Manual review:
- removed unsafe BigDecimal construction;
- verified conversion order;
- ran backend tests.

AI_USAGE.md:
- [x] Updated
```

## 15.2 AI_USAGE Updates

Update `AI_USAGE.md` when AI materially contributes to:

* architecture;
* specs;
* code generation;
* test generation;
* debugging;
* refactoring;
* final review;
* documentation.

## 15.3 AI Does Not Replace Authorship

The author must understand all committed changes.

Do not commit generated code that cannot be explained.

## 16. Documentation and Git

Documentation must evolve with the implementation.

Examples:

| Change                | Documentation to update                            |
| --------------------- | -------------------------------------------------- |
| API contract changes  | `docs/specs/04-api-contract.md`                    |
| Business rule changes | `docs/specs/03-business-rules.md`                  |
| Data model changes    | `docs/specs/05-data-model.md`, ER diagram          |
| Architecture changes  | `docs/specs/06-architecture.md`, ADRs, C4 diagrams |
| Test strategy changes | `docs/specs/07-testing-strategy.md`                |
| Setup changes         | `README.md`, `.env.example`, Docker docs           |
| AI-assisted work      | `AI_USAGE.md`                                      |

Documentation updates may be included in the same feature branch when directly related to the change.

## 17. Git History Quality Checklist

Before final delivery, verify:

```text
- Commit messages follow Conventional Commits.
- Feature work is separated by topic.
- No single implementation commit contains the entire project.
- History tells a coherent development story.
- PR descriptions explain what was done.
- Tests are mentioned in PR descriptions.
- AI usage is reflected in AI_USAGE.md.
- main contains the final stable version.
- final release tag exists.
```

## 18. Anti-Patterns

Avoid:

```text
- working directly on main;
- one giant final commit;
- vague commit messages;
- mixing unrelated changes;
- committing broken code intentionally;
- committing real secrets;
- committing generated build artifacts;
- leaving PR descriptions empty;
- hiding AI-generated work;
- rewriting public history after final delivery without reason.
```

## 19. Recommended Final History Shape

The final history should resemble:

```text
docs: define project specifications and architecture decisions
feat: add backend scaffold
feat: add initial database migrations
test: validate database schema migrations
feat: add exchange rate management
test: cover exchange rate validation
feat: implement pricing strategy engine
test: cover pricing calculation rules
feat: implement atomic settlement flow
test: cover settlement rollback and duplicate prevention
feat: add settlement detail query
test: cover settlement detail audit response
feat: add settlement statement query
test: cover statement query filters
feat: add angular frontend scaffold
feat: add pricing simulation screen
feat: add settlement statement grid
feat: add exchange rate screen
chore: finalize local runtime setup
docs: finalize technical challenge delivery
ci: add validation workflow
```

The exact history may differ, but it must remain readable and intentional.

## 20. Definition of Done for Git Workflow

The Git workflow is complete when:

```text
- develop contains completed implementation.
- main contains final stable delivery.
- final merge from develop to main is complete.
- tag v1.0.0 exists.
- commit messages follow Conventional Commits.
- feature work was organized through branches.
- Pull Requests or simulated Pull Requests exist.
- README documents the workflow.
- AI_USAGE.md reflects material AI use.
```

## 21. Related Documents

```text
README.md
AGENTS.md
AI_USAGE.md
docs/specs/08-acceptance-criteria.md
docs/specs/10-ai-workflow.md
docs/specs/11-delivery-checklist.md
docs/adr/ADR-006-git-workflow.md
docs/adr/ADR-007-ai-assisted-development.md
```

## 22. Change Policy

If the Git workflow changes, update:

```text
docs/specs/09-git-workflow.md
docs/adr/ADR-006-git-workflow.md
README.md
AGENTS.md
docs/prompts/*
docs/specs/11-delivery-checklist.md
```

Any workflow change must preserve the evaluation goals:

```text
traceability
readable history
professional process
controlled delivery
```
