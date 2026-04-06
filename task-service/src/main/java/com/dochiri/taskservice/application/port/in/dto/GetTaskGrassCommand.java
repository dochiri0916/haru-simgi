package com.dochiri.taskservice.application.port.in.dto;

import com.dochiri.taskservice.domain.TaskOwner;

import java.time.LocalDate;

public record GetTaskGrassCommand(
        TaskOwner owner,
        LocalDate from,
        LocalDate to
) {
}
