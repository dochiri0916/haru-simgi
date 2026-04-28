package com.dochiri.authservice.application.port.out;

public interface GuestHabitMigrationPort {

    int migrate(String guestId, String userPublicId);
}
