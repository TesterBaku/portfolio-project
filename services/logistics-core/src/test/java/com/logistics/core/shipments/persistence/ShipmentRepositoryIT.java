package com.logistics.core.shipments.persistence;

import com.logistics.core.shipments.domain.Shipment;
import com.logistics.core.shipments.domain.ShipmentStatus;
import com.logistics.core.support.AbstractPostgresIT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ShipmentRepositoryIT extends AbstractPostgresIT {

    @Autowired
    private ShipmentRepository shipmentRepository;

    private final UUID orderId = UUID.randomUUID();
    private final UUID otherOrderId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        shipmentRepository.deleteAll();
    }

    @Test
    void test_should_persist_and_retrieve_shipment_by_id() {
        UUID id = UUID.randomUUID();
        Shipment shipment = new Shipment(id, orderId, "NYC", "LAX", ShipmentStatus.CREATED);
        shipmentRepository.save(shipment);

        Optional<Shipment> found = shipmentRepository.findById(id);

        assertThat(found).isPresent();
        assertThat(found.get().getOrderId()).isEqualTo(orderId);
        assertThat(found.get().getOrigin()).isEqualTo("NYC");
        assertThat(found.get().getDestination()).isEqualTo("LAX");
        assertThat(found.get().getStatus()).isEqualTo(ShipmentStatus.CREATED);
        assertThat(found.get().getCreatedAt()).isNotNull();
        assertThat(found.get().getUpdatedAt()).isNotNull();
    }

    @Test
    void test_should_find_all_shipments_by_order_id() {
        shipmentRepository.save(new Shipment(UUID.randomUUID(), orderId, "NYC", "LAX", ShipmentStatus.CREATED));
        shipmentRepository.save(new Shipment(UUID.randomUUID(), orderId, "SFO", "ORD", ShipmentStatus.IN_TRANSIT));
        shipmentRepository.save(new Shipment(UUID.randomUUID(), otherOrderId, "BOS", "MIA", ShipmentStatus.CREATED));

        List<Shipment> results = shipmentRepository.findByOrderId(orderId);

        assertThat(results).hasSize(2);
        assertThat(results).extracting(Shipment::getOrderId)
                .containsOnly(orderId);
    }

    @Test
    void test_should_return_empty_list_when_no_shipments_for_order() {
        List<Shipment> results = shipmentRepository.findByOrderId(UUID.randomUUID());

        assertThat(results).isEmpty();
    }

    @Test
    void test_should_find_shipment_by_id_and_order_id() {
        UUID shipmentId = UUID.randomUUID();
        shipmentRepository.save(new Shipment(shipmentId, orderId, "NYC", "LAX", ShipmentStatus.CREATED));

        Optional<Shipment> found = shipmentRepository.findByIdAndOrderId(shipmentId, orderId);

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(shipmentId);
    }

    @Test
    void test_should_return_empty_when_shipment_belongs_to_different_order() {
        UUID shipmentId = UUID.randomUUID();
        shipmentRepository.save(new Shipment(shipmentId, orderId, "NYC", "LAX", ShipmentStatus.CREATED));

        Optional<Shipment> found = shipmentRepository.findByIdAndOrderId(shipmentId, otherOrderId);

        assertThat(found).isEmpty();
    }

    @Test
    void test_should_persist_status_update() {
        UUID shipmentId = UUID.randomUUID();
        Shipment shipment = new Shipment(shipmentId, orderId, "NYC", "LAX", ShipmentStatus.CREATED);
        shipmentRepository.save(shipment);

        shipment.setStatus(ShipmentStatus.IN_TRANSIT);
        shipmentRepository.save(shipment);

        Shipment updated = shipmentRepository.findById(shipmentId).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(ShipmentStatus.IN_TRANSIT);
    }
}
