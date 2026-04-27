package com.dochiri.habitservice.infrastructure.adapter.in.web.external.request;

import com.dochiri.habitservice.application.port.in.dto.SwapHabitIndexCommand;
import com.dochiri.habitservice.domain.habit.HabitOwner;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "습관 정렬 순서 교환 요청")
public record SwapHabitIndexRequest(
        @NotBlank
        @Schema(description = "정렬 순서를 바꿀 첫 번째 습관 ID") String sourceHabitId,
        @NotBlank
        @Schema(description = "정렬 순서를 바꿀 두 번째 습관 ID") String targetHabitId
) {
    public SwapHabitIndexCommand toCommand(HabitOwner owner) {
        return new SwapHabitIndexCommand(
                sourceHabitId,
                targetHabitId,
                owner
        );
    }
}
