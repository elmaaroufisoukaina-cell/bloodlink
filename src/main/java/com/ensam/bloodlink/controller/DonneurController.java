package com.ensam.bloodlink.controller;

import com.ensam.bloodlink.service.HopitalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/donneur")
@RequiredArgsConstructor
public class DonneurController {
    private final HopitalService hopitalService;
    // liste des hopitaux valides avec filtre par ville (BF3)
    @GetMapping("/hopitaux")
    public String hopitaux(@RequestParam(defaultValue = "") String ville, Model model) {
        model.addAttribute("hopitaux", hopitalService.listerValides(ville));
        model.addAttribute("ville", ville);
        return "donneur/hopitaux";
    }
}