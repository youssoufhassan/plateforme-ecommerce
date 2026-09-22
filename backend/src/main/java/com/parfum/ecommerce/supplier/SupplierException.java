package com.parfum.ecommerce.supplier;

public class SupplierException extends RuntimeException {

    private final SupplierErrorType type;
    private final String supplierName;

    public SupplierException(SupplierErrorType type, String supplierName, String message) {
        super(message);
        this.type = type;
        this.supplierName = supplierName;
    }

    public SupplierErrorType getType() { return type; }
    public String getSupplierName() { return supplierName; }
}