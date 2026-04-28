package com.dochiri.habitservice.infrastructure.adapter.out.http;

import java.util.Optional;

public interface GuestSessionClient {

    Optional<GuestSessionResult> getGuestSession(String guestSessionToken);
}
