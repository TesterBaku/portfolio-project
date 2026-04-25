package com.logistics.core.shipments.api;

import com.logistics.core.shipments.domain.ShipmentStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateShipmentStatusRequest(
    @NotNull(message = "status is required")
    ShipmentStatus status
) {
}