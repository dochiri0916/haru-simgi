package com.dochiri.habitservice.application.service;

import com.dochiri.habitservice.application.port.in.dto.GetHabitRecordsCommand;
import com.dochiri.habitservice.application.port.in.dto.GetHabitRecordsResult;
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
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GetHabitRecordsServiceTest {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final HabitRepository habitRepository = mock(HabitRepository.class);
    private final HabitRecordRepository habitRecordRepository = mock(HabitRecordRepository.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-04-15T00:00:00Z"), KST);
    private final GetHabitRecordsService service = new GetHabitRecordsService(
            habitRepository,
            habitRecordRepository,
            clock
    );

    @Test
    void 기간을_지정하지_않으면_선택한_습관의_전체_기록을_조회한다() {
        HabitId habitId = HabitId.newId();
        HabitOwner owner = HabitOwner.user("user-1");
        Habit habit = habit(habitId, owner);
        HabitRecord oldRecord = HabitRecord.create(
                habitId,
                HabitCompletion.of(Instant.parse("2025-01-01T00:00:00Z"), 10, "오래된 기록")
        );
        HabitRecord recentRecord = HabitRecord.create(
                habitId,
                HabitCompletion.of(Instant.parse("2026-04-15T00:00:00Z"), 20, "최근 기록")
        );

        when(habitRepository.loadById(habitId)).thenReturn(habit);
        when(habitRecordRepository.findByHabitId(habitId)).thenReturn(List.of(oldRecord, recentRecord));

        GetHabitRecordsResult result = service.execute(GetHabitRecordsCommand.of(
                habitId.value(),
                owner.ownerId(),
                null,
                null
        ));

        assertThat(result.records())
                .extracting(GetHabitRecordsResult.RecordDto::id)
                .containsExactly(oldRecord.getId().value(), recentRecord.getId().value());
        assertThat(result.records())
                .extracting(GetHabitRecordsResult.RecordDto::level)
                .containsExactly(1, 1);
        verify(habitRecordRepository).findByHabitId(habitId);
        verify(habitRecordRepository, never()).findByHabitIdAndCompletedAtBetween(
                habitId,
                LocalDate.parse("2026-03-15").atStartOfDay(ZoneOffset.UTC).toInstant(),
                LocalDate.parse("2026-04-16").atStartOfDay(ZoneOffset.UTC).toInstant()
        );
    }

    @Test
    void 기간을_지정하면_기간_범위로_기록을_조회한다() {
        HabitId habitId = HabitId.newId();
        HabitOwner owner = HabitOwner.user("user-1");
        Habit habit = habit(habitId, owner);
        LocalDate from = LocalDate.parse("2026-04-01");
        LocalDate to = LocalDate.parse("2026-04-15");
        HabitRecord record = HabitRecord.create(
                habitId,
                HabitCompletion.of(Instant.parse("2026-04-15T00:00:00Z"), 20, "기간 기록")
        );

        when(habitRepository.loadById(habitId)).thenReturn(habit);
        when(habitRecordRepository.findByHabitIdAndCompletedAtBetween(
                habitId,
                from.atStartOfDay(ZoneOffset.UTC).toInstant(),
                to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant()
        )).thenReturn(List.of(record));

        GetHabitRecordsResult result = service.execute(GetHabitRecordsCommand.of(
                habitId.value(),
                owner.ownerId(),
                from,
                to
        ));

        assertThat(result.records()).singleElement()
                .satisfies(recordDto -> {
                    assertThat(recordDto.id()).isEqualTo(record.getId().value());
                    assertThat(recordDto.level()).isEqualTo(1);
                });
        verify(habitRecordRepository, never()).findByHabitId(habitId);
    }

    @Test
    void 소요_시간이_없는_완료_기록은_0분_level_1로_계산한다() {
        HabitId habitId = HabitId.newId();
        HabitOwner owner = HabitOwner.user("user-1");
        Habit habit = habit(habitId, owner);
        HabitRecord recordWithMinutes = HabitRecord.create(
                habitId,
                HabitCompletion.of(Instant.parse("2026-04-14T00:00:00Z"), 15, null)
        );
        HabitRecord recordWithoutMinutes = HabitRecord.create(
                habitId,
                HabitCompletion.of(Instant.parse("2026-04-15T00:00:00Z"), null, null)
        );

        when(habitRepository.loadById(habitId)).thenReturn(habit);
        when(habitRecordRepository.findByHabitId(habitId))
                .thenReturn(List.of(recordWithMinutes, recordWithoutMinutes));

        GetHabitRecordsResult result = service.execute(GetHabitRecordsCommand.of(
                habitId.value(),
                owner.ownerId(),
                null,
                null
        ));

        assertThat(result.records())
                .extracting(GetHabitRecordsResult.RecordDto::level)
                .containsExactly(1, 1);
        assertThat(result.records())
                .extracting(GetHabitRecordsResult.RecordDto::minutes)
                .containsExactly(15, 0);
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
