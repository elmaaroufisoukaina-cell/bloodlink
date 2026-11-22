package com.ensam.bloodlink.controller;

import com.ensam.bloodlink.config.CustomUserDetails;
import com.ensam.bloodlink.entity.DemandeSang;
import com.ensam.bloodlink.entity.Urgence;
import com.ensam.bloodlink.exception.BusinessException;
import com.ensam.bloodlink.service.DemandeTraitementService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/hopital/demandes/recues")
@RequiredArgsConstructor
public class DemandeRecueController {

    private final DemandeTraitementService traitement;

    @GetMapping
    public String liste(@RequestParam(required = false) Urgence urgence,
                        @RequestParam(defaultValue = "0") int page,
                        @AuthenticationPrincipal CustomUserDetails user, Model model) {
        model.addAttribute("page", traitement.recues(user.getHopitalId(), urgence, page));
        model.addAttribute("urgence", urgence);
        model.addAttribute("urgences", Urgence.values());
        model.addAttribute("baseUrl", "/hopital/demandes/recues?urgence=" + (urgence == null ? "" : urgence) + "&");
        return "hopital/demandes-recues";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails user, Model model) {
        DemandeSang demande = traitement.trouverRecue(id, user.getHopitalId());
        model.addAttribute("demande", demande);
        model.addAttribute("propositions", traitement.propositions(demande));
        model.addAttribute("today", LocalDate.now());
        return "hopital/demande-recue-detail";
    }

    @PostMapping("/{id}/accepter")
    public String accepter(@PathVariable Long id,
                           @RequestParam(name = "choix", required = false) List<String> choix,
                           @AuthenticationPrincipal CustomUserDetails user, RedirectAttributes ra) {
        try {
            DemandeSang d = traitement.accepter(id, user.getHopitalId(), choix);
            ra.addFlashAttribute("message", "Poches envoyées. Nouveau statut de la demande : " + d.getStatut());
        } catch (BusinessException e) {
            ra.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/hopital/demandes/recues/" + id;
    }

    @PostMapping("/{id}/refuser")
    public String refuser(@PathVariable Long id,
                          @AuthenticationPrincipal CustomUserDetails user, RedirectAttributes ra) {
        try {
            traitement.refuser(id, user.getHopitalId());
            ra.addFlashAttribute("message", "Demande refusée.");
        } catch (BusinessException e) {
            ra.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/hopital/demandes/recues/" + id;
    }
}