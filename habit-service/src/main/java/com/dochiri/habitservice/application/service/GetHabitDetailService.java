package com.dochiri.habitservice.application.service;

import com.dochiri.habitservice.application.port.in.GetHabitDetailUseCase;
import com.dochiri.habitservice.application.port.in.dto.GetHabitDetailCommand;
import com.dochiri.habitservice.application.port.in.dto.GetHabitDetailResult;
import com.dochiri.habitservice.application.port.out.HabitDomainExceptionMapper;
import com.dochiri.habitservice.application.port.out.HabitRepository;
import com.dochiri.habitservice.domain.Habit;
import com.dochiri.habitservice.domain.HabitOwner;
import com.dochiri.habitservice.domain.exception.HabitDomainException;
import com.dochiri.habitservice.domain.exception.HabitNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetHabitDetailService implements GetHabitDetailUseCase {

    private final HabitRepository habitRepository;
    private final HabitDomainExceptionMapper domainExceptionMapper;

    @Override
    public GetHabitDetailResult execute(GetHabitDetailCommand command) {
        try {
            Habit habit = habitRepository.findById(command.habitId())
                .orElseThrow(HabitNotFoundException::new);

            habit.validateOwner(HabitOwner.user(command.ownerReferenceId()));

            return new GetHabitDetailResult(
                habit.getId().value(),
                habit.getName().value()
            );
        } catch (HabitDomainException e) {
            throw domainExceptionMapper.map(e);
        }
    }

}
