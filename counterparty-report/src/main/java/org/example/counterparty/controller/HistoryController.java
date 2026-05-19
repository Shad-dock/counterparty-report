package org.example.counterparty.controller;

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
        return "Отчет по контрагенту\n" +
                "Наименование: " + data.getName() + "\n" +
                "ИНН: " + data.getInn() + "\n" +
                "ОГРН: " + data.getOgrn() + "\n" +
                "Адрес: " + data.getAddress() + "\n" +
                "Статус: " + data.getStatus() + "\n" +
                "Дата регистрации: " + data.getRegistrationDate();
    }
}
