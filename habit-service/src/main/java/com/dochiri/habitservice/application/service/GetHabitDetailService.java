package com.dochiri.habitservice.application.service;

import com.dochiri.errorhandling.BaseException;
import com.dochiri.habitservice.application.error.HabitErrorCode;
import com.dochiri.habitservice.application.port.in.GetHabitDetailUseCase;
import com.dochiri.habitservice.application.port.in.dto.GetHabitDetailCommand;
import com.dochiri.habitservice.application.port.in.dto.GetHabitDetailResult;
import com.dochiri.habitservice.application.port.out.HabitRepository;
import com.dochiri.habitservice.domain.Habit;
import com.dochiri.habitservice.domain.HabitOwner;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetHabitDetailService implements GetHabitDetailUseCase {

    private final HabitRepository habitRepository;

    @Override
    public GetHabitDetailResult execute(GetHabitDetailCommand command) {
        Habit habit = habitRepository.findById(command.habitId())
            .orElseThrow(() -> new BaseException(HabitErrorCode.HABIT_NOT_FOUND));

        habit.validateOwner(HabitOwner.user(command.ownerReferenceId()));

        return new GetHabitDetailResult(
            habit.getId().value(),
            habit.getName().value(),
            habit.getType()
        );
    }

}
