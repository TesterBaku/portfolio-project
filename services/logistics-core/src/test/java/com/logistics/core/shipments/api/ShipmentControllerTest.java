package com.logistics.core.shipments.api;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.logistics.core.shipments.domain.Shipment;
import com.logistics.core.shipments.domain.ShipmentStatus;
import com.logistics.core.shipments.service.ShipmentService;

@WebMvcTest(ShipmentController.class)
class ShipmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShipmentService shipmentService;

    @Test
    void test_should_create_shipment_for_order() throws Exception {
        UUID orderId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID shipmentId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        Shipment shipment = shipment(shipmentId, orderId, "Baku", "Ganja");

        when(shipmentService.createShipment(eq(orderId), eq("Baku"), eq("Ganja"))).thenReturn(shipment);

        mockMvc.perform(post("/api/orders/{orderId}/shipments", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "origin": "Baku",
                      "destination": "Ganja"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(shipmentId.toString()))
            .andExpect(jsonPath("$.orderId").value(orderId.toString()))
            .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void test_should_list_shipments_for_order() throws Exception {
        UUID orderId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        Shipment shipment = shipment(
            UUID.fromString("22222222-2222-2222-2222-222222222222"),
            orderId,
            "Baku",
            "Ganja"
        );

        when(shipmentService.listShipmentsByOrderId(orderId)).thenReturn(List.of(shipment));

        mockMvc.perform(get("/api/orders/{orderId}/shipments", orderId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(shipment.getId().toString()))
            .andExpect(jsonPath("$[0].orderId").value(orderId.toString()))
            .andExpect(jsonPath("$[0].origin").value("Baku"))
            .andExpect(jsonPath("$[0].destination").value("Ganja"));
    }

    private Shipment shipment(UUID shipmentId, UUID orderId, String origin, String destination) {
        Shipment shipment = new Shipment(shipmentId, orderId, origin, destination, ShipmentStatus.CREATED);
        shipment.setId(shipmentId);
        shipment.setOrderId(orderId);
        shipment.setOrigin(origin);
        shipment.setDestination(destination);
        shipment.setStatus(ShipmentStatus.CREATED);
        return shipment;
    }
}