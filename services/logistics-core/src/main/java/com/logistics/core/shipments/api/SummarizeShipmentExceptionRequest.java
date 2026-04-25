package com.logistics.core.shipments.api;

import jakarta.validation.constraints.NotBlank;

public record SummarizeShipmentExceptionRequest(
    @NotBlank(message = "exceptionType is required")
    String exceptionType,
    String operatorNotes
) {
}