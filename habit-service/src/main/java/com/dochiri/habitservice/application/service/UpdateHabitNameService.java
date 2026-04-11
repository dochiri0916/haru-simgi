package com.dochiri.habitservice.application.service;

import com.dochiri.habitservice.application.port.in.UpdateHabitNameUseCase;
import com.dochiri.habitservice.application.port.in.dto.UpdateHabitNameCommand;
import com.dochiri.habitservice.application.port.in.dto.UpdateHabitNameResult;
import com.dochiri.habitservice.application.port.out.HabitRepository;
import com.dochiri.habitservice.domain.Habit;
import com.dochiri.habitservice.domain.HabitId;
import com.dochiri.habitservice.domain.HabitName;
import com.dochiri.habitservice.domain.HabitOwner;
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
        HabitId habitId = HabitId.of(command.habitId());
        HabitOwner owner = HabitOwner.user(command.ownerReferenceId());
        HabitName newName = HabitName.of(command.newName());

        Habit habit = habitRepository.loadById(habitId);

        habit.assertOwner(owner);

        Habit updatedHabit = habit.rename(newName);

        Habit saved = habitRepository.save(updatedHabit);

        return toResult(saved);
    }

    private UpdateHabitNameResult toResult(Habit habit) {
        return new UpdateHabitNameResult(
                habit.getId().value(),
                habit.getName().value()
        );
    }

}