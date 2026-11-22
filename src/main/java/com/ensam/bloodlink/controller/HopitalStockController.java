package com.ensam.bloodlink.controller;

import com.ensam.bloodlink.config.CustomUserDetails;
import com.ensam.bloodlink.entity.TypeProduit;
import com.ensam.bloodlink.service.DemandeEnvoiService;
import com.ensam.bloodlink.service.GroupeSanguinService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/hopital")
@RequiredArgsConstructor
public class HopitalStockController {
    private final DemandeEnvoiService demandeEnvoiService;
    private final GroupeSanguinService groupeService;

    // recherche du stock des autres hopitaux, lecture seule
    @GetMapping("/stock")
    public String recherche(@RequestParam(defaultValue = "0") long groupeId,
                            @RequestParam(required = false) TypeProduit type,
                            @RequestParam(defaultValue = "") String ville,
                            @AuthenticationPrincipal CustomUserDetails user, Model model) {
        model.addAttribute("resultats", demandeEnvoiService.rechercherStock(user.getHopitalId(), groupeId, type, ville));
        model.addAttribute("groupes", groupeService.lister());
        model.addAttribute("types", TypeProduit.values());
        model.addAttribute("groupeId", groupeId);
        model.addAttribute("type", type);
        model.addAttribute("ville", ville);
        return "hopital/stock-recherche";
    }
}