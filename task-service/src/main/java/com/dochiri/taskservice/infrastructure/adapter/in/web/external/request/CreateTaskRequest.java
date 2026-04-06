package com.dochiri.taskservice.infrastructure.adapter.in.web.external.request;

import jakarta.validation.constraints.NotBlank;

public record CreateTaskRequest(
        @NotBlank
        String title
) {
}
