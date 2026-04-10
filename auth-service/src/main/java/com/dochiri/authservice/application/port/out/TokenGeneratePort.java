package com.dochiri.authservice.application.port.out;

import com.dochiri.authservice.application.port.out.dto.IssuedTokenResult;

public interface TokenGeneratePort {

    IssuedTokenResult generate(Long userId, String role);

}
