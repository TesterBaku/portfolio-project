---
name: "Testing Instructions"
description: "Standards for test design, coverage, and automation. Use when: writing tests, setting up CI/CD, or defining quality gates."
applyTo: ["**/*test*.py", "**/*Test.java", "**/*.spec.ts", "**/*.test.ts"]
---

# Testing Standards

This document defines how we approach testing at all levels: unit, integration, and end-to-end.

## Testing Pyramid

```
        /\
       /E2E\           Slow, comprehensive, UI-focused
      /-----\          Few tests (~10% of total)
     /       \
    /         \
   /-----------\
  /Integration \       Medium speed, cross-service boundaries
 /   Tests     \       Moderate (~20-30% of total)
/---------------\
/                 \
/   Unit Tests     \   Fast, isolated, logic-focused
/                   \  Many (~60-70% of total)
/-------------------\
```

## Coverage Targets

- **Business Logic**: ≥80% code coverage
- **Service Boundaries**: ≥70% integration test coverage
- **Critical User Paths**: E2E tests for all major workflows
- **Overall Project**: ≥75% coverage across all code

Track coverage in CI/CD and fail builds that reduce coverage.

## Unit Tests

### Purpose
Test individual functions, methods, or classes in isolation. Unit tests verify *logic*, not infrastructure.

### Characteristics
- ✅ Fast (< 100ms per test)
- ✅ Independent (no shared state)
- ✅ Deterministic (same input = same output every time)
- ✅ Isolated (mock external dependencies)

### Naming Convention

**Pattern**: `test_<description_of_what_should_happen>`

```java
// ✅ Good
public void test_should_reject_duplicate_shipment_numbers() { }
public void test_should_calculate_delivery_fee_based_on_distance() { }
public void test_should_throw_exception_when_customer_not_found() { }

// ❌ Avoid
public void test1() { }
public void testShipment() { }
public void test_shipment_creation() { } // ambiguous
```

### Test Structure (Arrange-Act-Assert)

```java
@Test
public void test_should_calculate_delivery_fee_for_short_distance() {
    // Arrange: set up test data
    Shipment shipment = new Shipment("NYC", "NJ", 50_000_000); // 50 meters
    DeliveryFeeCalculator calculator = new DeliveryFeeCalculator();
    
    // Act: call the method under test
    Money fee = calculator.calculateFee(shipment);
    
    // Assert: verify the result
    assertEquals(Money.dollars(5), fee);
}
```

### Mocking and Stubbing

- **Mock**: Replace a dependency with a test double to control its behavior
- **Stub**: Provide hardcoded responses for external calls

```java
@Test
public void test_should_retry_failed_payment() {
    // Arrange: mock the payment gateway to simulate failure
    PaymentGateway gateway = mock(PaymentGateway.class);
    when(gateway.charge(any(), any()))
        .thenThrow(new PaymentException("Connection timeout"))
        .thenReturn(new PaymentResult("success"));
    
    OrderService service = new OrderService(gateway);
    
    // Act: attempt to charge (will retry)
    PaymentResult result = service.processPayment(order);
    
    // Assert: verify retry happened
    verify(gateway, times(2)).charge(any(), any());
    assertTrue(result.isSuccessful());
}
```

### Don't Unit Test

- ❌ External APIs or services (mock these)
- ❌ Database queries (test with integration tests)
- ❌ Complex third-party libraries (test integration, not their internals)
- ❌ Infrastructure code (containers, servers, etc.)

## Integration Tests

### Purpose
Test the interaction between multiple components or services. Integration tests verify that *components work together*.

### Characteristics
- ✅ Medium speed (100ms - 1s per test)
- ✅ May use real databases, queues, or external services
- ✅ Focus on service boundaries and workflows
- ✅ Clear setup and teardown

### Naming Convention

**Pattern**: `test_<service>_should_<behavior>`

```java
// ✅ Good
public void test_order_service_should_persist_shipment_to_database() { }
public void test_payment_service_should_notify_customer_on_success() { }
public void test_inventory_service_should_reject_oversold_items() { }
```

### Test Setup

```java
@SpringBootTest
@ActiveProfiles("test")
public class OrderServiceIntegrationTest {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @BeforeEach
    public void setUp() {
        // Clear database before each test
        orderRepository.deleteAll();
    }
    
    @Test
    public void test_order_service_should_persist_shipment() {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest("ACME Inc", ...);
        
        // Act
        Order order = orderService.createOrder(request);
        
        // Assert: verify it was saved to the database
        Order saved = orderRepository.findById(order.getId()).orElse(null);
        assertNotNull(saved);
        assertEquals("ACME Inc", saved.getCustomer());
    }
}
```

### Database Testing

Use test containers or in-memory databases:

```java
@SpringBootTest
@Testcontainers
public class ShipmentRepositoryTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
        .withDatabaseName("logistics_test");
    
    @Autowired
    private ShipmentRepository shipmentRepository;
    
    @Test
    public void test_should_find_active_shipments() {
        // Arrange
        Shipment shipment1 = Shipment.builder().status(ACTIVE).build();
        Shipment shipment2 = Shipment.builder().status(DELIVERED).build();
        shipmentRepository.saveAll(List.of(shipment1, shipment2));
        
        // Act
        List<Shipment> active = shipmentRepository.findByStatus(ACTIVE);
        
        // Assert
        assertEquals(1, active.size());
        assertEquals(shipment1.getId(), active.get(0).getId());
    }
}
```

## End-to-End (E2E) Tests

### Purpose
Test complete user workflows from API request to response (or UI action to result). E2E tests verify *the system works as a whole*.

### Characteristics
- ⚠️ Slow (> 1s per test)
- ⚠️ Expensive to run
- ✅ Test real user scenarios
- ✅ Catch integration issues between services

