package com.logistics.core.shipments.domain;

public class InvalidStatusTransitionException extends RuntimeException {

    public InvalidStatusTransitionException(ShipmentStatus from, ShipmentStatus to) {
        super("Invalid shipment status transition: " + from + " -> " + to);
    }
}
