package com.campusfood.controller;

import com.campusfood.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/panel")
public class PanelController {

    @GetMapping
    public String verPanel(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("nombreUsuario", userDetails.getNombreCompleto());
        return "panel/dashboard";
    }
}
