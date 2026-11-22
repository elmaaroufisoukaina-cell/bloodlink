package com.ensam.bloodlink.controller;

import com.ensam.bloodlink.config.CustomUserDetails;
import com.ensam.bloodlink.dto.DonForm;
import com.ensam.bloodlink.entity.Don;
import com.ensam.bloodlink.entity.StatutDon;
import com.ensam.bloodlink.exception.BusinessException;
import com.ensam.bloodlink.service.DonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/medecin")
@RequiredArgsConstructor
public class MedecinDonController {

    private final DonService donService;

    @GetMapping("/don/nouveau")
    public String formulaire(@RequestParam(required = false) String cin, Model model) {
        DonForm form = new DonForm();
        form.setCin(cin);
        model.addAttribute("donForm", form);
        return "medecin/don-form";
    }

    @PostMapping("/don")
    public String enregistrer(@Valid @ModelAttribute("donForm") DonForm form, BindingResult result,
                              @AuthenticationPrincipal CustomUserDetails user, RedirectAttributes ra) {
        if (result.hasErrors()) {
            return "medecin/don-form";
        }
        try {
            Don don = donService.enregistrer(form, user.getId());
            if (don.getStatut() == StatutDon.REALISE) {
                ra.addFlashAttribute("message", "Don enregistré : les poches ont été ajoutées au stock.");
            } else {
                ra.addFlashAttribute("erreur", "Donneur non éligible : " + don.getQuestionnaire().getMotifRefus());
            }
            return "redirect:/medecin/dons";
        } catch (BusinessException e) {
            result.reject("metier", e.getMessage());
            return "medecin/don-form";
        }
    }

    @GetMapping("/dons")
    public String dons(@RequestParam(defaultValue = "0") int page,
                       @AuthenticationPrincipal CustomUserDetails user, Model model) {
        model.addAttribute("page", donService.listerPourHopital(user.getHopitalId(), page));
        model.addAttribute("baseUrl", "/medecin/dons?");
        return "medecin/dons";
    }
}