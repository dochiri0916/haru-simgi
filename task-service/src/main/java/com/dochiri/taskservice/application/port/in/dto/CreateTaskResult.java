package com.dochiri.taskservice.application.port.in.dto;

import java.time.Instant;

public record CreateTaskResult(
        String id,
        String ownerType,
        String ownerReferenceId,
        String title,
        boolean completed,
        Instant dueDate
) {
}