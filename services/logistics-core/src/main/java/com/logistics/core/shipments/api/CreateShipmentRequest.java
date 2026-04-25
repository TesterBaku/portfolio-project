package com.logistics.core.shipments.api;

import jakarta.validation.constraints.NotBlank;

public record CreateShipmentRequest(
    @NotBlank(message = "origin is required")
    String origin,
    @NotBlank(message = "destination is required")
    String destination
) {
}