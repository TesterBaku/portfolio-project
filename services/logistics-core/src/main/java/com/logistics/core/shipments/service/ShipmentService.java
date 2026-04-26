package com.logistics.core.shipments.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.logistics.core.assistant.contract.AiExceptionSummaryRequest;
import com.logistics.core.assistant.contract.AiExceptionSummaryResponse;
import com.logistics.core.assistant.contract.AssistantSummaryClient;
import com.logistics.core.orders.domain.Order;
import com.logistics.core.orders.persistence.OrderRepository;
import com.logistics.core.shipments.api.ShipmentTrackingResponse;
import com.logistics.core.shipments.domain.RelatedOrderNotFoundException;
import com.logistics.core.shipments.domain.Shipment;
import com.logistics.core.shipments.domain.ShipmentNotFoundException;
import com.logistics.core.shipments.domain.ShipmentStatus;
import com.logistics.core.shipments.persistence.ShipmentRepository;

@Service
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;
    private final AssistantSummaryClient assistantSummaryClient;

    public ShipmentService(
            ShipmentRepository shipmentRepository,
            OrderRepository orderRepository,
            AssistantSummaryClient assistantSummaryClient
    ) {
        this.shipmentRepository = shipmentRepository;
        this.orderRepository = orderRepository;
        this.assistantSummaryClient = assistantSummaryClient;
    }

    public Shipment createShipment(UUID orderId, String origin, String destination) {
        Shipment shipment = new Shipment(
                UUID.randomUUID(),
                orderId,
                origin,
                destination,
                ShipmentStatus.CREATED
        );
        return shipmentRepository.save(shipment);
    }

    public List<Shipment> listShipmentsByOrderId(UUID orderId) {
        return shipmentRepository.findByOrderId(orderId);
    }

    public Shipment transitionShipmentStatus(UUID orderId, UUID shipmentId, ShipmentStatus targetStatus) {
        Shipment shipment = shipmentRepository.findByIdAndOrderId(shipmentId, orderId)
                .orElseThrow(() -> new NoSuchElementException("Shipment not found for order"));

        ShipmentStatus currentStatus = shipment.getStatus();
        if (!isTransitionAllowed(currentStatus, targetStatus)) {
            throw new IllegalStateException(
                    "Invalid shipment status transition: " + currentStatus + " -> " + targetStatus
            );
        }

        shipment.setStatus(targetStatus);
        return shipmentRepository.save(shipment);
    }

    public AiExceptionSummaryResponse summarizeShipmentException(
            UUID orderId,
            UUID shipmentId,
            String exceptionType,
            String operatorNotes
    ) {
        Shipment shipment = shipmentRepository.findByIdAndOrderId(shipmentId, orderId)
                .orElseThrow(() -> new NoSuchElementException("Shipment not found for order"));

        AiExceptionSummaryRequest request = new AiExceptionSummaryRequest(
                shipment.getId().toString(),
                exceptionType,
                shipment.getStatus().name(),
                operatorNotes == null ? "" : operatorNotes
        );

        return assistantSummaryClient.summarizeException(request);
    }

    public ShipmentTrackingResponse getShipmentTracking(UUID shipmentId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ShipmentNotFoundException(shipmentId));

        Order order = orderRepository.findById(shipment.getOrderId())
                .orElseThrow(() -> new RelatedOrderNotFoundException(shipment.getOrderId()));

        return ShipmentTrackingResponse.from(shipment, order.getCustomerName());
    }

    private boolean isTransitionAllowed(ShipmentStatus currentStatus, ShipmentStatus targetStatus) {
        if (currentStatus == targetStatus) {
            return true;
        }

        return switch (currentStatus) {
            case CREATED -> targetStatus == ShipmentStatus.IN_TRANSIT || targetStatus == ShipmentStatus.CANCELED;
            case IN_TRANSIT -> targetStatus == ShipmentStatus.DELIVERED
                    || targetStatus == ShipmentStatus.DELAYED
                    || targetStatus == ShipmentStatus.CANCELED;
            case DELAYED -> targetStatus == ShipmentStatus.IN_TRANSIT
                    || targetStatus == ShipmentStatus.DELIVERED
                    || targetStatus == ShipmentStatus.CANCELED;
            case DELIVERED, CANCELED -> false;
        };
    }
}