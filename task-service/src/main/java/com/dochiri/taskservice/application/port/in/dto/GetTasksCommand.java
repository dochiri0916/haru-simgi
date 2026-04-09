package com.dochiri.taskservice.application.port.in.dto;

import com.dochiri.taskservice.domain.TaskOwner;

public record GetTasksCommand(
        TaskOwner owner,
        Boolean completed
) {
}
