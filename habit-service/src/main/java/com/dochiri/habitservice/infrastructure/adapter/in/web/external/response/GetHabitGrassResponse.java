package com.dochiri.habitservice.infrastructure.adapter.in.web.external.response;

import com.dochiri.habitservice.application.port.in.dto.GetHabitGrassResult;

import java.time.LocalDate;
import java.util.List;

public record GetHabitGrassResponse(LocalDate fromDate, LocalDate toDate, int totalValue, List<GrassDayItem> days) {

    public record GrassDayItem(LocalDate date, int value, int level) {}

    public static GetHabitGrassResponse from(GetHabitGrassResult result) {
        List<GrassDayItem> days = result.days().stream()
            .map(d -> new GrassDayItem(d.date(), d.value(), d.level()))
            .toList();
        return new GetHabitGrassResponse(result.fromDate(), result.toDate(), result.totalValue(), days);
    }

}
