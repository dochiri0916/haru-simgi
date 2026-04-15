package com.dochiri.habitservice.infrastructure.adapter.in.web.external.request;

import com.dochiri.habitservice.application.port.in.dto.UpdateHabitRecordCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.openapitools.jackson.nullable.JsonNullable;

import java.time.Instant;

@Schema(description = "습관 완료 기록 수정 요청")
public class UpdateHabitRecordRequest {

    @Schema(description = "완료 일시 (ISO 8601). 생략하면 기존 값을 유지합니다.", example = "2024-04-13T09:00:00Z")
    private Instant completedAt;

    @Min(value = 0, message = "소요 시간은 0분 이상이어야 합니다.")
    @Max(value = 1440, message = "소요 시간은 1440분 이하여야 합니다.")
    @Schema(description = "소요 시간 (분). 생략하면 기존 값을 유지합니다.", example = "30")
    private Integer minutes;

    @Schema(description = "메모 (최대 200자). 생략하면 기존 값을 유지, null로 보내면 메모를 삭제합니다.", example = "클린 아키텍처")
    private String memo;

    private boolean memoPresent;

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public Integer getMinutes() {
        return minutes;
    }

    public void setMinutes(Integer minutes) {
        this.minutes = minutes;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
        this.memoPresent = true;
    }

    public UpdateHabitRecordCommand toCommand(String habitId, String recordId, String userId) {
        return new UpdateHabitRecordCommand(
                habitId,
                recordId,
                userId,
                completedAt,
                minutes,
                memoPresent ? JsonNullable.of(memo) : JsonNullable.undefined()
        );
    }
}
