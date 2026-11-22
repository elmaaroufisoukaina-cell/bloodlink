package com.ensam.bloodlink.controller;

import com.ensam.bloodlink.config.CustomUserDetails;
import com.ensam.bloodlink.dto.PocheForm;
import com.ensam.bloodlink.entity.PocheSanguine;
import com.ensam.bloodlink.entity.StatutPoche;
import com.ensam.bloodlink.exception.BusinessException;
import com.ensam.bloodlink.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    // BF11 : jauges + liste des poches de mon hopital
    @GetMapping
    public String liste(@RequestParam(required = false) StatutPoche statut,
                        @RequestParam(defaultValue = "0") int page,
                        @AuthenticationPrincipal CustomUserDetails user, Model model) {
        model.addAttribute("jauges", stockService.jauges(user.getHopitalId()));
        model.addAttribute("page", stockService.lister(user.getHopitalId(), statut, page));
        model.addAttribute("statut", statut);
        model.addAttribute("statuts", StatutPoche.values());
        model.addAttribute("baseUrl", "/stock?statut=" + (statut == null ? "" : statut) + "&");
        return "stock/liste";
    }

    // BF12 : creer, modifier, marquer perimee, supprimer
    @GetMapping("/poches/nouvelle")
    public String nouvelle(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        model.addAttribute("form", new PocheForm());
        model.addAttribute("dons", stockService.donsRealises(user.getHopitalId()));
        model.addAttribute("edition", false);
        return "stock/poche-form";
    }

    @PostMapping("/poches")
    public String creer(@Valid @ModelAttribute("form") PocheForm form, BindingResult result,
                        @AuthenticationPrincipal CustomUserDetails user, Model model, RedirectAttributes ra) {
        model.addAttribute("dons", stockService.donsRealises(user.getHopitalId()));
        model.addAttribute("edition", false);
        if (result.hasErrors()) {
            return "stock/poche-form";
        }
        try {
            stockService.creer(form, user.getHopitalId());
        } catch (BusinessException e) {
            result.reject("metier", e.getMessage());
            return "stock/poche-form";
        }
        ra.addFlashAttribute("message", "Poche ajoutée au stock.");
        return "redirect:/stock";
    }

    @GetMapping("/poches/{id}/modifier")
    public String modifierForm(@PathVariable Long id,
                               @AuthenticationPrincipal CustomUserDetails user, Model model) {
        PocheSanguine p = stockService.trouverPourHopital(id, user.getHopitalId());
        PocheForm form = new PocheForm();
        form.setNumeroSerie(p.getNumeroSerie());
        form.setTypeProduit(p.getTypeProduit());
        form.setVolumeMl(p.getVolumeMl());
        form.setDatePrelevement(p.getDatePrelevement());
        form.setDatePeremption(p.getDatePeremption());
        form.setDonId(p.getDon().getId());
        model.addAttribute("form", form);
        model.addAttribute("pocheId", id);
        model.addAttribute("edition", true);
        return "stock/poche-form";
    }

    @PostMapping("/poches/{id}")
    public String modifier(@PathVariable Long id, @Valid @ModelAttribute("form") PocheForm form,
                           BindingResult result, @AuthenticationPrincipal CustomUserDetails user,
                           Model model, RedirectAttributes ra) {
        model.addAttribute("pocheId", id);
        model.addAttribute("edition", true);
        if (result.hasErrors()) {
            return "stock/poche-form";
        }
        try {
            stockService.modifier(id, form, user.getHopitalId());
        } catch (BusinessException e) {
            result.reject("metier", e.getMessage());
            return "stock/poche-form";
        }
        ra.addFlashAttribute("message", "Poche modifiée.");
        return "redirect:/stock";
    }

    @PostMapping("/poches/{id}/supprimer")
    public String supprimer(@PathVariable Long id,
                            @AuthenticationPrincipal CustomUserDetails user, RedirectAttributes ra) {
        try {
            stockService.supprimer(id, user.getHopitalId());
            ra.addFlashAttribute("message", "Poche supprimée.");
        } catch (BusinessException e) {
            ra.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/stock";
    }

    @PostMapping("/poches/{id}/perimer")
    public String perimer(@PathVariable Long id,
                          @AuthenticationPrincipal CustomUserDetails user, RedirectAttributes ra) {
        try {
            stockService.marquerPerimee(id, user.getHopitalId());
            ra.addFlashAttribute("message", "Poche marquée comme périmée.");
        } catch (BusinessException e) {
            ra.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/stock";
    }
}