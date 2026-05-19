package org.example.counterparty.controller;

import org.example.counterparty.entity.User;
import org.example.counterparty.service.UserService;
import org.example.counterparty.service.VerificationFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class VerificationController {

    @Autowired
    private VerificationFacade verificationFacade;

    @Autowired
    private UserService userService;

//    @GetMapping("/")
//    public String home() {
//        return "index";
//    }

    @PostMapping("/verify")
    public String verify(@RequestParam String innOgrn, Model model) {
        System.out.println("=== verify() called ===");
        System.out.println("INN: " + innOgrn);

        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        User user = userService.findByUsername(userDetails.getUsername()).orElse(null);
        if (user == null) {
            System.out.println("User not found!");
            model.addAttribute("error", "Пользователь не найден");
            return "index";
        }

        System.out.println("User: " + user.getUsername());

        VerificationFacade.VerificationResult result = verificationFacade.verify(innOgrn, user);

        System.out.println("Result - success: " + result.isSuccess());
        System.out.println("Result - notFound: " + result.isNotFound());
        System.out.println("Result - error: " + result.getError());

        if (result.isSuccess()) {
            System.out.println("Report length: " + (result.getReport() != null ? result.getReport().length() : 0));
            model.addAttribute("report", result.getReport());
            model.addAttribute("requestId", result.getRequestId());
        } else if (result.isNotFound()) {
            model.addAttribute("error", "Компания не найдена");
        } else {
            model.addAttribute("error", result.getError());
        }

        return "index";
    }
}