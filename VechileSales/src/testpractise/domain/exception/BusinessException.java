package com.dizzion.portal.domain.exception;

public class BusinessException extends RuntimeException {
    private final String errorCode;

    public BusinessException(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
