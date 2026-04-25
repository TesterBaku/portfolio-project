package com.logistics.core.orders.service;

import com.logistics.core.orders.api.CreateOrderRequest;
import com.logistics.core.orders.domain.Order;
import com.logistics.core.orders.domain.OrderStatus;
import com.logistics.core.orders.persistence.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order createOrder(CreateOrderRequest request) {
        Order order = new Order(UUID.randomUUID(), request.customerName(), OrderStatus.CREATED);
        return orderRepository.save(order);
    }

    public List<Order> listOrders() {
        return orderRepository.findAll();
    }
}
