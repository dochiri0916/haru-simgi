package com.dochiri.taskservice.infrastructure.adapter.in.web.external.response;

import com.dochiri.taskservice.application.port.in.dto.TaskSummaryResult;

import java.time.Instant;

public record TaskResponse(
        String id,
        String ownerType,
        String ownerReferenceId,
        String title,
        boolean completed,
        Instant completedAt
) {
    public static TaskResponse from(TaskSummaryResult result) {
        return new TaskResponse(
                result.id(),
                result.ownerType(),
                result.ownerReferenceId(),
                result.title(),
                result.completed(),
                result.completedAt()
        );
    }
}
