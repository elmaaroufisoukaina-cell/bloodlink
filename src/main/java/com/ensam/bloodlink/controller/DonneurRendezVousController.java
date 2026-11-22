package com.ensam.bloodlink.controller;

import com.ensam.bloodlink.config.CustomUserDetails;
import com.ensam.bloodlink.exception.BusinessException;
import com.ensam.bloodlink.service.HopitalService;
import com.ensam.bloodlink.service.RendezVousService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

@Controller
@RequestMapping("/donneur")
@RequiredArgsConstructor
public class DonneurRendezVousController {
    private final RendezVousService rendezVousService;
    private final HopitalService hopitalService;
    // creneaux d'un hopital pour un jour (BF4)
    @GetMapping("/hopitaux/{id}/rendez-vous")
    public String creneaux(@PathVariable Long id,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate jour,
                           Model model) {
        if (jour == null) {
            jour = LocalDate.now();
        }
        model.addAttribute("hopital", hopitalService.trouverValide(id));
        model.addAttribute("jour", jour);
        model.addAttribute("creneaux", rendezVousService.creneauxDisponibles(id, jour));
        return "donneur/creneaux";
    }
    @PostMapping("/rendez-vous")
    public String reserver(@RequestParam Long hopitalId, @RequestParam String dateHeure,
                           @AuthenticationPrincipal CustomUserDetails user, RedirectAttributes ra) {
        try {
            rendezVousService.reserver(user.getDonneurId(), hopitalId, LocalDateTime.parse(dateHeure));
            ra.addFlashAttribute("message", "Rendez-vous confirme.");
            return "redirect:/donneur/accueil";
        } catch (BusinessException | DateTimeParseException e) {
            ra.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/donneur/hopitaux/" + hopitalId + "/rendez-vous";
        }
    }

    // annulation (BF5)
    @PostMapping("/rendez-vous/{id}/annuler")
    public String annuler(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails user,
                          RedirectAttributes ra) {
        try {
            rendezVousService.annuler(id, user.getDonneurId());
            ra.addFlashAttribute("message", "Rendez-vous annule.");
        } catch (BusinessException e) {
            ra.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/donneur/accueil";
    }
}