package com.dochiri.taskservice.application.port.in.dto;

public record CreateTaskResult(
        String id,
        String ownerType,
        String ownerReferenceId,
        String title,
        boolean completed
) {
}