### When to Use E2E Tests

Only for critical user paths:
- ✅ Create an order (core flow)
- ✅ Update shipment status
- ✅ Process payment
- ❌ List all fields in a dropdown (unit test this instead)
- ❌ Edge cases (use unit tests)

### Test Count

- Unit tests: 60-70% of total
- Integration tests: 20-30% of total
- E2E tests: 5-10% of total

### Example E2E Test

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrderApiE2ETest {
    
    @LocalServerPort
    private int port;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    public void test_should_create_order_and_retrieve_status() {
        // Arrange
        String baseUrl = "http://localhost:" + port;
        CreateOrderRequest request = new CreateOrderRequest("ACME Inc", ...);
        
        // Act: Create order
        ResponseEntity<OrderDTO> createResponse = restTemplate.postForEntity(
            baseUrl + "/api/orders",
            request,
            OrderDTO.class
        );
        
        // Assert: Order was created
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        OrderDTO created = createResponse.getBody();
        assertNotNull(created.getId());
        
        // Act: Retrieve the order
        ResponseEntity<OrderDTO> getResponse = restTemplate.getForEntity(
            baseUrl + "/api/orders/" + created.getId(),
            OrderDTO.class
        );
        
        // Assert: Order can be retrieved
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        OrderDTO retrieved = getResponse.getBody();
        assertEquals(created.getId(), retrieved.getId());
    }
}
```

## Performance Tests

### Purpose
Verify that critical operations meet performance baselines.

### Characteristics
- Load test: Measure system behavior under high load
- Stress test: Measure breaking point
- Baseline test: Ensure code changes don't regress performance

### Example Performance Test

```java
@Test
public void test_should_retrieve_shipment_within_100ms() {
    // Arrange: create 1000 shipments
    List<Shipment> shipments = IntStream.range(0, 1000)
        .mapToObj(i -> Shipment.builder().trackingNumber("TRACK-" + i).build())
        .collect(Collectors.toList());
    shipmentRepository.saveAll(shipments);
    
    // Act: measure retrieval time
    long startTime = System.currentTimeMillis();
    Shipment found = shipmentRepository.findByTrackingNumber("TRACK-500");
    long duration = System.currentTimeMillis() - startTime;
    
    // Assert: must complete within baseline
    assertTrue(duration < 100, "Query took " + duration + "ms, expected < 100ms");
    assertNotNull(found);
}
```

## Security Tests

### Purpose
Verify that security controls are in place.

### Examples

```java
@Test
public void test_should_reject_unauthorized_access() {
    // Arrange: attempt to access endpoint without auth
    
    // Act & Assert
    ResponseEntity<ErrorDTO> response = restTemplate.getForEntity(
        baseUrl + "/api/admin/orders",
        ErrorDTO.class
    );
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
}

@Test
public void test_should_reject_sql_injection() {
    // Arrange
    String maliciousInput = "' OR '1'='1";
    
    // Act & Assert: query should not return unauthorized records
    List<Order> orders = orderRepository.findByCustomer(maliciousInput);
    assertTrue(orders.isEmpty());
}
```

## Test Data and Fixtures

### Factory Pattern

```java
public class OrderFixture {
    public static Order createOrder() {
        return Order.builder()
            .customerId("CUST-123")
            .total(Money.dollars(100))
            .status(OrderStatus.PENDING)
            .createdAt(Instant.now())
            .build();
    }
    
    public static Order createOrderWithItems(int itemCount) {
        Order order = createOrder();
        List<OrderItem> items = IntStream.range(0, itemCount)
            .mapToObj(i -> OrderItem.builder()
                .sku("SKU-" + i)
                .quantity(1)
                .price(Money.dollars(10))
                .build())
            .collect(Collectors.toList());
        order.setItems(items);
        return order;
    }
}

// Usage in tests
@Test
public void test_should_calculate_total() {
    Order order = OrderFixture.createOrderWithItems(3);
    assertEquals(Money.dollars(30), order.calculateTotal());
}
```

## CI/CD Integration

### Automated Test Execution

```yaml
# Example: GitHub Actions or GitLab CI
test:
  stage: test
  script:
    - mvn clean test
    - mvn verify # includes integration tests
    - mvn jacoco:report # code coverage
  coverage: '/Code Coverage: \d+\.\d+%/'
  artifacts:
    reports:
      coverage_report:
        coverage_format: cobertura
        path: target/site/jacoco/coverage.xml
```

### Fail Fast

- Unit tests run first (fastest)
- If any unit test fails, stop the pipeline
- Integration tests run next
- Only run E2E tests if earlier stages pass

### Test Timeouts

- Unit tests: 100ms timeout per test
- Integration tests: 2s timeout per test
- E2E tests: 10s timeout per test
- If a test regularly times out, it's too slow

## Common Testing Pitfalls

❌ **Don't**:
- Write tests that depend on execution order
- Use `Thread.sleep()` for synchronization
- Test implementation details (test behavior instead)
- Create brittle tests that fail on minor code changes
- Write tests without assertions

✅ **Do**:
- Write independent, deterministic tests
- Use mocks/stubs for external dependencies
- Test observable behavior
- Keep tests simple and focused
- Always assert something meaningful

---

## Tools and Frameworks

- **Java**: JUnit 5, Mockito, AssertJ, TestContainers
- **Python**: Pytest, Unittest, Mocking, Faker
- **JavaScript/TypeScript**: Jest, Mocha, Chai, Sinon
- **Coverage**: JaCoCo (Java), Coverage.py (Python)
- **Performance**: JMH (Java), Locust (Python)

## Questions?

If testing standards are unclear, open an issue to discuss and refine them.
