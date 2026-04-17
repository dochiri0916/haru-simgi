package com.dochiri.errorhandling;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DomainExceptionTest {

    enum TestErrorCode implements ErrorCode {
        INVALID_DOMAIN(HttpStatus.BAD_REQUEST, "도메인 값이 올바르지 않습니다.");

        private final HttpStatus httpStatus;
        private final String message;

        TestErrorCode(HttpStatus httpStatus, String message) {
            this.httpStatus = httpStatus;
            this.message = message;
        }

        @Override
        public HttpStatus getHttpStatus() {
            return httpStatus;
        }

        @Override
        public String getMessage() {
            return message;
        }
    }

    static class TestDomainException extends DomainException {

        TestDomainException(ErrorCode errorCode) {
            super(errorCode);
        }

        TestDomainException(ErrorCode errorCode, Throwable cause) {
            super(errorCode, cause);
        }

        TestDomainException(ErrorCode errorCode, Object... keyValues) {
            super(errorCode, keyValues);
        }

    }

    @Test
    @DisplayName("에러 코드를 기준으로 메시지와 원본 코드를 보관한다")
    void keepsErrorCodeAndMessage() {
        // given
        ErrorCode errorCode = TestErrorCode.INVALID_DOMAIN;

        // when
        DomainException exception = new TestDomainException(errorCode);

        // then
        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getMessage()).isEqualTo("도메인 값이 올바르지 않습니다.");
        assertThat(exception.getProperties()).isEmpty();
    }

    @Test
    @DisplayName("원인 예외를 함께 보관할 수 있다")
    void keepsCause() {
        // given
        RuntimeException cause = new RuntimeException("root cause");

        // when
        DomainException exception = new TestDomainException(TestErrorCode.INVALID_DOMAIN, cause);

        // then
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    @DisplayName("키와 값을 전달하면 속성으로 변환한다")
    void mapsKeyValuesToProperties() {
        // given
        String field = "habitName";
        String reason = "blank";

        // when
        DomainException exception = new TestDomainException(
                TestErrorCode.INVALID_DOMAIN,
                "field", field,
                "reason", reason
        );

        // then
        assertThat(exception.getProperties())
                .containsExactlyInAnyOrderEntriesOf(Map.of(
                        "field", field,
                        "reason", reason
                ));
    }

    @Test
    @DisplayName("키와 값이 짝이 맞지 않으면 예외가 발생한다")
    void throwsExceptionWhenKeyValuesAreOdd() {
        assertThatThrownBy(() -> new TestDomainException(TestErrorCode.INVALID_DOMAIN, "field"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("args는 키/값 쌍이어야 합니다.");
    }

}
