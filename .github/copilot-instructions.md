# Intelligent Logistics Control Tower — Development Infrastructure

This workspace uses specialized agents, consistent instructions, and reusable skills to maintain engineering rigor across all roles: architecture, development, testing, and code review.

## Quick Start for Developers

1. **Choosing your agent**: Type `/` in Copilot Chat to see available agents and skills.
   - Use **@Architect** for design decisions, ADRs, and system architecture.
   - Use **@Developer** for feature implementation, refactoring, and debugging.
   - Use **@Senior Developer** for independent PR/code review and merge-readiness checks.
   - Use **@Tester** for test strategy, SDET work, and quality assurance.

2. **Common workflows**: Several skills encapsulate best practices:
   - `/design-architecture` — Build architecture diagrams and ADRs.
   - `/manage-git-workflow` — Branching, rebasing, and PR workflow.
   - `/implement-tests` — Test strategy, coverage, and automation.

3. **Development standards**: Consult the instructions files for expected standards:
   - [Development Instructions](.github/instructions/development.instructions.md)
   - [Code Review Standards](.github/instructions/code-review.instructions.md)
   - [Testing Standards](.github/instructions/testing.instructions.md)

---

## Agents

### @Architect
Specializes in system design, architecture decisions, ADRs, and technical strategy. Use for:
- Designing services and boundaries
- Writing ADRs for significant decisions
- API contract design
- Data model evolution

### @Developer
Specializes in feature implementation, debugging, refactoring, and code quality. Use for:
- Building new features
- Fixing bugs
- Code review and improvements
- Performance optimization

### @Senior Developer
Specializes in independent PR review and quality gatekeeping. Use for:
- Reviewing feature PRs for correctness and edge cases
- Validating test coverage and merge readiness
- Enforcing code quality and review standards
- Escalating design concerns to @Architect and test strategy gaps to @Tester

### @Tester
Specializes in test strategy, automation, and quality assurance. Use for:
- Test design and coverage
- Implementing SDET workflows
- CI/CD integration
- Performance and security testing

---

## Skills (Reusable Workflows)

### `/design-architecture`
Create system architecture diagrams, write ADRs, and document design decisions. Bundles templates for ADRs, architecture diagrams, and decision matrices.

### `/manage-git-workflow`
Execute Git workflows: branching, rebasing, cherry-picking, and PR preparation. Includes scripts for safe multi-branch operations.

### `/implement-tests`
Implement test suites: unit tests, integration tests, and E2E tests. Includes templates for test fixtures, parameterized tests, and coverage reporting.

---

## Instructions (Applied Automatically)

Instructions are applied based on file patterns and provide context-specific guidance:

- **development.instructions.md**: Applied to all feature code. Covers code style, naming, structure, and practices.
- **code-review.instructions.md**: Applied during PR review. Covers review standards, safety checks, and approval criteria.
- **testing.instructions.md**: Applied to test files. Covers test design, naming, fixture patterns, and coverage expectations.

## VS Code Agent File Compatibility

When editing `.agent.md` files in `.github/agents/`, use VS Code custom-agent compatible structure:

- Keep supported keys in YAML frontmatter only (`name`, `description`, optional `model`, optional `tools`).
- Put behavioral guidance in the Markdown body after frontmatter, not in an `instructions:` frontmatter key.
- Do not use instruction-file keys like `applyTo` in `.agent.md` files.
- For maximum Claude/Copilot/ChatGPT portability, default to minimal frontmatter (`name`, `description`) and keep runtime/tool preferences in plain Markdown guidance.

If you see warnings like "instructions not supported", normalize the file to this format.

## AI Customization Commit Policy

AI-related project files are first-class project artifacts and must not be left as untracked clutter.

- Always commit intentional changes to `.github/copilot-instructions.md`, `.github/agents/**`, `.github/instructions/**`, and `.github/skills/**`.
- Do not leave temporary or untracked AI customization files in the workspace.
- Keep AI customization commits small and focused (single concern per commit).

---

## Project Standards

### Branch Strategy
- `main` — Production-ready, always deployable.
- `develop` — Integration branch for features.
- `feature/*` — Feature branches off `develop`, one feature per branch.
- `bugfix/*` — Bug fixes off `develop`.
- `hotfix/*` — Critical fixes off `main`, merged back to both `main` and `develop`.

See [development.instructions.md](.github/instructions/development.instructions.md) for full workflow.

### Code Review Process
1. Create a feature branch and push to origin.
2. Open a pull request to `develop`.
3. Request review from team. 
4. Address feedback. Re-request review after changes.
5. Once approved, use the `/manage-git-workflow` skill to rebase and merge.
6. Delete the feature branch.

### Testing Standards
- Unit tests: ≥80% coverage on business logic.
- Integration tests: All service boundaries and workflows.
- E2E tests: Critical user paths.
- All tests run on every commit via CI.

See [testing.instructions.md](.github/instructions/testing.instructions.md) for details.

---

## Getting Help

If you need guidance on a specific role or task:

1. **Architecture & Design**: `@Architect: <your question>`
2. **Feature Development**: `@Developer: <your question>`
3. **Code Review & PR Quality Gate**: `@Senior Developer: <your question>`
4. **Testing & QA**: `@Tester: <your question>`
5. **Git Workflows**: `/manage-git-workflow` skill or see development.instructions.md

---

## File Structure

```
.github/
  agents/                     # Custom agents
    architect.agent.md
    developer.agent.md
    senior-developer.agent.md
    tester-sdet.agent.md
  instructions/               # Auto-applied guidance
    development.instructions.md
    code-review.instructions.md
    testing.instructions.md
  skills/
    architecture-design/
      SKILL.md
      assets/
    git-workflow/
      SKILL.md
      assets/
    test-implementation/
      SKILL.md
      assets/
  copilot-instructions.md     # This file
```

---

## Philosophy

This infrastructure is built on these principles:

1. **Role clarity**: Each agent is specialized and knows its scope.
2. **Consistency**: Instructions ensure the same standards across all work.
3. **Reusability**: Skills encode best practices so they don't need to be reinvented.
4. **Independence**: All tools and workflows are local; no external dependencies.
5. **Transparency**: All decisions, standards, and processes are documented here.

All team members should read this file and the instructions before starting work.
