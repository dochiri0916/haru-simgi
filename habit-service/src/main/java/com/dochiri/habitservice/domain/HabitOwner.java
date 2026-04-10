package com.dochiri.habitservice.domain;

import com.dochiri.habitservice.domain.exception.InvalidHabitOwnerException;

public record HabitOwner(
        OwnerType type,
        String referenceId
) {
    public HabitOwner {
        if (type == null) {
            throw new InvalidHabitOwnerException(InvalidHabitOwnerException.Reason.INVALID_TYPE, "유효하지 않은 소유자 타입입니다.");
        }
        if (referenceId == null || referenceId.isBlank()) {
            throw new InvalidHabitOwnerException(InvalidHabitOwnerException.Reason.INVALID_REFERENCE_ID, "유효하지 않은 소유자 식별자입니다.");
        }
    }

    public static HabitOwner user(String userId) {
        return new HabitOwner(OwnerType.USER, userId);
    }

    public static HabitOwner guest(String guestId) {
        return new HabitOwner(OwnerType.GUEST, guestId);
    }

}