package com.dochiri.habitservice.infrastructure.adapter.in.web.external.request;

import com.dochiri.habitservice.application.port.in.dto.CreateHabitRecordCommand;
import com.dochiri.habitservice.domain.habit.HabitOwner;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "습관 완료 기록 생성 요청")
public record CreateHabitRecordRequest(
        @Schema(description = "완료 일시 (ISO 8601)", example = "2024-04-13T09:00:00Z") Instant completedAt,
        @Schema(description = "소요 시간 (분)", example = "30") Integer minutes,
        @Schema(description = "메모 (최대 200자)", example = "클린 아키텍처") String memo
) {
    public CreateHabitRecordCommand toCommand(String habitId, HabitOwner owner) {
        return new CreateHabitRecordCommand(
                habitId,
                owner,
                completedAt,
                minutes,
                memo
        );
    }
}
