package com.dochiri.taskservice.domain;

import com.dochiri.errorhandling.BaseException;
import com.dochiri.taskservice.application.error.TaskErrorCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class Task {

    private final String id;
    private TaskOwner owner;
    private String title;
    private boolean completed;
    private Instant completedAt;

    public static Task create(TaskOwner owner, String title) {
        return new Task(
                generateId(),
                requireNonNull(owner),
                validateTitle(title),
                false,
                null
        );
    }

    public static Task from(String publicId, TaskOwner owner, String title, boolean completed, Instant completedAt) {
        validateCompletionState(completed, completedAt);
        return new Task(
                requireNonNull(publicId),
                requireNonNull(owner),
                validateTitle(title),
                completed,
                completedAt
        );
    }

    public void rename(String title) {
        this.title = validateTitle(title);
    }

    public void complete(Instant completedAt) {
        this.completed = true;
        this.completedAt = requireNonNull(completedAt);
    }

    public void reopen() {
        this.completed = false;
        this.completedAt = null;
    }

    public void migrateTo(TaskOwner newOwner) {
        requireNonNull(newOwner);

        if (!owner.isGuest()) {
            throw new BaseException(TaskErrorCode.TASK_MIGRATION_SOURCE_NOT_GUEST);
        }
        if (!newOwner.isUser()) {
            throw new BaseException(TaskErrorCode.TASK_MIGRATION_TARGET_NOT_USER);
        }

        this.owner = newOwner;
    }

    private static String validateTitle(String title) {
        requireNonNull(title);

        String trimmed = title.trim();
        if (trimmed.isBlank()) {
            throw new BaseException(TaskErrorCode.TASK_TITLE_BLANK);
        }
        if (trimmed.length() > 100) {
            throw new BaseException(TaskErrorCode.TASK_TITLE_TOO_LONG);
        }
        return trimmed;
    }

    private static String generateId() {
        return UUID.randomUUID().toString();
    }

    private static void validateCompletionState(boolean completed, Instant completedAt) {
        if (completed && completedAt == null) {
            throw new BaseException(TaskErrorCode.TASK_COMPLETED_AT_REQUIRED);
        }
        if (!completed && completedAt != null) {
            throw new BaseException(TaskErrorCode.TASK_COMPLETED_AT_MUST_BE_NULL);
        }
    }

}