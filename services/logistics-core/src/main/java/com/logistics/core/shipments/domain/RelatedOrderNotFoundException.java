package com.logistics.core.shipments.domain;

import java.util.UUID;

public class RelatedOrderNotFoundException extends RuntimeException {

    public RelatedOrderNotFoundException(UUID orderId) {
        super("Related order not found: " + orderId);
    }
}
