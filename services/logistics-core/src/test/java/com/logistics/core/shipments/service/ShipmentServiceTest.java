package com.logistics.core.shipments.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.logistics.core.shipments.domain.Shipment;
import com.logistics.core.shipments.domain.ShipmentStatus;
import com.logistics.core.shipments.persistence.ShipmentRepository;

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

    @Test
    void test_should_transition_shipment_status_when_transition_is_allowed() {
        UUID orderId = UUID.randomUUID();
        UUID shipmentId = UUID.randomUUID();
        ShipmentService shipmentService = new ShipmentService(shipmentRepository);
        Shipment shipment = new Shipment(shipmentId, orderId, "Baku", "Ganja", ShipmentStatus.CREATED);

        when(shipmentRepository.findByIdAndOrderId(eq(shipmentId), eq(orderId))).thenReturn(Optional.of(shipment));
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Shipment updated = shipmentService.transitionShipmentStatus(orderId, shipmentId, ShipmentStatus.IN_TRANSIT);

        assertEquals(ShipmentStatus.IN_TRANSIT, updated.getStatus());
        verify(shipmentRepository).save(shipment);
    }

    @Test
    void test_should_throw_when_transition_is_not_allowed() {
        UUID orderId = UUID.randomUUID();
        UUID shipmentId = UUID.randomUUID();
        ShipmentService shipmentService = new ShipmentService(shipmentRepository);
        Shipment shipment = new Shipment(shipmentId, orderId, "Baku", "Ganja", ShipmentStatus.CREATED);

        when(shipmentRepository.findByIdAndOrderId(eq(shipmentId), eq(orderId))).thenReturn(Optional.of(shipment));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> shipmentService.transitionShipmentStatus(orderId, shipmentId, ShipmentStatus.DELIVERED)
        );

        assertEquals("Invalid shipment status transition: CREATED -> DELIVERED", ex.getMessage());
    }

    @Test
    void test_should_throw_when_shipment_not_found_for_order() {
        UUID orderId = UUID.randomUUID();
        UUID shipmentId = UUID.randomUUID();
        ShipmentService shipmentService = new ShipmentService(shipmentRepository);

        when(shipmentRepository.findByIdAndOrderId(eq(shipmentId), eq(orderId))).thenReturn(Optional.empty());

        NoSuchElementException ex = assertThrows(
                NoSuchElementException.class,
                () -> shipmentService.transitionShipmentStatus(orderId, shipmentId, ShipmentStatus.IN_TRANSIT)
        );

        assertEquals("Shipment not found for order", ex.getMessage());
    }
}