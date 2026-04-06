package com.dochiri.taskservice.domain;

import static java.util.Objects.requireNonNull;

public record TaskOwner(
        OwnerType type,
        String referenceId
) {
    public TaskOwner {
        requireNonNull(type);
        requireNonNull(referenceId);

        if (referenceId.isBlank()) {
            throw new IllegalArgumentException("referenceId is blank");
        }
    }

    public static TaskOwner guest(String guestId) {
        return new TaskOwner(OwnerType.GUEST, guestId);
    }

    public static TaskOwner user(String userId) {
        return new TaskOwner(OwnerType.USER, userId);
    }

    public boolean isGuest() {
        return type == OwnerType.GUEST;
    }

    public boolean isUser() {
        return type == OwnerType.USER;
    }
}
