# 08 - Frontend Scaffold Prompt

## Purpose

Use this prompt to create the Angular frontend foundation for the **SRM Credit Engine**.

This prompt focuses on:

* Angular application scaffold;
* Angular Material setup;
* routing and layout;
* shared/core folder structure;
* API base configuration;
* frontend Dockerfile.

This task must not implement business screens beyond minimal shell/navigation structure.

## Target Branch

Recommended branch:

```text
feature/frontend-scaffold
```

Recommended commit message:

```text
feat: add angular frontend scaffold
```

## Prompt

```text
You are working on the SRM Credit Engine repository.

This task is to create only the Angular frontend scaffold.

Read first:
- AGENTS.md
- README.md
- docs/specs/04-api-contract.md
- docs/specs/06-architecture.md
- docs/specs/08-acceptance-criteria.md
- docs/adr/ADR-005-frontend-stack.md
- docker-compose.yml
- .env.example

Task:
Create the Angular frontend foundation under frontend/.

Scope:
You may change:
- frontend/
- README.md only if frontend setup instructions are strictly necessary
- AI_USAGE.md only if you materially use AI and need to log the interaction

Do not change:
- backend/
- docs/specs/
- docs/adr/
- docs/diagrams/
- docs/prompts/
- docker-compose.yml unless the frontend service is missing or incompatible
- .env.example unless a frontend environment variable is missing

Requirements:
- Use Angular 22.x.
- Use Angular Material.
- Use a feature-oriented structure aligned with docs/specs/06-architecture.md.
- Create core/, shared/, features/ and models/ folders.
- Configure routing and a simple application shell.
- Configure API base URL through environment/runtime config.
- Add frontend Dockerfile suitable for production static serving.
- Ensure npm run build succeeds.

Constraints:
- Do not implement the official financial formula.
- Do not change backend contracts.
- Do not introduce NgRx.

Expected output:
- Summary
- Files changed
- Tests/build executed
- Validation performed
- Risks or follow-ups
```
