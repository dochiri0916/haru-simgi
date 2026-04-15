package com.dochiri.habitservice.infrastructure.adapter.in.web.external.response;

import com.dochiri.habitservice.application.port.in.dto.UpdateHabitRecordResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "습관 완료 기록 수정 응답")
public record UpdateHabitRecordResponse(
        @Schema(description = "기록 ID") String id,
        @Schema(description = "습관 ID") String habitId,
        @Schema(description = "완료 일시 (ISO 8601)") Instant completedAt,
        @Schema(description = "소요 시간 (분)") Integer minutes,
        @Schema(description = "메모") String memo
) {
    public static UpdateHabitRecordResponse from(UpdateHabitRecordResult result) {
        return new UpdateHabitRecordResponse(
                result.id(),
                result.habitId(),
                result.completedAt(),
                result.minutes(),
                result.memo()
        );
    }
}
