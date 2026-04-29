package com.dochiri.authservice.infrastructure.adapter.in.web.internal;

import com.dochiri.authservice.application.port.in.VerifyGuestSessionUseCase;
import com.dochiri.authservice.application.port.in.dto.VerifyGuestSessionCommand;
import com.dochiri.authservice.application.port.in.dto.VerifyGuestSessionResult;
import com.dochiri.authservice.infrastructure.adapter.in.web.internal.request.VerifyGuestSessionRequest;
import com.dochiri.authservice.infrastructure.adapter.in.web.internal.response.VerifyGuestSessionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/guest-sessions")
@RequiredArgsConstructor
public class InternalGuestSessionController {

    private final VerifyGuestSessionUseCase verifyGuestSessionUseCase;

    @PostMapping("/verify")
    public ResponseEntity<VerifyGuestSessionResponse> verify(@Valid @RequestBody VerifyGuestSessionRequest request) {
        VerifyGuestSessionResult result = verifyGuestSessionUseCase.execute(new VerifyGuestSessionCommand(request.token()));
        return ResponseEntity.ok(VerifyGuestSessionResponse.from(result));
    }
}
