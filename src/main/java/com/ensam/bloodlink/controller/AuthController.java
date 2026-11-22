package com.ensam.bloodlink.controller;

import com.ensam.bloodlink.dto.InscriptionForm;
import com.ensam.bloodlink.exception.BusinessException;
import com.ensam.bloodlink.service.DonneurService;
import com.ensam.bloodlink.service.GroupeSanguinService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final DonneurService donneurService;
    private final GroupeSanguinService groupeService;
    @GetMapping("/login")
    public String login() {
        return "login";
    }
    // formulaire d'inscription du donneur (BF2)
    @GetMapping("/inscription")
    public String formulaire(Model model) {
        model.addAttribute("form", new InscriptionForm());
        model.addAttribute("groupes", groupeService.lister());
        return "inscription";
    }
    @PostMapping("/inscription")
    public String inscrire(@Valid @ModelAttribute("form") InscriptionForm form, BindingResult result,
                           Model model, RedirectAttributes ra) {
        model.addAttribute("groupes", groupeService.lister());
        if (result.hasErrors()) {
            return "inscription";
        }
        try {
            donneurService.inscrire(form);
        } catch (BusinessException e) {
            result.reject("erreur", e.getMessage());
            return "inscription";
        }
        ra.addFlashAttribute("message", "Compte cree, vous pouvez vous connecter.");
        return "redirect:/login";
    }
}