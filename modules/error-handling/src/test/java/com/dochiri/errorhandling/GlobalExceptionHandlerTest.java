package com.dochiri.errorhandling;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    enum TestErrorCode implements ErrorCode {
        INVALID_HABIT(HttpStatus.BAD_REQUEST, "습관 값이 올바르지 않습니다."),
        CONFLICT_HABIT(HttpStatus.CONFLICT, "이미 처리된 습관입니다.");

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

        TestDomainException(ErrorCode errorCode, Object... keyValues) {
            super(errorCode, keyValues);
        }

    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    @DisplayName("도메인 예외를 ProblemDetail 응답으로 변환한다")
    void handlesDomainException() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/habits");
        DomainException exception = new TestDomainException(
                TestErrorCode.INVALID_HABIT,
                "field", "name",
                "reason", "blank"
        );

        // when
        ResponseEntity<?> response = handler.handleDomainException(exception, request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertProblemDetail(
                response,
                TestErrorCode.INVALID_HABIT,
                Map.of(
                        "field", "name",
                        "reason", "blank"
                )
        );
    }

    @Test
    @DisplayName("BaseException은 가진 ProblemDetail을 그대로 응답한다")
    void handlesBaseException() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("PATCH", "/api/habits/1");
        BaseException exception = BaseException.of(TestErrorCode.CONFLICT_HABIT, "habitId", 1L);

        // when
        ResponseEntity<?> response = handler.handleBaseException(exception, request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertProblemDetail(
                response,
                TestErrorCode.CONFLICT_HABIT,
                Map.of("habitId", 1L)
        );
    }

    @Test
    @DisplayName("예상하지 못한 예외는 내부 서버 오류 응답으로 변환한다")
    void handlesUnexpectedException() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/habits");
        RuntimeException exception = new RuntimeException("unexpected");

        // when
        ResponseEntity<?> response = handler.handleUnexpected(exception, request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertProblemDetail(response, CommonErrorCode.INTERNAL_SERVER_ERROR, Map.of());
    }

    @Test
    @DisplayName("접근 거부 예외는 권한 없음 응답으로 변환한다")
    void handlesAccessDeniedException() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("DELETE", "/api/habits/1");
        AccessDeniedException exception = new AccessDeniedException("forbidden");

        // when
        ResponseEntity<?> response = handler.handleAccessDeniedException(exception, request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertProblemDetail(response, CommonErrorCode.FORBIDDEN, Map.of());
    }

    @Test
    @DisplayName("MDC에 traceId가 있으면 응답 속성에 포함한다")
    void includesTraceId() {
        // given
        MDC.put("traceId", "trace-123");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/habits");
        BaseException exception = new BaseException(TestErrorCode.INVALID_HABIT);

        // when
        ResponseEntity<?> response = handler.handleBaseException(exception, request);

        // then
        ProblemDetail body = (ProblemDetail) response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getProperties()).containsEntry("traceId", "trace-123");
    }

    private void assertProblemDetail(
            ResponseEntity<?> response,
            ErrorCode errorCode,
            Map<String, Object> properties
    ) {
        ProblemDetail body = (ProblemDetail) response.getBody();

        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(errorCode.getHttpStatus().value());
        assertThat(body.getDetail()).isEqualTo(errorCode.getMessage());
        assertThat(body.getTitle()).isEqualTo(errorCode.name());
        assertThat(body.getType())
                .hasToString("/errors/" + errorCode.name().toLowerCase().replace('_', '-'));
        assertThat(body.getProperties()).containsEntry("code", errorCode.name());
        assertThat(body.getProperties()).containsAllEntriesOf(properties);
    }

}
