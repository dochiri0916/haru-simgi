package com.dochiri.habitservice.application.service;

import com.dochiri.habitservice.application.port.in.CreateHabitUseCase;
import com.dochiri.habitservice.application.port.in.dto.CreateHabitCommand;
import com.dochiri.habitservice.application.port.in.dto.CreateHabitResult;
import com.dochiri.habitservice.application.port.out.HabitRepository;
import com.dochiri.habitservice.domain.Habit;
import com.dochiri.habitservice.domain.HabitName;
import com.dochiri.habitservice.domain.HabitOwner;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateHabitService implements CreateHabitUseCase {

    private final HabitRepository habitRepository;

    @Transactional
    @Override
    public CreateHabitResult execute(CreateHabitCommand command) {
        HabitOwner owner = HabitOwner.user(command.ownerReferenceId());
        HabitName name = HabitName.of(command.name());

        Habit habit = Habit.create(owner, name);

        Habit saved = habitRepository.save(habit);

        return new CreateHabitResult(
                saved.getId().value(),
                saved.getName().value()
        );
    }

}