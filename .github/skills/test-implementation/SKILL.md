---
name: "implement-tests"
description: "Design and implement test suites: unit tests, integration tests, and E2E tests. Use when: writing tests, setting up test fixtures, designing test strategies, or improving coverage."
user-invocable: true
---

# Test Implementation Skill

Design, implement, and maintain comprehensive test suites using best practices and proven patterns.

## When to Use This Skill

- **Building unit tests**: Testing individual functions or classes
- **Integration testing**: Testing service boundaries and workflows
- **E2E testing**: Testing complete user workflows
- **Setting up test fixtures**: Creating reusable test data
- **Designing test strategy**: Planning coverage and automation
- **Improving coverage**: Identifying untested code paths

## What This Skill Provides

### Test Templates
Ready-to-use templates for unit, integration, and E2E tests.

Location: `.github/skills/test-implementation/assets/`

### Fixture Patterns
Reusable patterns for test data and mocking.

### Coverage Targets
Specific goals for different types of code.

## Workflow

### 1. Understand What to Test

Before writing a test, answer:
- What is the expected behavior?
- What edge cases might break this?
- What error conditions should be handled?
- What would a user experience if this failed?

### 2. Choose the Test Type

Use the testing pyramid:

```
        /\
       /E2E\           Test complete workflows (5-10%)
      /-----\
     /       \
    /         \
   /-----------\
  /Integration \       Test service boundaries (20-30%)
 /   Tests     \
/---------------\
/                 \
/   Unit Tests     \ Test individual functions (60-70%)
/                   \
/-------------------\
```

**Unit tests**: Fast, isolated, single responsibility
**Integration tests**: Cross-service, realistic conditions
**E2E tests**: Full workflows, slow but high confidence

### 3. Write the Test

Use the test template and follow the arrange-act-assert pattern:

```java
@Test
public void test_should_<expected_behavior>_when_<condition>() {
    // Arrange: set up test data and dependencies
    Order order = OrderFixture.createOrder();
    OrderService service = new OrderService(mockRepository);
    
    // Act: perform the action
    Money total = service.calculateTotal(order);
    
    // Assert: verify the result
    assertEquals(Money.dollars(100), total);
}
```

### 4. Ensure Proper Isolation

Mock external dependencies to keep tests focused:

```java
@Test
public void test_should_retry_failed_payment() {
    // Arrange: mock the payment gateway
    PaymentGateway gateway = mock(PaymentGateway.class);
    when(gateway.charge(any(), any()))
        .thenThrow(new PaymentException("timeout"))
        .thenReturn(new PaymentResult("success"));
    
    OrderService service = new OrderService(gateway);
    
    // Act & Assert
    PaymentResult result = service.processPayment(order);
    verify(gateway, times(2)).charge(any(), any());
    assertTrue(result.isSuccessful());
}
```

### 5. Track Coverage

Run coverage reports and track trends:

```bash
# Java
mvn clean test jacoco:report
open target/site/jacoco/index.html

# Python
pytest --cov=src tests/
coverage html
open htmlcov/index.html
```

**Target**: ≥80% for business logic

## Test Templates

### Unit Test Template (Java)

```java
@Test
public void test_should_<behavior>_when_<condition>() {
    // Arrange
    // Act
    // Assert
}
```

### Integration Test Template (Java)

```java
@SpringBootTest
@ActiveProfiles("test")
public class ServiceIntegrationTest {
    
    @Autowired
    private ServiceUnderTest service;
    
    @Autowired
    private RepositoryUnderTest repository;
    
    @BeforeEach
    public void setUp() {
        repository.deleteAll();
    }
    
    @Test
    public void test_should_<behavior>() {
        // Arrange
        // Act
        // Assert
    }
}
```

### Unit Test Template (Python)

```python
def test_should_<behavior>_when_<condition>():
    # Arrange
    # Act
    # Assert
```

## Fixture Patterns

### Factory Pattern

```java
public class OrderFixture {
    public static Order createOrder() {
        return Order.builder()
            .customerId("CUST-123")
            .total(Money.dollars(100))
            .status(OrderStatus.PENDING)
            .build();
    }
    
    public static Order createOrderWithItems(int count) {
        Order order = createOrder();
        order.setItems(createItems(count));
        return order;
    }
}

// Usage
Order order = OrderFixture.createOrder();
```

### Builder Pattern

```java
Order order = Order.builder()
    .customerId("CUST-456")
    .total(Money.dollars(200))
    .status(OrderStatus.SHIPPED)
    .build();
```

### Parameterized Tests

