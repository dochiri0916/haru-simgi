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
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

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
    void 잔디는_완료_기록_수로_레벨을_계산한다() {
        HabitOwner owner = HabitOwner.user("user-1");
        HabitId habitId = HabitId.newId();
        Habit habit = habit(habitId, owner);
        LocalDate date = LocalDate.parse("2026-04-15");

        when(habitRepository.findByOwner(owner)).thenReturn(List.of(habit));
        when(habitRecordRepository.countByOwnerAndCompletedDateBetween(
                owner,
                date,
                date
        )).thenReturn(Map.of(date, 1));

        GetHabitGrassResult result = service.execute(new GetHabitGrassCommand(
                owner.ownerId(),
                date,
                date
        ));

        assertThat(result.totalValue()).isEqualTo(1);
        assertThat(result.days()).singleElement()
                .satisfies(day -> {
                    assertThat(day.value()).isEqualTo(1);
                    assertThat(day.level()).isEqualTo(1);
                });
    }

    @Test
    void 같은_날_여러_습관_기록은_완료_기록_수를_합산해_잔디를_계산한다() {
        HabitOwner owner = HabitOwner.user("user-1");
        HabitId firstHabitId = HabitId.newId();
        HabitId secondHabitId = HabitId.newId();
        LocalDate date = LocalDate.parse("2026-04-15");

        when(habitRepository.findByOwner(owner)).thenReturn(List.of(
                habit(firstHabitId, owner),
                habit(secondHabitId, owner)
        ));
        when(habitRecordRepository.countByOwnerAndCompletedDateBetween(
                owner,
                date,
                date
        )).thenReturn(Map.of(date, 2));

        GetHabitGrassResult result = service.execute(new GetHabitGrassCommand(
                owner.ownerId(),
                date,
                date
        ));

        assertThat(result.totalValue()).isEqualTo(2);
        assertThat(result.days()).singleElement()
                .satisfies(day -> {
                    assertThat(day.value()).isEqualTo(2);
                    assertThat(day.level()).isEqualTo(2);
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
