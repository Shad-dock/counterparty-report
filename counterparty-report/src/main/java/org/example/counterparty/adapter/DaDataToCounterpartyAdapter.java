package org.example.counterparty.adapter;

import org.example.counterparty.dto.DaDataResponse;
import org.example.counterparty.entity.CounterpartyData;
import org.example.counterparty.entity.VerificationRequest;
import org.example.counterparty.service.DaDataService;
import org.springframework.stereotype.Component;

@Component
public class DaDataToCounterpartyAdapter implements CounterpartyDataAdapter {

    private final DaDataService daDataService;  // ← добавить поле

    public DaDataToCounterpartyAdapter(DaDataService daDataService) {
        this.daDataService = daDataService;
    }

    @Override
    public CounterpartyData adapt(String innOgrn, VerificationRequest request) {
        DaDataResponse response = daDataService.findPartyByInnOgrn(innOgrn);

        if (response == null || response.getFirstSuggestion() == null) {
            return null;
        }

        DaDataResponse.Data data = response.getFirstSuggestion().getData();
        if (data == null) return null;

        CounterpartyData counterpartyData = new CounterpartyData();
        counterpartyData.setRequest(request);
        counterpartyData.setName(data.getFullName());
        counterpartyData.setInn(data.getInn());
        counterpartyData.setOgrn(data.getOgrn());
        counterpartyData.setAddress(data.getAddress() != null ? data.getAddress().getValue() : null);
        counterpartyData.setStatus(data.getState() != null ? data.getState().getStatus() : null);

        if (data.getState() != null && data.getState().getRegistrationDate() != null) {
            String rawDate = data.getState().getRegistrationDate();
            try {
                long timestamp = Long.parseLong(rawDate);
                java.time.LocalDate date = java.time.Instant.ofEpochMilli(timestamp)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate();
                counterpartyData.setRegistrationDate(date.toString());
            } catch (NumberFormatException e) {
                counterpartyData.setRegistrationDate(rawDate);
            }
        }

        return counterpartyData;
    }
}