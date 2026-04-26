package com.logistics.core.shipments.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.logistics.core.shipments.domain.Shipment;
import com.logistics.core.shipments.domain.ShipmentStatus;

class ShipmentTrackingResponseTest {

    @Test
    void test_should_map_local_date_time_to_utc_instant_for_tracking_response() {
        UUID shipmentId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        Shipment shipment = new Shipment(shipmentId, orderId, "Baku", "Ganja", ShipmentStatus.IN_TRANSIT);
        LocalDateTime createdAt = LocalDateTime.of(2026, 4, 25, 10, 0, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 4, 25, 11, 0, 0);

        ReflectionTestUtils.setField(shipment, "createdAt", createdAt);
        ReflectionTestUtils.setField(shipment, "updatedAt", updatedAt);

        ShipmentTrackingResponse response = ShipmentTrackingResponse.from(shipment, "ACME Corp");

        assertEquals(Instant.parse("2026-04-25T10:00:00Z"), response.createdAt());
        assertEquals(Instant.parse("2026-04-25T11:00:00Z"), response.updatedAt());
        assertEquals(Instant.parse("2026-04-25T11:00:00Z"), response.events().get(0).timestamp());
    }
}
