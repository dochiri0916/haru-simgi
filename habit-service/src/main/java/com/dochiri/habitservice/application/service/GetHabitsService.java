package com.dochiri.habitservice.application.service;

import com.dochiri.habitservice.application.port.in.GetHabitsUseCase;
import com.dochiri.habitservice.application.port.in.dto.GetHabitsCommand;
import com.dochiri.habitservice.application.port.in.dto.GetHabitsResult;
import com.dochiri.habitservice.application.port.out.HabitRepository;
import com.dochiri.habitservice.domain.habit.Habit;
import com.dochiri.habitservice.domain.habit.HabitOwner;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetHabitsService implements GetHabitsUseCase {

    private final HabitRepository habitRepository;

    @Transactional(readOnly = true)
    @Override
    public GetHabitsResult execute(GetHabitsCommand command) {

        HabitOwner owner = HabitOwner.user(command.ownerPublicId());

        List<Habit> habits = habitRepository.findByOwner(owner);

        List<GetHabitsResult.HabitDto> dtos = habits.stream()
                .map(this::toDto)
                .toList();

        return new GetHabitsResult(dtos);
    }


    private GetHabitsResult.HabitDto toDto(Habit habit) {
        return new GetHabitsResult.HabitDto(
                habit.getId().value(),
                habit.getName().value(),
                habit.getColor().colorType().name(),
                habit.getColor().getHexValue(),
                habit.getIndex().value(),
                habit.getCreatedAt()
        );
    }

}
