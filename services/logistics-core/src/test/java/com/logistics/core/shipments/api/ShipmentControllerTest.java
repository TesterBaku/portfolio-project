package com.logistics.core.shipments.api;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.logistics.core.assistant.contract.AiExceptionSummaryResponse;
import com.logistics.core.shipments.domain.RelatedOrderNotFoundException;
import com.logistics.core.shipments.domain.Shipment;
import com.logistics.core.shipments.domain.ShipmentNotFoundException;
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

            @Test
            void test_should_update_shipment_status_for_order() throws Exception {
            UUID orderId = UUID.fromString("11111111-1111-1111-1111-111111111111");
            UUID shipmentId = UUID.fromString("22222222-2222-2222-2222-222222222222");
            Shipment shipment = shipment(shipmentId, orderId, "Baku", "Ganja");
            shipment.setStatus(ShipmentStatus.IN_TRANSIT);

            when(shipmentService.transitionShipmentStatus(eq(orderId), eq(shipmentId), eq(ShipmentStatus.IN_TRANSIT)))
                .thenReturn(shipment);

            mockMvc.perform(patch("/api/orders/{orderId}/shipments/{shipmentId}/status", orderId, shipmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "status": "IN_TRANSIT"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(shipmentId.toString()))
                .andExpect(jsonPath("$.status").value("IN_TRANSIT"));
            }

            @Test
            void test_should_return_bad_request_when_transition_is_invalid() throws Exception {
            UUID orderId = UUID.fromString("11111111-1111-1111-1111-111111111111");
            UUID shipmentId = UUID.fromString("22222222-2222-2222-2222-222222222222");

            when(shipmentService.transitionShipmentStatus(eq(orderId), eq(shipmentId), eq(ShipmentStatus.DELIVERED)))
                .thenThrow(new IllegalStateException("Invalid shipment status transition: CREATED -> DELIVERED"));

            mockMvc.perform(patch("/api/orders/{orderId}/shipments/{shipmentId}/status", orderId, shipmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "status": "DELIVERED"
                    }
                    """))
                .andExpect(status().isBadRequest());
            }

            @Test
            void test_should_return_not_found_when_shipment_does_not_exist_for_order() throws Exception {
            UUID orderId = UUID.fromString("11111111-1111-1111-1111-111111111111");
            UUID shipmentId = UUID.fromString("22222222-2222-2222-2222-222222222222");

            when(shipmentService.transitionShipmentStatus(eq(orderId), eq(shipmentId), eq(ShipmentStatus.IN_TRANSIT)))
                .thenThrow(new ShipmentNotFoundException(shipmentId, orderId));

            mockMvc.perform(patch("/api/orders/{orderId}/shipments/{shipmentId}/status", orderId, shipmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "status": "IN_TRANSIT"
                    }
                    """))
                .andExpect(status().isNotFound());
            }

            @Test
            void test_should_summarize_shipment_exception_for_order() throws Exception {
            UUID orderId = UUID.fromString("11111111-1111-1111-1111-111111111111");
            UUID shipmentId = UUID.fromString("22222222-2222-2222-2222-222222222222");

            when(shipmentService.summarizeShipmentException(
                eq(orderId),
                eq(shipmentId),
                eq("WEATHER_DELAY"),
                eq("Roads blocked")
            )).thenReturn(new AiExceptionSummaryResponse(
                "Shipment SHIP-101 delayed due to weather",
                "Contact carrier and notify customer"
            ));

            mockMvc.perform(post("/api/orders/{orderId}/shipments/{shipmentId}/exception-summary", orderId, shipmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "exceptionType": "WEATHER_DELAY",
                      "operatorNotes": "Roads blocked"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value("Shipment SHIP-101 delayed due to weather"))
                .andExpect(jsonPath("$.recommendedNextAction").value("Contact carrier and notify customer"));
            }

            @Test
            void test_should_return_not_found_when_summarize_target_shipment_missing() throws Exception {
            UUID orderId = UUID.fromString("11111111-1111-1111-1111-111111111111");
            UUID shipmentId = UUID.fromString("22222222-2222-2222-2222-222222222222");

            when(shipmentService.summarizeShipmentException(
                eq(orderId),
                eq(shipmentId),
                eq("WEATHER_DELAY"),
                eq("Roads blocked")
            )).thenThrow(new ShipmentNotFoundException(shipmentId, orderId));

            mockMvc.perform(post("/api/orders/{orderId}/shipments/{shipmentId}/exception-summary", orderId, shipmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "exceptionType": "WEATHER_DELAY",
                      "operatorNotes": "Roads blocked"
                    }
                    """))
                .andExpect(status().isNotFound());
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

    @Test
    void test_should_track_shipment_and_return_tracking_response() throws Exception {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID shipmentId = UUID.randomUUID();
        Instant now = Instant.parse("2024-04-25T10:30:00Z");

        ShipmentTrackingResponse mockResponse = new ShipmentTrackingResponse(
            shipmentId,
            orderId,
            "John Doe",
            "New York",
            "Los Angeles",
            ShipmentStatus.IN_TRANSIT,
            now,
            now,
            List.of(new TrackingEventResponse(ShipmentStatus.IN_TRANSIT, now))
        );

        when(shipmentService.getShipmentTracking(shipmentId)).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(get("/api/orders/{orderId}/shipments/{shipmentId}/track", orderId, shipmentId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.shipmentId").value(shipmentId.toString()))
            .andExpect(jsonPath("$.orderId").value(orderId.toString()))
            .andExpect(jsonPath("$.customerName").value("John Doe"))
            .andExpect(jsonPath("$.origin").value("New York"))
            .andExpect(jsonPath("$.destination").value("Los Angeles"))
            .andExpect(jsonPath("$.currentStatus").value("IN_TRANSIT"))
            .andExpect(jsonPath("$.createdAt").value("2024-04-25T10:30:00Z"))
            .andExpect(jsonPath("$.updatedAt").value("2024-04-25T10:30:00Z"))
            .andExpect(jsonPath("$.events", hasSize(1)))
            .andExpect(jsonPath("$.events[0].status").value("IN_TRANSIT"))
            .andExpect(jsonPath("$.events[0].timestamp").value("2024-04-25T10:30:00Z"));
    }

    @Test
    void test_should_return_not_found_when_tracking_shipment_not_found() throws Exception {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID shipmentId = UUID.randomUUID();

        when(shipmentService.getShipmentTracking(shipmentId))
            .thenThrow(new ShipmentNotFoundException(shipmentId));

        // Act & Assert
        mockMvc.perform(get("/api/orders/{orderId}/shipments/{shipmentId}/track", orderId, shipmentId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message", containsString("Shipment not found")));
    }

    @Test
    void test_should_return_not_found_when_tracking_related_order_not_found() throws Exception {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID shipmentId = UUID.randomUUID();

        when(shipmentService.getShipmentTracking(shipmentId))
            .thenThrow(new RelatedOrderNotFoundException(orderId));

        // Act & Assert
        mockMvc.perform(get("/api/orders/{orderId}/shipments/{shipmentId}/track", orderId, shipmentId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message", containsString("Related order not found")));
    }

    @Test
    void test_should_serialize_instant_in_iso8601_format_for_tracking() throws Exception {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID shipmentId = UUID.randomUUID();
        Instant fixedInstant = Instant.parse("2024-04-25T15:45:30.123Z");

        ShipmentTrackingResponse mockResponse = new ShipmentTrackingResponse(
            shipmentId,
            orderId,
            "Jane Smith",
            "Boston",
            "Miami",
            ShipmentStatus.CREATED,
            fixedInstant,
            fixedInstant,
            List.of(new TrackingEventResponse(ShipmentStatus.CREATED, fixedInstant))
        );

        when(shipmentService.getShipmentTracking(shipmentId)).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(get("/api/orders/{orderId}/shipments/{shipmentId}/track", orderId, shipmentId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.createdAt").value("2024-04-25T15:45:30.123Z"))
            .andExpect(jsonPath("$.updatedAt").value("2024-04-25T15:45:30.123Z"))
            .andExpect(jsonPath("$.events[0].timestamp").value("2024-04-25T15:45:30.123Z"));
    }

    @Test
    void test_should_include_all_required_fields_in_tracking_response() throws Exception {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID shipmentId = UUID.randomUUID();
        Instant now = Instant.now();

        ShipmentTrackingResponse mockResponse = new ShipmentTrackingResponse(
            shipmentId,
            orderId,
            "Customer Name",
            "Origin",
            "Destination",
            ShipmentStatus.DELIVERED,
            now,
            now,
            List.of(new TrackingEventResponse(ShipmentStatus.DELIVERED, now))
        );

        when(shipmentService.getShipmentTracking(shipmentId)).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(get("/api/orders/{orderId}/shipments/{shipmentId}/track", orderId, shipmentId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.shipmentId").exists())
            .andExpect(jsonPath("$.orderId").exists())
            .andExpect(jsonPath("$.customerName").exists())
            .andExpect(jsonPath("$.origin").exists())
            .andExpect(jsonPath("$.destination").exists())
            .andExpect(jsonPath("$.currentStatus").exists())
            .andExpect(jsonPath("$.createdAt").exists())
            .andExpect(jsonPath("$.updatedAt").exists())
            .andExpect(jsonPath("$.events").exists())
            .andExpect(jsonPath("$.events[0].status").exists())
            .andExpect(jsonPath("$.events[0].timestamp").exists());
    }
}