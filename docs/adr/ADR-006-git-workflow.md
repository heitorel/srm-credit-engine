# ADR-006 — Git Workflow

## Status

Accepted

## Date

2026-07-07

## Context

The SRM Credit Engine is a technical challenge where Git usage is explicitly part of the evaluation.

The repository must demonstrate:

* organized development flow;
* readable history;
* atomic commits;
* Conventional Commits;
* feature isolation;
* simulated Pull Requests;
* final release tagging;
* traceability between requirements, implementation and tests.

Although the project is developed individually, the Git workflow must simulate professional team practices.

The initial repository state contains only an empty `README.md` and `.gitignore` in `main`.

A `develop` branch was created from `main` to consolidate specifications, architecture decisions, diagrams, prompts and configuration before implementation begins.

## Decision

The project will use a simplified Git Flow model:

```text id="r7r9xr"
main
└── develop
    ├── feature/*
    ├── docs/*
    ├── fix/*
    └── chore/*
```

Branch responsibilities:

| Branch      | Responsibility                        |
| ----------- | ------------------------------------- |
| `main`      | Stable release branch                 |
| `develop`   | Integration branch                    |
| `feature/*` | Isolated functional implementation    |
| `docs/*`    | Documentation-specific changes        |
| `fix/*`     | Bug fixes                             |
| `chore/*`   | Tooling, configuration or maintenance |

The final delivery will be created by merging `develop` into `main` and tagging the final version as:

```text id="d4j4cj"
v1.0.0
```

## Rationale

### 1. Git is part of the evaluation

The challenge evaluates whether the repository history tells a coherent story.

A single final commit with all changes would hide the engineering process and weaken the delivery.

This workflow demonstrates:

* planning;
* incremental implementation;
* review discipline;
* scope control;
* clear traceability.

### 2. `develop` is useful for this challenge

The project starts from a nearly empty `main`.

Using `develop` allows the work to be organized in phases:

```text id="oylbgi"
1. Specification phase
2. Backend implementation
3. Frontend implementation
4. Integration
5. Final documentation
6. Release
```

This keeps `main` stable until the final delivery is complete.

### 3. Feature branches simulate team workflow

Even though the project is individual, feature branches and Pull Requests help demonstrate how the author would work in a team environment.

Each feature branch should have a focused purpose and a small reviewable diff.

### 4. Conventional Commits improve readability

Conventional Commits make the history easier to scan and classify.

They also support future automation such as changelog generation and semantic versioning.

## Branch Strategy

## 1. Main Branch

Branch:

```text id="n0b3wo"
main
```

Purpose:

* stable branch;
* final evaluated version;
* release tag source.

Rules:

* do not develop directly on `main`;
* only merge stable `develop` into `main` for final delivery;
* create final release tag from `main`.

Expected final tag:

```text id="lif7l9"
v1.0.0
```

## 2. Develop Branch

Branch:

```text id="apq5yw"
develop
```

Purpose:

* integration branch;
* base branch for feature development;
* place where completed feature PRs are merged before final release.

Rules:

* initial specification and configuration phase is committed directly once to `develop`;
* after the initial specification commit, avoid direct implementation commits on `develop`;
* new implementation work must happen in feature branches;
* feature branches are merged into `develop` through simulated Pull Requests.

## 3. Feature Branches

Pattern:

```text id="ia26ke"
feature/<short-feature-name>
```

Examples:

```text id="mw2snw"
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

Rules:

* create from `develop`;
* keep scope narrow;
* include tests when business logic is changed;
* update documentation when behavior changes;
* open a simulated Pull Request into `develop`.

## 4. Documentation Branches

Pattern:

```text id="u5s2gn"
docs/<short-topic>
```

Examples:

```text id="xj4tv0"
docs/final-readme
docs/update-api-contract
docs/add-c4-diagrams
```

Rules:

* use when documentation changes are meaningful and isolated;
* small documentation fixes may be included with related feature branches;
* final documentation pass should use a dedicated docs branch.

## 5. Fix Branches

Pattern:

```text id="xeu9y1"
fix/<short-bug-description>
```

Examples:

```text id="58gfno"
fix/cross-currency-rounding
fix/settlement-rollback
fix/statement-date-filter
```

Rules:

* create from `develop` before release;
* create from `main` only if simulating a post-release hotfix;
* include regression tests when applicable.

## 6. Chore Branches

Pattern:

```text id="npxvm4"
chore/<short-task-name>
```

Examples:

```text id="60jbhq"
chore/configure-ci
chore/update-docker-compose
chore/configure-linting
```

Rules:

* use for tooling and non-feature changes;
* avoid mixing chore changes with business feature implementation.

## Initial Specification Commit

The first meaningful commit on `develop` will consolidate:

* root configuration files;
* SDD specification files;
* ADRs;
* diagrams;
* AI prompts;
* initial README;
* AI usage log.

Recommended commit message:

```text id="bstn9b"
docs: define project specifications and architecture decisions
```

This commit represents the specification baseline for future implementation.

It is intentionally large because it establishes the documentation foundation before code development begins.

After this commit, work should be broken into smaller feature branches and commits.

## Conventional Commits

All commits after the initial specification commit must follow Conventional Commits.

Allowed types:

```text id="zvfdxn"
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

