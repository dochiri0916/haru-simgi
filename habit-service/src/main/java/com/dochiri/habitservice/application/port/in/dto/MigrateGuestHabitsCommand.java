package com.dochiri.habitservice.application.port.in.dto;

import static java.util.Objects.requireNonNull;

public record MigrateGuestHabitsCommand(
        String guestId,
        String userPublicId
) {

    public MigrateGuestHabitsCommand {
        requireNonBlank(guestId, "guestId는 비어 있을 수 없습니다.");
        requireNonBlank(userPublicId, "userPublicId는 비어 있을 수 없습니다.");
    }

    private static void requireNonBlank(String value, String message) {
        requireNonNull(value, message);
        if (value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}
