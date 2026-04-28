package com.dochiri.authservice.application.port.in.dto;

import static java.util.Objects.requireNonNull;

public record KakaoLoginResult(
        IssueAuthTokenResult tokens,
        GuestMergeStatus guestMerge
) {
    public KakaoLoginResult {
        requireNonNull(tokens, "tokens는 필수입니다.");
        requireNonNull(guestMerge, "guestMerge는 필수입니다.");
    }
}
