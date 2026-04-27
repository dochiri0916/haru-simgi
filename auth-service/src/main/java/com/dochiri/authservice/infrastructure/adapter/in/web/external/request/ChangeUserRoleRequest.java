package com.dochiri.authservice.infrastructure.adapter.in.web.external.request;

import com.dochiri.authservice.application.port.in.dto.ChangeUserRoleCommand;
import com.dochiri.security.role.UserRole;
import jakarta.validation.constraints.NotNull;

public record ChangeUserRoleRequest(
        @NotNull UserRole role
) {
    public ChangeUserRoleCommand toCommand(String publicId) {
        return new ChangeUserRoleCommand(publicId, role);
    }
}