## Commit Type Rules

### `feat`

Use for new product behavior.

Examples:

```text id="y0xgw5"
feat: add pricing simulation endpoint
feat: implement currency exchange engine
feat: add settlement statement grid
```

### `fix`

Use for bug corrections.

Examples:

```text id="6wkh65"
fix: correct cross-currency conversion order
fix: prevent duplicate settlement persistence
fix: handle missing exchange rate error
```

### `test`

Use for test-only changes.

Examples:

```text id="7n9n0x"
test: cover pricing strategy resolution
test: add settlement rollback integration test
test: cover statement query filters
```

### `docs`

Use for documentation changes.

Examples:

```text id="s3r6mu"
docs: update API contract for settlement response
docs: add final setup instructions
docs: document AI-assisted implementation
```

### `refactor`

Use for behavior-preserving code changes.

Examples:

```text id="wfnttz"
refactor: extract pricing calculation value object
refactor: isolate settlement statement query repository
```

### `chore`

Use for maintenance or tooling.

Examples:

```text id="wcsp56"
chore: configure editor settings
chore: update docker compose service names
```

### `build`

Use for build-system changes.

Examples:

```text id="xauzt3"
build: configure Maven dependencies
build: add Angular production build
```

### `ci`

Use for CI pipeline changes.

Examples:

```text id="8qrj4k"
ci: add backend test workflow
ci: run frontend build on pull requests
```

## Commit Quality Rules

Commits should be:

* atomic;
* focused;
* readable;
* linked to a logical task;
* small enough to review;
* ordered in a coherent development story.

Avoid:

```text id="xqx6fv"
finalizado
ajustes
wip
teste
arrumando coisas
commit final
```

Avoid mixing unrelated changes, such as:

```text id="7zy38l"
backend pricing engine + frontend layout + Docker + README rewrite
```

Prefer splitting into separate commits.

## Pull Request Simulation

Every feature branch should be integrated into `develop` through a simulated Pull Request.

The PR description should include:

```text id="hluhll"
## Summary

## Specs Covered

## Changes

## Tests

## Screenshots or Evidence

## Risks and Trade-offs

## AI Usage
```

## Pull Request Template

Recommended PR body:

````md id="8530oi"
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
- [ ] Manual validation performed

Commands executed:

```bash
<commands>
````

## Screenshots or Evidence

Add screenshots for frontend changes or Swagger/API evidence for backend changes.

## Risks and Trade-offs

Describe relevant risks, assumptions or follow-ups.

## AI Usage

Describe whether AI was used and whether `AI_USAGE.md` was updated.

````

## Review Expectations

Before merging a PR into `develop`, verify:

- scope is focused;
- specs were followed;
- ADRs were followed;
- tests pass;
- documentation was updated when needed;
- `AI_USAGE.md` was updated when AI materially contributed;
- no secrets were committed;
- no unrelated files were changed;
- financial code does not use floating-point types.

## Merge Strategy

Preferred merge strategy:

```text id="o8l4yu"
Squash and merge for feature branches
````

Rationale:

* keeps `develop` history readable;
* groups each feature into a meaningful unit;
* avoids excessive WIP commits;
* makes final project history easier to inspect.

Alternative:

```text id="9m482v"
Rebase and merge
```

Acceptable when the branch history is already clean and meaningful.

Avoid unnecessary merge commits unless intentionally demonstrating branch history.

## Commit History Story

The final Git history should tell a story similar to:

```text id="jvt943"
docs: define project specifications and architecture decisions
feat: add backend scaffold
feat: add database migrations
feat: implement currency exchange engine
feat: implement pricing strategy engine
test: cover pricing strategy rules
feat: implement atomic settlement flow
test: cover settlement rollback and duplicate prevention
feat: add settlement detail query
test: cover settlement detail audit response
feat: add settlement statement query
feat: add angular frontend scaffold
feat: add pricing simulation screen
feat: add settlement statement grid
feat: add exchange rate screen
chore: finalize local runtime setup
docs: finalize delivery documentation
ci: add validation workflow
```

