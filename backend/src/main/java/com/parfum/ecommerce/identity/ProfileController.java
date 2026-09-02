package com.parfum.ecommerce.identity;

import com.parfum.ecommerce.identity.dto.ProfileResponse;
import com.parfum.ecommerce.identity.dto.UpdateProfileRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/me")
public class ProfileController {

    private final UserRepository userRepository;

    public ProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public ProfileResponse getProfile(Authentication auth) {
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
        return toResponse(user);
    }

    @PutMapping
    public ProfileResponse updateProfile(Authentication auth, @RequestBody UpdateProfileRequest request) {
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());

        userRepository.save(user);
        return toResponse(user);
    }

    private ProfileResponse toResponse(User user) {
        return new ProfileResponse(user.getEmail(), user.getFirstName(), user.getLastName(), user.getPhone());
    }
}