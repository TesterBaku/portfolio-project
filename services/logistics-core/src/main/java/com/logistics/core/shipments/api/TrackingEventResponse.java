package com.logistics.core.shipments.api;

import java.time.LocalDateTime;

import com.logistics.core.shipments.domain.ShipmentStatus;

public record TrackingEventResponse(
    ShipmentStatus status,
    LocalDateTime timestamp
) {
}
