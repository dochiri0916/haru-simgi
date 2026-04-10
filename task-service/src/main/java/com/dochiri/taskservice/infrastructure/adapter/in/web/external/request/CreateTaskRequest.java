package com.dochiri.taskservice.infrastructure.adapter.in.web.external.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record CreateTaskRequest(
        @NotBlank
        String title,
        @NotNull
        Instant dueDate
) {
}