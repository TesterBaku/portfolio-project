package com.logistics.core.shipments.service;

import com.logistics.core.shipments.domain.Shipment;
import com.logistics.core.shipments.domain.ShipmentStatus;
import com.logistics.core.shipments.persistence.ShipmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;

    public ShipmentService(ShipmentRepository shipmentRepository) {
        this.shipmentRepository = shipmentRepository;
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
}