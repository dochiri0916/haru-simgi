package com.dochiri.habitservice.infrastructure.adapter.in.web.external.response;

import com.dochiri.habitservice.application.port.in.dto.GetHabitGrassResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "잔디 조회 응답")
public record GetHabitGrassResponse(
        @Schema(description = "조회 시작일") LocalDate fromDate,
        @Schema(description = "조회 종료일") LocalDate toDate,
        @Schema(description = "기간 내 총 완료 건수") int totalValue,
        @Schema(description = "일별 잔디 데이터") List<GrassDayItem> days
) {
    @Schema(description = "일별 잔디 항목")
    public record GrassDayItem(
            @Schema(description = "날짜") LocalDate date,
            @Schema(description = "완료 건수") int value,
            @Schema(description = "잔디 레벨 (0~4)") int level
    ) {
    }

    public static GetHabitGrassResponse from(GetHabitGrassResult result) {
        List<GrassDayItem> days = result.days().stream()
                .map(day -> new GrassDayItem(day.date(), day.value(), day.level()))
                .toList();
        return new GetHabitGrassResponse(
                result.fromDate(),
                result.toDate(),
                result.totalValue(),
                days
        );
    }
}
