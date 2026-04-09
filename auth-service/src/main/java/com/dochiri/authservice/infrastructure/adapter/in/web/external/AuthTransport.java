package com.dochiri.authservice.infrastructure.adapter.in.web.external;

import jakarta.servlet.http.HttpServletRequest;

enum AuthTransport {
    COOKIE,
    BEARER;

    boolean usesCookies() {
        return this == COOKIE;
    }

    static AuthTransport from(HttpServletRequest request) {
        String transport = request.getHeader(AuthController.AUTH_TRANSPORT_HEADER);

        if ("bearer".equalsIgnoreCase(transport)) {
            return BEARER;
        }

        return COOKIE;
    }
}
