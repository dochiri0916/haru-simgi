package com.dochiri.habitservice.infrastructure.adapter.in.web.external.response;

import com.dochiri.habitservice.application.port.in.dto.CreateHabitResult;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "습관 생성 응답")
public record CreateHabitResponse(
        @Schema(description = "생성된 습관 ID") String id,
        @Schema(description = "습관 이름") String name,
        @Schema(description = "색상") String color,
        @Schema(description = "색상 HEX 코드") String colorHex
) {
    public static CreateHabitResponse from(CreateHabitResult result) {
        return new CreateHabitResponse(
                result.id(),
                result.name(),
                result.color(),
                result.colorHex()
        );
    }
}