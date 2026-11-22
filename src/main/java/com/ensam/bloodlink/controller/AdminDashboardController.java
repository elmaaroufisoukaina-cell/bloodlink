package com.ensam.bloodlink.controller;

import com.ensam.bloodlink.service.DashboardService;
import com.ensam.bloodlink.service.ExpirationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final DashboardService dashboardService;
    private final ExpirationService expirationService;

    // tableau de bord
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("d", dashboardService.construire());
        return "admin/dashboard";
    }

    // lance la peremption tout de suite
    @PostMapping("/expirer")
    public String expirer(RedirectAttributes ra) {
        int nombre = expirationService.perimerPochesExpirees();
        ra.addFlashAttribute("message", nombre + " poche(s) passee(s) au statut PERIMEE.");
        return "redirect:/admin/dashboard";
    }
}