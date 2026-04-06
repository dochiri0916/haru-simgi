package com.dochiri.taskservice.application.port.in.dto;

public record CompleteTaskCommand(
        String taskId,
        String requesterUserId
) {
}
