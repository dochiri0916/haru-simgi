package com.dochiri.errorhandling;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class DomainException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Map<String, Object> properties;

    protected DomainException(ErrorCode errorCode) {
        this(errorCode, (Throwable) null);
    }

    protected DomainException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.properties = Map.of();
    }

    protected DomainException(ErrorCode errorCode, Object... keyValues) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.properties = mapArgs(keyValues);
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    private static Map<String, Object> mapArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return Map.of();
        }

        if (args.length % 2 != 0) {
            throw new IllegalArgumentException("args는 키/값 쌍이어야 합니다.");
        }

        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < args.length; i += 2) {
            map.put(String.valueOf(args[i]), args[i + 1]);
        }
        return Map.copyOf(map);
    }

}