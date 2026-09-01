package com.parfum.ecommerce.identity;

import com.parfum.ecommerce.identity.dto.AddressRequest;
import com.parfum.ecommerce.identity.dto.AddressResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public AddressController(AddressRepository addressRepository, UserRepository userRepository) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<AddressResponse> getMyAddresses(Authentication auth) {
        return addressRepository.findByUserEmail(auth.getName())
                .stream()
                .map(a -> new AddressResponse(a.getId(), a.getStreet(), a.getCity(), a.getPostalCode(), a.getCountry()))
                .toList();
    }

    @PostMapping
    public AddressResponse createAddress(Authentication auth, @Valid @RequestBody AddressRequest request) {
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        Address address = new Address(user, request.getStreet(), request.getCity(), request.getPostalCode(), request.getCountry());
        Address saved = addressRepository.save(address);

        return new AddressResponse(saved.getId(), saved.getStreet(), saved.getCity(), saved.getPostalCode(), saved.getCountry());
    }
}