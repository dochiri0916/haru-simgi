package com.dochiri.taskservice.application.port.in.dto;

import com.dochiri.taskservice.domain.TaskOwner;

import static java.util.Objects.requireNonNull;

public record GetTasksCommand(
        TaskOwner owner,
        Boolean completed
) {
    public GetTasksCommand {
        requireNonNull(owner);
        requireNonNull(completed);
    }
}