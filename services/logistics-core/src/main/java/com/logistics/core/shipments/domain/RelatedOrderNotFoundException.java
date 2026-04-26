package com.logistics.core.shipments.domain;

import java.util.UUID;

/**
 * Thrown when a related order for a shipment cannot be found.
 * This exception indicates a cross-aggregate assembly failure: the shipment exists
 * but its parent order does not. This is typically a data consistency error.
 * Should be caught by the controller to map to HTTP 404.
 */
public class RelatedOrderNotFoundException extends RuntimeException {

    public RelatedOrderNotFoundException(UUID orderId) {
        super("Related order not found: " + orderId);
    }

    public RelatedOrderNotFoundException(UUID shipmentId, UUID orderId) {
        super("Related order not found for shipment: " + shipmentId + ", order ID: " + orderId);
    }

    public RelatedOrderNotFoundException(String message) {
        super(message);
    }
}
