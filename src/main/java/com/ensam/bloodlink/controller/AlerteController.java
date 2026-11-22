package com.ensam.bloodlink.controller;

import com.ensam.bloodlink.config.CustomUserDetails;
import com.ensam.bloodlink.service.AlerteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class AlerteController {

    private final AlerteService alerteService;

    // BF14 : declencher l'alerte de penurie
    @GetMapping("/stock/alerte")
    public String alerte(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        model.addAttribute("alertes", alerteService.verifier(user.getHopitalId()));
        return "stock/alerte";
    }
}
