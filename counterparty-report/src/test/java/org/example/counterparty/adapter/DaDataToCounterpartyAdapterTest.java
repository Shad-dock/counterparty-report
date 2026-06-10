package org.example.counterparty.adapter;

import org.example.counterparty.dto.DaDataResponse;
import org.example.counterparty.entity.CounterpartyData;
import org.example.counterparty.entity.User;
import org.example.counterparty.entity.VerificationRequest;
import org.example.counterparty.service.DaDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты для DaDataToCounterpartyAdapter")
class DaDataToCounterpartyAdapterTest {

    @Mock
    private DaDataService daDataService;  // ← добавили мок

    @InjectMocks
    private DaDataToCounterpartyAdapter adapter;

    private VerificationRequest request;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User("testuser", "password", "USER");
        request = new VerificationRequest(user, "7707083893");
        request.setId(1L);
    }

    @Test
    @DisplayName("Должен преобразовать валидный ответ DaData в CounterpartyData")
    void shouldConvertValidResponseToCounterpartyData() {
        String innOgrn = "7707083893";
        DaDataResponse response = createValidDaDataResponse();

        when(daDataService.findPartyByInnOgrn(innOgrn)).thenReturn(response);

        CounterpartyData result = adapter.adapt(innOgrn, request);

        assertThat(result).isNotNull();
        assertThat(result.getRequest()).isEqualTo(request);
        assertThat(result.getInn()).isEqualTo("7707083893");
        assertThat(result.getOgrn()).isEqualTo("1027700132195");
        assertThat(result.getName()).isEqualTo("ПУБЛИЧНОЕ АКЦИОНЕРНОЕ ОБЩЕСТВО \"СБЕРБАНК РОССИИ\"");
        assertThat(result.getAddress()).isEqualTo("г Москва, ул Вавилова, д 19");
        assertThat(result.getStatus()).isEqualTo("ACTIVE");
        assertThat(result.getRegistrationDate()).isNotNull();

        verify(daDataService).findPartyByInnOgrn(innOgrn);
    }

    @Test
    @DisplayName("Должен вернуть null, если ответ от API пустой")
    void shouldReturnNullWhenResponseIsNull() {
        String innOgrn = "7707083893";
        when(daDataService.findPartyByInnOgrn(innOgrn)).thenReturn(null);

        CounterpartyData result = adapter.adapt(innOgrn, request);

        assertThat(result).isNull();
        verify(daDataService).findPartyByInnOgrn(innOgrn);
    }

    @Test
    @DisplayName("Должен вернуть null, если нет подсказок (suggestions)")
    void shouldReturnNullWhenNoSuggestions() {
        String innOgrn = "7707083893";
        DaDataResponse response = new DaDataResponse();
        response.setSuggestions(new ArrayList<>());
        when(daDataService.findPartyByInnOgrn(innOgrn)).thenReturn(response);

        CounterpartyData result = adapter.adapt(innOgrn, request);

        assertThat(result).isNull();
        verify(daDataService).findPartyByInnOgrn(innOgrn);
    }

    @Test
    @DisplayName("Должен обработать отсутствие адреса")
    void shouldHandleMissingAddress() {
        String innOgrn = "7707083893";
        DaDataResponse response = createResponseWithoutAddress();
        when(daDataService.findPartyByInnOgrn(innOgrn)).thenReturn(response);

        CounterpartyData result = adapter.adapt(innOgrn, request);

        assertThat(result).isNotNull();
        assertThat(result.getInn()).isEqualTo("7707083893");
        assertThat(result.getAddress()).isNull();
        verify(daDataService).findPartyByInnOgrn(innOgrn);
    }

    @Test
    @DisplayName("Должен обработать отсутствие статуса")
    void shouldHandleMissingStatus() {
        String innOgrn = "7707083893";
        DaDataResponse response = createResponseWithoutState();
        when(daDataService.findPartyByInnOgrn(innOgrn)).thenReturn(response);

        CounterpartyData result = adapter.adapt(innOgrn, request);

        assertThat(result).isNotNull();
        assertThat(result.getInn()).isEqualTo("7707083893");
        assertThat(result.getStatus()).isNull();
        verify(daDataService).findPartyByInnOgrn(innOgrn);
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
