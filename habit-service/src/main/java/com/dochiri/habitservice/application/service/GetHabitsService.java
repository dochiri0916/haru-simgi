package com.dochiri.habitservice.application.service;

import com.dochiri.habitservice.application.port.in.GetHabitsUseCase;
import com.dochiri.habitservice.application.port.in.dto.GetHabitsCommand;
import com.dochiri.habitservice.application.port.in.dto.GetHabitsResult;
import com.dochiri.habitservice.application.port.out.HabitRepository;
import com.dochiri.habitservice.domain.habit.HabitOwner;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetHabitsService implements GetHabitsUseCase {

    private final HabitRepository habitRepository;

    @Transactional(readOnly = true)
    @Override
    public GetHabitsResult execute(GetHabitsCommand command) {
        HabitOwner owner = HabitOwner.user(command.ownerPublicId());
        return GetHabitsResult.from(habitRepository.findByOwner(owner));
    }

}
