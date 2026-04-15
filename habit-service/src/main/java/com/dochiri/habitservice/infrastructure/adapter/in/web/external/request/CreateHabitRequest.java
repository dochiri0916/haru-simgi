package com.dochiri.habitservice.infrastructure.adapter.in.web.external.request;

import com.dochiri.habitservice.application.port.in.dto.CreateHabitCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "습관 생성 요청")
public record CreateHabitRequest(
        @NotBlank(message = "습관 이름은 필수입니다.")
        @Size(max = 50, message = "습관 이름은 50자 이하여야 합니다.")
        @Schema(description = "습관 이름", example = "매일 운동하기") String name,

        @Pattern(
                regexp = "\\s*|BLUE|GREEN|RED|YELLOW|PURPLE|PINK",
                message = "색상은 BLUE, GREEN, RED, YELLOW, PURPLE, PINK 중 하나여야 합니다."
        )
        @Schema(description = "색상 (BLUE, GREEN, RED, YELLOW, PURPLE, PINK)", example = "GREEN") String color
) {
    public CreateHabitCommand toCommand(String userId) {
        return new CreateHabitCommand(
                userId,
                name,
                color
        );
    }
}
