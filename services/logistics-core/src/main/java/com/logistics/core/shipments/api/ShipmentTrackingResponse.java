package com.logistics.core.shipments.api;

import java.time.LocalDateTime;
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
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    List<TrackingEventResponse> events
) {
    public static ShipmentTrackingResponse from(Shipment shipment, String customerName) {
        // For now, create a single event representing current status
        TrackingEventResponse currentEvent = new TrackingEventResponse(
            shipment.getStatus(),
            shipment.getUpdatedAt()
        );

        return new ShipmentTrackingResponse(
            shipment.getId(),
            shipment.getOrderId(),
            customerName,
            shipment.getOrigin(),
            shipment.getDestination(),
            shipment.getStatus(),
            shipment.getCreatedAt(),
            shipment.getUpdatedAt(),
            List.of(currentEvent)
        );
    }
}
