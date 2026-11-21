package com.ensam.bloodlink.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

// ajoute le role et l'email a toutes les pages
@ControllerAdvice
public class GlobalModelAdvice {

    @ModelAttribute
    public void ajouterUtilisateur(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails user) {
            model.addAttribute("role", user.getRole().name());
            model.addAttribute("utilisateurEmail", user.getEmail());
        }
    }
}