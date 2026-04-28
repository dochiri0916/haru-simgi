package com.dochiri.habitservice.infrastructure.adapter.in.web.internal;

import com.dochiri.habitservice.application.port.in.MigrateGuestHabitsUseCase;
import com.dochiri.habitservice.infrastructure.adapter.in.web.internal.request.MigrateGuestHabitsRequest;
import com.dochiri.habitservice.infrastructure.adapter.in.web.internal.response.MigrateGuestHabitsResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/habits")
@RequiredArgsConstructor
public class InternalHabitController {

    private final MigrateGuestHabitsUseCase migrateGuestHabitsUseCase;

    @PatchMapping("/guest-owner")
    public ResponseEntity<MigrateGuestHabitsResponse> migrateGuestOwner(
            @Valid @RequestBody MigrateGuestHabitsRequest request
    ) {
        return ResponseEntity.ok(MigrateGuestHabitsResponse.from(
                migrateGuestHabitsUseCase.execute(request.toCommand())
        ));
    }
}
