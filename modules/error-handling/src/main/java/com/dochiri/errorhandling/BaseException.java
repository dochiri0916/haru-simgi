package com.dochiri.errorhandling;

import org.slf4j.MDC;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class BaseException extends ErrorResponseException {

    private final ErrorCode errorCode;

    public BaseException(ErrorCode errorCode) {
        this(errorCode, Map.of(), null);
    }

    public BaseException(ErrorCode errorCode, Map<String, Object> properties) {
        this(errorCode, properties, null);
    }

    public BaseException(ErrorCode errorCode, Throwable cause) {
        this(errorCode, Map.of(), cause);
    }

    public BaseException(ErrorCode errorCode, Map<String, Object> properties, Throwable cause) {
        super(requireErrorCode(errorCode).getHttpStatus(), createBody(errorCode, properties), cause);
        this.errorCode = errorCode;
    }

    public static BaseException of(ErrorCode errorCode, Object... keyValues) {
        return new BaseException(errorCode, mapArgs(keyValues));
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    private static ProblemDetail createBody(ErrorCode errorCode, Map<String, Object> properties) {
        ProblemDetail body = ProblemDetail.forStatusAndDetail(
                errorCode.getHttpStatus(),
                errorCode.getMessage()
        );

        body.setType(URI.create("/errors/" + toKebabCase(errorCode.name())));
        body.setTitle(errorCode.name());

        body.setProperty("code", errorCode.name());

        String traceId = MDC.get("traceId");
        if (traceId != null) {
            body.setProperty("traceId", traceId);
        }

        properties.forEach(body::setProperty);

        return body;
    }

    private static ErrorCode requireErrorCode(ErrorCode errorCode) {
        return Objects.requireNonNull(errorCode, "errorCode는 필수입니다.");
    }

    private static String toKebabCase(String name) {
        return name.toLowerCase().replace('_', '-');
    }

    private static Map<String, Object> mapArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return Map.of();
        }

        if (args.length % 2 != 0) {
            throw new IllegalArgumentException("args는 키/값 쌍이어야 합니다.");
        }

        Map<String, Object> mapped = new LinkedHashMap<>();
        for (int i = 0; i < args.length; i += 2) {
            mapped.put(String.valueOf(args[i]), args[i + 1]);
        }
        return Map.copyOf(mapped);
    }

}