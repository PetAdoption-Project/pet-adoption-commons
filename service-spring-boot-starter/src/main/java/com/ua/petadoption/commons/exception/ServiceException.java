package com.ua.petadoption.commons.exception;

import org.springframework.http.HttpStatus;

public class ServiceException extends RuntimeException {

    private final HttpStatus status;
    private final ErrorCode errorCode;

    public ServiceException(HttpStatus status, ErrorCode errorCode) {
        super(errorCode.getCode());
        this.status = status;
        this.errorCode = errorCode;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
