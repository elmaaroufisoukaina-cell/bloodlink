package com.ensam.bloodlink.controller;

import com.ensam.bloodlink.service.DonneurService;
import com.ensam.bloodlink.service.GroupeSanguinService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/admin/donneurs")
@RequiredArgsConstructor
public class AdminDonneurController {

    private final DonneurService donneurService;
    private final GroupeSanguinService groupeService;

    // recherche des donneurs par groupe et par ville
    @GetMapping
    public String liste(@RequestParam(defaultValue = "0") long groupeId,
                        @RequestParam(defaultValue = "") String ville,
                        @RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("page", donneurService.rechercher(groupeId, ville, page));
        model.addAttribute("groupes", groupeService.lister());
        model.addAttribute("groupeId", groupeId);
        model.addAttribute("ville", ville);
        String v = URLEncoder.encode(ville, StandardCharsets.UTF_8);
        model.addAttribute("baseUrl", "/admin/donneurs?groupeId=" + groupeId + "&ville=" + v + "&");
        return "admin/donneurs";
    }
}