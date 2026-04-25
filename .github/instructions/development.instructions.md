---
name: "Development Instructions"
description: "Standard practices for feature development, code style, branching, and PR workflow. Use when: writing new features, following project conventions, or understanding the development process."
applyTo: "src/**"
---

# Development Standards

This document defines the standards for writing code, structuring features, and collaborating on this project.

## Branch Strategy

### Main Branches
- **`main`**: Production-ready, always deployable. Protected branch. Changes via PR from `develop` or `hotfix/*` only.
- **`develop`**: Integration branch for features. Base branch for feature PRs. Should always be stable and buildable.

### Main Merge Gate

- Direct pushes to `main` are blocked.
- Pull requests targeting `main` must originate from `develop` or `hotfix/*`.
- This is enforced by branch protection plus required status checks.

### Feature Branches
- **Pattern**: `feature/<ticket-id>-<short-description>`
- **Example**: `feature/CARGO-123-shipment-tracking`
- **Off**: `develop`
- **Merge to**: `develop` (via PR with review)
- **Lifetime**: Until PR is merged, then delete
- **Naming**: 
  - Use hyphen-separated words, lowercase
  - Include ticket ID if using an issue tracker
  - Keep description under 40 characters

### Bug Fix Branches
- **Pattern**: `bugfix/<ticket-id>-<short-description>`
- **Example**: `bugfix/CARGO-456-order-status-race-condition`
- **Off**: `develop`
- **Merge to**: `develop` (via PR with review)

### Hotfix Branches
- **Pattern**: `hotfix/<short-description>`
- **Example**: `hotfix/payment-gateway-timeout`
- **Off**: `main`
- **Merge to**: `main` (via PR), then merge result back to `develop`
- **Lifetime**: Until both merges are complete

## Commit Messages

### Format
```
<type>(<scope>): <subject>

<body>

<footer>
```

### Type
- `feat`: A new feature
- `fix`: A bug fix
- `docs`: Documentation only
- `style`: Changes that don't affect code meaning (formatting, missing semicolons, etc.)
- `refactor`: Code change that doesn't fix a bug or add a feature
- `perf`: Code change that improves performance
- `test`: Adding or updating tests
- `ci`: Changes to CI/CD configuration
- `chore`: Changes to build process, dependencies, or tooling

### Subject
- Imperative mood ("add", not "added" or "adds")
- Don't capitalize first letter
- No period at the end
- Limit to 50 characters

### Body
- Explain *what* and *why*, not *how*
- Wrap at 72 characters
- Separate from subject with a blank line
- Use bullet points where appropriate

### Footer
- Reference issues: `Fixes #123`, `Closes #456`
- Note breaking changes: `BREAKING CHANGE: <description>`

### Examples

Good:
```
feat(shipment): add delivery date estimation

Implement ML-based delivery date prediction using Gemma
model to provide shipment ETAs to operators and customers.
Uses historical delivery patterns and current logistics factors.

Fixes #234
```

```
fix(order): resolve race condition in status update

Acquire write lock before checking and updating order status.
Previously, concurrent updates could skip intermediate states.

Fixes #567
BREAKING CHANGE: order.status is now read-only; use update_status() method
```

## Code Organization

### Directory Structure

```
src/
  main/
    java/
      com/
        logistics/
          core/
            orders/        # Order management module
              domain/      # Domain models
              api/         # REST endpoints
              service/     # Business logic
              persistence/ # Data access
            shipments/     # Shipment management
            exceptions/    # Exception handling
            config/        # Configuration
  test/
    java/
      com/
        logistics/
          core/
            orders/
              service/     # Service tests
            integration/   # Integration tests
```

### Package Naming
- `domain`: Entity classes, value objects, domain logic
- `api`: Controllers, DTOs, API endpoints
- `service`: Business logic, orchestration
- `persistence`: Repository interfaces, implementations
- `config`: Configuration classes, beans
- `exception`: Custom exception types
- `util`: Utility functions, helpers

## Code Style

### Java

- **Formatter**: Follow Google Java Style Guide
- **IDE Config**: Use `.editorconfig` for consistency across editors
- **Indentation**: 4 spaces (never tabs)
- **Line Length**: 100 characters (soft limit), 120 (hard limit)

#### Naming Conventions
- Classes: `PascalCase` (`OrderService`, `ShipmentDTO`)
- Methods: `camelCase` (`getOrderById`, `updateShipmentStatus`)
- Constants: `UPPER_SNAKE_CASE` (`DEFAULT_TIMEOUT_SECONDS`, `MAX_RETRIES`)
- Variables: `camelCase` (`orderCount`, `isActive`)
- Private fields: `camelCase`, prefix with optional `_` if desired (`_cache`, `_logger`)

#### Class Structure
```java
public class OrderService {
    // 1. Static fields
    private static final Logger LOG = LoggerFactory.getLogger(OrderService.class);
    
    // 2. Instance fields
    @Autowired
    private OrderRepository orderRepository;
    
    // 3. Constructors
    public OrderService(OrderRepository repo) {
        this.orderRepository = repo;
    }
    
    // 4. Public methods
    public Order createOrder(CreateOrderRequest request) {
        // ...
    }
    
    // 5. Protected methods
    protected void validateOrder(Order order) {
        // ...
    }
    
    // 6. Private methods
    private void logOrderCreation(Order order) {
        // ...
    }
}
```

