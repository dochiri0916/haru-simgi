# error-handling 모듈

공통 예외 처리 모듈. Spring Boot Auto-configuration으로 자동 등록된다.

## 구조

```
ErrorCode (interface)          ← 각 서비스의 도메인 레이어에서 구현
DomainException (abstract)     ← 도메인 예외의 기반 (HTTP 의존성 없음)
BaseException                  ← HTTP 응답용 예외 (ProblemDetail 생성)
GlobalExceptionHandler         ← DomainException → HTTP 응답 변환
```

**흐름:** 도메인이 `DomainException`을 던지면 `GlobalExceptionHandler`가 `BaseException`으로 변환해 RFC 7807 ProblemDetail 응답을 반환한다.

## 사용법

### 1. 에러 코드 정의 (도메인 레이어)

```java
// domain/exception/HabitErrorCode.java
public enum HabitErrorCode implements ErrorCode {

    HABIT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 습관을 찾을 수 없습니다."),
    HABIT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근할 수 없습니다.");

    private final HttpStatus status;
    private final String message;

    HabitErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    @Override public HttpStatus getHttpStatus() { return status; }
    @Override public String getMessage() { return message; }
}
```

### 2. 도메인 예외 정의 (도메인 레이어)

생성자에서 키/값 쌍으로 추가 정보를 전달하면 HTTP 응답에 자동으로 포함된다.

```java
// domain/exception/HabitNotFoundException.java
public class HabitNotFoundException extends DomainException {

    public HabitNotFoundException(HabitId habitId) {
        super(HabitErrorCode.HABIT_NOT_FOUND, "habitId", habitId.value());
    }
}
```

### 3. 도메인에서 예외 던지기

```java
if (habit == null) {
    throw new HabitNotFoundException(habitId);
}
```

`GlobalExceptionHandler`가 자동으로 잡아서 아래 형태로 응답한다.

```json
{
  "type": "/errors/habit-not-found",
  "title": "HABIT_NOT_FOUND",
  "status": 404,
  "detail": "해당 습관을 찾을 수 없습니다.",
  "code": "HABIT_NOT_FOUND",
  "habitId": "uuid-value",
  "traceId": "abc123"
}
```

## 제공 클래스

| 클래스 | 용도 |
|---|---|
| `ErrorCode` | 에러 코드 인터페이스. 각 서비스에서 enum으로 구현 |
| `DomainException` | 도메인 예외 기반 클래스. 프레임워크 의존성 없음 |
| `BaseException` | HTTP 응답용 예외. 직접 던지거나 어댑터에서 사용 |
| `CommonErrorCode` | 공통 에러 코드 (INTERNAL_SERVER_ERROR, INVALID_INPUT 등) |
| `UnauthorizedException` | 인증 실패 시 사용 |
