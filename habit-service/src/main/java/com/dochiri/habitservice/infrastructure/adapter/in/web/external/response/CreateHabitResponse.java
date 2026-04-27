package com.dochiri.habitservice.infrastructure.adapter.in.web.external.response;

import com.dochiri.habitservice.application.port.in.dto.CreateHabitResult;
import com.dochiri.habitservice.application.port.in.dto.HabitView;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "습관 생성 응답")
public record CreateHabitResponse(
        @Schema(description = "생성된 습관 ID") String id,
        @Schema(description = "습관 이름") String name,
        @Schema(description = "색상") String color,
        @Schema(description = "색상 HEX 코드") String colorHex,
        @Schema(description = "습관 정렬 순서") int index,
        @Schema(description = "습관 생성 시각") Instant createdAt
) {
    public static CreateHabitResponse from(CreateHabitResult result) {
        HabitView habit = result.habit();
        return new CreateHabitResponse(
                habit.id(),
                habit.name(),
                habit.color(),
                habit.colorHex(),
                habit.index(),
                habit.createdAt()
        );
    }
}
