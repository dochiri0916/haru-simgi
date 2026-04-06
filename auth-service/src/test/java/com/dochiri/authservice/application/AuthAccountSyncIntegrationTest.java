package com.dochiri.authservice.application;

import com.dochiri.authservice.application.port.in.AuthenticateUseCase;
import com.dochiri.authservice.application.port.in.ReissueTokenUseCase;
import com.dochiri.authservice.application.port.in.SyncAuthUserUseCase;
import com.dochiri.authservice.application.port.in.dto.LoginCommand;
import com.dochiri.authservice.application.port.in.dto.RefreshTokenCommand;
import com.dochiri.authservice.application.port.in.dto.SyncAuthUserCommand;
import com.dochiri.errorhandling.BaseException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.config.import-check.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:auth-sync-it;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class AuthAccountSyncIntegrationTest {

    @Autowired
    private SyncAuthUserUseCase syncAuthUserUseCase;

    @Autowired
    private AuthenticateUseCase authenticateUseCase;

    @Autowired
    private ReissueTokenUseCase reissueTokenUseCase;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void 동일_사용자를_다시_동기화하면_이메일과_비밀번호가_갱신된다() {
        syncAuthUserUseCase.sync(new SyncAuthUserCommand(
                1L,
                "user-public-id",
                "alice@example.com",
                passwordEncoder.encode("secret123"),
                "USER"
        ));
        syncAuthUserUseCase.sync(new SyncAuthUserCommand(
                1L,
                "user-public-id",
                "alice+updated@example.com",
                passwordEncoder.encode("changed-secret"),
                "USER"
        ));

        assertThatThrownBy(() -> authenticateUseCase.authenticate(new LoginCommand("alice@example.com", "secret123")))
                .isInstanceOf(BaseException.class);

        var result = authenticateUseCase.authenticate(new LoginCommand("alice+updated@example.com", "changed-secret"));

        assertThat(result.accessToken()).isNotBlank();
        assertThat(result.refreshToken()).isNotBlank();
    }

    @Test
    void 새로_로그인하면_이전_리프레시_토큰은_더이상_사용할_수_없다() {
        syncAuthUserUseCase.sync(new SyncAuthUserCommand(
                1L,
                "user-public-id",
                "alice@example.com",
                passwordEncoder.encode("secret123"),
                "USER"
        ));

        var first = authenticateUseCase.authenticate(new LoginCommand("alice@example.com", "secret123"));
        var second = authenticateUseCase.authenticate(new LoginCommand("alice@example.com", "secret123"));

        assertThat(first.refreshToken()).isNotEqualTo(second.refreshToken());
        assertThatThrownBy(() -> reissueTokenUseCase.reissue(new RefreshTokenCommand(first.refreshToken())))
                .isInstanceOf(BaseException.class);
        assertThat(reissueTokenUseCase.reissue(new RefreshTokenCommand(second.refreshToken())).accessToken())
                .isNotBlank();
    }
}
