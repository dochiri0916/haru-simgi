package com.dochiri.habitservice.infrastructure.adapter.in.web.external.request;

import com.dochiri.habitservice.application.port.in.dto.UpdateHabitRecordCommand;
import com.dochiri.habitservice.domain.habit.HabitOwner;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateHabitRecordRequestTest {

    @Test
    void 메모를_생략하면_커맨드에서_메모를_수정하지_않는다() {
        UpdateHabitRecordRequest request = new UpdateHabitRecordRequest();

        UpdateHabitRecordCommand command = request.toCommand("habit-id", "record-id", HabitOwner.user("user-id"));

        assertThat(command.memo().isPresent()).isFalse();
    }

    @Test
    void 메모_문자열을_보내면_커맨드에_수정값을_담는다() {
        UpdateHabitRecordRequest request = new UpdateHabitRecordRequest();
        request.setMemo("새 메모");

        UpdateHabitRecordCommand command = request.toCommand("habit-id", "record-id", HabitOwner.user("user-id"));

        assertThat(command.memo().isPresent()).isTrue();
        assertThat(command.memo().orElse(null)).isEqualTo("새 메모");
    }

    @Test
    void 메모에_null을_보내면_커맨드에_삭제_의도를_담는다() {
        UpdateHabitRecordRequest request = new UpdateHabitRecordRequest();
        request.setMemo(null);

        UpdateHabitRecordCommand command = request.toCommand("habit-id", "record-id", HabitOwner.user("user-id"));

        assertThat(command.memo().isPresent()).isTrue();
        assertThat(command.memo().orElse(null)).isNull();
    }

}
