package com.logistics.core.shipments.persistence;

import com.logistics.core.shipments.domain.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShipmentRepository extends JpaRepository<Shipment, UUID> {
    List<Shipment> findByOrderId(UUID orderId);

    Optional<Shipment> findByIdAndOrderId(UUID id, UUID orderId);
}
