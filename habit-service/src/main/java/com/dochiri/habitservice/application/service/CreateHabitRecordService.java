package com.dochiri.habitservice.application.service;

import com.dochiri.habitservice.application.port.in.CreateHabitRecordUseCase;
import com.dochiri.habitservice.application.port.in.dto.CreateHabitRecordCommand;
import com.dochiri.habitservice.application.port.in.dto.CreateHabitRecordResult;
import com.dochiri.habitservice.application.port.out.HabitDomainExceptionMapper;
import com.dochiri.habitservice.application.port.out.HabitRecordRepository;
import com.dochiri.habitservice.application.port.out.HabitRepository;
import com.dochiri.habitservice.domain.HabitId;
import com.dochiri.habitservice.domain.HabitOwner;
import com.dochiri.habitservice.domain.HabitRecord;
import com.dochiri.habitservice.domain.exception.HabitDomainException;
import com.dochiri.habitservice.domain.exception.HabitNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateHabitRecordService implements CreateHabitRecordUseCase {

    private final HabitRepository habitRepository;
    private final HabitRecordRepository habitRecordRepository;
    private final HabitDomainExceptionMapper domainExceptionMapper;

    @Override
    public CreateHabitRecordResult execute(CreateHabitRecordCommand command) {
        try {
            var habit = habitRepository.findById(command.habitId())
                .orElseThrow(HabitNotFoundException::new);

            habit.validateOwner(HabitOwner.user(command.ownerReferenceId()));

            HabitRecord saved = habitRecordRepository.save(HabitRecord.create(
                    HabitId.of(command.habitId()),
                    command.completedAt(),
                    command.value()
            ));

            return new CreateHabitRecordResult(
                    saved.getId().value(),
                    saved.getHabitId().value(),
                    saved.getCompletedAt(),
                    saved.getValue()
            );
        } catch (HabitDomainException e) {
            throw domainExceptionMapper.map(e);
        }
    }

}