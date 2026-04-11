package com.dochiri.habitservice.application.service;

import com.dochiri.habitservice.application.port.in.GetHabitsUseCase;
import com.dochiri.habitservice.application.port.in.dto.GetHabitsCommand;
import com.dochiri.habitservice.application.port.in.dto.GetHabitsResult;
import com.dochiri.habitservice.application.port.out.HabitDomainExceptionMapper;
import com.dochiri.habitservice.application.port.out.HabitRepository;
import com.dochiri.habitservice.domain.Habit;
import com.dochiri.habitservice.domain.HabitOwner;
import com.dochiri.habitservice.domain.exception.HabitDomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetHabitsService implements GetHabitsUseCase {

    private final HabitRepository habitRepository;
    private final HabitDomainExceptionMapper domainExceptionMapper;

    @Override
    public GetHabitsResult execute(GetHabitsCommand command) {
        try {
            List<Habit> habits = habitRepository.findByOwner(HabitOwner.user(command.ownerReferenceId()));

            return new GetHabitsResult(habits.stream()
                    .map(h -> new GetHabitsResult.HabitDto(
                            h.getId().value(),
                            h.getName().value()
                    ))
                    .toList());
        } catch (HabitDomainException e) {
            throw domainExceptionMapper.map(e);
        }
    }

}