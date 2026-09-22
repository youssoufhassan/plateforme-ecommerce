package com.parfum.ecommerce.identity;

import com.parfum.ecommerce.identity.dto.CustomerResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
        @GetMapping("/page")
    public com.parfum.ecommerce.common.dto.PageResponse<CustomerResponse> getCustomersPage(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        int safeSize = (size <= 0) ? 20 : Math.min(size, 50);
        var pageable = org.springframework.data.domain.PageRequest.of(
                Math.max(0, page), safeSize, org.springframework.data.domain.Sort.by("email"));

        var result = (q == null || q.isBlank())
                ? userRepository.findAll(pageable)
                : userRepository.findByEmailContainingIgnoreCase(q.trim(), pageable);

        return com.parfum.ecommerce.common.dto.PageResponse.of(result,
                u -> new CustomerResponse(u.getId(), u.getEmail(), u.getFirstName(), u.getLastName()));
    }
}