package com.dochiri.taskservice.application.port.in.dto;

import java.time.LocalDate;
import java.util.List;

public record TaskGrassResult(
        LocalDate from,
        LocalDate to,
        int totalCompletedCount,
        List<TaskGrassDayResult> days
) {
}