package com.dochiri.habitservice.infrastructure.adapter.in.web.external.request;

import com.dochiri.habitservice.application.port.in.dto.CreateHabitRecordCommand;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "습관 완료 기록 생성 요청")
public record CreateHabitRecordRequest(
        @Schema(description = "완료 일시 (ISO 8601)", example = "2024-04-13T09:00:00Z") Instant completedAt,
        @Schema(description = "소요 시간 (분)", example = "30") int value
) {
    public CreateHabitRecordCommand toCommand(String habitId, String userId) {
        return new CreateHabitRecordCommand(
                habitId,
                userId,
                completedAt,
                value
        );
    }
}