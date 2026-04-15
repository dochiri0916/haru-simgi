package com.dochiri.habitservice.application.service;

import com.dochiri.habitservice.application.port.in.dto.GetHabitGrassCommand;
import com.dochiri.habitservice.application.port.in.dto.GetHabitGrassResult;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetHabitGrassServiceTest {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final HabitRecordRepository habitRecordRepository = mock(HabitRecordRepository.class);
    private final HabitRepository habitRepository = mock(HabitRepository.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-04-15T00:00:00Z"), KST);
    private final GetHabitGrassService service = new GetHabitGrassService(
            habitRecordRepository,
            habitRepository,
            clock
    );

    @Test
    void 잔디는_완료_기록_수가_아니라_소요_시간_합계로_레벨을_계산한다() {
        HabitOwner owner = HabitOwner.user("user-1");
        HabitId habitId = HabitId.newId();
        Habit habit = habit(habitId, owner);
        LocalDate date = LocalDate.parse("2026-04-15");
        HabitRecord record = HabitRecord.create(
                habitId,
                HabitCompletion.of(Instant.parse("2026-04-15T10:00:00Z"), 100, null)
        );

        when(habitRepository.findByOwner(owner)).thenReturn(List.of(habit));
        when(habitRecordRepository.findByOwnerAndCompletedAtBetween(
                owner,
                date.atStartOfDay(KST).toInstant(),
                date.plusDays(1).atStartOfDay(KST).toInstant()
        )).thenReturn(List.of(record));

        GetHabitGrassResult result = service.execute(new GetHabitGrassCommand(
                owner.ownerId(),
                date,
                date
        ));

        assertThat(result.totalValue()).isEqualTo(100);
        assertThat(result.days()).singleElement()
                .satisfies(day -> {
                    assertThat(day.value()).isEqualTo(100);
                    assertThat(day.level()).isEqualTo(3);
                });
    }

    @Test
    void 같은_날_여러_습관_기록은_소요_시간을_합산해_잔디를_계산한다() {
        HabitOwner owner = HabitOwner.user("user-1");
        HabitId firstHabitId = HabitId.newId();
        HabitId secondHabitId = HabitId.newId();
        LocalDate date = LocalDate.parse("2026-04-15");
        HabitRecord firstRecord = HabitRecord.create(
                firstHabitId,
                HabitCompletion.of(Instant.parse("2026-04-15T01:00:00Z"), 30, null)
        );
        HabitRecord secondRecord = HabitRecord.create(
                secondHabitId,
                HabitCompletion.of(Instant.parse("2026-04-15T09:00:00Z"), 40, null)
        );

        when(habitRepository.findByOwner(owner)).thenReturn(List.of(
                habit(firstHabitId, owner),
                habit(secondHabitId, owner)
        ));
        when(habitRecordRepository.findByOwnerAndCompletedAtBetween(
                owner,
                date.atStartOfDay(KST).toInstant(),
                date.plusDays(1).atStartOfDay(KST).toInstant()
        )).thenReturn(List.of(firstRecord, secondRecord));

        GetHabitGrassResult result = service.execute(new GetHabitGrassCommand(
                owner.ownerId(),
                date,
                date
        ));

        assertThat(result.totalValue()).isEqualTo(70);
        assertThat(result.days()).singleElement()
                .satisfies(day -> {
                    assertThat(day.value()).isEqualTo(70);
                    assertThat(day.level()).isEqualTo(3);
                });
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
