package com.logistics.core.shipments.api;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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

    @PatchMapping("/{shipmentId}/status")
    public ShipmentResponse updateShipmentStatus(
            @PathVariable("orderId") UUID orderId,
            @PathVariable("shipmentId") UUID shipmentId,
            @Valid @RequestBody UpdateShipmentStatusRequest request
    ) {
        try {
            return ShipmentResponse.from(
                shipmentService.transitionShipmentStatus(orderId, shipmentId, request.status())
            );
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @GetMapping("/{shipmentId}/track")
    public ShipmentTrackingResponse trackShipment(
            @PathVariable("orderId") UUID orderId,
            @PathVariable("shipmentId") UUID shipmentId
    ) {
        return shipmentService.getShipmentTracking(orderId, shipmentId);
    }

    @PostMapping("/{shipmentId}/exception-summary")
    public ShipmentExceptionSummaryResponse summarizeShipmentException(
            @PathVariable UUID orderId,
            @PathVariable UUID shipmentId,
            @Valid @RequestBody SummarizeShipmentExceptionRequest request
    ) {
        try {
            var summary = shipmentService.summarizeShipmentException(
                    orderId,
                    shipmentId,
                    request.exceptionType(),
                    request.operatorNotes()
            );

            return new ShipmentExceptionSummaryResponse(
                    summary.summary(),
                    summary.recommendedNextAction()
            );
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }
}