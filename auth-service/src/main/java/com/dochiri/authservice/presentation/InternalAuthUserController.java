package com.dochiri.authservice.presentation;

import com.dochiri.authservice.application.port.in.SyncAuthUserUseCase;
import com.dochiri.authservice.presentation.request.SyncAuthUserRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/auth/users")
@RequiredArgsConstructor
public class InternalAuthUserController {

    private final SyncAuthUserUseCase syncAuthUserUseCase;

    @PostMapping
    public ResponseEntity<Void> sync(@Valid @RequestBody SyncAuthUserRequest request) {
        syncAuthUserUseCase.sync(request.toCommand());
        return ResponseEntity.accepted().build();
    }
}
