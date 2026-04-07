package com.dochiri.userservice.infrastructure.adapter.in.web.internal;

import com.dochiri.userservice.application.port.in.ProvisionUserUseCase;
import com.dochiri.userservice.infrastructure.adapter.in.web.internal.request.ProvisionUserRequest;
import com.dochiri.userservice.infrastructure.adapter.in.web.internal.response.ProvisionUserResponse;
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

    private final ProvisionUserUseCase provisionUserUseCase;

    @PostMapping
    public ResponseEntity<ProvisionUserResponse> provision(@Valid @RequestBody ProvisionUserRequest request) {
        return ResponseEntity.ok(
                ProvisionUserResponse.from(
                        provisionUserUseCase.provision(request.toCommand())
                )
        );
    }

}
