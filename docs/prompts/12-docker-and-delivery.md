# 12 - Docker and Delivery Prompt

## Purpose

Use this prompt to finalize local runtime and delivery documentation for the **SRM Credit Engine**.

This prompt focuses on:

* final `docker-compose.yml` alignment;
* `.env.example` completeness;
* README run instructions;
* delivery checklist consistency;
* local execution validation evidence.

## Target Branch

Recommended branches:

```text
chore/docker-compose-final
docs/final-delivery
```

Recommended commit messages:

```text
chore: finalize local runtime setup
docs: finalize technical challenge delivery
```

## Prompt

```text
You are working on the SRM Credit Engine repository.

This task is to finalize runtime and delivery documentation.

Read first:
- AGENTS.md
- README.md
- AI_USAGE.md
- docker-compose.yml
- .env.example
- docs/specs/08-acceptance-criteria.md
- docs/specs/09-git-workflow.md
- docs/specs/10-ai-workflow.md
- docs/specs/11-delivery-checklist.md
- docs/adr/ADR-006-git-workflow.md
- docs/adr/ADR-007-ai-assisted-development.md

Task:
Adjust final runtime and delivery documentation so the repository is ready for final review.

Scope:
You may change:
- README.md
- AI_USAGE.md
- docker-compose.yml
- .env.example
- docs/specs/11-delivery-checklist.md
- docs/diagrams/* only if a final consistency correction is strictly necessary

Do not change:
- backend business logic
- frontend business logic
- docs/specs/03-business-rules.md
- docs/specs/04-api-contract.md
- docs/specs/05-data-model.md unless a proven drift must be corrected

Requirements:
- Ensure docker compose up --build is the documented final path.
- Ensure required environment variables are documented.
- Ensure README includes backend, frontend, Swagger and Docker instructions.
- Ensure final prompt inventory and delivery checklist are aligned.
- Record material AI usage in AI_USAGE.md.

Tests:
- Run the relevant local validation commands available at this stage.

Expected output:
- Summary
- Files changed
- Validation commands executed
- Remaining risks or follow-ups
```
