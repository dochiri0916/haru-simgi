package com.dochiri.habitservice.application.service;

import com.dochiri.habitservice.application.port.in.dto.SwapHabitIndexCommand;
import com.dochiri.habitservice.application.port.in.dto.SwapHabitIndexResult;
import com.dochiri.habitservice.application.port.out.HabitRepository;
import com.dochiri.habitservice.domain.habit.ColorType;
import com.dochiri.habitservice.domain.habit.Habit;
import com.dochiri.habitservice.domain.habit.HabitColor;
import com.dochiri.habitservice.domain.habit.HabitId;
import com.dochiri.habitservice.domain.habit.HabitIndex;
import com.dochiri.habitservice.domain.habit.HabitName;
import com.dochiri.habitservice.domain.habit.HabitOwner;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SwapHabitIndexServiceTest {

    private final HabitRepository habitRepository = mock(HabitRepository.class);
    private final SwapHabitIndexService service = new SwapHabitIndexService(habitRepository);

    @Test
    void 두_습관의_정렬_순서를_서로_바꾼다() {
        HabitOwner owner = HabitOwner.user("user-1");
        HabitId sourceHabitId = HabitId.newId();
        HabitId targetHabitId = HabitId.newId();
        Habit sourceHabit = habit(sourceHabitId, owner, "물 마시기", 0);
        Habit targetHabit = habit(targetHabitId, owner, "러닝", 2);
        SwapHabitIndexCommand command = new SwapHabitIndexCommand(
                sourceHabitId.value(),
                targetHabitId.value(),
                owner.ownerId()
        );

        when(habitRepository.loadById(sourceHabitId)).thenReturn(sourceHabit);
        when(habitRepository.loadById(targetHabitId)).thenReturn(targetHabit);
        when(habitRepository.save(any(Habit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SwapHabitIndexResult result = service.execute(command);

        assertThat(result.habits())
                .extracting(SwapHabitIndexResult.HabitDto::id, SwapHabitIndexResult.HabitDto::index)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(sourceHabitId.value(), 2),
                        org.assertj.core.groups.Tuple.tuple(targetHabitId.value(), 0)
                );
    }

    private Habit habit(HabitId id, HabitOwner owner, String name, int index) {
        return Habit.from(
                id,
                owner,
                HabitName.of(name),
                HabitColor.of(ColorType.GREEN),
                HabitIndex.of(index),
                Instant.parse("2026-04-01T00:00:00Z")
        );
    }

}
