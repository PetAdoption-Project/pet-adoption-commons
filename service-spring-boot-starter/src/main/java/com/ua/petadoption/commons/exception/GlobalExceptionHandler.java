package com.ua.petadoption.commons.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Locale;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String PROPERTY_CODE = "code";
    private static final String PROPERTY_ERRORS = "errors";

    private final MessageSource messageSource;

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ProblemDetail> handleServiceException(ServiceException ex, Locale locale) {
        ErrorCode errorCode = ex.getErrorCode();
        String message = resolveMessage(errorCode, locale);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(ex.getStatus(), message);
        problem.setProperty(PROPERTY_CODE, errorCode.getCode());
        log.warn("ServiceException: {}", errorCode.getCode());
        return ResponseEntity.status(ex.getStatus()).body(problem);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String message = resolveMessage(CommonErrorCode.VALIDATION_ERROR, resolveLocale(request));
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
        problem.setProperty(PROPERTY_ERRORS, ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new FieldError(fe.getField(), fe.getDefaultMessage()))
                .toList());
        log.warn("Validation failed: {}", problem.getProperties());
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGeneral(Exception ex, Locale locale) {
        log.error("Unexpected error", ex);
        String message = resolveMessage(CommonErrorCode.INTERNAL_ERROR, locale);
        return ResponseEntity.internalServerError()
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, message));
    }

    private String resolveMessage(ErrorCode errorCode, Locale locale) {
        try {
            return messageSource.getMessage(errorCode.getMessageKey(), null, locale);
        } catch (NoSuchMessageException e) {
            return errorCode.getCode();
        }
    }

    private Locale resolveLocale(WebRequest request) {
        String acceptLanguage = request.getHeader(HttpHeaders.ACCEPT_LANGUAGE);
        if (acceptLanguage == null) return Locale.ENGLISH;
        return Locale.forLanguageTag(acceptLanguage.split(",")[0].trim());
    }

    public record FieldError(String field, String message) {}
}
