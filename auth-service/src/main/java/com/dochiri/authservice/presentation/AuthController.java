package com.dochiri.authservice.presentation;

import com.dochiri.authservice.application.port.in.AuthenticateUseCase;
import com.dochiri.authservice.application.port.in.ReissueTokenUseCase;
import com.dochiri.authservice.presentation.request.LoginRequest;
import com.dochiri.authservice.presentation.request.RefreshTokenRequest;
import com.dochiri.authservice.presentation.response.AuthTokenResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticateUseCase authenticateUseCase;
    private final ReissueTokenUseCase reissueTokenUseCase;

    @PostMapping("/login")
    public ResponseEntity<AuthTokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(
                AuthTokenResponse.from(
                        authenticateUseCase.authenticate(request.toCommand())
                )
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthTokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(
                AuthTokenResponse.from(
                        reissueTokenUseCase.reissue(request.toCommand())
                )
        );
    }
}
