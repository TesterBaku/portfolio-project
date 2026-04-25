package com.logistics.core.shipments.service;

import com.logistics.core.shipments.domain.Shipment;
import com.logistics.core.shipments.domain.ShipmentStatus;
import com.logistics.core.shipments.persistence.ShipmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    @Test
    void test_should_create_shipment_with_created_status() {
        UUID orderId = UUID.randomUUID();
        ShipmentService shipmentService = new ShipmentService(shipmentRepository);
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Shipment created = shipmentService.createShipment(orderId, "Baku", "Ganja");

        assertNotNull(created.getId());
        assertEquals(orderId, created.getOrderId());
        assertEquals("Baku", created.getOrigin());
        assertEquals("Ganja", created.getDestination());
        assertEquals(ShipmentStatus.CREATED, created.getStatus());
    }

    @Test
    void test_should_list_shipments_by_order_id() {
        UUID orderId = UUID.randomUUID();
        ShipmentService shipmentService = new ShipmentService(shipmentRepository);
        List<Shipment> expected = List.of(
                new Shipment(UUID.randomUUID(), orderId, "Baku", "Ganja", ShipmentStatus.CREATED)
        );
        when(shipmentRepository.findByOrderId(orderId)).thenReturn(expected);

        List<Shipment> actual = shipmentService.listShipmentsByOrderId(orderId);

        assertEquals(expected, actual);
        verify(shipmentRepository).findByOrderId(orderId);
    }
}