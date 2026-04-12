package com.dochiri.habitservice.infrastructure.adapter.in.web.external.request;

import com.dochiri.habitservice.application.port.in.dto.UpdateHabitNameCommand;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "습관 이름 수정 요청")
public record UpdateHabitNameRequest(
        @Schema(description = "변경할 습관 이름", example = "매일 독서하기") String name
) {
    public UpdateHabitNameCommand toCommand(String habitId, String userId) {
        return new UpdateHabitNameCommand(
                habitId,
                userId,
                name
        );
    }
}
