package com.logistics.core.shipments.api;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import com.logistics.core.shipments.domain.Shipment;
import com.logistics.core.shipments.domain.ShipmentStatus;

public record ShipmentTrackingResponse(
    UUID shipmentId,
    UUID orderId,
    String customerName,
    String origin,
    String destination,
    ShipmentStatus currentStatus,
    Instant createdAt,
    Instant updatedAt,
    List<TrackingEventResponse> events
) {
    public static ShipmentTrackingResponse from(Shipment shipment, String customerName) {
        Instant createdAt = toInstant(shipment.getCreatedAt());
        Instant updatedAt = toInstant(shipment.getUpdatedAt());

        TrackingEventResponse currentEvent = new TrackingEventResponse(shipment.getStatus(), updatedAt);

        return new ShipmentTrackingResponse(
            shipment.getId(),
            shipment.getOrderId(),
            customerName,
            shipment.getOrigin(),
            shipment.getDestination(),
            shipment.getStatus(),
            createdAt,
            updatedAt,
            List.of(currentEvent)
        );
    }

    private static Instant toInstant(LocalDateTime value) {
        if (value == null) {
            return null;
        }

        return value.atZone(ZoneId.systemDefault()).toInstant();
    }
}
