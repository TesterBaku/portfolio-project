package com.logistics.core.shipments.api;

import java.time.LocalDateTime;
import java.util.UUID;

import com.logistics.core.shipments.domain.Shipment;
import com.logistics.core.shipments.domain.ShipmentStatus;

public record ShipmentResponse(
    UUID id,
    UUID orderId,
    String origin,
    String destination,
    ShipmentStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static ShipmentResponse from(Shipment shipment) {
        return new ShipmentResponse(
            shipment.getId(),
            shipment.getOrderId(),
            shipment.getOrigin(),
            shipment.getDestination(),
            shipment.getStatus(),
            shipment.getCreatedAt(),
            shipment.getUpdatedAt()
        );
    }
}