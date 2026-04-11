package com.dochiri.habitservice.application.service;

import com.dochiri.habitservice.application.port.in.CreateHabitUseCase;
import com.dochiri.habitservice.application.port.in.dto.CreateHabitCommand;
import com.dochiri.habitservice.application.port.in.dto.CreateHabitResult;
import com.dochiri.habitservice.application.port.out.HabitDomainExceptionMapper;
import com.dochiri.habitservice.application.port.out.HabitRepository;
import com.dochiri.habitservice.domain.Habit;
import com.dochiri.habitservice.domain.HabitOwner;
import com.dochiri.habitservice.domain.exception.HabitDomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateHabitService implements CreateHabitUseCase {

    private final HabitRepository habitRepository;
    private final HabitDomainExceptionMapper domainExceptionMapper;

    @Override
    public CreateHabitResult execute(CreateHabitCommand command) {
        try {
            HabitOwner owner = HabitOwner.user(command.ownerReferenceId());

            Habit saved = habitRepository.save(Habit.create(owner, command.name()));

            return new CreateHabitResult(
                    saved.getId().value(),
                    saved.getName().value()
            );
        } catch (HabitDomainException e) {
            throw domainExceptionMapper.map(e);
        }
    }

}
