package com.dochiri.habitservice.infrastructure.adapter.in.web.external.response;

import com.dochiri.habitservice.application.port.in.dto.CreateHabitRecordResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "습관 완료 기록 생성 응답")
public record CreateHabitRecordResponse(
        @Schema(description = "기록 ID") String id,
        @Schema(description = "습관 ID") String habitId,
        @Schema(description = "완료 일시 (ISO 8601)") Instant completedAt,
        @Schema(description = "소요 시간 (분). 입력하지 않은 완료 기록은 0") int minutes,
        @Schema(description = "완료 기록 잔디 레벨 (1~4). 완료 기록은 최소 1") int level,
        @Schema(description = "메모") String memo
) {
    public static CreateHabitRecordResponse from(CreateHabitRecordResult result) {
        return new CreateHabitRecordResponse(
                result.id(),
                result.habitId(),
                result.completedAt(),
                result.minutes(),
                result.level(),
                result.memo()
        );
    }
}
