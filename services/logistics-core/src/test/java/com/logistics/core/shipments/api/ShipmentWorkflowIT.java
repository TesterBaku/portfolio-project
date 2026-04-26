package com.logistics.core.shipments.api;

import com.logistics.core.assistant.contract.AiExceptionSummaryRequest;
import com.logistics.core.assistant.contract.AiExceptionSummaryResponse;
import com.logistics.core.assistant.contract.AssistantSummaryClient;
import com.logistics.core.support.AbstractPostgresIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SuppressWarnings("rawtypes")
class ShipmentWorkflowIT extends AbstractPostgresIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private AssistantSummaryClient assistantSummaryClient;

    @Test
    void test_should_complete_full_shipment_lifecycle() {
        // create order
        var createOrderResponse = restTemplate.postForEntity(
                "/api/orders",
                Map.of("customerName", "Workflow Corp"),
                Map.class
        );
        assertThat(createOrderResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String orderId = (String) createOrderResponse.getBody().get("id");
        assertThat(orderId).isNotNull();

        // create shipment for the order
        var createShipmentResponse = restTemplate.postForEntity(
                "/api/orders/" + orderId + "/shipments",
                Map.of("origin", "NYC", "destination", "LAX"),
                Map.class
        );
        assertThat(createShipmentResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Map<?, ?> shipment = createShipmentResponse.getBody();
        String shipmentId = (String) shipment.get("id");
        assertThat(shipmentId).isNotNull();
        assertThat(shipment.get("status")).isEqualTo("CREATED");

        // transition to IN_TRANSIT
        ResponseEntity<Map> inTransitResponse = restTemplate.exchange(
                "/api/orders/" + orderId + "/shipments/" + shipmentId + "/status",
                HttpMethod.PATCH,
                new HttpEntity<>(Map.of("status", "IN_TRANSIT")),
                Map.class
        );
        assertThat(inTransitResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(inTransitResponse.getBody().get("status")).isEqualTo("IN_TRANSIT");

        // transition to DELIVERED
        ResponseEntity<Map> deliveredResponse = restTemplate.exchange(
                "/api/orders/" + orderId + "/shipments/" + shipmentId + "/status",
                HttpMethod.PATCH,
                new HttpEntity<>(Map.of("status", "DELIVERED")),
                Map.class
        );
        assertThat(deliveredResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(deliveredResponse.getBody().get("status")).isEqualTo("DELIVERED");

        // list shipments for order — delivered shipment is visible
        ResponseEntity<Object[]> listResponse = restTemplate.getForEntity(
                "/api/orders/" + orderId + "/shipments",
                Object[].class
        );
        assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listResponse.getBody()).hasSize(1);
    }

    @Test
    void test_should_reject_invalid_status_transition() {
        var orderResp = restTemplate.postForEntity("/api/orders", Map.of("customerName", "Bad Transition Co"), Map.class);
        String orderId = (String) orderResp.getBody().get("id");

        var shipmentResp = restTemplate.postForEntity(
                "/api/orders/" + orderId + "/shipments",
                Map.of("origin", "BOS", "destination", "MIA"),
                Map.class
        );
        String shipmentId = (String) shipmentResp.getBody().get("id");

        // CREATED → DELIVERED is not a valid transition
        ResponseEntity<Map> badTransition = restTemplate.exchange(
                "/api/orders/" + orderId + "/shipments/" + shipmentId + "/status",
                HttpMethod.PATCH,
                new HttpEntity<>(Map.of("status", "DELIVERED")),
                Map.class
        );
        assertThat(badTransition.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void test_should_return_not_found_for_shipment_on_wrong_order() {
        var orderResp = restTemplate.postForEntity("/api/orders", Map.of("customerName", "Cross Order Co"), Map.class);
        String orderId = (String) orderResp.getBody().get("id");

        var anotherOrderResp = restTemplate.postForEntity("/api/orders", Map.of("customerName", "Other Order Co"), Map.class);
        String otherOrderId = (String) anotherOrderResp.getBody().get("id");

        var shipmentResp = restTemplate.postForEntity(
                "/api/orders/" + orderId + "/shipments",
                Map.of("origin", "SEA", "destination", "DEN"),
                Map.class
        );
        String shipmentId = (String) shipmentResp.getBody().get("id");

        // try to transition the shipment under the wrong orderId
        ResponseEntity<Map> notFoundResponse = restTemplate.exchange(
                "/api/orders/" + otherOrderId + "/shipments/" + shipmentId + "/status",
                HttpMethod.PATCH,
                new HttpEntity<>(Map.of("status", "IN_TRANSIT")),
                Map.class
        );
        assertThat(notFoundResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void test_should_summarize_exception_via_ai_assistant() {
        when(assistantSummaryClient.summarizeException(any(AiExceptionSummaryRequest.class)))
                .thenReturn(new AiExceptionSummaryResponse("Weather delay on route", "Reroute via Denver"));

        var orderResp = restTemplate.postForEntity("/api/orders", Map.of("customerName", "Exception Corp"), Map.class);
        String orderId = (String) orderResp.getBody().get("id");

        var shipmentResp = restTemplate.postForEntity(
                "/api/orders/" + orderId + "/shipments",
                Map.of("origin", "NYC", "destination", "LAX"),
                Map.class
        );
        String shipmentId = (String) shipmentResp.getBody().get("id");

        // transition to IN_TRANSIT first so the exception is plausible
        restTemplate.exchange(
                "/api/orders/" + orderId + "/shipments/" + shipmentId + "/status",
                HttpMethod.PATCH,
                new HttpEntity<>(Map.of("status", "IN_TRANSIT")),
                Map.class
        );

        ResponseEntity<Map> summaryResponse = restTemplate.postForEntity(
                "/api/orders/" + orderId + "/shipments/" + shipmentId + "/exception-summary",
                Map.of("exceptionType", "DELAYED", "operatorNotes", "Storm on I-95"),
                Map.class
        );
        assertThat(summaryResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(summaryResponse.getBody().get("summary")).isEqualTo("Weather delay on route");
        assertThat(summaryResponse.getBody().get("recommendedNextAction")).isEqualTo("Reroute via Denver");
    }
}
