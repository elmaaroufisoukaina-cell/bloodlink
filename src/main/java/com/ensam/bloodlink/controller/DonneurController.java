package com.ensam.bloodlink.controller;

import com.ensam.bloodlink.config.CustomUserDetails;
import com.ensam.bloodlink.entity.Donneur;
import com.ensam.bloodlink.service.DonneurService;
import com.ensam.bloodlink.service.EligibiliteService;
import com.ensam.bloodlink.service.HopitalService;
import com.ensam.bloodlink.service.RendezVousService;
import com.ensam.bloodlink.service.TracabiliteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/donneur")
@RequiredArgsConstructor
public class DonneurController {
    private final HopitalService hopitalService;
    private final DonneurService donneurService;
    private final EligibiliteService eligibiliteService;
    private final RendezVousService rendezVousService;
    private final TracabiliteService tracabiliteService;
    // liste des hopitaux valides avec filtre par ville (BF3)
    @GetMapping("/hopitaux")
    public String hopitaux(@RequestParam(defaultValue = "") String ville, Model model) {
        model.addAttribute("hopitaux", hopitalService.listerValides(ville));
        model.addAttribute("ville", ville);
        return "donneur/hopitaux";
    }

    // page "Mon espace" du donneur
    @GetMapping("/accueil")
    public String accueil(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        Donneur donneur = donneurService.trouver(user.getDonneurId());
        EligibiliteService.Resultat r = eligibiliteService.verifier(donneur, LocalDate.now());

        model.addAttribute("donneur", donneur);
        model.addAttribute("eligible", r.eligible());
        model.addAttribute("motif", r.motif());
        model.addAttribute("rendezVous", rendezVousService.historique(donneur.getId()));
        model.addAttribute("dons", donneurService.historique(donneur.getId()));
        model.addAttribute("utilisations", tracabiliteService.utilisationsPourDonneur(donneur.getId()));
        model.addAttribute("maintenant", LocalDateTime.now());
        return "donneur/accueil";
    }
}