package com.ensam.bloodlink.controller;

import com.ensam.bloodlink.exception.BusinessException;
import com.ensam.bloodlink.service.GroupeSanguinService;
import com.ensam.bloodlink.service.ParametreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminParametreController {

    private final ParametreService parametreService;
    private final GroupeSanguinService groupeService;
    // seuils d'alerte et delais
    @GetMapping("/parametres")
    public String parametres(Model model) {
        model.addAttribute("parametres", parametreService.findAll());
        model.addAttribute("groupes", groupeService.lister());
        return "admin/parametres";
    }

    @PostMapping("/parametres/{id}")
    public String modifierParametre(@PathVariable Long id, @RequestParam String valeur, RedirectAttributes ra) {
        try {
            parametreService.modifier(id, valeur);
            ra.addFlashAttribute("message", "Parametre modifie.");
        } catch (BusinessException e) {
            ra.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/admin/parametres";
    }
    @PostMapping("/groupes/{id}/seuil")
    public String modifierSeuil(@PathVariable Long id, @RequestParam Integer seuil, RedirectAttributes ra) {
        try {
            groupeService.modifierSeuil(id, seuil);
            ra.addFlashAttribute("message", "Seuil modifie.");
        } catch (BusinessException e) {
            ra.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/admin/parametres";
    }
}