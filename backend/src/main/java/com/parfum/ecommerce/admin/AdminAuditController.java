package com.parfum.ecommerce.admin;

import com.parfum.ecommerce.common.dto.PageResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/audit-logs")
public class AdminAuditController {

    private final AdminAuditLogRepository repository;

    public AdminAuditController(AdminAuditLogRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public PageResponse<AdminAuditLog> list(@RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "50") int size) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        return PageResponse.of(
                repository.findAllByOrderByCreatedAtDesc(PageRequest.of(Math.max(0, page), safeSize)),
                log -> log);
    }
}