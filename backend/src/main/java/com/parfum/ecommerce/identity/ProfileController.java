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
        return toResponse(findUser(auth.getName()));
    }

    @PutMapping
    public ProfileResponse updateProfile(Authentication auth, @RequestBody UpdateProfileRequest request) {
        User user = findUser(auth.getName());

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());

        userRepository.save(user);
        return toResponse(user);
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
    }

    private ProfileResponse toResponse(User user) {
        return new ProfileResponse(
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.isEmailVerified()
        );
    }
}