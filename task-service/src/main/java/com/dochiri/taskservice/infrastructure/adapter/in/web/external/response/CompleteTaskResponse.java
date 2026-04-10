package com.dochiri.taskservice.infrastructure.adapter.in.web.external.response;

import com.dochiri.taskservice.application.port.in.dto.CompleteTaskResult;

import java.time.Instant;

public record CompleteTaskResponse(
        String id,
        String ownerType,
        String ownerReferenceId,
        String title,
        boolean completed,
        Instant completedAt
) {
    public static CompleteTaskResponse from(CompleteTaskResult result) {
        return new CompleteTaskResponse(
                result.id(),
                result.ownerType(),
                result.ownerReferenceId(),
                result.title(),
                result.completed(),
                result.completedAt()
        );
    }
}