package com.dochiri.habitservice.infrastructure.adapter.in.web.external.response;

import com.dochiri.habitservice.application.port.in.dto.GetHabitsResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "습관 목록 조회 응답")
public record GetHabitsResponse(
        @Schema(description = "습관 목록") List<HabitItem> habits
) {
    @Schema(description = "습관 항목")
    public record HabitItem(
            @Schema(description = "습관 ID") String id,
            @Schema(description = "습관 이름") String name,
            @Schema(description = "색상") String color,
            @Schema(description = "색상 HEX 코드") String colorHex
    ) {
    }

    public static GetHabitsResponse from(GetHabitsResult result) {
        List<HabitItem> habits = result.habits().stream()
                .map(habit -> new HabitItem(habit.id(), habit.name(), habit.color(), habit.colorHex()))
                .toList();
        return new GetHabitsResponse(
                habits
        );
    }
}
