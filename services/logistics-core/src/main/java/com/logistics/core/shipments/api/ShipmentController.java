package com.logistics.core.shipments.api;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.logistics.core.shipments.service.ShipmentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/orders/{orderId}/shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShipmentResponse createShipment(
            @PathVariable UUID orderId,
            @Valid @RequestBody CreateShipmentRequest request
    ) {
        return ShipmentResponse.from(
            shipmentService.createShipment(orderId, request.origin(), request.destination())
        );
    }

    @GetMapping
    public List<ShipmentResponse> listShipments(@PathVariable UUID orderId) {
        return shipmentService.listShipmentsByOrderId(orderId).stream().map(ShipmentResponse::from).toList();
    }
}