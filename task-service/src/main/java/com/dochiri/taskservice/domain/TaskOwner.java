package com.dochiri.taskservice.domain;

import com.dochiri.errorhandling.BaseException;
import com.dochiri.taskservice.application.error.TaskErrorCode;

public record TaskOwner(
        OwnerType type,
        String referenceId
) {
    public TaskOwner {
        if (type == null) {
            throw new BaseException(TaskErrorCode.TASK_OWNER_TYPE_MISSING);
        }
        if (referenceId == null) {
            throw new BaseException(TaskErrorCode.TASK_OWNER_REFERENCE_ID_MISSING);
        }

        if (referenceId.isBlank()) {
            throw new BaseException(TaskErrorCode.TASK_OWNER_REFERENCE_ID_BLANK);
        }
    }

    public static TaskOwner guest(String guestId) {
        return new TaskOwner(OwnerType.GUEST, guestId);
    }

    public static TaskOwner user(String userId) {
        return new TaskOwner(OwnerType.USER, userId);
    }

    public boolean isGuest() {
        return type == OwnerType.GUEST;
    }

    public boolean isUser() {
        return type == OwnerType.USER;
    }

    public boolean isOwnedByUser(String userId) {
        return isUser() && referenceId.equals(userId);
    }

}