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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public String verify(@RequestParam String innOgrn,
                         RedirectAttributes redirectAttributes) {
        
        System.out.println("INN: " + innOgrn);

        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        User user = userService.findByUsername(userDetails.getUsername()).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Пользователь не найден");
            return "redirect:/";
        }

        VerificationFacade.VerificationResult result = verificationFacade.verify(innOgrn, user);

        if (result.isSuccess()) {
            redirectAttributes.addFlashAttribute("report", result.getReport());
            redirectAttributes.addFlashAttribute("requestId", result.getRequestId());
            return "redirect:/report/current";
        } else if (result.isNotFound()) {
            redirectAttributes.addFlashAttribute("error", "Компания с ИНН/ОГРН '" + innOgrn + "' не найдена");
            return "redirect:/";
        } else {
            redirectAttributes.addFlashAttribute("error", result.getError());
            return "redirect:/";
        }
    }

    @GetMapping("/report/current")
    public String currentReport(Model model) {
        if (!model.containsAttribute("report")) {
            return "redirect:/";
        }
        return "report";
    }
}