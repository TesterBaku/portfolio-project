package com.logistics.core.assistant.contract;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiExceptionSummaryRequest(
    @JsonProperty("shipment_id")
    String shipmentId,
    @JsonProperty("exception_type")
    String exceptionType,
    @JsonProperty("latest_status")
    String latestStatus,
    @JsonProperty("operator_notes")
    String operatorNotes
) {
}