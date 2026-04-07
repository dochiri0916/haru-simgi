package com.dochiri.userservice.infrastructure.adapter.in.web.internal;

import com.dochiri.userservice.application.port.in.ProvisionSocialUserUseCase;
import com.dochiri.userservice.infrastructure.adapter.in.web.internal.request.ProvisionSocialUserRequest;
import com.dochiri.userservice.infrastructure.adapter.in.web.internal.response.ProvisionSocialUserResponse;
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

    private final ProvisionSocialUserUseCase provisionSocialUserUseCase;

    @PostMapping
    public ResponseEntity<ProvisionSocialUserResponse> provision(@Valid @RequestBody ProvisionSocialUserRequest request) {
        return ResponseEntity.ok(
                ProvisionSocialUserResponse.from(
                        provisionSocialUserUseCase.provision(request.toCommand())
                )
        );
    }
}
