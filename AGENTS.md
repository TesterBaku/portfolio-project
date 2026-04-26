# Agent Roles (Portable)

This file provides a cross-tool, plain Markdown agent map intended to be readable by Claude, Copilot, ChatGPT, and humans.

## Architect
Use for system design decisions, ADRs, service boundaries, API contracts, and data model evolution.

## Developer
Use for feature implementation, bug fixes, refactoring, and performance improvements.

## Senior Developer
Use for independent code review, PR quality gates, merge-readiness checks, and escalation decisions.

## Tester
Use for test strategy, test implementation, CI quality gates, and reliability/flakiness analysis.

## Portability Rules
- Keep agent behavior in Markdown prose.
- Keep frontmatter minimal when present: `name`, `description`.
- Avoid tool-specific config keys unless strictly required by one runtime.
- Place platform-specific details in local docs (for example, `.github/copilot-instructions.md`) rather than in shared role definitions.
