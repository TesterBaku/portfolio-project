package com.logistics.core.orders.service;

import com.logistics.core.orders.api.CreateOrderRequest;
import com.logistics.core.orders.domain.Order;
import com.logistics.core.orders.domain.OrderStatus;
import com.logistics.core.orders.persistence.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository);
    }

    @Test
    void test_should_create_order_with_created_status() {
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order created = orderService.createOrder(new CreateOrderRequest("ACME Corp"));

        assertNotNull(created.getId());
        assertEquals("ACME Corp", created.getCustomerName());
        assertEquals(OrderStatus.CREATED, created.getStatus());
    }
}
