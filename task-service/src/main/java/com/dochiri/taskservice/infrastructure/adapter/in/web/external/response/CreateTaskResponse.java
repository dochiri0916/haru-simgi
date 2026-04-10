package com.dochiri.taskservice.infrastructure.adapter.in.web.external.response;

import com.dochiri.taskservice.application.port.in.dto.CreateTaskResult;

import java.time.Instant;

public record CreateTaskResponse(
        String id,
        String ownerType,
        String ownerReferenceId,
        String title,
        boolean completed,
        Instant dueDate
) {
    public static CreateTaskResponse from(CreateTaskResult result) {
        return new CreateTaskResponse(
                result.id(),
                result.ownerType(),
                result.ownerReferenceId(),
                result.title(),
                result.completed(),
                result.dueDate()
        );
    }
}