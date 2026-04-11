package com.dochiri.habitservice.application.service;

import com.dochiri.habitservice.application.port.in.UpdateHabitNameUseCase;
import com.dochiri.habitservice.application.port.in.dto.UpdateHabitNameCommand;
import com.dochiri.habitservice.application.port.in.dto.UpdateHabitNameResult;
import com.dochiri.habitservice.application.port.out.HabitRepository;
import com.dochiri.habitservice.domain.Habit;
import com.dochiri.habitservice.domain.HabitOwner;
import com.dochiri.habitservice.domain.exception.HabitNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateHabitNameService implements UpdateHabitNameUseCase {

    private final HabitRepository habitRepository;

    @Transactional
    @Override
    public UpdateHabitNameResult execute(UpdateHabitNameCommand command) {
        Habit habit = habitRepository.findById(command.habitId())
            .orElseThrow(HabitNotFoundException::new);

        habit.validateOwner(HabitOwner.user(command.ownerReferenceId()));

        Habit updatedHabit = habit.rename(command.newName());
        Habit saved = habitRepository.save(updatedHabit);

        return new UpdateHabitNameResult(
            saved.getId().value(),
            saved.getName().value()
        );
    }

}