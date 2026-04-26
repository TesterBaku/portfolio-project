package com.logistics.core.shipments.api;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import com.logistics.core.shipments.domain.Shipment;
import com.logistics.core.shipments.domain.ShipmentStatus;

/**
 * Shipment tracking response combining shipment and order details.
 * This DTO represents cross-aggregate assembly: shipment data + customer name from order.
 * Timestamps are serialized as ISO-8601 formatted strings (Instant).
 */
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
        ZoneId systemZone = ZoneId.systemDefault();
        
        // For now, create a single event representing current status
        TrackingEventResponse currentEvent = new TrackingEventResponse(
            shipment.getStatus(),
            shipment.getUpdatedAt().atZone(systemZone).toInstant()
        );

        return new ShipmentTrackingResponse(
            shipment.getId(),
            shipment.getOrderId(),
            customerName,
            shipment.getOrigin(),
            shipment.getDestination(),
            shipment.getStatus(),
            shipment.getCreatedAt().atZone(systemZone).toInstant(),
            shipment.getUpdatedAt().atZone(systemZone).toInstant(),
            List.of(currentEvent)
        );
    }
}