The exact order may differ, but the repository should show controlled progression.

## Recommended Implementation Branch Plan

After the initial specification commit, create these branches in order:

### 1. Backend Scaffold

```text id="9qjt9k"
feature/backend-scaffold
```

Expected work:

* Spring Boot project;
* Maven configuration;
* base packages;
* health endpoint if useful;
* global exception handler skeleton;
* OpenAPI setup;
* initial Dockerfile.

Expected commit:

```text id="3smxhk"
feat: add backend scaffold
```

### 2. Database Migrations

```text id="a7sih6"
feature/database-migrations
```

Expected work:

* Flyway setup;
* initial schema;
* seed currencies;
* seed receivable types;
* indexes and constraints.

Expected commit:

```text id="1rwc1m"
feat: add initial database migrations
```

### 3. Currency Engine

```text id="2h83l4"
feature/currency-engine
```

Expected work:

* create exchange rate endpoint;
* latest exchange rate lookup;
* validation;
* persistence;
* tests.

Expected commits:

```text id="lo37zv"
feat: add exchange rate management
test: cover exchange rate validation
```

### 4. Pricing Engine

```text id="p4p1um"
feature/pricing-engine
```

Expected work:

* domain value objects;
* pricing strategy interface;
* strategy implementations;
* pricing engine;
* simulation endpoint;
* unit tests.

Expected commits:

```text id="7aguu7"
feat: implement pricing strategy engine
test: cover pricing calculation rules
```

### 5. Settlement Flow

```text id="6fj2xz"
feature/settlement-flow
```

Expected work:

* settlement creation endpoint;
* transactional application service;
* settlement items;
* audit snapshot;
* duplicate prevention;
* rollback behavior.

Expected commits:

```text id="vbzga6"
feat: implement atomic settlement flow
test: cover settlement rollback and duplicate prevention
```

### 6. Settlement Detail

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

### 7. Statement Query

```text id="7mg8ps"
feature/statement-query
```

Expected work:

* settlement statement endpoint;
* filters;
* server-side pagination;
* optimized query/projection;
* tests.

Expected commits:

```text id="qy6rn5"
feat: add settlement statement query
test: cover statement query filters
```

### 8. Frontend Scaffold

```text id="vfbcne"
feature/frontend-scaffold
```

Expected work:

* Angular project;
* Angular Material setup;
* routing;
* layout;
* base API config;
* Dockerfile.

Expected commit:

```text id="9p052b"
feat: add angular frontend scaffold
```

### 9. Frontend Simulation

```text id="bklkjy"
feature/frontend-simulation
```

Expected work:

* pricing simulation form;
* API integration;
* result panel;
* validation and errors.

Expected commit:

```text id="iz36s0"
feat: add pricing simulation screen
```

### 10. Frontend Statement Grid

```text id="lx2j8g"
feature/frontend-statement-grid
```

Expected work:

* settlement grid;
* filters;
* server-side pagination;
* loading and empty states.

Expected commit:

```text id="u8f7e5"
feat: add settlement statement grid
```

### 11. Frontend Exchange Rates

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

### 12. Final Documentation and Delivery

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
* AI usage completion;
* diagrams review;
* setup instructions;
* delivery checklist;
* local execution evidence.

Expected commits:

```text
chore: finalize local runtime setup
docs: finalize technical challenge delivery
```

## Release Flow

Final release process:

```bash id="s6jsao"
git checkout develop
git pull origin develop

# verify tests and documentation

git checkout main
git pull origin main
git merge --no-ff develop

git tag -a v1.0.0 -m "Release v1.0.0"
git push origin main
git push origin v1.0.0
```

If using PR simulation for final delivery:

```text id="in9lp7"
Open PR: develop -> main
Merge PR
Create tag v1.0.0 from main
Create GitHub/GitLab release
```

## Release Tagging

Final release tag:

```text id="foky0r"
v1.0.0
```

Rationale:

* first complete delivery;
* semantic versioning format;
* easy evaluator reference;
* demonstrates release discipline.

Optional pre-release tags may be used if helpful:

```text id="h2n8wl"
v0.1.0-specs
v0.5.0-backend
```

For this challenge, only `v1.0.0` is required.

## Hotfix / Crisis Simulation

A full crisis simulation is not required for Pleno-level delivery.

