package com.dochiri.userservice.infrastructure.adapter.in.web.internal;

import com.dochiri.userservice.application.port.in.CreateUserUseCase;
import com.dochiri.userservice.infrastructure.adapter.in.web.internal.request.CreateSocialUserRequest;
import com.dochiri.userservice.infrastructure.adapter.in.web.internal.response.CreateSocialUserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/users/social")
@RequiredArgsConstructor
public class InternalSocialUserController {

    private final CreateUserUseCase createUserUseCase;

    @PostMapping
    public ResponseEntity<CreateSocialUserResponse> create(@Valid @RequestBody CreateSocialUserRequest request) {
        return ResponseEntity.ok(
                CreateSocialUserResponse.from(
                        createUserUseCase.execute(request.toCommand())
                )
        );
    }
}
