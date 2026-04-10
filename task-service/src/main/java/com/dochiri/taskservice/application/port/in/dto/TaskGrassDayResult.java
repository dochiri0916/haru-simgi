package com.dochiri.taskservice.application.port.in.dto;

import java.time.LocalDate;

public record TaskGrassDayResult(
        LocalDate date,
        int completedCount
) {
}