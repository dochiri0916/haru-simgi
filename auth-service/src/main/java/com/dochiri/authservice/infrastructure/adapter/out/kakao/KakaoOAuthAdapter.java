package com.dochiri.authservice.infrastructure.adapter.out.kakao;

import com.dochiri.authservice.application.error.AuthErrorCode;
import com.dochiri.authservice.application.port.out.KakaoOAuthPort;
import com.dochiri.authservice.application.port.out.dto.KakaoUserProfile;
import com.dochiri.authservice.infrastructure.adapter.out.kakao.response.KakaoTokenResponse;
import com.dochiri.authservice.infrastructure.adapter.out.kakao.response.KakaoUserInfoResponse;
import com.dochiri.authservice.infrastructure.config.KakaoLoginProperties;
import com.dochiri.errorhandling.BaseException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class KakaoOAuthAdapter implements KakaoOAuthPort {

    private final RestClient restClient;
    private final KakaoLoginProperties kakaoLoginProperties;

    public KakaoOAuthAdapter(
            RestClient.Builder restClientBuilder,
            KakaoLoginProperties kakaoLoginProperties
    ) {
        this.restClient = restClientBuilder.build();
        this.kakaoLoginProperties = kakaoLoginProperties;
    }

    @Override
    public String buildAuthorizeUrl(String state) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(kakaoLoginProperties.authorizeUri())
                .queryParam("response_type", "code")
                .queryParam("client_id", kakaoLoginProperties.restApiKey())
                .queryParam("redirect_uri", kakaoLoginProperties.redirectUri());

        if (StringUtils.hasText(state)) {
            builder.queryParam("state", state);
        }

        return builder.build(true).toUriString();
    }

    @Override
    public KakaoUserProfile authenticate(String authorizationCode) {
        String accessToken = requestAccessToken(authorizationCode);
        return requestUserProfile(accessToken);
    }

    private String requestAccessToken(String authorizationCode) {
        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", kakaoLoginProperties.restApiKey());
        body.add("redirect_uri", kakaoLoginProperties.redirectUri());
        body.add("code", authorizationCode);

        if (StringUtils.hasText(kakaoLoginProperties.clientSecret())) {
            body.add("client_secret", kakaoLoginProperties.clientSecret());
        }

        try {
            KakaoTokenResponse response = restClient.post()
                    .uri(kakaoLoginProperties.tokenUri())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve()
                    .body(KakaoTokenResponse.class);

            if (response == null || !StringUtils.hasText(response.accessToken())) {
                throw new BaseException(AuthErrorCode.KAKAO_AUTHENTICATION_FAILED);
            }

            return response.accessToken();
        } catch (RestClientResponseException exception) {
            throw mapProviderException(exception);
        } catch (RestClientException exception) {
            throw new BaseException(AuthErrorCode.KAKAO_PROVIDER_UNAVAILABLE, exception);
        }
    }

    private KakaoUserProfile requestUserProfile(String accessToken) {
        try {
            KakaoUserInfoResponse response = restClient.get()
                    .uri(kakaoLoginProperties.userInfoUri())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(KakaoUserInfoResponse.class);

            return mapToProfile(response);
        } catch (RestClientResponseException exception) {
            throw mapProviderException(exception);
        } catch (RestClientException exception) {
            throw new BaseException(AuthErrorCode.KAKAO_PROVIDER_UNAVAILABLE, exception);
        }
    }

    private KakaoUserProfile mapToProfile(KakaoUserInfoResponse response) {
        if (response == null || response.id() == null) {
            throw new BaseException(AuthErrorCode.KAKAO_AUTHENTICATION_FAILED);
        }

        KakaoUserInfoResponse.KakaoAccount kakaoAccount = response.kakaoAccount();
        String email = kakaoAccount != null ? kakaoAccount.email() : null;
        KakaoUserInfoResponse.Profile profile = kakaoAccount != null ? kakaoAccount.profile() : null;
        String nickname = profile != null ? profile.nickname() : null;
        String profileImageUrl = profile != null ? profile.profileImageUrl() : null;

        return new KakaoUserProfile(response.id(), email, nickname, profileImageUrl);
    }

    private BaseException mapProviderException(RestClientResponseException exception) {
        if (exception.getStatusCode().is4xxClientError()) {
            return new BaseException(AuthErrorCode.KAKAO_AUTHENTICATION_FAILED, exception);
        }
        return new BaseException(AuthErrorCode.KAKAO_PROVIDER_UNAVAILABLE, exception);
    }

}