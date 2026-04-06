package com.dochiri.taskservice.application.port.in.dto;

import com.dochiri.taskservice.domain.TaskOwner;

import static java.util.Objects.requireNonNull;

public record CreateTaskCommand(
        TaskOwner owner,
        String title
) {
    public CreateTaskCommand {
        requireNonNull(owner);
        requireNonNull(title);
    }
}
