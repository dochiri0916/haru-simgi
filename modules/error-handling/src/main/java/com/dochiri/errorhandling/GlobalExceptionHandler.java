package com.dochiri.errorhandling;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<?> handleDomainException(DomainException e, HttpServletRequest request) {

        String traceId = MDC.get("traceId");

        log.warn(
                "domain_exception type={} code={} uri={} traceId={}",
                e.getClass().getSimpleName(),
                e.getErrorCode().name(),
                request.getRequestURI(),
                traceId
        );

        BaseException baseException = new BaseException(e.getErrorCode(), e.getProperties(), e);

        return ResponseEntity
                .status(baseException.getStatusCode())
                .body(baseException.getBody());
    }

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<?> handleBaseException(BaseException e, HttpServletRequest request) {

        String traceId = MDC.get("traceId");

        log.warn(
                "base_exception code={} uri={} traceId={}",
                e.getErrorCode().name(),
                request.getRequestURI(),
                traceId,
                e
        );

        return ResponseEntity
                .status(e.getStatusCode())
                .body(e.getBody());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        String traceId = MDC.get("traceId");

        log.warn(
                "access_denied_exception uri={} traceId={}",
                request.getRequestURI(),
                traceId,
                e
        );

        BaseException baseException = new BaseException(CommonErrorCode.FORBIDDEN, e);

        return ResponseEntity
                .status(baseException.getStatusCode())
                .body(baseException.getBody());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValid(MethodArgumentNotValidException e, HttpServletRequest request) {
        String traceId = MDC.get("traceId");

        log.warn(
                "validation_exception uri={} traceId={}",
                request.getRequestURI(),
                traceId
        );

        BaseException baseException = new BaseException(CommonErrorCode.INVALID_INPUT, e);

        return ResponseEntity
                .status(baseException.getStatusCode())
                .body(baseException.getBody());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleUnexpected(Exception e, HttpServletRequest request) {

        String traceId = MDC.get("traceId");

        log.error(
                "unexpected_exception type={} uri={} traceId={}",
                e.getClass().getSimpleName(),
                request.getRequestURI(),
                traceId,
                e
        );

        BaseException baseException = new BaseException(
                CommonErrorCode.INTERNAL_SERVER_ERROR,
                e
        );

        return ResponseEntity
                .status(baseException.getStatusCode())
                .body(baseException.getBody());
    }

}
