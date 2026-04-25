# Test Fixture Patterns

Reusable patterns for creating test data and mocking dependencies.

## Pattern 1: Factory Class

Create objects with sensible defaults for tests.

### When to Use
- Creating multiple test instances
- Complex object initialization
- Sharing test data across tests

### Example

```java
public class OrderFixture {
    // Default factory method
    public static Order createOrder() {
        return Order.builder()
            .id(UUID.randomUUID())
            .customerId("CUST-123")
            .status(OrderStatus.PENDING)
            .createdAt(Instant.now())
            .total(Money.dollars(100))
            .build();
    }
    
    // Variant: with custom field
    public static Order createOrder(OrderStatus status) {
        return createOrder().toBuilder()
            .status(status)
            .build();
    }
    
    // Variant: with items
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

// Usage
Order order = OrderFixture.createOrder();
Order shipped = OrderFixture.createOrder(OrderStatus.SHIPPED);
Order multi = OrderFixture.createOrderWithItems(5);
```

## Pattern 2: Builder Pattern

Fluent, readable test data construction.

### When to Use
- One-off test data
- Readable test setup
- Complex nested structures

### Example

```java
Order order = Order.builder()
    .id(UUID.randomUUID())
    .customerId("CUST-456")
    .status(OrderStatus.SHIPPED)
    .total(Money.dollars(250))
    .items(List.of(
        OrderItem.builder()
            .sku("SKU-ABC")
            .quantity(2)
            .price(Money.dollars(125))
            .build()
    ))
    .build();
```

## Pattern 3: Parameterized Tests

Test multiple scenarios with a single test method.

### When to Use
- Testing the same logic with different inputs
- Edge case coverage
- Reducing boilerplate

### Example

```java
@ParameterizedTest
@CsvSource({
    "0,    0",     // 0 items = $0
    "1,    10",    // 1 item @ $10
    "5,    50",    // 5 items @ $10 each
    "100,  1000",  // large quantity
})
public void test_should_calculate_total_for_items(int quantity, int expectedTotal) {
    OrderItem item = OrderItem.builder()
        .quantity(quantity)
        .price(Money.dollars(10))
        .build();
    
    Money total = item.calculateLineTotal();
    
    assertEquals(Money.dollars(expectedTotal), total);
}
```

### Example with Multiple Fields

```java
@ParameterizedTest
@CsvSource({
    "NYC,    NJ,      50000000,    5",
    "NYC,    Boston,  300000000,   20",
    "LA,     SF,      600000000,   40",
})
public void test_should_calculate_fee_by_distance(
        String origin, String destination, long distanceMeters, int expectedFee) {
    Shipment shipment = Shipment.builder()
        .origin(origin)
        .destination(destination)
        .distanceMeters(distanceMeters)
        .build();
    
    Money fee = calculator.calculateFee(shipment);
    assertEquals(Money.dollars(expectedFee), fee);
}
```

## Pattern 4: Mock Object

Control external dependencies in tests.

### When to Use
- Testing interactions with external systems
- Verifying method calls and arguments
- Simulating error conditions

### Example

```java
@Test
public void test_should_call_payment_gateway_with_order_total() {
    // Arrange
    PaymentGateway gateway = mock(PaymentGateway.class);
    when(gateway.charge(any(), any()))
        .thenReturn(new PaymentResult("success"));
    
    OrderService service = new OrderService(gateway);
    Order order = OrderFixture.createOrder();
    
    // Act
    service.processPayment(order);
    
    // Assert: verify the gateway was called with correct arguments
    verify(gateway).charge(
        eq(order.getCustomerId()),
        eq(order.getTotal())
    );
}
```

## Pattern 5: Spy Object

Monitor real objects while allowing them to behave normally.

### When to Use
- Verifying calls to real methods
- Partial mocking
- Wrapping real objects with monitoring

### Example

