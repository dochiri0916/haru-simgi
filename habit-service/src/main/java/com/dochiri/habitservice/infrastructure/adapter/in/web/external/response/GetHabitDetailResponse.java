package com.dochiri.habitservice.infrastructure.adapter.in.web.external.response;

import com.dochiri.habitservice.application.port.in.dto.GetHabitDetailResult;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "습관 상세 조회 응답")
public record GetHabitDetailResponse(
        @Schema(description = "습관 ID") String id,
        @Schema(description = "습관 이름") String name
) {
    public static GetHabitDetailResponse from(GetHabitDetailResult result) {
        return new GetHabitDetailResponse(
                result.id(),
                result.name()
        );
    }
}
