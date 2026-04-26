package com.logistics.core.shipments.api;

import java.time.Instant;

import com.logistics.core.shipments.domain.ShipmentStatus;

/**
 * Represents a single tracking event in shipment history.
 * Timestamps are serialized as ISO-8601 formatted strings (Instant).
 */
public record TrackingEventResponse(
    ShipmentStatus status,
    Instant timestamp
) {
}
