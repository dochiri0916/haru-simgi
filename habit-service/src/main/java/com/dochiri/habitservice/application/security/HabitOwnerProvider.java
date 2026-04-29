package com.dochiri.habitservice.application.security;

import com.dochiri.habitservice.domain.habit.HabitOwner;

public interface HabitOwnerProvider {

    HabitOwner currentOwner();

}
