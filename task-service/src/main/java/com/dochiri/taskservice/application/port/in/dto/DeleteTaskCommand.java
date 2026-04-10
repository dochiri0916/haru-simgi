package com.dochiri.taskservice.application.port.in.dto;

import static java.util.Objects.*;

public record DeleteTaskCommand(
        String id,
        String requesterUserId
) {
    public DeleteTaskCommand {
        requireNonNull(id);
        requireNonNull(requesterUserId);
    }
}