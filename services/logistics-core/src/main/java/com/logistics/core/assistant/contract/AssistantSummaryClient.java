package com.logistics.core.assistant.contract;

public interface AssistantSummaryClient {
    AiExceptionSummaryResponse summarizeException(AiExceptionSummaryRequest request);
}