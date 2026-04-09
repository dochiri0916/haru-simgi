package com.dochiri.taskservice.application.port.in.dto;

public record ReopenTaskCommand(
        String taskId,
        String requesterUserId
) {
}
