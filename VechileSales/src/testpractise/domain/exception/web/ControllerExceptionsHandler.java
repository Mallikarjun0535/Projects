package com.dizzion.portal.domain.exception.web;

import com.dizzion.portal.domain.exception.BusinessException;
import com.dizzion.portal.domain.exception.EntityNotFoundException;
import com.dizzion.portal.domain.exception.UniqueConstraintException;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;
import java.util.concurrent.CompletionException;

import static com.dizzion.portal.domain.exception.web.ErrorCode.INVALID_CONTENT;
import static java.util.stream.Collectors.toSet;
import static org.springframework.http.HttpStatus.*;

@ControllerAdvice
@Slf4j
public class ControllerExceptionsHandler {

    private final HandlerExceptionResolver handlerExceptionResolver;

    public ControllerExceptionsHandler(HandlerExceptionResolver handlerExceptionResolver) {
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity handleException(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(INTERNAL_SERVER_ERROR).build();
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity handleNotFound(EntityNotFoundException ex) {
        log.warn(ex.getMessage(), ex);
        return messageFromException(ex, NOT_FOUND);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity handleAccessDenied(HttpServletRequest req, AccessDeniedException ex) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.warn("Access denied for user={} trying to access URL={}", email, req.getRequestURL());
        return messageFromException(ex, FORBIDDEN);
    }

    @ExceptionHandler(ServletRequestBindingException.class)
    public ResponseEntity handleRequestBindingException(ServletRequestBindingException ex) {
        log.warn(ex.getMessage(), ex);
        return messageFromException(ex, BAD_REQUEST);
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            IllegalStateException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity handleIllegalArgument(RuntimeException ex) {
        log.warn(ex.getMessage(), ex);
        return messageFromException(ex, BAD_REQUEST);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity handleBusinessException(BusinessException ex) {
        return ResponseEntity.status(BAD_REQUEST).body(ImmutableMap.of("businessExceptionCode", ex.getErrorCode()));
    }

    @ExceptionHandler(CompletionException.class)
    public ModelAndView handleCompletionException(HttpServletRequest request, HttpServletResponse response, CompletionException ex) {
        return handlerExceptionResolver.resolveException(request, response, null, (Exception) ex.getCause());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.warn(ex.getMessage(), ex);
        return ResponseEntity.status(BAD_REQUEST).build();
    }

    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity handleRestClientResponseException(RestClientResponseException ex) {
        log.error("RestClientResponseException: " + ex.getMessage() + "; Response body:\n" + ex.getResponseBodyAsString(), ex);
        return ResponseEntity.status(BAD_REQUEST).body(ImmutableMap.of(
                "Rest client error", ex.getMessage(),
                "Response body", ex.getResponseBodyAsString()));
    }

    @ExceptionHandler
    public ResponseEntity<ValidationError> handleRequestValidationException(MethodArgumentNotValidException ex) {
        return ResponseEntity.status(BAD_REQUEST)
                .body(validationErrorFromException(ex));
    }

    @ExceptionHandler(UniqueConstraintException.class)
    public ResponseEntity handleUniqueConstraint(UniqueConstraintException ex) {
        return ResponseEntity.status(CONFLICT).body("Unique constraint violation: " + ex.getUniquePropertyName());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(UNAUTHORIZED).body("Invalid credentials");
    }

    private ResponseEntity messageFromException(Exception ex, HttpStatus status) {
        return ResponseEntity
                .status(status)
                .body(ex.getLocalizedMessage());
    }

    private ValidationError validationErrorFromException(MethodArgumentNotValidException ex) {
        Set<String> errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getField() + " " + fieldError.getDefaultMessage())
                .collect(toSet());
        return ValidationError.builder().errorCode(INVALID_CONTENT).errors(errors).build();
    }
}
