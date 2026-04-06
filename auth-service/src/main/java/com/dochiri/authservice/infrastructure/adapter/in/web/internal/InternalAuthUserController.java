package com.dochiri.authservice.infrastructure.adapter.in.web.internal;

import com.dochiri.authservice.application.port.in.SyncAuthUserUseCase;
import com.dochiri.authservice.infrastructure.adapter.in.web.internal.request.CreateAuthUserRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/auth-users")
@RequiredArgsConstructor
public class InternalAuthUserController {

    private final SyncAuthUserUseCase syncAuthUserUseCase;

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody CreateAuthUserRequest request) {
        syncAuthUserUseCase.sync(request.toCommand());
        return ResponseEntity.noContent().build();
    }

}