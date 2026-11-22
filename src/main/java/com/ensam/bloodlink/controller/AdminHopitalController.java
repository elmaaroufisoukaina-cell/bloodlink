package com.ensam.bloodlink.controller;

import com.ensam.bloodlink.entity.Hopital;
import com.ensam.bloodlink.exception.BusinessException;
import com.ensam.bloodlink.service.HopitalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/hopitaux")
@RequiredArgsConstructor
public class AdminHopitalController {

    private final HopitalService hopitalService;

    @GetMapping
    public String liste(@RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("page", hopitalService.lister(page));
        model.addAttribute("baseUrl", "/admin/hopitaux?");
        return "admin/hopitaux";
    }
    @GetMapping("/nouveau")
    public String nouveau(Model model) {
        Hopital h = new Hopital();
        h.setDureeCreneauMin(30);
        h.setCapaciteParCreneau(3);
        model.addAttribute("hopital", h);
        model.addAttribute("edition", false);
        return "admin/hopital-form";
    }
    @PostMapping
    public String creer(@Valid @ModelAttribute("hopital") Hopital hopital, BindingResult result,
                        Model model, RedirectAttributes ra) {
        model.addAttribute("edition", false);
        if (result.hasErrors()) {
            return "admin/hopital-form";
        }
        try {
            hopitalService.enregistrer(hopital);
        } catch (BusinessException e) {
            result.reject("erreur", e.getMessage());
            return "admin/hopital-form";
        }
        ra.addFlashAttribute("message", "Hopital cree.");
        return "redirect:/admin/hopitaux";
    }
    @GetMapping("/{id}/modifier")
    public String modifierForm(@PathVariable Long id, Model model) {
        model.addAttribute("hopital", hopitalService.trouver(id));
        model.addAttribute("edition", true);
        return "admin/hopital-form";
    }
    @PostMapping("/{id}")
    public String modifier(@PathVariable Long id, @Valid @ModelAttribute("hopital") Hopital hopital,
                           BindingResult result, Model model, RedirectAttributes ra) {
        hopital.setId(id);
        model.addAttribute("edition", true);
        if (result.hasErrors()) {
            return "admin/hopital-form";
        }
        try {
            hopitalService.enregistrer(hopital);
        } catch (BusinessException e) {
            result.reject("erreur", e.getMessage());
            return "admin/hopital-form";
        }
        ra.addFlashAttribute("message", "Hopital modifie.");
        return "redirect:/admin/hopitaux";
    }
    // valider ou invalider
    @PostMapping("/{id}/valider")
    public String valider(@PathVariable Long id, RedirectAttributes ra) {
        hopitalService.basculerValidation(id);
        ra.addFlashAttribute("message", "Statut de l'hopital modifie.");
        return "redirect:/admin/hopitaux";
    }
}