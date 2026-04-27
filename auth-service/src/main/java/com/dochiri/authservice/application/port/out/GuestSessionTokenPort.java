package com.dochiri.authservice.application.port.out;

import com.dochiri.authservice.application.port.out.dto.GeneratedGuestSessionToken;

public interface GuestSessionTokenPort {

    GeneratedGuestSessionToken generate();

    String hash(String token);

}