However, if time allows, the project may document a simulated hotfix workflow as a senior-level differential.

Example:

```text id="msp0k3"
main
└── hotfix/cross-currency-rounding
```

Possible commands:

```bash id="sub4oq"
git checkout main
git checkout -b hotfix/cross-currency-rounding
# apply fix
git commit -m "fix: correct cross-currency rounding"
git checkout main
git merge --no-ff hotfix/cross-currency-rounding
git tag -a v1.0.1 -m "Release v1.0.1"
```

This is optional and should not compromise required delivery scope.

## AI Usage and Git

When AI materially contributes to a feature, the PR must mention it.

`AI_USAGE.md` must be updated when AI contributes to:

* implementation;
* test generation;
* architecture decisions;
* debugging;
* documentation;
* review.

AI usage does not replace authorship.

The author remains responsible for understanding and validating all committed changes.

## Commit Signing

Commit signing is not required for this technical challenge.

If already configured locally, signed commits may be used.

Do not delay delivery to configure commit signing.

## Git Hooks

Git hooks are optional.

If time allows, simple hooks may be added for:

* formatting;
* linting;
* backend tests;
* frontend tests.

Possible tools:

```text id="4hj871"
pre-commit
Husky
Maven test phase
npm scripts
```

Hooks are a senior-level differential and should not compromise core implementation.

## CI/CD Relationship

The Git workflow should support future CI/CD.

Recommended final CI checks:

* backend tests;
* frontend build;
* linting if configured;
* Docker Compose validation if feasible.

CI/CD is not the primary scope of this ADR but should be compatible with the branch strategy.

## Documentation Updates

Documentation must be updated in the same branch as the behavior change when practical.

Examples:

* API behavior changes must update `docs/specs/04-api-contract.md`;
* data model changes must update `docs/specs/05-data-model.md`;
* architecture changes must update `docs/specs/06-architecture.md` and relevant ADRs;
* AI-assisted work must update `AI_USAGE.md`;
* final setup changes must update `README.md`.

## Risks

### Risk 1 — Too Many Branches for a Small Project

Mitigation:

* keep branches aligned with meaningful deliverables;
* avoid creating branches for trivial changes;
* squash merge feature branches when appropriate.

### Risk 2 — Large Initial Documentation Commit

Mitigation:

* accept one initial documentation commit as specification baseline;
* after that, enforce smaller scoped commits.

### Risk 3 — PR Simulation Becomes Performative

Mitigation:

* write useful PR descriptions;
* include specs covered;
* include tests run;
* include risks and trade-offs;
* keep diffs reviewable.

### Risk 4 — History Pollution

Mitigation:

* squash WIP commits;
* use clear commit messages;
* avoid unnecessary merge commits;
* keep branches focused.

### Risk 5 — Documentation and Code Diverge

Mitigation:

* update docs with behavior changes;
* use final review checklist;
* include documentation review in PRs.

## Consequences

### Positive Consequences

This workflow provides:

* clear development story;
* professional repository structure;
* traceability;
* isolated feature work;
* evaluator-friendly history;
* clear final release reference;
* alignment with the challenge seniority expectations.

### Negative Consequences

This workflow adds:

* more process than direct commits;
* more branch management;
* more documentation overhead;
* need to keep PR descriptions meaningful.

### Mitigations

The workflow is intentionally simplified:

* one integration branch;
* feature branches only for meaningful work;
* no complex release branches;
* final tag only;
* optional hooks and CI/CD only if time allows.

## Decision Validation

This ADR is valid if the final repository demonstrates:

* `main` as stable final branch;
* `develop` as integration branch;
* feature branches or PR history;
* Conventional Commits;
* readable commit history;
* simulated PR descriptions;
* final merge into `main`;
* final tag `v1.0.0`;
* documentation aligned with implementation;
* AI usage reflected in `AI_USAGE.md`.

## Related Documents

```text id="v7y0dh"
README.md
AGENTS.md
AI_USAGE.md
docs/specs/09-git-workflow.md
docs/specs/10-ai-workflow.md
docs/specs/11-delivery-checklist.md
docs/adr/ADR-007-ai-assisted-development.md
```

## Review Notes

This ADR may be revisited if:

* the evaluator requests a different delivery branch;
* repository hosting limitations affect Pull Request simulation;
* CI/CD constraints require changes;
* the project scope changes significantly.

Any change to Git workflow must update:

* this ADR;
* `README.md`;
* `AGENTS.md`;
* `docs/specs/09-git-workflow.md`;
* relevant prompts;
* final delivery checklist.
