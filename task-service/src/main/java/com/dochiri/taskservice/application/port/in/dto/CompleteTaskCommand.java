package com.dochiri.taskservice.application.port.in.dto;

import static java.util.Objects.requireNonNull;

public record CompleteTaskCommand(
        String id,
        String requesterUserId
) {
    public CompleteTaskCommand {
        requireNonNull(id());
        requireNonNull(requesterUserId());
    }
}