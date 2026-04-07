package com.dochiri.userservice.infrastructure.adapter.in.web.external;

import com.dochiri.security.jwt.JwtPrincipal;
import com.dochiri.security.role.UserRole;
import com.dochiri.userservice.application.port.in.GetCurrentUserUseCase;
import com.dochiri.userservice.infrastructure.adapter.in.web.external.response.CurrentUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final GetCurrentUserUseCase getCurrentUserUseCase;

    @GetMapping("/me")
    public ResponseEntity<CurrentUserResponse> me(@AuthenticationPrincipal JwtPrincipal principal) {
        return ResponseEntity.ok(
                CurrentUserResponse.from(
                        principal.userId(),
                        getCurrentUserUseCase.getCurrentUser(principal.userId()),
                        UserRole.from(principal.role())
                )
        );
    }

}