#### Comments and Documentation
- Use JavaDoc for public classes, methods, and fields
- Use inline comments sparingly; prefer clear code over comments
- Explain *why*, not *what* (the code explains what)

```java
/**
 * Estimates delivery date based on origin, destination, and current load.
 * 
 * Uses historical data from the last 90 days and current logistics factors
 * (weather, traffic, capacity) to provide a 95% confidence interval.
 * 
 * @param shipment the shipment to estimate for
 * @return delivery date estimate with confidence interval
 * @throws ShipmentNotFoundException if shipment does not exist
 */
public DeliveryEstimate estimateDeliveryDate(Shipment shipment) {
    // ...
}
```

### Python

- **Formatter**: Black (default line length: 88)
- **Linter**: Pylint or Flake8
- **Type Hints**: Required for function signatures and public methods
- **Indentation**: 4 spaces

#### Naming Conventions
- Classes: `PascalCase` (`OrderService`, `ShipmentDTO`)
- Functions/Methods: `snake_case` (`get_order_by_id`, `update_shipment_status`)
- Constants: `UPPER_SNAKE_CASE` (`DEFAULT_TIMEOUT_SECONDS`)
- Variables: `snake_case` (`order_count`, `is_active`)
- Private functions: prefix with `_` (`_validate_order`)

#### Type Hints
```python
from typing import Optional, List
from datetime import datetime

def create_order(customer_id: str, items: List[OrderItem]) -> Order:
    """Create a new order for a customer."""
    pass

def get_order(order_id: str) -> Optional[Order]:
    """Fetch order by ID or return None."""
    pass
```

## Testing Expectations

- Minimum 80% coverage for business logic
- All public APIs have test cases
- Happy path, error path, and edge cases
- Unit tests run in <100ms per test
- Integration tests are explicitly marked and can run separately

See [testing.instructions.md](./testing.instructions.md) for details.

## Pull Request Process

### Stage A: Feature Or Bug Slice To `develop`

1. **Create branch** off `develop` with proper naming.
2. **Implement one small slice** with unit and integration tests.
3. **Run local tests** before opening PR.
4. **Open PR to `develop`** with clear scope and validation notes.
5. **CI gates run** (`develop-gates`): unit/integration checks must pass.
6. **Independent review required** (see code-review.instructions.md).
7. **Address feedback**, rerun tests, and repeat until green + approved.
8. **Merge to `develop`**.

### Stage B: Promotion PR From `develop` To `main`

1. **Open promotion PR** from `develop` to `main`.
2. **CI promotion gates run** (`main-promotion-gates`) and source policy check (`main-source-policy`).
3. **Run or confirm E2E coverage when needed** for critical user paths.
4. **Independent review required**.
5. **Address feedback**, rerun tests, and repeat until green + approved.
6. **Merge to `main`** (direct pushes are blocked).

### Hotfix Path

1. Branch from `main` using `hotfix/*`.
2. Fix and add tests.
3. Open PR to `main`, pass checks, review, merge.
4. Back-merge the hotfix into `develop` immediately.

## Incremental PR Policy (Default)

All work is delivered as small, single-concern PRs.

### Rules

- One PR should solve one problem only (one feature slice, one bug, one refactor, or one infra change).
- Do not mix unrelated concerns in the same PR (for example, API feature + lint cleanup + CI changes).
- Prefer vertical slices that are independently mergeable.
- Every non-trivial PR must include tests for the behavior it changes.
- If a change is too large, split it into stacked PRs against `develop`.

### Recommended PR Size

- Target: under 300 changed lines (excluding lockfiles and generated files)
- Soft warning: over 500 changed lines
- Hard rule: if over 800 changed lines, split unless explicitly approved by maintainer
- Target: under 10 files changed when possible

### Required Sequence For Large Features

1. PR-0: Project or infra scaffolding
2. PR-1: Data model and migration changes
3. PR-2: Service/domain logic
4. PR-3: API layer and validation
5. PR-4: Tests and docs hardening

If needed, these can be split further (for example, PR-2a and PR-2b).

### PR Planning Requirement

Before coding, define the PR slices in the issue or task description:

- PR title for each slice
- Scope for each slice (in and out)
- Expected files to change
- Validation plan per slice

This improves review speed and reduces integration risk.

## Before You Commit

- [ ] Code follows style guide
- [ ] Tests pass locally
- [ ] No debug code or commented-out code
- [ ] Commit message follows template
- [ ] No secrets, credentials, or PII
- [ ] Change is scoped to one concern only
- [ ] PR size is within policy or split plan is documented

## Performance Considerations

- Profile before optimizing
- Consider query complexity (N+1 queries, missing indexes)
- Use connection pooling for databases
- Implement caching strategically
- Monitor memory usage in long-running processes

## Security Checklist

- [ ] No hardcoded secrets or credentials
- [ ] Input validation on all user inputs
- [ ] SQL injection protection (use parameterized queries)
- [ ] CORS configured correctly
- [ ] Authentication/authorization checks on protected endpoints
- [ ] Sensitive data not logged
- [ ] Dependencies up-to-date

## Documentation

- Update README when behavior changes
- Add docstrings/JavaDoc for public APIs
- Document configuration options
- Include example requests/responses for API endpoints
- Add architecture notes for complex logic
