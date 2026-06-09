package org.example.counterparty.controller;

import org.example.counterparty.entity.User;
import org.example.counterparty.service.CounterpartyService;
import org.example.counterparty.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @Autowired
    private UserService userService;

    @Autowired
    private CounterpartyService counterpartyService;

    @GetMapping("/")
    public String home(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            User user = userService.findByUsername(userDetails.getUsername()).orElse(null);
            if (user != null) {
                int remaining = counterpartyService.getRemainingRequests(user);
                model.addAttribute("remainingRequests", remaining);
            }
        }
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }
}