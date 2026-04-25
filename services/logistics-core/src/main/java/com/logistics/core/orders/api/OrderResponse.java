package com.logistics.core.orders.api;

import com.logistics.core.orders.domain.Order;
import com.logistics.core.orders.domain.OrderStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderResponse(
    UUID id,
    String customerName,
    OrderStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
            order.getId(),
            order.getCustomerName(),
            order.getStatus(),
            order.getCreatedAt(),
            order.getUpdatedAt()
        );
    }
}
