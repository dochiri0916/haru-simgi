package com.dochiri.habitservice.application.service;

import com.dochiri.habitservice.application.port.in.MigrateGuestHabitsUseCase;
import com.dochiri.habitservice.application.port.in.dto.MigrateGuestHabitsCommand;
import com.dochiri.habitservice.application.port.in.dto.MigrateGuestHabitsResult;
import com.dochiri.habitservice.application.port.out.HabitRepository;
import com.dochiri.habitservice.domain.habit.HabitOwner;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MigrateGuestHabitsService implements MigrateGuestHabitsUseCase {

    private final HabitRepository habitRepository;

    @Transactional
    @Override
    public MigrateGuestHabitsResult execute(MigrateGuestHabitsCommand command) {
        int migratedCount = habitRepository.migrateOwner(
                HabitOwner.guest(command.guestId()),
                HabitOwner.user(command.userPublicId())
        );
        return new MigrateGuestHabitsResult(migratedCount);
    }
}
