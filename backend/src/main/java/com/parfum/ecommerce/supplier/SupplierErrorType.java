package com.parfum.ecommerce.supplier;

import org.springframework.http.HttpStatus;

public enum SupplierErrorType {

    QUOTA_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS),
    AUTH_FAILED(HttpStatus.BAD_GATEWAY),
    UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE),
    INVALID_RESPONSE(HttpStatus.BAD_GATEWAY),
    NOT_CONFIGURED(HttpStatus.SERVICE_UNAVAILABLE),
    NOT_SUPPORTED(HttpStatus.NOT_IMPLEMENTED);

    private final HttpStatus httpStatus;

    SupplierErrorType(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}