```java
@ParameterizedTest
@CsvSource({
    "1, 1",
    "2, 2",
    "10, 10",
    "100, 100"
})
public void test_should_calculate_fee(int distance, int expectedFee) {
    Shipment shipment = new Shipment("A", "B", distance * 1_000_000);
    Money fee = calculator.calculateFee(shipment);
    assertEquals(Money.dollars(expectedFee), fee);
}
```

## Mocking and Stubbing

### Mocking: Verify Behavior

```java
@Test
public void test_should_log_order_creation() {
    // Arrange
    Logger logger = mock(Logger.class);
    OrderService service = new OrderService(logger);
    
    // Act
    service.createOrder(order);
    
    // Assert: verify the method was called
    verify(logger).info("Order created: " + order.getId());
}
```

### Stubbing: Return a Value

```java
@Test
public void test_should_use_cached_result() {
    // Arrange
    Cache cache = mock(Cache.class);
    when(cache.get("CUST-123")).thenReturn(customer);
    
    // Act
    Customer result = cache.get("CUST-123");
    
    // Assert
    assertEquals(customer, result);
}
```

### Test Containers: Real Database

```java
@SpringBootTest
@Testcontainers
public class RepositoryTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");
    
    @Autowired
    private OrderRepository repository;
    
    @Test
    public void test_should_persist_order() {
        Order order = OrderFixture.createOrder();
        repository.save(order);
        
        Order saved = repository.findById(order.getId()).orElseThrow();
        assertEquals(order.getId(), saved.getId());
    }
}
```

## Coverage Targets

### By Code Type

| Code Type | Target Coverage | Examples |
|-----------|-----------------|----------|
| Business logic | ≥80% | Calculations, validations, workflows |
| Service layer | ≥75% | Data transformations, orchestration |
| API controllers | ≥70% | Request/response handling |
| Data access | ≥70% | Repository queries (if custom) |
| Infrastructure | ≥50% | Configuration, logging |
| Utilities | ≥70% | Helper functions |

### By Test Type

| Test Type | % of Total | Speed |
|-----------|-----------|-------|
| Unit | 60-70% | < 100ms each |
| Integration | 20-30% | 100ms - 1s each |
| E2E | 5-10% | > 1s each |

## CI/CD Integration

### Run Tests Locally

```bash
# All tests
mvn clean test
pytest

# Specific test file
mvn test -Dtest=OrderServiceTest
pytest tests/test_order_service.py

# With coverage
mvn clean test jacoco:report
pytest --cov=src tests/ --cov-report=html
```

### Automated Test Execution in CI

```yaml
test:
  script:
    - mvn clean test          # Unit tests first (fast)
    - mvn verify              # Integration tests
    - mvn jacoco:report       # Coverage report
  coverage: '/Coverage: \d+\.\d+%/'
```

**Fast feedback**: Run unit tests first, integration tests second, E2E last.

## Troubleshooting

### Test is Flaky (Sometimes Passes, Sometimes Fails)

Causes:
- Timing issues (Thread.sleep, eventual consistency)
- Shared state (tests interfering with each other)
- External dependencies (network, file system)

Solutions:
```java
// ❌ Don't use Thread.sleep
Thread.sleep(1000);

// ✅ Use polling with timeout
Awaitility.await()
    .atMost(Duration.ofSeconds(5))
    .until(() -> cache.contains(key));

// ✅ Isolate tests with @BeforeEach
@BeforeEach
public void setUp() {
    // Clean database
    repository.deleteAll();
}
```

### Test is Too Slow

Causes:
- Connecting to real database for unit test
- Network calls in tests
- Inefficient assertions (checking all objects instead of key fields)

Solutions:
```java
// ✅ Mock external dependencies for unit tests
PaymentGateway gateway = mock(PaymentGateway.class);
when(gateway.charge(any())).thenReturn(result);

// ✅ Use assertThat for focused assertions
assertThat(order)
    .hasFieldOrPropertyWithValue("status", OrderStatus.SHIPPED)
    .hasFieldOrPropertyWithValue("totalPrice", Money.dollars(100));

// ✅ Parallelize tests in CI
mvn test -T 1C  # 1 thread per core
```

### Test Coverage is Low

Steps to improve:
1. Run coverage report and identify gaps: `mvn jacoco:report`
2. Add tests for uncovered branches
3. Prioritize business logic and edge cases first
4. Don't obsess over 100% coverage (diminishing returns)

---

## Related Standards

- **Testing standards**: See [testing.instructions.md](../../instructions/testing.instructions.md)
- **Code style**: See [development.instructions.md](../../instructions/development.instructions.md)
- **Code review**: See [code-review.instructions.md](../../instructions/code-review.instructions.md)

---

Questions? Ask `@Tester` for help with test design and strategy.
