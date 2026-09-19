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
                .map(this::toResponse)
                .toList();
    }

    @PostMapping
    public AddressResponse createAddress(Authentication auth, @Valid @RequestBody AddressRequest request) {
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        Address address = new Address();
        address.setUser(user);
        address.setFirstName(request.getFirstName());
        address.setLastName(request.getLastName());
        address.setStreet(request.getStreet());
        address.setComplement(request.getComplement());
        address.setCity(request.getCity());
        address.setPostalCode(request.getPostalCode());
        address.setCountryCode(request.getCountryCode().toUpperCase());
        address.setCountry(request.getCountryCode().toUpperCase());
        address.setPhone(request.getPhone());

        return toResponse(addressRepository.save(address));
    }

    private AddressResponse toResponse(Address a) {
        return new AddressResponse(
                a.getId(), a.getFirstName(), a.getLastName(), a.getStreet(),
                a.getComplement(), a.getCity(), a.getPostalCode(),
                a.getCountryCode(), a.getPhone()
        );
    }
}