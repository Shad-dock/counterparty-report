package org.example.counterparty.service;

import org.example.counterparty.dto.DaDataResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class DaDataService {
    private final WebClient webClient;

    public DaDataService(@Value("${dadata.api.url}") String apiUrl,
                         @Value("${dadata.api.token}") String apiToken) {
        this.webClient = WebClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader("Authorization", "Token " + apiToken)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public DaDataResponse findPartyByInnOgrn(String query) {
        DaDataResponse response = webClient.post()
                .bodyValue(new QueryRequest(query))
                .retrieve()
                .bodyToMono(DaDataResponse.class)
                .block();

        // Для отладки
        if (response != null && response.getFirstSuggestion() != null) {
            System.out.println("Найдено: " + response.getFirstSuggestion().getValue());
        }

        return response;
    }

    private static class QueryRequest {
        private String query;

        public QueryRequest(String query) {
            this.query = query;
        }

        public String getQuery() { return query; }
    }
}
