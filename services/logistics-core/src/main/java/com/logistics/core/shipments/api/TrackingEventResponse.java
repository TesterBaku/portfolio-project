package com.logistics.core.shipments.api;

import java.time.Instant;

import com.logistics.core.shipments.domain.ShipmentStatus;

public record TrackingEventResponse(
    ShipmentStatus status,
    Instant timestamp
) {
}
