package com.dochiri.habitservice.infrastructure.adapter.in.web.external.response;

import com.dochiri.habitservice.application.port.in.dto.GetHabitRecordsResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(description = "습관 기록 조회 응답")
public record GetHabitRecordsResponse(
        @Schema(description = "습관 ID") String habitId,
        @Schema(description = "완료 기록 목록") List<RecordItem> records
) {
    @Schema(description = "완료 기록 항목")
    public record RecordItem(
            @Schema(description = "기록 ID") String id,
            @Schema(description = "완료 일시 (ISO 8601)") Instant completedAt,
            @Schema(description = "소요 시간 (분)") int value
    ) {
    }

    public static GetHabitRecordsResponse from(GetHabitRecordsResult result) {
        List<RecordItem> records = result.records().stream()
                .map(record -> new RecordItem(record.id(), record.completedAt(), record.minutes()))
                .toList();
        return new GetHabitRecordsResponse(
                result.habitId(),
                records
        );
    }
}
