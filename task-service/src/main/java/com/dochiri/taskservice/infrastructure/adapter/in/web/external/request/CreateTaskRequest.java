package com.dochiri.taskservice.infrastructure.adapter.in.web.external.request;

import com.dochiri.taskservice.application.port.in.dto.CreateTaskCommand;
import com.dochiri.taskservice.domain.OwnerType;
import com.dochiri.taskservice.domain.TaskOwner;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateTaskRequest(
        @NotNull
        OwnerType ownerType,

        @NotBlank
        String ownerReferenceId,

        @NotBlank
        String title
) {
    public CreateTaskCommand toCommand() {
        return new CreateTaskCommand(
                new TaskOwner(ownerType, ownerReferenceId),
                title
        );
    }
}
