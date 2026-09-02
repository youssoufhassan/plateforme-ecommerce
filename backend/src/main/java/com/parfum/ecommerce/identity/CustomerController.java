package com.parfum.ecommerce.identity;

import com.parfum.ecommerce.identity.dto.CustomerResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/customers")
public class CustomerController {

    private final UserRepository userRepository;

    public CustomerController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<CustomerResponse> getAllCustomers() {
        return userRepository.findAll().stream()
                .map(u -> new CustomerResponse(u.getId(), u.getEmail(), u.getFirstName(), u.getLastName()))
                .toList();
    }
}