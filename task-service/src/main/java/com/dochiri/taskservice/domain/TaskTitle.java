package com.dochiri.taskservice.domain;

import com.dochiri.errorhandling.BaseException;
import com.dochiri.taskservice.application.error.TaskErrorCode;

import static java.util.Objects.requireNonNull;

public record TaskTitle(String value) {

    public TaskTitle {
        requireNonNull(value);
        value = value.trim();
        if (value.isBlank()) {
            throw new BaseException(TaskErrorCode.TASK_TITLE_BLANK);
        }
        if (value.length() > 100) {
            throw new BaseException(TaskErrorCode.TASK_TITLE_TOO_LONG);
        }
    }
}