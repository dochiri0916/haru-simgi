package com.dochiri.taskservice.application.port.in.dto;

import com.dochiri.taskservice.domain.Task;

import java.time.Instant;

public record CompleteTaskResult(
        String id,
        String ownerType,
        String ownerReferenceId,
        String title,
        boolean completed,
        Instant completedAt
) {
    public static CompleteTaskResult from(Task task) {
        return new CompleteTaskResult(
                task.getId(),
                task.getOwner().type().name(),
                task.getOwner().referenceId(),
                task.getTitle(),
                task.isCompleted(),
                task.getCompletedAt()
        );
    }
}