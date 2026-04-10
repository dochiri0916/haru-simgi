package com.dochiri.authservice.application.port.out;

import com.dochiri.authservice.application.port.out.dto.ParseRefreshTokenResult;

public interface TokenParsePort {

    ParseRefreshTokenResult parseRefreshToken(String token);

}
