package com.dochiri.userservice.application.port.in.dto;

import static java.util.Objects.requireNonNull;

public record GetCurrentUserCommand(
        String publicId
) {
    public GetCurrentUserCommand {
        requireNonNull(publicId);
    }
}
