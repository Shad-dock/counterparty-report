package org.example.counterparty.adapter;

import org.example.counterparty.dto.DaDataResponse;
import org.example.counterparty.entity.CounterpartyData;
import org.example.counterparty.entity.VerificationRequest;
import org.springframework.stereotype.Component;

@Component
public class DaDataToCounterpartyAdapter implements CounterpartyDataAdapter {

    @Override
    public CounterpartyData adapt(DaDataResponse response, VerificationRequest request) {
        DaDataResponse.Suggestion suggestion = response.getFirstSuggestion();
        if (suggestion == null) return null;

        DaDataResponse.Data data = suggestion.getData();
        if (data == null) return null;

        CounterpartyData counterpartyData = new CounterpartyData();
        counterpartyData.setRequest(request);
        counterpartyData.setName(data.getFullName());
        counterpartyData.setInn(data.getInn());
        counterpartyData.setOgrn(data.getOgrn());
        counterpartyData.setAddress(data.getAddress() != null ? data.getAddress().getValue() : null);
        counterpartyData.setStatus(data.getState() != null ? data.getState().getStatus() : null);
        //counterpartyData.setRegistrationDate(data.getRegistrationDate());
        String regDate = null;
        if (data.getState() != null && data.getState().getRegistrationDate() != null) {
            try {
                long timestamp = Long.parseLong(data.getState().getRegistrationDate());
                java.time.LocalDate date = java.time.Instant.ofEpochMilli(timestamp)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate();
                regDate = date.toString();
            } catch (NumberFormatException e) {
                regDate = data.getState().getRegistrationDate();
            }
        }
        counterpartyData.setRegistrationDate(regDate);

        return counterpartyData;
    }
}