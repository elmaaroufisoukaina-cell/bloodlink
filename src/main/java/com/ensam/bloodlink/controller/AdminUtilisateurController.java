package com.ensam.bloodlink.controller;

import com.ensam.bloodlink.config.CustomUserDetails;
import com.ensam.bloodlink.dto.PersonnelForm;
import com.ensam.bloodlink.entity.Role;
import com.ensam.bloodlink.exception.BusinessException;
import com.ensam.bloodlink.service.HopitalService;
import com.ensam.bloodlink.service.UtilisateurAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/utilisateurs")
@RequiredArgsConstructor
public class AdminUtilisateurController {

    private static final List<Role> ROLES_PERSONNEL = List.of(Role.ADMIN_HOPITAL, Role.MEDECIN, Role.GESTIONNAIRE_STOCK);
    private final UtilisateurAdminService utilisateurService;
    private final HopitalService hopitalService;

    @GetMapping
    public String liste(@RequestParam(required = false) Role role,
                        @RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("page", utilisateurService.lister(role, page));
        model.addAttribute("roleFiltre", role);
        model.addAttribute("roles", Role.values());
        model.addAttribute("rolesPersonnel", ROLES_PERSONNEL);
        String r = (role == null) ? "" : role.name();
        model.addAttribute("baseUrl", "/admin/utilisateurs?role=" + r + "&");
        return "admin/utilisateurs";
    }

    @GetMapping("/nouveau")
    public String nouveau(Model model) {
        model.addAttribute("form", new PersonnelForm());
        model.addAttribute("hopitaux", hopitalService.listerTous());
        model.addAttribute("rolesPersonnel", ROLES_PERSONNEL);
        return "admin/utilisateur-form";
    }

    @PostMapping
    public String creer(@Valid @ModelAttribute("form") PersonnelForm form, BindingResult result,
                        Model model, RedirectAttributes ra) {
        model.addAttribute("hopitaux", hopitalService.listerTous());
        model.addAttribute("rolesPersonnel", ROLES_PERSONNEL);
        if (result.hasErrors()) {
            return "admin/utilisateur-form";
        }
        try {
            utilisateurService.creerPersonnel(form);
        } catch (BusinessException e) {
            result.reject("erreur", e.getMessage());
            return "admin/utilisateur-form";
        }
        ra.addFlashAttribute("message", "Compte cree.");
        return "redirect:/admin/utilisateurs";
    }

    @PostMapping("/{id}/actif")
    public String basculerActif(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails user, RedirectAttributes ra) {
        try {
            utilisateurService.basculerActif(id, user.getId());
            ra.addFlashAttribute("message", "Compte mis a jour.");
        } catch (BusinessException e) {
            ra.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/admin/utilisateurs";
    }

    @PostMapping("/{id}/role")
    public String changerRole(@PathVariable Long id, @RequestParam Role role, RedirectAttributes ra) {
        try {
            utilisateurService.changerRole(id, role);
            ra.addFlashAttribute("message", "Role modifie.");
        } catch (BusinessException e) {
            ra.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/admin/utilisateurs";
    }
}