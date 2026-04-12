package com.dochiri.habitservice.infrastructure.adapter.in.web.external.response;

import com.dochiri.habitservice.application.port.in.dto.UpdateHabitNameResult;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "습관 이름 수정 응답")
public record UpdateHabitNameResponse(
        @Schema(description = "습관 ID") String id,
        @Schema(description = "변경된 습관 이름") String name
) {
    public static UpdateHabitNameResponse from(UpdateHabitNameResult result) {
        return new UpdateHabitNameResponse(
                result.id(),
                result.name()
        );
    }
}
