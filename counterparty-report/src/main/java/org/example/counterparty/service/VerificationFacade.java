package org.example.counterparty.service;

import org.example.counterparty.adapter.CounterpartyDataAdapter;
import org.example.counterparty.builder.ReportBuilder;
import org.example.counterparty.dto.DaDataResponse;
import org.example.counterparty.entity.CounterpartyData;
import org.example.counterparty.entity.User;
import org.example.counterparty.entity.VerificationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VerificationFacade {

    @Autowired
    private DaDataService daDataService;

    @Autowired
    private CounterpartyService counterpartyService;

    @Autowired
    private CounterpartyDataAdapter adapter;

    @Autowired
    private ReportBuilder reportBuilder;

    public VerificationResult verify(String innOgrn, User user) {
        if (!isValidInnOgrn(innOgrn)) {
            return VerificationResult.error("Неверный формат ИНН/ОГРН");
        }

        VerificationRequest request = counterpartyService.createRequest(user, innOgrn);

        try {
            DaDataResponse response = daDataService.findPartyByInnOgrn(innOgrn);

            if (response == null || response.getFirstSuggestion() == null) {
                counterpartyService.updateRequestStatus(request, "NOT_FOUND", "Компания не найдена");
                return VerificationResult.notFound("Компания не найдена");
            }

            CounterpartyData data = adapter.adapt(response, request);
            if (data == null) {
                counterpartyService.updateRequestStatus(request, "NOT_FOUND", "Компания не найдена");
                return VerificationResult.notFound("Компания не найдена");
            }

            counterpartyService.saveCounterpartyData(request, data);
            counterpartyService.updateRequestStatus(request, "SUCCESS", null);

            reportBuilder.reset();
            String report = reportBuilder
                    .setTitle("Отчет по контрагенту")
                    .addSection("Наименование: " + data.getName())
                    .addSection("ИНН: " + data.getInn())
                    .addSection("ОГРН: " + data.getOgrn())
                    .addSection("Адрес: " + data.getAddress())
                    .addSection("Статус: " + data.getStatus())
                    .addSection("Дата регистрации: " + data.getRegistrationDate())
                    .setFooter("Дата формирования: " + java.time.LocalDate.now())
                    .build();

            return VerificationResult.success(report, request.getId());

        } catch (Exception e) {
            counterpartyService.updateRequestStatus(request, "ERROR", e.getMessage());
            return VerificationResult.error("Ошибка при обращении к API: " + e.getMessage());
        }
    }

    private boolean isValidInnOgrn(String innOgrn) {
        if (innOgrn == null) return false;
        int len = innOgrn.length();
        return len == 10 || len == 12 || len == 13 || len == 15;
    }

    public static class VerificationResult {
        private boolean success;
        private boolean notFound;
        private String error;
        private String report;
        private Long requestId;

        public static VerificationResult success(String report, Long requestId) {
            VerificationResult result = new VerificationResult();
            result.success = true;
            result.report = report;
            result.requestId = requestId;
            return result;
        }

        public static VerificationResult error(String error) {
            VerificationResult result = new VerificationResult();
            result.success = false;
            result.error = error;
            return result;
        }

        public static VerificationResult notFound(String error) {
            VerificationResult result = new VerificationResult();
            result.success = false;
            result.notFound = true;
            result.error = error;
            return result;
        }

        public boolean isSuccess() { return success; }
        public boolean isNotFound() { return notFound; }
        public String getError() { return error; }
        public String getReport() { return report; }
        public Long getRequestId() { return requestId; }
    }
}