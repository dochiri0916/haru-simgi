package com.dochiri.habitservice.infrastructure.adapter.in.web.external.response;

import com.dochiri.habitservice.application.port.in.dto.GetHabitsResult;

import java.util.List;

public record GetHabitsResponse(List<HabitItem> habits) {

    public record HabitItem(String id, String name) {}

    public static GetHabitsResponse from(GetHabitsResult result) {
        List<HabitItem> habits = result.habits().stream()
            .map(h -> new HabitItem(h.id(), h.name()))
            .toList();
        return new GetHabitsResponse(habits);
    }

}
