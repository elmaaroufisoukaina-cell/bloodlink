package com.ensam.bloodlink.controller;

import com.ensam.bloodlink.config.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    // redirige chaque role vers sa page apres la connexion
    @GetMapping({"/","/accueil"})
    public String accueil(@AuthenticationPrincipal CustomUserDetails user) {
        switch (user.getRole()) {
            case ADMIN_SYSTEME:
                return "redirect:/admin/dashboard";
            case ADMIN_HOPITAL:
                return "redirect:/hopital/stock";
            case MEDECIN:
                return "redirect:/medecin/rendez-vous";
            case GESTIONNAIRE_STOCK:
                return "redirect:/stock";
            default:
                return "redirect:/donneur/accueil";
        }
    }
}