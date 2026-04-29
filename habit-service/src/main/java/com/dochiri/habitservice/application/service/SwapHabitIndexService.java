package com.dochiri.habitservice.application.service;

import com.dochiri.habitservice.application.port.in.SwapHabitIndexUseCase;
import com.dochiri.habitservice.application.port.in.dto.SwapHabitIndexCommand;
import com.dochiri.habitservice.application.port.in.dto.SwapHabitIndexResult;
import com.dochiri.habitservice.application.port.out.HabitRepository;
import com.dochiri.habitservice.domain.habit.Habit;
import com.dochiri.habitservice.domain.habit.HabitId;
import com.dochiri.habitservice.domain.habit.HabitIndex;
import com.dochiri.habitservice.domain.habit.HabitOwner;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SwapHabitIndexService implements SwapHabitIndexUseCase {

    private static final HabitIndex SWAP_PARKING_INDEX = HabitIndex.of(Integer.MAX_VALUE);

    private final HabitRepository habitRepository;

    @Transactional
    @Override
    public SwapHabitIndexResult execute(SwapHabitIndexCommand command) {
        HabitOwner owner = command.owner();
        Habit sourceHabit = habitRepository.loadById(HabitId.of(command.sourceHabitId()));
        Habit targetHabit = habitRepository.loadById(HabitId.of(command.targetHabitId()));

        sourceHabit.assertOwner(owner);
        targetHabit.assertOwner(owner);

        HabitIndex sourceOriginalIndex = sourceHabit.getIndex();
        HabitIndex targetOriginalIndex = targetHabit.getIndex();

        habitRepository.save(sourceHabit.reorder(SWAP_PARKING_INDEX));
        Habit savedTargetHabit = habitRepository.save(targetHabit.reorder(sourceOriginalIndex));
        Habit savedSourceHabit = habitRepository.save(sourceHabit.reorder(targetOriginalIndex));

        return SwapHabitIndexResult.from(List.of(savedSourceHabit, savedTargetHabit));
    }

}
