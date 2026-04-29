package com.dochiri.habitservice.application.service;

import com.dochiri.habitservice.application.common.Patchable;
import com.dochiri.habitservice.application.port.in.dto.UpdateHabitRecordCommand;
import com.dochiri.habitservice.application.port.in.dto.UpdateHabitRecordResult;
import com.dochiri.habitservice.application.port.out.HabitRecordRepository;
import com.dochiri.habitservice.application.port.out.HabitRepository;
import com.dochiri.habitservice.domain.habit.ColorType;
import com.dochiri.habitservice.domain.habit.Habit;
import com.dochiri.habitservice.domain.habit.HabitColor;
import com.dochiri.habitservice.domain.habit.HabitId;
import com.dochiri.habitservice.domain.habit.HabitIndex;
import com.dochiri.habitservice.domain.habit.HabitName;
import com.dochiri.habitservice.domain.habit.HabitOwner;
import com.dochiri.habitservice.domain.record.HabitDuration;
import com.dochiri.habitservice.domain.record.HabitMemo;
import com.dochiri.habitservice.domain.record.HabitRecord;
import com.dochiri.habitservice.domain.record.HabitRecordId;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UpdateHabitRecordServiceTest {

    private final HabitRepository habitRepository = mock(HabitRepository.class);
    private final HabitRecordRepository habitRecordRepository = mock(HabitRecordRepository.class);
    private final UpdateHabitRecordService service = new UpdateHabitRecordService(
            habitRepository,
            habitRecordRepository
    );

    @Test
    void 메모_필드를_생략하고_소요_시간만_수정하면_기존_메모를_유지한다() {
        HabitId habitId = HabitId.newId();
        HabitRecordId recordId = HabitRecordId.newId();
        HabitOwner owner = HabitOwner.user("user-1");
        Habit habit = habit(habitId, owner);
        HabitRecord record = HabitRecord.from(
                recordId,
                habitId,
                Instant.parse("2026-04-15T10:00:00Z"),
                HabitDuration.of(20),
                HabitMemo.of("기존 메모")
        );
        UpdateHabitRecordCommand command = new UpdateHabitRecordCommand(
                habitId.value(),
                recordId.value(),
                owner,
                null,
                45,
                null
        );

        when(habitRepository.loadById(habitId)).thenReturn(habit);
        when(habitRecordRepository.loadById(recordId)).thenReturn(record);
        when(habitRecordRepository.save(any(HabitRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateHabitRecordResult result = service.execute(command);

        assertThat(result.completedAt()).isEqualTo(record.getCompletedAt());
        assertThat(result.minutes()).isEqualTo(45);
        assertThat(result.level()).isEqualTo(2);
        assertThat(result.memo()).isEqualTo("기존 메모");
    }

    @Test
    void 메모에_null을_명시하면_메모를_삭제한다() {
        HabitId habitId = HabitId.newId();
        HabitRecordId recordId = HabitRecordId.newId();
        HabitOwner owner = HabitOwner.user("user-1");
        Habit habit = habit(habitId, owner);
        HabitRecord record = HabitRecord.from(
                recordId,
                habitId,
                Instant.parse("2026-04-15T10:00:00Z"),
                HabitDuration.of(20),
                HabitMemo.of("기존 메모")
        );
        UpdateHabitRecordCommand command = new UpdateHabitRecordCommand(
                habitId.value(),
                recordId.value(),
                owner,
                null,
                null,
                Patchable.of(null)
        );

        when(habitRepository.loadById(habitId)).thenReturn(habit);
        when(habitRecordRepository.loadById(recordId)).thenReturn(record);
        when(habitRecordRepository.save(any(HabitRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateHabitRecordResult result = service.execute(command);

        assertThat(result.completedAt()).isEqualTo(record.getCompletedAt());
        assertThat(result.minutes()).isEqualTo(20);
        assertThat(result.memo()).isNull();
    }

    @Test
    void 소요_시간이_없는_기록은_0분으로_반환한다() {
        HabitId habitId = HabitId.newId();
        HabitRecordId recordId = HabitRecordId.newId();
        HabitOwner owner = HabitOwner.user("user-1");
        Habit habit = habit(habitId, owner);
        HabitRecord record = HabitRecord.from(
                recordId,
                habitId,
                Instant.parse("2026-04-15T10:00:00Z"),
                null,
                HabitMemo.empty()
        );
        UpdateHabitRecordCommand command = new UpdateHabitRecordCommand(
                habitId.value(),
                recordId.value(),
                owner,
                null,
                null,
                Patchable.undefined()
        );

        when(habitRepository.loadById(habitId)).thenReturn(habit);
        when(habitRecordRepository.loadById(recordId)).thenReturn(record);
        when(habitRecordRepository.save(any(HabitRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateHabitRecordResult result = service.execute(command);

        assertThat(result.minutes()).isZero();
        assertThat(result.level()).isEqualTo(1);
        assertThat(result.memo()).isNull();
    }

    private Habit habit(HabitId habitId, HabitOwner owner) {
        return Habit.from(
                habitId,
                owner,
                HabitName.of("물 마시기"),
                HabitColor.of(ColorType.GREEN),
                HabitIndex.of(0),
                Instant.parse("2026-04-01T00:00:00Z")
        );
    }

}
