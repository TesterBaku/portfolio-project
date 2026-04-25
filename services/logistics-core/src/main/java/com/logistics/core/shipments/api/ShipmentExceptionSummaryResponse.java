package com.logistics.core.shipments.api;

public record ShipmentExceptionSummaryResponse(
    String summary,
    String recommendedNextAction
) {
}