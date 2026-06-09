package org.example.counterparty.service;

import org.example.counterparty.adapter.CounterpartyDataAdapter;
import org.example.counterparty.builder.ReportBuilder;
import org.example.counterparty.dto.DaDataResponse;
import org.example.counterparty.entity.CounterpartyData;
import org.example.counterparty.entity.User;
import org.example.counterparty.entity.VerificationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.example.counterparty.builder.ConcreteReportBuilder;

@Service
public class VerificationFacade {
    private static final int MAX_REQUESTS_PER_USER = 10;

    @Autowired
    private DaDataService daDataService;

    @Autowired
    private CounterpartyService counterpartyService;

    @Autowired
    private CounterpartyDataAdapter adapter;

    public VerificationResult verify(String innOgrn, User user) {
        int requestCount = counterpartyService.getUserRequestsCount(user);
        if (requestCount >= MAX_REQUESTS_PER_USER) {
            return VerificationResult.error("Превышен лимит проверок (" + MAX_REQUESTS_PER_USER + "). " +
                    "Вы уже использовали все доступные проверки.");
        }

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

            ReportBuilder reportBuilder = new ConcreteReportBuilder();

            String report = reportBuilder
                    .createNew()
                    .setTitle("ОТЧЕТ ПО КОНТРАГЕНТУ")
                    .addSection("\n" + "-".repeat(50))
                    .addSection("КАРТОЧКА КОМПАНИИ")
                    .addSection("-".repeat(50))
                    .addSection("\n1. ОСНОВНЫЕ СВЕДЕНИЯ:")
                    .addSection("   Наименование: " + (data.getName() != null ? data.getName() : "Не указано"))
                    .addSection("   ИНН: " + (data.getInn() != null ? data.getInn() : "Не указан"))
                    .addSection("   ОГРН: " + (data.getOgrn() != null ? data.getOgrn() : "Не указан"))
                    .addSection("\n2. ЮРИДИЧЕСКИЙ АДРЕС:")
                    .addSection("   " + (data.getAddress() != null ? data.getAddress() : "Не указан"))
                    .addSection("\n3. СТАТУС ОРГАНИЗАЦИИ:")
                    .addSection("   " + formatStatus(data.getStatus()))
                    .addSection("\n4. ДАТА РЕГИСТРАЦИИ:")
                    .addSection("   " + (data.getRegistrationDate() != null ? data.getRegistrationDate() : "Не указана"))
                    .addSection("\n" + "-".repeat(50))
                    .addSection("КРАТКОЕ РЕЗЮМЕ")
                    .addSection("-".repeat(50))
                    .addSection(getResume(data))
                    .setFooter("\n" + "-".repeat(50) + "\nДата формирования отчета: " + java.time.LocalDate.now())
                    .build();

            return VerificationResult.success(report, request.getId());

        } catch (Exception e) {
            counterpartyService.updateRequestStatus(request, "ERROR", e.getMessage());
            return VerificationResult.error("Ошибка при обращении к API: " + e.getMessage());
        }
    }

    private String formatStatus(String status) {
        if (status == null) return "Неизвестно";
        switch (status) {
            case "ACTIVE": return "Действующее";
            case "LIQUIDATING": return "В процессе ликвидации";
            case "LIQUIDATED": return "Ликвидировано";
            default: return status;
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

    private String getResume(CounterpartyData data) {
        if (data == null) {
            return "Нет данных для анализа";
        }

        String status = data.getStatus();
        String registrationDate = data.getRegistrationDate();

        String statusAnalysis;
        if ("ACTIVE".equals(status)) {
            statusAnalysis = "Организация действует, можно заключать договор";
        } else if ("LIQUIDATING".equals(status)) {
            statusAnalysis = "Организация в процессе ликвидации, требуется осторожность";
        } else if ("LIQUIDATED".equals(status)) {
            statusAnalysis = "Организация ликвидирована, не рекомендуется заключать договор";
        } else {
            statusAnalysis = "Статус организации не определён, требуется дополнительная проверка";
        }

        String ageAnalysis;
        if (registrationDate != null && !registrationDate.isEmpty()) {
            ageAnalysis = "Организация зарегистрирована " + registrationDate;
        } else {
            ageAnalysis = "Дата регистрации не указана";
        }

        return statusAnalysis + "\n" + ageAnalysis;
    }
}