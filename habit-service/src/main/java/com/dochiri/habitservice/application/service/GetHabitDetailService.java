package com.dochiri.habitservice.application.service;

import com.dochiri.habitservice.application.port.in.GetHabitDetailUseCase;
import com.dochiri.habitservice.application.port.in.dto.GetHabitDetailCommand;
import com.dochiri.habitservice.application.port.in.dto.GetHabitDetailResult;
import com.dochiri.habitservice.application.port.out.HabitRepository;
import com.dochiri.habitservice.domain.habit.Habit;
import com.dochiri.habitservice.domain.habit.HabitId;
import com.dochiri.habitservice.domain.habit.HabitOwner;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetHabitDetailService implements GetHabitDetailUseCase {

    private final HabitRepository habitRepository;

    @Transactional(readOnly = true)
    @Override
    public GetHabitDetailResult execute(GetHabitDetailCommand command) {
        HabitId habitId = HabitId.of(command.habitId());
        HabitOwner owner = HabitOwner.user(command.ownerPublicId());

        Habit habit = habitRepository.loadById(habitId);

        habit.assertOwner(owner);

        return GetHabitDetailResult.from(habit);
    }

}
