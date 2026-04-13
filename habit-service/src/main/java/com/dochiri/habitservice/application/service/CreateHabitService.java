package com.dochiri.habitservice.application.service;

import com.dochiri.habitservice.application.port.in.CreateHabitUseCase;
import com.dochiri.habitservice.application.port.in.dto.CreateHabitCommand;
import com.dochiri.habitservice.application.port.in.dto.CreateHabitResult;
import com.dochiri.habitservice.application.port.out.HabitRepository;
import com.dochiri.habitservice.domain.*;
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

        HabitColor.ColorType colorType = parseColorType(command.color());
        HabitColor color = HabitColor.of(colorType);

        Habit habit = Habit.create(owner, name, color);
        Habit saved = habitRepository.save(habit);

        return new CreateHabitResult(
                saved.getId().value(),
                saved.getName().value(),
                saved.getColor().colorType().name(),
                saved.getColor().colorType().getHexValue()
        );
    }

    private HabitColor.ColorType parseColorType(String colorString) {
        if (colorString == null || colorString.isBlank()) {
            return null;
        }

        try {
            return HabitColor.ColorType.valueOf(colorString.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}