---
name: "security-review"
description: "Run an application security review across code, config, CI, and dependencies. Use when: reviewing PRs for security risk, validating auth/authz and input validation, checking secrets handling, evaluating container and workflow security, or preparing merge-readiness for security-sensitive changes."
user-invocable: true
---

# Security Review Skill

Perform a structured AppSec review for backend services, infrastructure configuration, and CI workflows.

## When to Use This Skill

- **PR security gate**: A PR changes API, auth, persistence, config, or workflows.
- **Threat-focused review**: You need explicit abuse-case and attack-surface analysis.
- **Hardening pass**: You are preparing release candidates or high-risk feature merges.
- **Security regression check**: A refactor might weaken prior security guarantees.

## What This Skill Covers

### 1. Authentication and Authorization
- Verify all sensitive endpoints enforce authentication.
- Verify authorization is checked per action/resource, not only at controller entry.
- Confirm role/permission checks are tested for allow and deny paths.

### 2. Input Validation and Injection Defense
- Ensure request validation exists at boundaries.
- Ensure SQL access is parameterized; no string-built queries from user input.
- Ensure command/file/path operations sanitize or constrain untrusted input.

### 3. Secrets and Sensitive Data
- Confirm no secrets in source, test fixtures, workflow files, or logs.
- Confirm tokens/credentials are injected through environment or secret stores.
- Confirm logs avoid PII or sensitive operational details unless masked.

### 4. Dependency and Supply Chain Risk
- Check for newly introduced dependencies and lockfile updates.
- Confirm dependency updates are scoped and justified.
- Verify CI uses pinned or trusted actions/images where possible.

### 5. Container and Runtime Hardening
- Check Docker and compose settings for least privilege.
- Avoid unnecessary privileged flags, host mounts, and broad network exposure.
- Prefer non-root runtime users and explicit port exposure.

### 6. CI and Workflow Security
- Verify workflow permissions are minimal for the job's purpose.
- Validate branch/policy checks cannot be bypassed trivially.
- Check that untrusted PR input is not executed unsafely in shell commands.

## Review Workflow

### 1. Identify Change Surface
- List files touching: auth, API, persistence, workflows, Docker, dependencies.

### 2. Apply Security Checklist
- Walk through the six domains above and record findings by severity.

### 3. Verify with Tests
- Require tests for auth deny paths, input validation failures, and error handling.
- Add or request missing tests before approval for security-sensitive changes.

### 4. Produce Security Verdict
- **PASS**: No unresolved critical/high issues.
- **CONDITIONAL PASS**: Only medium/low issues with tracked follow-up.
- **BLOCK**: Any critical/high issue or missing mandatory auth validation.

## Output Format

Use this structure when reporting:

```markdown
## Security Review

### Verdict: PASS | CONDITIONAL PASS | BLOCK

### Critical
- <issue> - <file>:<line> - <fix>

### High
- <issue> - <file>:<line> - <fix>

### Medium
- <issue> - <file>:<line> - <fix or mitigation>

### Low
- <issue> - <file>:<line> - <hardening suggestion>

### Required Tests
- <missing test>
```

## Exit Criteria

- No unresolved critical/high vulnerabilities.
- Security-sensitive paths have automated tests for deny/failure behavior.
- No secrets or unsafe workflow patterns introduced.
