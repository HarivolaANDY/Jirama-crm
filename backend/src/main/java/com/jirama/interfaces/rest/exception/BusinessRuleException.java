package com.jirama.interfaces.rest.exception;

/**
 * Exception thrown when a business rule is violated.
 * Results in HTTP 409 Conflict.
 */
public class BusinessRuleException extends RuntimeException {

    private final String code;

    public BusinessRuleException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
