package com.parfum.ecommerce.supplier;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "supplier_sync_logs")
public class SupplierSyncLog {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "supplier_key", nullable = false)
    private String supplierKey;

    /** IMPORT ou STOCK_SYNC */
    @Column(nullable = false)
    private String operation;

    /** SUCCESS, PARTIAL ou FAILED */
    @Column(nullable = false)
    private String status;

    @Column(name = "error_type")
    private String errorType;

    @Column(length = 500)
    private String message;

    @Column(name = "items_processed", nullable = false)
    private int itemsProcessed;

    @Column(name = "items_skipped", nullable = false)
    private int itemsSkipped;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    public SupplierSyncLog() {}

    public SupplierSyncLog(String supplierKey, String operation) {
        this.supplierKey = supplierKey;
        this.operation = operation;
        this.status = "RUNNING";
        this.startedAt = LocalDateTime.now();
    }

    public void succeed(int processed, int skipped, String message) {
        this.status = skipped > 0 && processed == 0 ? "PARTIAL" : "SUCCESS";
        this.itemsProcessed = processed;
        this.itemsSkipped = skipped;
        this.message = truncate(message);
        this.finishedAt = LocalDateTime.now();
    }

    public void fail(SupplierErrorType type, String message) {
        this.status = "FAILED";
        this.errorType = type != null ? type.name() : null;
        this.message = truncate(message);
        this.finishedAt = LocalDateTime.now();
    }

    private String truncate(String value) {
        if (value == null) return null;
        return value.length() > 500 ? value.substring(0, 500) : value;
    }

    public UUID getId() { return id; }
    public String getSupplierKey() { return supplierKey; }
    public String getOperation() { return operation; }
    public String getStatus() { return status; }
    public String getErrorType() { return errorType; }
    public String getMessage() { return message; }
    public int getItemsProcessed() { return itemsProcessed; }
    public int getItemsSkipped() { return itemsSkipped; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public LocalDateTime getFinishedAt() { return finishedAt; }
}