package com.dochiri.habitservice.infrastructure.adapter.in.web.external.request;

import com.dochiri.habitservice.application.port.in.dto.CreateHabitCommand;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "습관 생성 요청")
public record CreateHabitRequest(
        @Schema(description = "습관 이름", example = "매일 운동하기") String name
) {
    public CreateHabitCommand toCommand(String userId) {
        return new CreateHabitCommand(
                userId,
                name
        );
    }
}