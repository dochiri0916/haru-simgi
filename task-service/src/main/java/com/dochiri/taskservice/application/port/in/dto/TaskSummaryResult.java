package com.dochiri.taskservice.application.port.in.dto;

import java.time.Instant;

public record TaskSummaryResult(
        String id,
        String ownerType,
        String ownerReferenceId,
        String title,
        boolean completed,
        Instant completedAt,
        Instant dueDate
) {
}