package com.re.busticket.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class UserController {

    @GetMapping
    public String home(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/login";
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> "ADMIN".equals(grantedAuthority.getAuthority()));
        if (isAdmin) {
            return "redirect:/admin/dashboard";
        }

        boolean isPassenger = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> "PASSENGER".equals(grantedAuthority.getAuthority()));
        if (isPassenger) {
            return "redirect:/passenger/dashboard";
        }

        return "redirect:/login";
    }
}
