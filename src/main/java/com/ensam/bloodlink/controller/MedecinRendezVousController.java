package com.ensam.bloodlink.controller;

import com.ensam.bloodlink.config.CustomUserDetails;
import com.ensam.bloodlink.entity.StatutRendezVous;
import com.ensam.bloodlink.service.RendezVousService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/medecin")
@RequiredArgsConstructor
public class MedecinRendezVousController {
    private final RendezVousService rendezVousService;
    // le medecin voit seulement les rdv de son hopital (BF10, R12)
    @GetMapping("/rendez-vous")
    public String liste(@RequestParam(required = false) StatutRendezVous statut,
                        @RequestParam(defaultValue = "0") int page,
                        @AuthenticationPrincipal CustomUserDetails user, Model model) {
        model.addAttribute("page", rendezVousService.pourHopital(user.getHopitalId(), statut, page));
        model.addAttribute("statut", statut);
        model.addAttribute("statuts", StatutRendezVous.values());
        String s = (statut == null) ? "" : statut.name();
        model.addAttribute("baseUrl", "/medecin/rendez-vous?statut=" + s + "&");
        return "medecin/rendez-vous";
    }
}