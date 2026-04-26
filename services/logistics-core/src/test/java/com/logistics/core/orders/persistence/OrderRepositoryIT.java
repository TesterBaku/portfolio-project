package com.logistics.core.orders.persistence;

import com.logistics.core.orders.domain.Order;
import com.logistics.core.orders.domain.OrderStatus;
import com.logistics.core.support.AbstractPostgresIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderRepositoryIT extends AbstractPostgresIT {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void test_should_persist_and_retrieve_order_by_id() {
        UUID id = UUID.randomUUID();
        Order order = new Order(id, "ACME Corp", OrderStatus.CREATED);
        orderRepository.save(order);

        Optional<Order> found = orderRepository.findById(id);

        assertThat(found).isPresent();
        assertThat(found.get().getCustomerName()).isEqualTo("ACME Corp");
        assertThat(found.get().getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(found.get().getCreatedAt()).isNotNull();
        assertThat(found.get().getUpdatedAt()).isNotNull();
    }

    @Test
    void test_should_list_all_persisted_orders() {
        orderRepository.deleteAll();

        orderRepository.save(new Order(UUID.randomUUID(), "Buyer A", OrderStatus.CREATED));
        orderRepository.save(new Order(UUID.randomUUID(), "Buyer B", OrderStatus.IN_PROGRESS));

        List<Order> orders = orderRepository.findAll();

        assertThat(orders).hasSize(2);
        assertThat(orders).extracting(Order::getCustomerName)
                .containsExactlyInAnyOrder("Buyer A", "Buyer B");
    }

    @Test
    void test_should_return_empty_when_order_not_found() {
        Optional<Order> found = orderRepository.findById(UUID.randomUUID());

        assertThat(found).isEmpty();
    }
}
