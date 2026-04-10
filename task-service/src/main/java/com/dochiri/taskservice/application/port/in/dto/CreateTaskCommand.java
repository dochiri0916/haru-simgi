package com.dochiri.taskservice.application.port.in.dto;

import com.dochiri.taskservice.domain.TaskOwner;

import java.time.Instant;

import static java.util.Objects.requireNonNull;

public record CreateTaskCommand(
        TaskOwner owner,
        String title,
        Instant dueDate
) {
    public CreateTaskCommand {
        requireNonNull(owner);
        requireNonNull(title);
        requireNonNull(dueDate);
    }
}