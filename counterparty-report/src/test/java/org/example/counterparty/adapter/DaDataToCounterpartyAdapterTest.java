package org.example.counterparty.adapter;

import org.example.counterparty.dto.DaDataResponse;
import org.example.counterparty.entity.CounterpartyData;
import org.example.counterparty.entity.VerificationRequest;
import org.example.counterparty.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
public class DaDataToCounterpartyAdapterTest {
    private DaDataToCounterpartyAdapter adapter;
    public VerificationRequest request;

    @BeforeEach
    void setUp() {
        adapter = new DaDataToCounterpartyAdapter();

        User user = new User("testuser", "password", "USER");
        request = new VerificationRequest(user, "7707083893");
    }

    @Test
    @DisplayName("Должен преобразовать валидный ответ DaData в CounterpartyData")
    void shouldConvertValidResponseToCounterpartyData() {
        DaDataResponse response = createValidDaDataResponse();

        CounterpartyData result = adapter.adapt(response, request);

        assertThat(result).isNotNull();
        assertThat(result.getRequest()).isEqualTo(request);
        assertThat(result.getInn()).isEqualTo("7707083893");
        assertThat(result.getOgrn()).isEqualTo("1027700132195");
        assertThat(result.getName()).isEqualTo("ПУБЛИЧНОЕ АКЦИОНЕРНОЕ ОБЩЕСТВО \"СБЕРБАНК РОССИИ\"");
        assertThat(result.getAddress()).isEqualTo("г Москва, ул Вавилова, д 19");
        assertThat(result.getStatus()).isEqualTo("ACTIVE");
        assertThat(result.getRegistrationDate()).isNotNull();
    }

    @Test
    @DisplayName("Должен вернуть null, если ответ от API пустой")
    void shouldReturnNullWhenResponseIsNull() {
        CounterpartyData result = adapter.adapt(null, request);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Должен вернуть null, если нет suggestions")
    void shouldReturnNullWhenNoSuggestions() {
        DaDataResponse response = new DaDataResponse();
        response.setSuggestions(new ArrayList<>());

        CounterpartyData result = adapter.adapt(response, request);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Должен обработать отсутствие адреса")
    void shouldHandleMissingAddress() {
        DaDataResponse response = createResponseWithoutAddress();

        CounterpartyData result = adapter.adapt(response, request);

        assertThat(result).isNotNull();
        assertThat(result.getInn()).isEqualTo("7707083893");
        assertThat(result.getAddress()).isNull();
    }

    @Test
    @DisplayName("Должен обработать отсутствие статуса")
    void shouldHandleMissingStatus() {
        DaDataResponse response = createResponseWithoutState();

        CounterpartyData result = adapter.adapt(response, request);

        assertThat(result).isNotNull();
        assertThat(result.getInn()).isEqualTo("7707083893");
        assertThat(result.getStatus()).isNull();
    }

    private DaDataResponse createValidDaDataResponse() {
        DaDataResponse response = new DaDataResponse();

        DaDataResponse.Suggestion suggestion = new DaDataResponse.Suggestion();
        suggestion.setValue("ПАО СБЕРБАНК");
        suggestion.setUnrestrictedValue("ПАО СБЕРБАНК, г Москва, ул Вавилова, д 19");

        DaDataResponse.Data data = new DaDataResponse.Data();
        data.setInn("7707083893");
        data.setOgrn("1027700132195");

        DaDataResponse.Name name = new DaDataResponse.Name();
        name.setFull("ПУБЛИЧНОЕ АКЦИОНЕРНОЕ ОБЩЕСТВО \"СБЕРБАНК РОССИИ\"");
        data.setName(name);

        DaDataResponse.Address address = new DaDataResponse.Address();
        address.setValue("г Москва, ул Вавилова, д 19");
        data.setAddress(address);

        DaDataResponse.State state = new DaDataResponse.State();
        state.setStatus("ACTIVE");
        state.setRegistrationDate("172800000000");
        data.setState(state);

        suggestion.setData(data);

        List<DaDataResponse.Suggestion> suggestions = new ArrayList<>();
        suggestions.add(suggestion);
        response.setSuggestions(suggestions);

        return response;
    }

    private DaDataResponse createResponseWithoutAddress() {
        DaDataResponse response = new DaDataResponse();

        DaDataResponse.Suggestion suggestion = new DaDataResponse.Suggestion();

        DaDataResponse.Data data = new DaDataResponse.Data();
        data.setInn("7707083893");
        data.setOgrn("1027700132195");

        DaDataResponse.Name name = new DaDataResponse.Name();
        name.setFull("ПУБЛИЧНОЕ АКЦИОНЕРНОЕ ОБЩЕСТВО \"СБЕРБАНК РОССИИ\"");
        data.setName(name);

        // address нет

        DaDataResponse.State state = new DaDataResponse.State();
        state.setStatus("ACTIVE");
        data.setState(state);

        suggestion.setData(data);

        List<DaDataResponse.Suggestion> suggestions = new ArrayList<>();
        suggestions.add(suggestion);
        response.setSuggestions(suggestions);

        return response;
    }

    private DaDataResponse createResponseWithoutState() {
        DaDataResponse response = new DaDataResponse();

        DaDataResponse.Suggestion suggestion = new DaDataResponse.Suggestion();

        DaDataResponse.Data data = new DaDataResponse.Data();
        data.setInn("7707083893");
        data.setOgrn("1027700132195");

        DaDataResponse.Name name = new DaDataResponse.Name();
        name.setFull("ПУБЛИЧНОЕ АКЦИОНЕРНОЕ ОБЩЕСТВО \"СБЕРБАНК РОССИИ\"");
        data.setName(name);

        DaDataResponse.Address address = new DaDataResponse.Address();
        address.setValue("г Москва, ул Вавилова, д 19");
        data.setAddress(address);

        // state нет

        suggestion.setData(data);

        List<DaDataResponse.Suggestion> suggestions = new ArrayList<>();
        suggestions.add(suggestion);
        response.setSuggestions(suggestions);

        return response;
    }

}
