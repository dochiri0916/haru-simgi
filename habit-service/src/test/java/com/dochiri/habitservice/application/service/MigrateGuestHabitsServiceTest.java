package com.dochiri.habitservice.application.service;

import com.dochiri.habitservice.application.port.in.dto.MigrateGuestHabitsCommand;
import com.dochiri.habitservice.application.port.out.HabitRepository;
import com.dochiri.habitservice.domain.habit.HabitOwner;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MigrateGuestHabitsServiceTest {

    private final HabitRepository habitRepository = mock(HabitRepository.class);
    private final MigrateGuestHabitsService service = new MigrateGuestHabitsService(habitRepository);

    @Test
    void 게스트_owner의_습관을_사용자_owner로_이전한다() {
        when(habitRepository.migrateOwner(HabitOwner.guest("guest-id"), HabitOwner.user("user-id")))
                .thenReturn(3);

        var result = service.execute(new MigrateGuestHabitsCommand("guest-id", "user-id"));

        ArgumentCaptor<HabitOwner> sourceCaptor = ArgumentCaptor.forClass(HabitOwner.class);
        ArgumentCaptor<HabitOwner> targetCaptor = ArgumentCaptor.forClass(HabitOwner.class);
        verify(habitRepository).migrateOwner(sourceCaptor.capture(), targetCaptor.capture());
        assertThat(sourceCaptor.getValue()).isEqualTo(HabitOwner.guest("guest-id"));
        assertThat(targetCaptor.getValue()).isEqualTo(HabitOwner.user("user-id"));
        assertThat(result.migratedCount()).isEqualTo(3);
    }
}
