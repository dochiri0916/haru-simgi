package com.dochiri.habitservice.domain.habit;

import com.dochiri.habitservice.domain.habit.exception.InvalidHabitOwnerException;

public record HabitOwner(
        OwnerType type,
        String ownerId
) {

    public HabitOwner {
        validate(type, ownerId);
    }

    public static HabitOwner user(String userId) {
        return new HabitOwner(OwnerType.USER, userId);
    }

    public static HabitOwner guest(String guestId) {
        return new HabitOwner(OwnerType.GUEST, guestId);
    }

    public static HabitOwner of(OwnerType type, String ownerId) {
        return new HabitOwner(type, ownerId);
    }

    private static void validate(OwnerType type, String ownerId) {

        if (type == null) {
            throw new InvalidHabitOwnerException(
                    InvalidHabitOwnerException.Reason.INVALID_TYPE,
                    null,
                    ownerId
            );
        }

        if (ownerId == null || ownerId.isBlank()) {
            throw new InvalidHabitOwnerException(
                    InvalidHabitOwnerException.Reason.INVALID_OWNER_ID,
                    type,
                    ownerId
            );
        }
    }

}
