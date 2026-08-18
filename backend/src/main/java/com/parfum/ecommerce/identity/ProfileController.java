package com.parfum.ecommerce.identity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProfileController {

    @GetMapping("/api/me")
    public String me(@AuthenticationPrincipal UserDetails user) {
        return "Connecté en tant que : " + user.getUsername();
    }
}