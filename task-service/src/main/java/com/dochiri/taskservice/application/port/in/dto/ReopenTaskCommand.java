package com.dochiri.taskservice.application.port.in.dto;

import static java.util.Objects.requireNonNull;

public record ReopenTaskCommand(
        String id,
        String requesterUserId
) {
    public ReopenTaskCommand {
        requireNonNull(id);
        requireNonNull(requesterUserId);
    }
}