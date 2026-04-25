package com.logistics.core.assistant.contract;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiExceptionSummaryResponse(
    String summary,
    @JsonProperty("recommended_next_action")
    String recommendedNextAction
) {
}