package com.dochiri.taskservice.application.port.in.dto;

public record DeleteTaskCommand(
        String taskId,
        String requesterUserId
) {
}
