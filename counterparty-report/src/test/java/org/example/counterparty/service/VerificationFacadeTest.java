package org.example.counterparty.service;

import org.example.counterparty.adapter.CounterpartyDataAdapter;
import org.example.counterparty.dto.DaDataResponse;
import org.example.counterparty.entity.CounterpartyData;
import org.example.counterparty.entity.User;
import org.example.counterparty.entity.VerificationRequest;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты для VerificationFacade")
public class VerificationFacadeTest {
    @Mock
    private DaDataService daDataService;

    @Mock
    private CounterpartyService counterpartyService;

    @Mock
    private CounterpartyDataAdapter adapter;

    @InjectMocks
    private VerificationFacade verificationFacade;

    private User testUser;
    private VerificationRequest testRequest;
    private CounterpartyData testData;
    private DaDataResponse testResponse;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "password", "USER");
        testRequest = new VerificationRequest(testUser, "7707083893");
        testRequest.setId(1L);

        testData = new CounterpartyData();
        testData.setId(10L);
        testData.setRequest(testRequest);
        testData.setInn("7707083893");
        testData.setName("ПАО СБЕРБАНК");

        //testResponse = new DaDataResponse();
        testResponse = createValidDaDataResponse();
    }

    @Test
    @DisplayName("Успешная проверка контрагента")
    void shouldSuccessfullyVerifyCompany() {
        String innOgrn = "7707083893";

        when(counterpartyService.createRequest(testUser, innOgrn)).thenReturn(testRequest);
        when(daDataService.findPartyByInnOgrn(innOgrn)).thenReturn(testResponse);
        when(adapter.adapt(testResponse, testRequest)).thenReturn(testData);

        VerificationFacade.VerificationResult result = verificationFacade.verify(innOgrn, testUser);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getReport()).isNotNull();
        assertThat(result.getRequestId()).isEqualTo(1L);

        verify(counterpartyService).createRequest(testUser, innOgrn);
        verify(daDataService).findPartyByInnOgrn(innOgrn);
        verify(adapter).adapt(testResponse, testRequest);
        verify(counterpartyService).saveCounterpartyData(testRequest, testData);
        verify(counterpartyService).updateRequestStatus(testRequest, "SUCCESS", null);
    }

    @Test
    @DisplayName("Неверный формат ИНН/ОГРН")
    void shouldReturnErrorWhenInvalidFormat() {
        String invalidInn = "123";

        VerificationFacade.VerificationResult result = verificationFacade.verify(invalidInn, testUser);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getError()).contains("Неверный формат");

        verify(counterpartyService, never()).createRequest(any(), any());
        verify(daDataService, never()).findPartyByInnOgrn(any());
        verify(adapter, never()).adapt(any(), any());
    }

    @Test
    @DisplayName("Компания не найдена в DaData")
    void shouldReturnNotFoundWhenCompanyNotFound() {
        String innOgrn = "1234567890";

        when(counterpartyService.createRequest(testUser, innOgrn)).thenReturn(testRequest);
        when(daDataService.findPartyByInnOgrn(innOgrn)).thenReturn(null);

        VerificationFacade.VerificationResult result = verificationFacade.verify(innOgrn, testUser);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.isNotFound()).isTrue();
        assertThat(result.getError()).contains("не найдена");

        verify(counterpartyService).createRequest(testUser, innOgrn);
        verify(daDataService).findPartyByInnOgrn(innOgrn);
        verify(counterpartyService).updateRequestStatus(testRequest, "NOT_FOUND", "Компания не найдена");

        verify(adapter, never()).adapt(any(), any());
        verify(counterpartyService, never()).saveCounterpartyData(any(), any());
    }

    @Test
    @DisplayName("Адаптер вернул null (невалидные данные)")
    void shouldReturnNotFoundWhenAdapterReturnsNull() {
        String innOgrn = "7707083893";

        when(counterpartyService.createRequest(testUser, innOgrn)).thenReturn(testRequest);
        when(daDataService.findPartyByInnOgrn(innOgrn)).thenReturn(testResponse);
        when(adapter.adapt(testResponse, testRequest)).thenReturn(null);

        VerificationFacade.VerificationResult result = verificationFacade.verify(innOgrn, testUser);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.isNotFound()).isTrue();
        assertThat(result.getError()).contains("не найдена");

        verify(counterpartyService).createRequest(testUser, innOgrn);
        verify(daDataService).findPartyByInnOgrn(innOgrn);
        verify(adapter).adapt(testResponse, testRequest);
        verify(counterpartyService).updateRequestStatus(testRequest, "NOT_FOUND", "Компания не найдена");

        verify(counterpartyService, never()).saveCounterpartyData(any(), any());
    }

    @Test
    @DisplayName("Ошибка при вызове DaData API")
    void shouldReturnErrorWhenApiThrowsException() {
        String innOgrn = "7707083893";

        when(counterpartyService.createRequest(testUser, innOgrn)).thenReturn(testRequest);
        when(daDataService.findPartyByInnOgrn(innOgrn)).thenThrow(new RuntimeException("Network error"));

        VerificationFacade.VerificationResult result = verificationFacade.verify(innOgrn, testUser);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.isNotFound()).isFalse();
        assertThat(result.getError()).contains("Ошибка");

        verify(counterpartyService).createRequest(testUser, innOgrn);
        verify(daDataService).findPartyByInnOgrn(innOgrn);
        verify(counterpartyService).updateRequestStatus(eq(testRequest), eq("ERROR"), anyString());

        verify(adapter, never()).adapt(any(), any());
        verify(counterpartyService, never()).saveCounterpartyData(any(), any());
    }


    private DaDataResponse createValidDaDataResponse() {
        DaDataResponse response = new DaDataResponse();

        DaDataResponse.Suggestion suggestion = new DaDataResponse.Suggestion();
        DaDataResponse.Data data = new DaDataResponse.Data();
        data.setInn("7707083893");
        data.setOgrn("1027700132195");

        DaDataResponse.Name name = new DaDataResponse.Name();
        name.setFull("ПАО СБЕРБАНК");
        data.setName(name);

        DaDataResponse.Address address = new DaDataResponse.Address();
        address.setValue("г Москва, ул Вавилова, д 19");
        data.setAddress(address);

        DaDataResponse.State state = new DaDataResponse.State();
        state.setStatus("ACTIVE");
        data.setState(state);

        suggestion.setData(data);
        suggestion.setValue("ПАО СБЕРБАНК");

        List<DaDataResponse.Suggestion> suggestions = new ArrayList<>();
        suggestions.add(suggestion);
        response.setSuggestions(suggestions);

        return response;
    }

}
