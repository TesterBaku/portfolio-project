package com.logistics.core.shipments.domain;

import java.util.UUID;

/**
 * Thrown when a shipment cannot be found by the requested identifier.
 * This exception is thrown at the domain layer to indicate a missing shipment
 * and should be caught by the controller to map to HTTP 404.
 */
public class ShipmentNotFoundException extends RuntimeException {

    public ShipmentNotFoundException(UUID shipmentId) {
        super("Shipment not found: " + shipmentId);
    }

    public ShipmentNotFoundException(UUID shipmentId, UUID orderId) {
        super("Shipment not found for shipment ID: " + shipmentId + ", order ID: " + orderId);
    }

    public ShipmentNotFoundException(String message) {
        super(message);
    }
}
