package org.example.counterparty.service;

import org.example.counterparty.entity.CounterpartyData;
import org.example.counterparty.entity.User;
import org.example.counterparty.entity.VerificationRequest;
import org.example.counterparty.repository.CounterpartyDataRepository;
import org.example.counterparty.repository.VerificationRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты для CounterpartyService")
public class CounterpartyServiceTest {
    @Mock
    private VerificationRequestRepository verificationRequestRepository;

    @Mock
    private CounterpartyDataRepository counterpartyDataRepository;

    @InjectMocks
    private CounterpartyService counterpartyService;

    private User testUser;
    private VerificationRequest testRequest;
    private CounterpartyData testData;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "password", "USER");
        testUser.setId(1L);

        testRequest = new VerificationRequest(testUser, "7707083893");
        testRequest.setId(1L);

        testData = new CounterpartyData();
        testData.setId(10L);
        testData.setRequest(testRequest);
        testData.setInn("7707083893");
        testData.setName("ПАО СБЕРБАНК");
    }

    @Test
    @DisplayName("Должен создать новый запрос на проверку")
    void shouldCreateNewRequest() {
        String innOgrn = "7707083893";
        when(verificationRequestRepository.save(any(VerificationRequest.class))).thenReturn(testRequest);

        VerificationRequest result = counterpartyService.createRequest(testUser, innOgrn);

        assertThat(result).isNotNull();
        assertThat(result.getUser()).isEqualTo(testUser);
        assertThat(result.getInnOgrn()).isEqualTo(innOgrn);
        assertThat(result.getStatus()).isEqualTo("PENDING");

        verify(verificationRequestRepository).save(any(VerificationRequest.class));
    }

    @Test
    @DisplayName("Должен получить все запросы пользователя")
    void shouldGetUserRequests() {
        List<VerificationRequest> expectedRequests = Arrays.asList(testRequest);
        when(verificationRequestRepository.findByUserOrderByCreatedAtDesc(testUser))
                .thenReturn(expectedRequests);

        List<VerificationRequest> result = counterpartyService.getUserRequests(testUser);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testRequest);
        verify(verificationRequestRepository).findByUserOrderByCreatedAtDesc(testUser);
    }

    @Test
    @DisplayName("Должен найти запрос по ID")
    void shouldGetRequestByIdWhenExists() {
        when(verificationRequestRepository.findById(1L)).thenReturn(Optional.of(testRequest));

        VerificationRequest result = counterpartyService.getRequestById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getInnOgrn()).isEqualTo("7707083893");
        verify(verificationRequestRepository).findById(1L);
    }

    @Test
    @DisplayName("Должен вернуть null, если запрос по ID не найден")
    void shouldReturnNullWhenRequestNotFound() {
        when(verificationRequestRepository.findById(99L)).thenReturn(Optional.empty());

        VerificationRequest result = counterpartyService.getRequestById(99L);

        assertThat(result).isNull();
        verify(verificationRequestRepository).findById(99L);
    }

    @Test
    @DisplayName("Должен обновить статус запроса")
    void shouldUpdateRequestStatus() {
        when(verificationRequestRepository.save(testRequest)).thenReturn(testRequest);

        counterpartyService.updateRequestStatus(testRequest, "SUCCESS", null);

        assertThat(testRequest.getStatus()).isEqualTo("SUCCESS");
        assertThat(testRequest.getErrorMessage()).isNull();
        verify(verificationRequestRepository).save(testRequest);
    }

    @Test
    @DisplayName("Должен обновить статус запроса с сообщением об ошибке")
    void shouldUpdateRequestStatusWithErrorMessage() {
        String errorMessage = "Ошибка подключения к API";
        when(verificationRequestRepository.save(testRequest)).thenReturn(testRequest);

        counterpartyService.updateRequestStatus(testRequest, "ERROR", errorMessage);

        assertThat(testRequest.getStatus()).isEqualTo("ERROR");
        assertThat(testRequest.getErrorMessage()).isEqualTo(errorMessage);
        verify(verificationRequestRepository).save(testRequest);
    }

    @Test
    @DisplayName("Должен сохранить данные о контрагенте и связать с запросом")
    void shouldSaveCounterpartyData() {
        when(verificationRequestRepository.save(testRequest)).thenReturn(testRequest);

        counterpartyService.saveCounterpartyData(testRequest, testData);

        assertThat(testRequest.getCounterpartyData()).isEqualTo(testData);
        verify(verificationRequestRepository).save(testRequest);
    }
}
