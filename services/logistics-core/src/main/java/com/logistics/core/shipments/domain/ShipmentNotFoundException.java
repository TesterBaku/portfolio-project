package com.logistics.core.shipments.domain;

import java.util.UUID;

public class ShipmentNotFoundException extends RuntimeException {

    public ShipmentNotFoundException(UUID shipmentId) {
        super("Shipment not found: " + shipmentId);
    }

    public ShipmentNotFoundException(String message) {
        super(message);
    }
}
