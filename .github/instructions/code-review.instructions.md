---
name: "Code Review Instructions"
description: "Standards for reviewing code, providing feedback, and approving changes. Use when: reviewing PRs, providing code feedback, or understanding approval criteria."
applyTo: []
---

# Code Review Standards

This document defines the process and criteria for reviewing code changes on this project.

## Code Review Principles

1. **Correctness**: Does the code do what it claims? Are there edge cases?
2. **Clarity**: Can another engineer understand this code in 5 minutes?
3. **Consistency**: Does it follow project conventions and patterns?
4. **Performance**: Are there obvious inefficiencies or resource leaks?
5. **Security**: Are there potential vulnerabilities (injection, exposure, etc.)?
6. **Testing**: Is it well-tested? Are edge cases covered?
7. **Maintainability**: Will this be easy to change in 6 months?

## Review Process

### 1. PR Opening Checklist

Before requesting review, the author should verify:
- [ ] PR title clearly describes the change
- [ ] PR description explains the "why" and "what"
- [ ] Related ticket/issue is linked
- [ ] At least one reviewer is explicitly requested
- [ ] All tests pass locally (`npm test`, `mvn test`, `pytest`, etc.)
- [ ] No debug code, console.log, or commented-out code
- [ ] Code follows style guide
- [ ] New public APIs are documented
- [ ] No secrets or credentials committed
- [ ] Commits follow the commit message format

### 2. Reviewer Responsibilities

When assigned to review a PR:

1. **Understand the context**: Read the ticket, the PR description, and any related discussion.
2. **Run the code**: Check it out locally, run tests, and verify the behavior if possible.
3. **Read the code**: Review for correctness, clarity, and adherence to standards.
4. **Be constructive**: Provide actionable feedback and explanations.
5. **Suggest improvements**: Point out opportunities for simplification or optimization.
6. **Approve or request changes**: Use GitHub's review tools (Comment, Approve, Request Changes).

### 3. Approval Criteria

Approve if:
- [ ] Code is correct and handles edge cases
- [ ] Tests are comprehensive (≥80% coverage for business logic)
- [ ] Code follows project style and conventions
- [ ] No obvious performance or security issues
- [ ] Documentation is clear and up-to-date
- [ ] Commits are well-structured and messages are clear
- [ ] PR is scoped to one concern and is reviewable in one sitting

Do not approve if:
- [ ] Tests are insufficient
- [ ] Code quality is below project standards
- [ ] There are unresolved security or performance concerns
- [ ] Documentation is missing or unclear
- [ ] The PR scope is unclear or too broad

## PR Scope And Size Review Policy

Reviewers should enforce small, focused PRs.

### Acceptable Scope

- One bug fix
- One feature slice
- One refactor with no behavior change
- One infrastructure or CI change

### Must Be Split

- Feature logic mixed with broad formatting-only changes
- Schema migration + unrelated service rewrites
- New feature + unrelated dependency upgrades
- PRs that are too large to review confidently in one pass

### Size Guidelines For Reviewers

- Target: < 300 changed lines
- Warning: > 500 changed lines (ask for split plan)
- Split expected: > 800 changed lines, unless pre-approved with clear rationale

When a PR is overscoped, request that the author split it into sequential PRs and identify merge order.

### 4. Addressing Feedback

After receiving feedback:

1. **Respond to comments**: Clarify your intent, ask follow-up questions, or acknowledge the feedback.
2. **Make changes**: Fix the issues raised, or explain why they shouldn't be changed.
3. **Mark as resolved**: Use GitHub's "Resolve conversation" feature for addressed comments.
4. **Request re-review**: Re-request review from the same reviewer.

## Common Review Comments

### ✅ Code Quality

**Good observations**:
- "This function is doing two things. Consider splitting into `validateOrder` and `persistOrder`."
- "This query could have an N+1 problem. Consider fetching all items in one query."
- "The error message doesn't explain what went wrong. Users won't know how to fix it."
- "This value is computed twice in the same request. Cache it in the context object."

