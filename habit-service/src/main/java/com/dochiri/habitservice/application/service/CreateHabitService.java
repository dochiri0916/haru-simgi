package com.dochiri.habitservice.application.service;

import com.dochiri.habitservice.application.port.in.CreateHabitUseCase;
import com.dochiri.habitservice.application.port.in.dto.CreateHabitCommand;
import com.dochiri.habitservice.application.port.in.dto.CreateHabitResult;
import com.dochiri.habitservice.application.port.out.HabitRepository;
import com.dochiri.habitservice.domain.habit.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class CreateHabitService implements CreateHabitUseCase {

    private final HabitRepository habitRepository;
    private final Clock clock;

    @Transactional
    @Override
    public CreateHabitResult execute(CreateHabitCommand command) {
        HabitOwner owner = HabitOwner.user(command.ownerPublicId());
        HabitName name = HabitName.of(command.name());

        ColorType colorType = parseColorType(command.color());
        HabitColor color = HabitColor.of(colorType);
        HabitIndex index = habitRepository.nextIndex(owner);

        Habit habit = Habit.create(owner, name, color, index, Instant.now(clock));
        Habit saved = habitRepository.save(habit);

        return new CreateHabitResult(
                saved.getId().value(),
                saved.getName().value(),
                saved.getColor().colorType().name(),
                saved.getColor().colorType().getHexValue(),
                saved.getIndex().value(),
                saved.getCreatedAt()
        );
    }

    private ColorType parseColorType(String colorString) {
        if (colorString == null || colorString.isBlank()) {
            return null;
        }

        try {
            return ColorType.valueOf(colorString.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