```java
@Test
public void test_should_log_shipment_status_changes() {
    // Arrange
    Shipment realShipment = OrderFixture.createShipment();
    Shipment spyShipment = spy(realShipment);
    
    // Act
    spyShipment.updateStatus(ShipmentStatus.DELIVERED);
    
    // Assert: verify the real method was called
    verify(spyShipment).updateStatus(ShipmentStatus.DELIVERED);
}
```

## Pattern 6: Test Containers

Use real external services (database, queue, etc.) in tests.

### When to Use
- Integration testing
- Testing with real database behavior
- Realistic test conditions

### Example

```java
@SpringBootTest
@Testcontainers
public class OrderRepositoryTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
        .withDatabaseName("logistics_test");
    
    @Autowired
    private OrderRepository repository;
    
    @Test
    public void test_should_find_orders_by_status() {
        // Arrange
        Order pending = OrderFixture.createOrder(OrderStatus.PENDING);
        Order shipped = OrderFixture.createOrder(OrderStatus.SHIPPED);
        repository.saveAll(List.of(pending, shipped));
        
        // Act
        List<Order> results = repository.findByStatus(OrderStatus.PENDING);
        
        // Assert
        assertEquals(1, results.size());
        assertEquals(pending.getId(), results.get(0).getId());
    }
}
```

## Pattern 7: Test Fixture with Setup/Teardown

Reusable setup for multiple tests.

### When to Use
- Complex setup shared by many tests
- Resource allocation and cleanup
- Consistent test environment

### Example

```java
@SpringBootTest
public class OrderServiceTest {
    
    @Autowired
    private OrderService service;
    
    @Autowired
    private OrderRepository repository;
    
    private Order testOrder;
    private Customer testCustomer;
    
    @BeforeEach
    public void setUp() {
        // Clean database
        repository.deleteAll();
        
        // Create shared test data
        testCustomer = CustomerFixture.createCustomer("ACME Inc");
        testOrder = OrderFixture.createOrder()
            .toBuilder()
            .customerId(testCustomer.getId())
            .build();
    }
    
    @AfterEach
    public void tearDown() {
        // Cleanup if needed (often not necessary with @Transactional)
        repository.deleteAll();
    }
    
    @Test
    public void test_should_create_order_for_customer() {
        service.createOrder(testOrder);
        
        List<Order> orders = repository.findByCustomerId(testCustomer.getId());
        assertEquals(1, orders.size());
    }
}
```

## Pattern 8: Faker for Random Data

Generate realistic but varied test data.

### When to Use
- Property-based testing
- Stress testing
- Data variability

### Example

```java
@Test
public void test_should_handle_various_customer_names() {
    Faker faker = new Faker();
    
    for (int i = 0; i < 100; i++) {
        String customerName = faker.company().name();
        Order order = OrderFixture.createOrder()
            .toBuilder()
            .customerName(customerName)
            .build();
        
        service.validateOrder(order);  // Should not throw
    }
}
```

## Best Practices

✅ **Do**:
- Create factories for complex objects
- Use builders for readability
- Name fixtures clearly (`createValidOrder`, `createExpiredShipment`)
- Keep fixtures lightweight
- Share fixtures across related tests

❌ **Don't**:
- Create test data with unnecessary fields
- Use real IDs or secrets in fixtures
- Make fixtures too clever or magic
- Create interdependent test data
- Modify shared fixtures in tests

## Anti-Patterns to Avoid

🚩 **God Fixture**: Fixture used for every test, doesn't match test needs
🚩 **Brittle Fixture**: Small change to logic breaks many tests
🚩 **Hidden Dependencies**: Fixtures secretly depend on order of execution
🚩 **Over-Specification**: Fixture has 20 fields but test only uses 2

---

## Quick Reference

| Pattern | Use When | Complexity |
|---------|----------|-----------|
| Factory | Multiple similar objects | Low |
| Builder | Complex single object | Low-Medium |
| Parameterized | Multiple scenarios, same logic | Low |
| Mock | Replacing external dependency | Medium |
| Spy | Monitoring real object | Medium |
| Containers | Testing with real infra | High |
| Fixture Class | Shared setup | Medium |
| Faker | Random realistic data | Low-Medium |