**Avoid**:
- "This is bad" (explain why)
- "I don't like this" (explain what the issue is)
- Nitpicking formatting (use automated linters)

### ✅ Testing

**Good observations**:
- "The happy path is tested, but what if the database returns null?"
- "This integration test is flaky because it depends on timing. Use a mock instead."
- "This covers the happy path. What about when the payment gateway times out?"
- "Test coverage dropped from 82% to 75%. Let's bring it back to 80%."

**Approve if**:
- All public methods have test cases
- Edge cases and error scenarios are covered
- Integration points are tested
- Coverage is ≥80% for business logic

### ✅ Security

**Always check**:
- Hardcoded credentials or secrets
- SQL injection vulnerabilities (use parameterized queries)
- Cross-site scripting (XSS) vulnerabilities
- Cross-site request forgery (CSRF) protection
- Authentication and authorization checks
- Input validation
- Sensitive data in logs

**Example good comment**:
- "This endpoint doesn't check `user.canApproveOrders()`. Add an authorization check."
- "This SQL query is vulnerable to injection. Use a parameterized query or ORM."

### ✅ Performance

**Good observations**:
- "This query runs on every request. Consider caching with a TTL."
- "Loading 10,000 items into memory will be expensive. Use pagination."
- "This algorithm is O(n²). Consider using a hash set for O(n)."

**Balance**:
- Don't optimize prematurely
- Profile first, then optimize
- Document significant performance decisions

### ✅ Documentation

**Good observations**:
- "This API endpoint needs documentation. Add a comment explaining the parameters and response."
- "This breaking change should be noted in the CHANGELOG."
- "The README doesn't explain how to set up the dev environment for this feature."

## Difficult Conversations

### When You Disagree

1. **Ask questions**: "Help me understand why you chose this approach."
2. **Share concerns**: "I'm worried about X because of Y."
3. **Suggest alternatives**: "What if we did Z instead?"
4. **Decide together**: If still unclear, sync up synchronously or escalate to a lead.

### When the Author Disagrees

1. **Listen**: Understand their perspective.
2. **Explain**: Clarify why the feedback matters.
3. **Compromise**: Find a middle ground if possible.
4. **Escalate**: If you can't agree, involve a tech lead.

## Review Turnaround

- Aim to review within 24 hours
- For urgent/hotfix PRs, prioritize (turn around within 2-4 hours if possible)
- If you can't review, let the author know and suggest another reviewer

## Anti-Patterns to Avoid

❌ **Don't**:
- Hold up a PR over style preferences (use automated linters)
- Request major rewrites when minor fixes would work
- Approve PRs you didn't fully understand
- Request changes without explaining why
- Use reviews as a teaching moment for pet peeves

✅ **Do**:
- Focus on correctness, clarity, and maintainability
- Ask clarifying questions
- Suggest improvements with explanations
- Approve when criteria are met
- Be respectful and constructive

## Metrics

Track these over time to improve code quality:

- Average PR review time
- Number of review rounds per PR
- Test coverage percentage
- Bug escape rate (bugs found in production)
- Time to fix bugs (mean time to resolution)

---

## Tools and Automation

### Automated Checks on Every PR

- Linting (style guide compliance)
- Unit and integration tests
- Code coverage (fail if <80% for changed code)
- Security scanning (secrets, vulnerable dependencies)
- Performance tests (fail if key queries/APIs exceed baseline)

### GitHub PR Template

Use this template for all PRs:

```markdown
## Description
Briefly describe the change and why it's needed.

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Refactoring
- [ ] Performance improvement
- [ ] Documentation update

## Related Ticket
Fixes #123 (or link to ticket)

## Testing
- [ ] Unit tests added/updated
- [ ] Integration tests added/updated
- [ ] Manual testing performed

## Checklist
- [ ] Code follows style guide
- [ ] Tests pass locally
- [ ] No debug code
- [ ] Documentation updated
- [ ] No breaking changes (or documented)

## Screenshots (if applicable)
```

---

## Questions? 

If a code review standard is unclear, open an issue to discuss and refine it. This document should evolve with the team's needs.
