package com.ua.petadoption.commons.exception;

public enum CommonErrorCode implements ErrorCode {

    VALIDATION_ERROR("error.validation"),
    INTERNAL_ERROR("error.internal"),
    UNAUTHORIZED("error.unauthorized"),
    FORBIDDEN("error.forbidden"),
    NOT_FOUND("error.not.found");

    private final String messageKey;

    CommonErrorCode(String messageKey) {
        this.messageKey = messageKey;
    }

    @Override
    public String getCode() {
        return this.name();
    }

    @Override
    public String getMessageKey() {
        return messageKey;
    }
}
