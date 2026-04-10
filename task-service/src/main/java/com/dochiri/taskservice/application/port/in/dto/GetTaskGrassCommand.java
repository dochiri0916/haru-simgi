package com.dochiri.taskservice.application.port.in.dto;

import com.dochiri.taskservice.domain.TaskOwner;

import java.time.LocalDate;

import static java.util.Objects.requireNonNull;

public record GetTaskGrassCommand(
        TaskOwner owner,
        LocalDate from,
        LocalDate to
) {
    public GetTaskGrassCommand {
        requireNonNull(owner);
        requireNonNull(from);
        requireNonNull(to);
    }
}