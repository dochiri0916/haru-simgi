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
    private TaskTitle title;
    private boolean completed;
    private Instant completedAt;
    private Instant dueDate;

    public static Task create(TaskOwner owner, String title, Instant dueDate) {
        return new Task(
                generateId(),
                requireNonNull(owner),
                new TaskTitle(title),
                false,
                null,
                requireNonNull(dueDate)
        );
    }

    public static Task from(String id, TaskOwner owner, String title, boolean completed, Instant completedAt, Instant dueDate) {
        validateCompletionState(completed, completedAt);
        return new Task(
                requireNonNull(id),
                requireNonNull(owner),
                new TaskTitle(title),
                completed,
                completedAt,
                requireNonNull(dueDate)
        );
    }

    public void validateOwnership(String userId) {
        if (!owner.isOwnedByUser(userId)) {
            throw new BaseException(TaskErrorCode.TASK_OWNER_FORBIDDEN);
        }
    }

    public void rename(String title) {
        this.title = new TaskTitle(title);
    }

    public void complete(Instant completedAt) {
        if (this.completed) {
            throw new BaseException(TaskErrorCode.TASK_ALREADY_COMPLETED);
        }
        this.completed = true;
        this.completedAt = requireNonNull(completedAt);
    }

    public void reopen() {
        if (!this.completed) {
            throw new BaseException(TaskErrorCode.TASK_NOT_COMPLETED);
        }
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

    private static void validateCompletionState(boolean completed, Instant completedAt) {
        if (completed && completedAt == null) {
            throw new BaseException(TaskErrorCode.TASK_COMPLETED_AT_REQUIRED);
        }
        if (!completed && completedAt != null) {
            throw new BaseException(TaskErrorCode.TASK_COMPLETED_AT_MUST_BE_NULL);
        }
    }

    private static String generateId() {
        return UUID.randomUUID().toString();
    }

}