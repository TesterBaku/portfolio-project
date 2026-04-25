package com.logistics.core.orders.api;

import jakarta.validation.constraints.NotBlank;

public record CreateOrderRequest(
    @NotBlank(message = "customerName is required")
    String customerName
) {
}
