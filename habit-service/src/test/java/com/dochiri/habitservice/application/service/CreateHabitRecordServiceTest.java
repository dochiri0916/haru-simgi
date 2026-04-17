package com.dochiri.habitservice.application.service;

import com.dochiri.habitservice.application.port.in.dto.CreateHabitRecordCommand;
import com.dochiri.habitservice.application.port.in.dto.CreateHabitRecordResult;
import com.dochiri.habitservice.application.port.out.HabitRecordRepository;
import com.dochiri.habitservice.application.port.out.HabitRepository;
import com.dochiri.habitservice.domain.habit.ColorType;
import com.dochiri.habitservice.domain.habit.Habit;
import com.dochiri.habitservice.domain.habit.HabitColor;
import com.dochiri.habitservice.domain.habit.HabitId;
import com.dochiri.habitservice.domain.habit.HabitIndex;
import com.dochiri.habitservice.domain.habit.HabitName;
import com.dochiri.habitservice.domain.habit.HabitOwner;
import com.dochiri.habitservice.domain.record.HabitCompletion;
import com.dochiri.habitservice.domain.record.HabitRecord;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreateHabitRecordServiceTest {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final HabitRepository habitRepository = mock(HabitRepository.class);
    private final HabitRecordRepository habitRecordRepository = mock(HabitRecordRepository.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-04-15T16:00:00Z"), KST);
    private final CreateHabitRecordService service = new CreateHabitRecordService(
            habitRepository,
            habitRecordRepository,
            clock
    );

    @Test
    void 같은_습관이_한국_날짜_기준으로_이미_완료되어_있으면_새로_저장하지_않고_기존_기록을_반환한다() {
        HabitId habitId = HabitId.newId();
        HabitOwner owner = HabitOwner.user("user-1");
        Habit habit = habit(habitId, owner);
        HabitRecord existing = HabitRecord.create(
                habitId,
                HabitCompletion.of(Instant.parse("2026-04-15T23:00:00Z"), 10, "기존")
        );
        CreateHabitRecordCommand command = new CreateHabitRecordCommand(
                habitId.value(),
                owner.ownerId(),
                Instant.parse("2026-04-16T12:00:00Z"),
                20,
                "새 기록"
        );

        when(habitRepository.loadById(habitId)).thenReturn(habit);
        when(habitRecordRepository.findByHabitIdAndCompletedDate(
                habitId,
                LocalDate.parse("2026-04-16")
        )).thenReturn(Optional.of(existing));

        CreateHabitRecordResult result = service.execute(command);

        assertThat(result.id()).isEqualTo(existing.getId().value());
        assertThat(result.completedAt()).isEqualTo(existing.getCompletedAt());
        assertThat(result.minutes()).isEqualTo(10);
        assertThat(result.memo()).isEqualTo("기존");
        verify(habitRecordRepository, never()).save(any());
    }

    @Test
    void 같은_습관의_한국_날짜_완료_기록이_없으면_새로_저장한다() {
        HabitId habitId = HabitId.newId();
        HabitOwner owner = HabitOwner.user("user-1");
        Habit habit = habit(habitId, owner);
        Instant completedAt = Instant.parse("2026-04-16T12:00:00Z");
        CreateHabitRecordCommand command = new CreateHabitRecordCommand(
                habitId.value(),
                owner.ownerId(),
                completedAt,
                20,
                "새 기록"
        );

        when(habitRepository.loadById(habitId)).thenReturn(habit);
        when(habitRecordRepository.findByHabitIdAndCompletedDate(
                habitId,
                LocalDate.parse("2026-04-16")
        )).thenReturn(Optional.empty());
        when(habitRecordRepository.save(any(HabitRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateHabitRecordResult result = service.execute(command);

        assertThat(result.habitId()).isEqualTo(habitId.value());
        assertThat(result.completedAt()).isEqualTo(completedAt);
        assertThat(result.minutes()).isEqualTo(20);
        assertThat(result.level()).isEqualTo(1);
        assertThat(result.memo()).isEqualTo("새 기록");
        verify(habitRecordRepository).save(any(HabitRecord.class));
    }

    @Test
    void 소요_시간을_입력하지_않은_완료_기록은_0분으로_반환한다() {
        HabitId habitId = HabitId.newId();
        HabitOwner owner = HabitOwner.user("user-1");
        Habit habit = habit(habitId, owner);
        Instant completedAt = Instant.parse("2026-04-16T12:00:00Z");
        CreateHabitRecordCommand command = new CreateHabitRecordCommand(
                habitId.value(),
                owner.ownerId(),
                completedAt,
                null,
                null
        );

        when(habitRepository.loadById(habitId)).thenReturn(habit);
        when(habitRecordRepository.findByHabitIdAndCompletedDate(
                habitId,
                LocalDate.parse("2026-04-16")
        )).thenReturn(Optional.empty());
        when(habitRecordRepository.save(any(HabitRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateHabitRecordResult result = service.execute(command);

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
