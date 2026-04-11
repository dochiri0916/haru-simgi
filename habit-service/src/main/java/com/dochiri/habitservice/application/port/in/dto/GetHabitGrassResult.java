package com.dochiri.habitservice.application.port.in.dto;

import java.time.LocalDate;
import java.util.List;

public record GetHabitGrassResult(
        LocalDate fromDate,
        LocalDate toDate,
        int totalValue,
        List<HabitGrassDayResult> days
) {
    public record HabitGrassDayResult(
            LocalDate date,
            int value,
            int level
    ) {
    }

}