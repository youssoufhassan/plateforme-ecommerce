package com.parfum.ecommerce.admin;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "admin_audit_logs")
public class AdminAuditLog {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "admin_email", nullable = false)
    private String adminEmail;

    @Column(name = "http_method", nullable = false)
    private String httpMethod;

    @Column(nullable = false)
    private String path;

    @Column(name = "status_code", nullable = false)
    private int statusCode;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public AdminAuditLog() {}

    public AdminAuditLog(String adminEmail, String httpMethod, String path, int statusCode, String ipAddress) {
        this.adminEmail = adminEmail;
        this.httpMethod = httpMethod;
        this.path = path;
        this.statusCode = statusCode;
        this.ipAddress = ipAddress;
    }

    public UUID getId() { return id; }
    public String getAdminEmail() { return adminEmail; }
    public String getHttpMethod() { return httpMethod; }
    public String getPath() { return path; }
    public int getStatusCode() { return statusCode; }
    public String getIpAddress() { return ipAddress; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}