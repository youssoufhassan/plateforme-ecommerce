package com.parfum.ecommerce.identity;

import com.parfum.ecommerce.admin.AdminDetailService;
import com.parfum.ecommerce.admin.dto.CustomerDetailResponse;
import com.parfum.ecommerce.common.dto.PageResponse;
import com.parfum.ecommerce.identity.dto.CustomerResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/customers")
public class CustomerController {

    private final UserRepository userRepository;
    private final AdminDetailService adminDetailService;

    public CustomerController(UserRepository userRepository, AdminDetailService adminDetailService) {
        this.userRepository = userRepository;
        this.adminDetailService = adminDetailService;
    }

    @GetMapping
    public List<CustomerResponse> getAllCustomers() {
        return userRepository.findAll().stream()
                .map(u -> new CustomerResponse(u.getId(), u.getEmail(), u.getFirstName(), u.getLastName()))
                .toList();
    }

    @GetMapping("/page")
    public PageResponse<CustomerResponse> getCustomersPage(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        int safeSize = (size <= 0) ? 20 : Math.min(size, 50);
        var pageable = PageRequest.of(Math.max(0, page), safeSize, Sort.by("email"));

        var result = (q == null || q.isBlank())
                ? userRepository.findAll(pageable)
                : userRepository.findByEmailContainingIgnoreCase(q.trim(), pageable);

        return PageResponse.of(result,
                u -> new CustomerResponse(u.getId(), u.getEmail(), u.getFirstName(), u.getLastName()));
    }

    /** Doit rester APRÈS /page, sinon Spring prendrait "page" pour un identifiant. */
    @GetMapping("/{userId}")
    public CustomerDetailResponse detail(@PathVariable UUID userId) {
        return adminDetailService.customerDetail(userId);
    }
}