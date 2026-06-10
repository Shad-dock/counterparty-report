package org.example.counterparty.service;

import org.example.counterparty.adapter.CounterpartyDataAdapter;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты для VerificationFacade")
public class VerificationFacadeTest {

    @Mock
    private CounterpartyService counterpartyService;

    @Mock
    private CounterpartyDataAdapter adapter;

    @InjectMocks
    private VerificationFacade verificationFacade;

    private User testUser;
    private VerificationRequest testRequest;
    private CounterpartyData testData;

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
    }

    @Test
    @DisplayName("Успешная проверка контрагента")
    void shouldSuccessfullyVerifyCompany() {
        String innOgrn = "7707083893";

        when(counterpartyService.createRequest(testUser, innOgrn)).thenReturn(testRequest);
        when(counterpartyService.getUserRequestsCount(testUser)).thenReturn(0);
        when(adapter.adapt(eq(innOgrn), any(VerificationRequest.class))).thenReturn(testData);

        VerificationFacade.VerificationResult result = verificationFacade.verify(innOgrn, testUser);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getReport()).isNotNull();
        assertThat(result.getRequestId()).isEqualTo(1L);

        verify(counterpartyService).createRequest(testUser, innOgrn);
        verify(adapter).adapt(eq(innOgrn), eq(testRequest));
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
        verify(adapter, never()).adapt(any(), any());
    }

    @Test
    @DisplayName("Компания не найдена (адаптер вернул null)")
    void shouldReturnNotFoundWhenCompanyNotFound() {
        String innOgrn = "1234567890";

        when(counterpartyService.createRequest(testUser, innOgrn)).thenReturn(testRequest);
        when(counterpartyService.getUserRequestsCount(testUser)).thenReturn(0);
        when(adapter.adapt(eq(innOgrn), any(VerificationRequest.class))).thenReturn(null);

        VerificationFacade.VerificationResult result = verificationFacade.verify(innOgrn, testUser);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.isNotFound()).isTrue();
        assertThat(result.getError()).contains("не найдена");

        verify(counterpartyService).createRequest(testUser, innOgrn);
        verify(adapter).adapt(eq(innOgrn), eq(testRequest));
        verify(counterpartyService).updateRequestStatus(testRequest, "NOT_FOUND", "Компания не найдена");
        verify(counterpartyService, never()).saveCounterpartyData(any(), any());
    }

    @Test
    @DisplayName("Адаптер выбросил исключение (ошибка API)")
    void shouldReturnErrorWhenAdapterThrowsException() {
        String innOgrn = "7707083893";

        when(counterpartyService.createRequest(testUser, innOgrn)).thenReturn(testRequest);
        when(counterpartyService.getUserRequestsCount(testUser)).thenReturn(0);
        when(adapter.adapt(eq(innOgrn), any(VerificationRequest.class))).thenThrow(new RuntimeException("Network error"));

        VerificationFacade.VerificationResult result = verificationFacade.verify(innOgrn, testUser);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.isNotFound()).isFalse();
        assertThat(result.getError()).contains("Ошибка");

        verify(counterpartyService).createRequest(testUser, innOgrn);
        verify(adapter).adapt(eq(innOgrn), eq(testRequest));
        verify(counterpartyService).updateRequestStatus(eq(testRequest), eq("ERROR"), anyString());
        verify(counterpartyService, never()).saveCounterpartyData(any(), any());
    }

    @Test
    @DisplayName("Превышен лимит проверок → ошибка")
    void shouldReturnErrorWhenLimitExceeded() {
        String innOgrn = "7707083893";

        when(counterpartyService.getUserRequestsCount(testUser)).thenReturn(10);

        VerificationFacade.VerificationResult result = verificationFacade.verify(innOgrn, testUser);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getError()).contains("Превышен лимит");

        verify(counterpartyService, never()).createRequest(any(), any());
        verify(adapter, never()).adapt(any(), any());
    }
}