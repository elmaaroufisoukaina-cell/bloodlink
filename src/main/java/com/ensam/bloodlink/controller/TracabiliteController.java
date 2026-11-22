package com.ensam.bloodlink.controller;

import com.ensam.bloodlink.config.CustomUserDetails;
import com.ensam.bloodlink.service.TracabiliteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class TracabiliteController {

    private final TracabiliteService tracabiliteService;

    @GetMapping("/stock/poches/{id}/tracer")
    public String tracer(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails user, Model model) {
        model.addAttribute("poche", tracabiliteService.tracer(id, user.getHopitalId()));
        return "stock/trace";
    }
}