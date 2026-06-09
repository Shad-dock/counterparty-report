package org.example.counterparty.controller;

import org.example.counterparty.builder.ConcreteReportBuilder;
import org.example.counterparty.builder.ReportBuilder;
import org.example.counterparty.entity.CounterpartyData;
import org.example.counterparty.entity.User;
import org.example.counterparty.entity.VerificationRequest;
import org.example.counterparty.service.CounterpartyService;
import org.example.counterparty.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class HistoryController {

    @Autowired
    private CounterpartyService counterpartyService;

    @Autowired
    private UserService userService;

    @GetMapping("/history")
    public String history(Model model) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        User user = userService.findByUsername(userDetails.getUsername()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        List<VerificationRequest> requests = counterpartyService.getUserRequests(user);
        model.addAttribute("requests", requests);
        return "history";
    }

    @GetMapping("/report/{id}")
    public String report(@PathVariable Long id, Model model) {
        VerificationRequest request = counterpartyService.getRequestById(id);
        if (request == null) {
            return "redirect:/history";
        }

        model.addAttribute("report", request.getCounterpartyData() != null ?
                buildReportFromData(request.getCounterpartyData()) : "Данные не найдены");
        model.addAttribute("requestId", id);
        return "report";
    }

    private String buildReportFromData(CounterpartyData data) {
        ReportBuilder reportBuilder = new ConcreteReportBuilder();
        return reportBuilder
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
