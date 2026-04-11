package com.dochiri.habitservice.domain;

import com.dochiri.habitservice.domain.exception.InvalidHabitOwnerException;

public record HabitOwner(
        OwnerType type,
        String referenceId
) {

    public HabitOwner {
        validate(type, referenceId);
    }

    public static HabitOwner user(String userId) {
        return new HabitOwner(OwnerType.USER, userId);
    }

    public static HabitOwner guest(String guestId) {
        return new HabitOwner(OwnerType.GUEST, guestId);
    }

    private static void validate(OwnerType type, String referenceId) {

        if (type == null) {
            throw new InvalidHabitOwnerException(
                    InvalidHabitOwnerException.Reason.INVALID_TYPE,
                    null,
                    referenceId
            );
        }

        if (referenceId == null || referenceId.isBlank()) {
            throw new InvalidHabitOwnerException(
                    InvalidHabitOwnerException.Reason.INVALID_REFERENCE_ID,
                    type,
                    referenceId
            );
        }
    }

}