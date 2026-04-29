package com.dochiri.authservice.infrastructure.adapter.out.http;

import com.dochiri.authservice.application.port.out.GuestHabitMigrationPort;
import com.dochiri.authservice.domain.exception.AuthErrorCode;
import com.dochiri.authservice.infrastructure.adapter.out.http.request.MigrateGuestHabitsRequest;
import com.dochiri.authservice.infrastructure.adapter.out.http.response.MigrateGuestHabitsResponse;
import com.dochiri.errorhandling.BaseException;
import com.dochiri.security.internalapi.InternalRestClient;
import com.dochiri.security.internalapi.InternalRestClient.InternalRpcRequest;
import org.springframework.stereotype.Component;

@Component
public class HabitServiceGuestHabitMigrationAdapter implements GuestHabitMigrationPort {

    private static final String HABIT_SERVICE_NAME = "habit-service";
    private static final String MIGRATE_HABITS_PATH = "/internal/habits/guest-owner";

    private final InternalRestClient internalRestClient;

    public HabitServiceGuestHabitMigrationAdapter(InternalRestClient internalRestClient) {
        this.internalRestClient = internalRestClient;
    }

    @Override
    public int migrate(String guestId, String userPublicId) {
        MigrateGuestHabitsResponse response = internalRestClient.exchange(
                InternalRpcRequest.patch(
                        HABIT_SERVICE_NAME,
                        MIGRATE_HABITS_PATH,
                        new MigrateGuestHabitsRequest(guestId, userPublicId),
                        MigrateGuestHabitsResponse.class,
                        AuthErrorCode.HABIT_SERVICE_UNAVAILABLE
                )
        );

        if (response == null) {
            throw new BaseException(AuthErrorCode.HABIT_SERVICE_UNAVAILABLE);
        }

        return response.migratedCount();
    }
}
