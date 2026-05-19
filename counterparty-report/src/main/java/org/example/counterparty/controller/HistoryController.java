package org.example.counterparty.controller;

import org.example.counterparty.builder.ConcreteReportBuilder;
import org.example.counterparty.builder.ReportBuilder;
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
        return "index";
    }

    private String buildReportFromData(org.example.counterparty.entity.CounterpartyData data) {
        ReportBuilder reportBuilder = new ConcreteReportBuilder();
        return reportBuilder
                .createNew()
                .setTitle("ОТЧЕТ ПО КОНТРАГЕНТУ")
                .addSection("\n1. ОСНОВНЫЕ СВЕДЕНИЯ:")
                .addSection("   Наименование: " + data.getName())
                .addSection("   ИНН: " + data.getInn())
                .addSection("   ОГРН: " + data.getOgrn())
                .addSection("\n2. ЮРИДИЧЕСКИЙ АДРЕС:")
                .addSection("   " + data.getAddress())
                .addSection("\n3. СТАТУС ОРГАНИЗАЦИИ:")
                .addSection("   " + formatStatus(data.getStatus()))
                .addSection("\n4. ДАТА РЕГИСТРАЦИИ:")
                .addSection("   " + data.getRegistrationDate())
                .setFooter("\nДата формирования отчета: " + java.time.LocalDate.now())
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

}
