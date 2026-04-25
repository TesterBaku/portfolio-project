package com.logistics.core.assistant.contract;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class HttpAssistantSummaryClient implements AssistantSummaryClient {

    private final RestClient restClient;

    public HttpAssistantSummaryClient(
            RestClient.Builder restClientBuilder,
            @Value("${ai.assistant.base-url:http://localhost:8000}") String assistantBaseUrl
    ) {
        this.restClient = restClientBuilder.baseUrl(assistantBaseUrl).build();
    }

    @Override
    public AiExceptionSummaryResponse summarizeException(AiExceptionSummaryRequest request) {
        return restClient.post()
                .uri("/api/assistant/summarize")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(AiExceptionSummaryResponse.class);
    }
}