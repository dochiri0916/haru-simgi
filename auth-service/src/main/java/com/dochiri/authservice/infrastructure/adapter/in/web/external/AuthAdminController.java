package com.dochiri.authservice.infrastructure.adapter.in.web.external;

import com.dochiri.authservice.application.port.in.ChangeUserRoleUseCase;
import com.dochiri.authservice.infrastructure.adapter.in.web.external.request.ChangeUserRoleRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AuthAdminController {

    private final ChangeUserRoleUseCase changeUserRoleUseCase;

    @PatchMapping("/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> changeRole(@PathVariable Long userId, @Valid @RequestBody ChangeUserRoleRequest request) {
        changeUserRoleUseCase.changeRole(request.toCommand(userId));
        return ResponseEntity.noContent().build();
    }

}
