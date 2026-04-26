# CLAUDE.md

## Workflow Orchestration

### 1. Plan Mode Default
- Enter plan mode for ANY non-trivial task (3+ steps or architectural decisions)
- If something goes sideways, STOP and re-plan immediately
- Use plan mode for verification steps, not just building
- Write detailed specs upfront to reduce ambiguity

### 2. Subagent Strategy
- Use subagents liberally to keep main context window clean
- Offload research, exploration, and parallel analysis to subagents
- For complex problems, throw more compute at it via subagents
- One task per subagent for focused execution

### 3. Self-Improvement Loop
- After ANY correction from the user: update .github/tasks/lessons.md with the pattern
- Write rules for yourself that prevent the same mistake
- Ruthlessly iterate on these lessons until mistake rate drops
- Review lessons at session start for relevant project

### 4. Verification Before Done
- Never mark a task complete without proving it works
- Diff behavior between main and your changes when relevant
- Ask yourself: "Would a staff engineer approve this?"
- Run tests, check logs, demonstrate correctness

### 5. Demand Elegance (Balanced)
- For non-trivial changes: pause and ask "is there a more elegant way?"
- If a fix feels hacky: "Knowing everything I know now, implement the elegant solution"
- Skip this for simple, obvious fixes -- don't over-engineer
- Challenge your own work before presenting it

### 6. Autonomous Bug Fixing
- When given a bug report: just fix it. Don't ask for hand-holding
- Point at logs, errors, failing tests -- then resolve them
- Zero context switching required from the user
- Go fix failing CI tests without being told how

---

## Task Management

0. **Pre-Session Checklist:** At the start of every session, read these files in order:
   1. `.github/tasks/lessons.md` — apply lessons from prior corrections before doing anything
   2. `.github/tasks/todo.md` — identify pending or blocking work
   3. `.github/instructions/development.instructions.md` — branch rules, commit format, PR policy
   4. `.github/pull_request_template.md` — required format for every PR body
1. **Plan First:** Write plan to `.github/tasks/todo.md` with checkable items
2. **Verify Plan:** Check in before starting implementation
3. **Track Progress:** Mark items complete as you go
4. **Explain Changes:** High-level summary at each step
5. **Document Results:** Add review section to `.github/tasks/todo.md`
6. **Capture Lessons:** Update `.github/tasks/lessons.md` after corrections

---

## PR Workflow

Every PR follows this sequence — no shortcuts:

### Opening a PR
1. Cut branch from latest `origin/develop` (never from stale local or main):
   ```
   git fetch origin
   git checkout develop && git pull --ff-only origin develop
   git checkout -b <branch-name>
   ```
2. Use `.github/pull_request_template.md` as the PR body — fill every section, do not invent a different format.
3. Mark **@Senior Developer** as the requested reviewer in the PR body (Reviewer Assignment section).

### Before Merging
4. Invoke the **Senior Developer agent** (`.github/agents/senior-developer.agent.md`) to review the PR. Do not substitute the inline `/review` skill — it does not fulfill the independent review requirement.
5. Only merge after the agent returns **APPROVE** with no unresolved Critical or Major issues.

### Merging and Cleanup
6. Squash merge into `develop`.
7. Delete the remote branch (use `--delete-branch` with `gh pr merge`).
8. Pull `develop` locally (`git pull --ff-only`) and prune stale refs (`git fetch --prune`).

---

## Core Principles

- **Simplicity First:** Make every change as simple as possible. Impact minimal code.
- **No Laziness:** Find root causes. No temporary fixes. Senior developer standards.
- **Minimal Impact:** Only touch what's necessary. No side effects with new bugs.
