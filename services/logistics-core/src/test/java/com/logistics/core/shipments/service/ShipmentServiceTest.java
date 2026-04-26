package com.logistics.core.shipments.service;

import java.util.List;
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
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.logistics.core.assistant.contract.AiExceptionSummaryRequest;
import com.logistics.core.assistant.contract.AiExceptionSummaryResponse;
import com.logistics.core.assistant.contract.AssistantSummaryClient;
import com.logistics.core.orders.domain.Order;
import com.logistics.core.orders.domain.OrderStatus;
import com.logistics.core.orders.persistence.OrderRepository;
import com.logistics.core.shipments.api.ShipmentTrackingResponse;
import com.logistics.core.shipments.domain.InvalidStatusTransitionException;
import com.logistics.core.shipments.domain.OrderNotFoundException;
import com.logistics.core.shipments.domain.RelatedOrderNotFoundException;
import com.logistics.core.shipments.domain.Shipment;
import com.logistics.core.shipments.domain.ShipmentNotFoundException;
import com.logistics.core.shipments.domain.ShipmentStatus;
import com.logistics.core.shipments.persistence.ShipmentRepository;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private AssistantSummaryClient assistantSummaryClient;

    @Mock
    private OrderRepository orderRepository;

    @Test
    void test_should_create_shipment_with_created_status() {
        UUID orderId = UUID.randomUUID();
        ShipmentService shipmentService = new ShipmentService(
            shipmentRepository,
            orderRepository,
            assistantSummaryClient
        );
        when(orderRepository.existsById(orderId)).thenReturn(true);
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Shipment created = shipmentService.createShipment(orderId, "Baku", "Ganja");

        assertNotNull(created.getId());
        assertEquals(orderId, created.getOrderId());
        assertEquals("Baku", created.getOrigin());
        assertEquals("Ganja", created.getDestination());
        assertEquals(ShipmentStatus.CREATED, created.getStatus());
    }

    @Test
    void test_should_throw_when_order_not_found_on_create() {
        UUID orderId = UUID.randomUUID();
        ShipmentService shipmentService = new ShipmentService(
            shipmentRepository,
            orderRepository,
            assistantSummaryClient
        );
        when(orderRepository.existsById(orderId)).thenReturn(false);

        OrderNotFoundException ex = assertThrows(
            OrderNotFoundException.class,
            () -> shipmentService.createShipment(orderId, "Baku", "Ganja")
        );

        assertEquals("Order not found: " + orderId, ex.getMessage());
    }

    @Test
    void test_should_list_shipments_by_order_id() {
        UUID orderId = UUID.randomUUID();
        ShipmentService shipmentService = new ShipmentService(
            shipmentRepository,
            orderRepository,
            assistantSummaryClient
        );
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
        ShipmentService shipmentService = new ShipmentService(
            shipmentRepository,
            orderRepository,
            assistantSummaryClient
        );
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
        ShipmentService shipmentService = new ShipmentService(
            shipmentRepository,
            orderRepository,
            assistantSummaryClient
        );
        Shipment shipment = new Shipment(shipmentId, orderId, "Baku", "Ganja", ShipmentStatus.CREATED);

        when(shipmentRepository.findByIdAndOrderId(eq(shipmentId), eq(orderId))).thenReturn(Optional.of(shipment));

        InvalidStatusTransitionException ex = assertThrows(
                InvalidStatusTransitionException.class,
                () -> shipmentService.transitionShipmentStatus(orderId, shipmentId, ShipmentStatus.DELIVERED)
        );

        assertEquals("Invalid shipment status transition: CREATED -> DELIVERED", ex.getMessage());
    }

    @Test
    void test_should_throw_when_shipment_not_found_for_order() {
        UUID orderId = UUID.randomUUID();
        UUID shipmentId = UUID.randomUUID();
        ShipmentService shipmentService = new ShipmentService(
            shipmentRepository,
            orderRepository,
            assistantSummaryClient
        );

        when(shipmentRepository.findByIdAndOrderId(eq(shipmentId), eq(orderId))).thenReturn(Optional.empty());

        ShipmentNotFoundException ex = assertThrows(
                ShipmentNotFoundException.class,
                () -> shipmentService.transitionShipmentStatus(orderId, shipmentId, ShipmentStatus.IN_TRANSIT)
        );

        assertEquals("Shipment not found: " + shipmentId, ex.getMessage());
    }

    @Test
    void test_should_summarize_shipment_exception_with_ai_contract() {
        UUID orderId = UUID.randomUUID();
        UUID shipmentId = UUID.randomUUID();
        ShipmentService shipmentService = new ShipmentService(
            shipmentRepository,
            orderRepository,
            assistantSummaryClient
        );
        Shipment shipment = new Shipment(shipmentId, orderId, "Baku", "Ganja", ShipmentStatus.DELAYED);
        AiExceptionSummaryResponse expected = new AiExceptionSummaryResponse(
                "Shipment SHIP-1 delayed",
                "Contact carrier and notify customer"
        );

        when(shipmentRepository.findByIdAndOrderId(eq(shipmentId), eq(orderId))).thenReturn(Optional.of(shipment));
        when(assistantSummaryClient.summarizeException(any(AiExceptionSummaryRequest.class))).thenReturn(expected);

        AiExceptionSummaryResponse actual = shipmentService.summarizeShipmentException(
                orderId,
                shipmentId,
                "WEATHER_DELAY",
                "Heavy rain near highway"
        );

        ArgumentCaptor<AiExceptionSummaryRequest> captor = ArgumentCaptor.forClass(AiExceptionSummaryRequest.class);
        verify(assistantSummaryClient).summarizeException(captor.capture());

        AiExceptionSummaryRequest sent = captor.getValue();
        assertEquals(shipmentId.toString(), sent.shipmentId());
        assertEquals("WEATHER_DELAY", sent.exceptionType());
        assertEquals("DELAYED", sent.latestStatus());
        assertEquals("Heavy rain near highway", sent.operatorNotes());
        assertEquals(expected, actual);
    }

    @Test
    void test_should_throw_when_summarize_shipment_not_found_for_order() {
        UUID orderId = UUID.randomUUID();
        UUID shipmentId = UUID.randomUUID();
        ShipmentService shipmentService = new ShipmentService(
            shipmentRepository,
            orderRepository,
            assistantSummaryClient
        );

        when(shipmentRepository.findByIdAndOrderId(eq(shipmentId), eq(orderId))).thenReturn(Optional.empty());

        ShipmentNotFoundException ex = assertThrows(
                ShipmentNotFoundException.class,
                () -> shipmentService.summarizeShipmentException(orderId, shipmentId, "WEATHER_DELAY", "")
        );

        assertEquals("Shipment not found: " + shipmentId, ex.getMessage());
    }

    @Test
    void test_should_get_shipment_tracking_success() {
        UUID orderId = UUID.randomUUID();
        UUID shipmentId = UUID.randomUUID();
        ShipmentService shipmentService = new ShipmentService(
                shipmentRepository,
                orderRepository,
                assistantSummaryClient
        );
        Shipment shipment = new Shipment(shipmentId, orderId, "Baku", "Ganja", ShipmentStatus.IN_TRANSIT);
        Order order = new Order(orderId, "ACME Corp", OrderStatus.IN_PROGRESS);

        when(shipmentRepository.findByIdAndOrderId(eq(shipmentId), eq(orderId))).thenReturn(Optional.of(shipment));
        when(orderRepository.findById(eq(orderId))).thenReturn(Optional.of(order));

        ShipmentTrackingResponse response = shipmentService.getShipmentTracking(orderId, shipmentId);

        assertEquals(shipmentId, response.shipmentId());
        assertEquals(orderId, response.orderId());
        assertEquals("ACME Corp", response.customerName());
        assertEquals("Baku", response.origin());
        assertEquals("Ganja", response.destination());
        assertEquals(ShipmentStatus.IN_TRANSIT, response.currentStatus());
        assertEquals(1, response.events().size());
        assertEquals(ShipmentStatus.IN_TRANSIT, response.events().get(0).status());
    }

    @Test
    void test_should_throw_when_shipment_not_found_for_tracking() {
        UUID orderId = UUID.randomUUID();
        UUID shipmentId = UUID.randomUUID();
        ShipmentService shipmentService = new ShipmentService(
                shipmentRepository,
                orderRepository,
                assistantSummaryClient
        );

        when(shipmentRepository.findByIdAndOrderId(eq(shipmentId), eq(orderId))).thenReturn(Optional.empty());

        ShipmentNotFoundException ex = assertThrows(
                ShipmentNotFoundException.class,
            () -> shipmentService.getShipmentTracking(orderId, shipmentId)
        );

        assertEquals("Shipment not found: " + shipmentId, ex.getMessage());
    }

    @Test
    void test_should_throw_when_related_order_not_found_for_tracking() {
        UUID shipmentId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        ShipmentService shipmentService = new ShipmentService(
                shipmentRepository,
                orderRepository,
                assistantSummaryClient
        );
        Shipment shipment = new Shipment(shipmentId, orderId, "Baku", "Ganja", ShipmentStatus.IN_TRANSIT);

        when(shipmentRepository.findByIdAndOrderId(eq(shipmentId), eq(orderId))).thenReturn(Optional.of(shipment));
        when(orderRepository.findById(eq(orderId))).thenReturn(Optional.empty());

        RelatedOrderNotFoundException ex = assertThrows(
                RelatedOrderNotFoundException.class,
            () -> shipmentService.getShipmentTracking(orderId, shipmentId)
        );

        assertEquals("Related order not found: " + orderId, ex.getMessage());
    }

        @Test
        void test_should_throw_when_shipment_does_not_belong_to_order_for_tracking() {
        UUID orderId = UUID.randomUUID();
        UUID shipmentId = UUID.randomUUID();
        ShipmentService shipmentService = new ShipmentService(
            shipmentRepository,
            orderRepository,
            assistantSummaryClient
        );

        when(shipmentRepository.findByIdAndOrderId(eq(shipmentId), eq(orderId))).thenReturn(Optional.empty());

        ShipmentNotFoundException ex = assertThrows(
            ShipmentNotFoundException.class,
            () -> shipmentService.getShipmentTracking(orderId, shipmentId)
        );

        assertEquals("Shipment not found: " + shipmentId, ex.getMessage());
        }
}