package com.dochiri.userservice.infrastructure.adapter.in.web.internal;

import com.dochiri.userservice.application.port.in.CreateUserUseCase;
import com.dochiri.userservice.infrastructure.adapter.in.web.internal.request.CreateUserRequest;
import com.dochiri.userservice.infrastructure.adapter.in.web.internal.response.CreateUserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final CreateUserUseCase createUserUseCase;

    @PostMapping
    public ResponseEntity<CreateUserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(
                CreateUserResponse.from(
                        createUserUseCase.execute(
                                request.toCommand()
                        )
                )
        );
    }
}
