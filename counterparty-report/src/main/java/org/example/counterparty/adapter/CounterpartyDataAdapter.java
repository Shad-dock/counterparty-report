package org.example.counterparty.adapter;

import org.example.counterparty.dto.DaDataResponse;
import org.example.counterparty.entity.CounterpartyData;
import org.example.counterparty.entity.VerificationRequest;

public interface CounterpartyDataAdapter {
    CounterpartyData adapt(String innOgrn, VerificationRequest request);
}