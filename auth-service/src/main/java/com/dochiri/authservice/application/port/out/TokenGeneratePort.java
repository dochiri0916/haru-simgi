package com.dochiri.authservice.application.port.out;

import com.dochiri.authservice.application.port.out.dto.IssuedTokenResult;

public interface TokenGeneratePort {

    IssuedTokenResult generate(String publicId, String role);

}
