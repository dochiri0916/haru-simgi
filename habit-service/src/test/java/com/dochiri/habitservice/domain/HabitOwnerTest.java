package com.dochiri.habitservice.domain;

import com.dochiri.habitservice.domain.habit.HabitOwner;
import com.dochiri.habitservice.domain.habit.OwnerType;
import com.dochiri.habitservice.domain.habit.exception.InvalidHabitOwnerException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HabitOwnerTest {

    @Test
    void 사용자_owner를_생성한다() {
        HabitOwner owner = HabitOwner.of(OwnerType.USER, "user-public-id");

        assertThat(owner.type()).isEqualTo(OwnerType.USER);
        assertThat(owner.ownerId()).isEqualTo("user-public-id");
    }

    @Test
    void 게스트_owner를_생성한다() {
        HabitOwner owner = HabitOwner.of(OwnerType.GUEST, "guest-public-id");

        assertThat(owner.type()).isEqualTo(OwnerType.GUEST);
        assertThat(owner.ownerId()).isEqualTo("guest-public-id");
    }

    @Test
    void owner_type은_필수다() {
        assertThatThrownBy(() -> HabitOwner.of(null, "owner-id"))
                .isInstanceOf(InvalidHabitOwnerException.class);
    }

    @Test
    void owner_id는_필수다() {
        assertThatThrownBy(() -> HabitOwner.of(OwnerType.GUEST, " "))
                .isInstanceOf(InvalidHabitOwnerException.class);
    }
}
