package com.ensam.bloodlink.controller;

import com.ensam.bloodlink.config.CustomUserDetails;
import com.ensam.bloodlink.dto.DemandeForm;
import com.ensam.bloodlink.entity.Hopital;
import com.ensam.bloodlink.entity.TypeProduit;
import com.ensam.bloodlink.entity.Urgence;
import com.ensam.bloodlink.exception.BusinessException;
import com.ensam.bloodlink.service.DemandeEnvoiService;
import com.ensam.bloodlink.service.GroupeSanguinService;
import com.ensam.bloodlink.service.HopitalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/hopital/demandes")
@RequiredArgsConstructor
public class DemandeEnvoiController {

    private final DemandeEnvoiService demandeEnvoiService;
    private final GroupeSanguinService groupeService;
    private final HopitalService hopitalService;
    // formulaire de demande, pre-rempli si on vient de la recherche (BF16)
    @GetMapping("/nouvelle")
    public String nouvelle(@RequestParam(required = false) Long fournisseurId,
                           @RequestParam(required = false) Long groupeId,
                           @RequestParam(required = false) TypeProduit type,
                           @AuthenticationPrincipal CustomUserDetails user, Model model) {
        DemandeForm form = new DemandeForm();
        form.setFournisseurId(fournisseurId);
        if (groupeId != null && groupeId != 0) {
            form.getLignes().get(0).setGroupeId(groupeId);
        }
        form.getLignes().get(0).setTypeProduit(type);
        model.addAttribute("form", form);
        remplirListes(model, user.getHopitalId());
        return "hopital/demande-form";
    }

    @PostMapping
    public String envoyer(@Valid @ModelAttribute("form") DemandeForm form, BindingResult result,
                          @AuthenticationPrincipal CustomUserDetails user, Model model, RedirectAttributes ra) {
        remplirListes(model, user.getHopitalId());
        if (result.hasErrors()) {
            return "hopital/demande-form";
        }
        try {
            demandeEnvoiService.envoyer(form, user.getId(), user.getHopitalId());
        } catch (BusinessException e) {
            result.reject("erreur", e.getMessage());
            return "hopital/demande-form";
        }
        ra.addFlashAttribute("message", "Demande envoyee.");
        return "redirect:/hopital/demandes/envoyees";
    }
    // suivi des demandes envoyees
    @GetMapping("/envoyees")
    public String envoyees(@RequestParam(required = false) Urgence urgence,
                           @RequestParam(defaultValue = "0") int page,
                           @AuthenticationPrincipal CustomUserDetails user, Model model) {
        model.addAttribute("page", demandeEnvoiService.envoyees(user.getHopitalId(), urgence, page));
        model.addAttribute("urgence", urgence);
        model.addAttribute("urgences", Urgence.values());
        String u = (urgence == null) ? "" : urgence.name();
        model.addAttribute("baseUrl", "/hopital/demandes/envoyees?urgence=" + u + "&");
        return "hopital/demandes-envoyees";
    }
    // listes deroulantes du formulaire
    private void remplirListes(Model model, Long monHopitalId) {
        List<Hopital> autres = new ArrayList<>();
        for (Hopital h : hopitalService.listerValides("")) {
            if (!h.getId().equals(monHopitalId)) {
                autres.add(h);
            }
        }
        model.addAttribute("hopitaux", autres);
        model.addAttribute("groupes", groupeService.lister());
        model.addAttribute("types", TypeProduit.values());
        model.addAttribute("urgences", Urgence.values());
    }
}