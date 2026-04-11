# Error Handling 설계 검토 리포트

## 📋 검토 기준
- DDD (Domain-Driven Design) 원칙
- Hexagonal Architecture (포트-어댑터 패턴)
- Spring 프레임워크 표준 관례
- 대기업 실무 코드 품질 기준

---

## 1️⃣ 현황 분석

### error-handling 모듈 구조
```
BaseException (extends ErrorResponseException)
  ├─ ErrorCode interface → ErrorCode enum 구현
  ├─ ProblemDetail 자동 생성
  └─ properties Map 지원

DomainException (extends RuntimeException)
  └─ 추상 클래스, message만 받음

GlobalExceptionHandler (@RestControllerAdvice)
  ├─ DomainException → 400 BAD_REQUEST
  ├─ Security Exception → 403 FORBIDDEN
  └─ Exception → 500 INTERNAL_SERVER_ERROR
```

### habit-service 사용 패턴
```
Domain Layer
  └─ HabitDomainException (extends DomainException)
      ├─ HabitNotFoundException
      ├─ HabitAccessDeniedException
      ├─ InvalidHabitIdException
      ├─ InvalidHabitNameException
      └─ InvalidHabitOwnerException

Application Layer
  └─ Service에서 도메인 예외 throw

Infrastructure Layer
  ├─ HabitErrorCode (enum implements ErrorCode)
  └─ HabitExceptionHandler (@RestControllerAdvice)
      └─ 각 도메인 예외 → ErrorCode 매핑
```

---

## 2️⃣ 주요 문제점

### 🔴 Critical Issues

#### 1. **BaseException과 DomainException의 목적 불명확**
```java
// error-handling 모듈
public class BaseException extends ErrorResponseException { }
public abstract class DomainException extends RuntimeException { }

// habit-service
public abstract class HabitDomainException extends DomainException { }
public class HabitNotFoundException extends HabitDomainException { }

// ExceptionHandler에서 다시 BaseException으로 감싸기
private ResponseEntity<ProblemDetail> toResponse(HabitErrorCode errorCode, Exception cause) {
    BaseException baseException = new BaseException(errorCode, cause);  // ⚠️ 변환 과정
    return ResponseEntity.status(baseException.getStatusCode()).body(baseException.getBody());
}
```

**문제:**
- DomainException은 단순 marker, 실제 HTTP 응답은 BaseException에서만 생성
- 도메인 예외 → ExceptionHandler → BaseException으로 변환하는 불필요한 단계
- DomainException이 하는 일이 없음

---

#### 2. **계층별 책임이 뒤섞여 있음**
```java
// Domain Layer: 도메인 논리가 아닌 예외 클래스만 정의
public class HabitNotFoundException extends HabitDomainException {
    public HabitNotFoundException() {
        super("해당 습관을 찾을 수 없습니다.");  // 예외 메시지만 가지고 있음
    }
}

// Application Layer: 도메인 로직에서 예외 throw
Habit habit = habitRepository.findById(command.habitId())
    .orElseThrow(HabitNotFoundException::new);

// Infrastructure Layer: 예외를 ErrorCode로 변환
@ExceptionHandler(HabitNotFoundException.class)
public ResponseEntity<ProblemDetail> handleHabitNotFound(HabitNotFoundException e) {
    return toResponse(HabitErrorCode.HABIT_NOT_FOUND, e);
}
```

**문제:**
- 도메인 예외가 HTTP 상태와 무관하게 정의됨
- 서비스마다 ExceptionHandler를 작성해야 함 → 중복 코드
- 예외와 ErrorCode가 이원화됨

---

#### 3. **DDD 위반: 도메인 레이어에서 HTTP 상세 정보를 알아야 함**

현재 구조에서 새로운 예외를 추가하려면:
```
1. Domain에 예외 클래스 추가
   └─ 도메인은 HTTP 상태를 모름

2. ErrorCode enum에 추가
   └─ Infrastructure가 HTTP 상태 정의

3. ExceptionHandler에 핸들러 추가
   └─ 3개 위치에서 수정 필요 (DDD 위반)
```

---

#### 4. **GlobalExceptionHandler의 과도한 권한**

```java
@ExceptionHandler(DomainException.class)
public ResponseEntity<Object> handleDomainException(DomainException exception, HttpServletRequest request) {
    log.warn("매핑되지 않은 도메인 예외입니다...");
    return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)  // ⚠️ 모든 DomainException을 400으로 강제
            .body(createProblemDetail(...));
}
```

**문제:**
- HabitAccessDeniedException은 403 또는 404여야 하지만 400으로 처리됨
- 따라서 habit-service의 ExceptionHandler가 필수 (우회 필요)
- GlobalExceptionHandler를 제대로 쓸 수 없음

---

#### 5. **HabitAccessDeniedException의 모호한 의미**

```java
// HabitExceptionHandler에서
@ExceptionHandler(HabitAccessDeniedException.class)
public ResponseEntity<ProblemDetail> handleHabitAccessDenied(HabitAccessDeniedException e) {
    return toResponse(HabitErrorCode.HABIT_NOT_FOUND, e);  // ⚠️ 403이 아닌 404로 응답
}
```

**문제:**
- 접근 거부(403)와 찾을 수 없음(404)을 의도적으로 섞음
- 보안 상 좋은 패턴이지만, 예외 클래스명과 맞지 않음
- 의도가 코드에 명확하지 않음

---

### 🟡 Major Issues

#### 6. **여러 ExceptionHandler로 인한 일관성 부족**

```
GlobalExceptionHandler (error-handling 모듈)
  ├─ DomainException
  ├─ AccessDeniedException
  └─ Exception

HabitExceptionHandler (habit-service)
  ├─ HabitNotFoundException
  ├─ HabitAccessDeniedException
  ├─ InvalidHabitIdException
  └─ ... (6개 더)

+ 다른 서비스들의 ExceptionHandler들...
```

**문제:**
- 로깅, 응답 형식, 필드 처리가 서비스마다 다를 수 있음
- 새 서비스마다 ExceptionHandler 작성 필수

---

#### 7. **BaseException.of() 메서드의 불명확한 용도**

```java
public static BaseException of(ErrorCode errorCode, Object... keyValues) {
    return new BaseException(errorCode, mapArgs(keyValues));
}

// 사용처를 찾기 어려움 - 도메인 예외에서는 사용 안 함
```

---

#### 8. **로깅 전략 불일치**

```java
// GlobalExceptionHandler
@ExceptionHandler(DomainException.class)
public ResponseEntity<Object> handleDomainException(DomainException exception, HttpServletRequest request) {
    log.warn("매핑되지 않은 도메인 예외입니다. uri={}, method={}, exception={}",
            request.getRequestURI(), request.getMethod(), exception.getClass().getSimpleName());
    // ⚠️ 요청 정보를 로깅하지만...
}

// HabitExceptionHandler
private ResponseEntity<ProblemDetail> toResponse(HabitErrorCode errorCode, Exception cause) {
    BaseException baseException = new BaseException(errorCode, cause);
    // ⚠️ 로깅 없음!
    return ResponseEntity.status(baseException.getStatusCode()).body(baseException.getBody());
}
```

---

## 3️⃣ 대기업 실무 기준 점수

| 항목 | 점수 | 피드백 |
|------|------|--------|
| **DDD 준수** | 4/10 | 도메인 예외 정의는 좋으나, ErrorCode와의 관계가 불명확 |
| **Hexagonal 준수** | 5/10 | 포트는 정의했으나 어댑터의 예외 처리 책임이 불명확 |
| **Spring 표준** | 6/10 | @RestControllerAdvice 사용은 맞으나, 다중 핸들러로 인한 복잡도 증가 |
| **일관성** | 4/10 | 서비스마다 다른 패턴, 예외 정의 방식 불일치 |
| **유지보수성** | 5/10 | 새 예외 추가 시 3곳 수정 필요, 중복 코드 많음 |
| **테스트 용이성** | 6/10 | 예외 클래스는 단순하나, ExceptionHandler 테스트 필요 |
| **문서화** | 5/10 | 예외 정의에 의도가 불명확 (ErrorCode 매핑 규칙 등) |

**평균: 5.1/10** ⚠️ 개선 필요

---

## 4️⃣ 구체적 문제 사례

### Case 1: HabitAccessDeniedException이 HABIT_NOT_FOUND로 응답되는 이유
```java
// Habit.java
public void validateOwner(HabitOwner requestOwner) {
    if (!this.owner.equals(requestOwner)) {
        throw new HabitAccessDeniedException();  // 의도: 접근 거부
    }
}

// HabitExceptionHandler.java
@ExceptionHandler(HabitAccessDeniedException.class)
public ResponseEntity<ProblemDetail> handleHabitAccessDenied(HabitAccessDeniedException e) {
    return toResponse(HabitErrorCode.HABIT_NOT_FOUND, e);  // 404로 응답
}
```

**의도:**
- 보안 상 세부 정보 노출 방지 (타인의 습관 존재 여부 파악 불가)

**문제:**
- 클라이언트가 "접근 거부"인지 "존재하지 않음"인지 구분 불가
- 디버깅 어려움
- 코드 리더가 의도를 즉시 파악 어려움

---

### Case 2: 새로운 예외 추가 시 필요한 수정

```java
// 1단계: Domain에 예외 정의
public class InvalidHabitDateException extends HabitDomainException {
    public InvalidHabitDateException() {
        super("유효하지 않은 날짜입니다.");
    }
}

// 2단계: ErrorCode enum에 추가
public enum HabitErrorCode implements ErrorCode {
    INVALID_HABIT_DATE(HttpStatus.BAD_REQUEST, "유효하지 않은 날짜입니다."),
    // ...
}

// 3단계: ExceptionHandler에 핸들러 추가
@ExceptionHandler(InvalidHabitDateException.class)
public ResponseEntity<ProblemDetail> handleInvalidHabitDate(InvalidHabitDateException e) {
    return toResponse(HabitErrorCode.INVALID_HABIT_DATE, e);
}
```

**문제:**
- 3개 파일 수정 필요
- 예외 메시지가 2곳에 정의됨 (중복)
- 확장성이 떨어짐

---

## 5️⃣ 개선 권장사항

### ✅ 권장안 1: **통합 예외 모델 (권장)**

```
예외를 ErrorCode 기반으로 통합하되, DDD 영역을 명확히 함

Domain Layer:
  └─ 도메인 예외는 비즈니스 의미에만 집중
     └─ ErrorCode와의 매핑은 Application/Infrastructure에서만

Application Layer:
  └─ 도메인 예외를 ErrorCode로 변환하는 책임 (또는 포트로 위임)

Infrastructure Layer:
  └─ ErrorCode → HTTP 응답 변환
     └─ 하나의 통합 GlobalExceptionHandler만 사용
```

**장점:**
- 예외 추가 시 1곳 수정 (ErrorCode enum)
- GlobalExceptionHandler 하나로 통일
- DDD, Hexagonal 원칙 준수

**단점:**
- 도메인 예외 클래스가 필요 없을 수 있음

---

### ✅ 권장안 2: **ErrorResponseException 직접 사용**

```java
// Domain 예외를 정의하되, BaseException으로 직접 throw
public class HabitNotFoundException extends BaseException {
    public HabitNotFoundException() {
        super(HabitErrorCode.HABIT_NOT_FOUND);
    }
}
```

**장점:**
- DomainException을 제거하면서도 타입 안전성 유지
- 추가 변환 단계 제거
- 도메인과 HTTP 응답이 일대일 대응

**단점:**
- Domain Layer가 HTTP Status를 알아야 함 (DDD 위반)
- 포트 인터페이스 정의가 어려움

---

### ✅ 권장안 3: **명시적 매핑 계층 추가**

```java
// Application Layer에 ExceptionMapper 추가
public interface DomainExceptionMapper {
    BaseException toHttpException(DomainException ex);
}

// Infrastructure에서 구현
@Component
public class HabitDomainExceptionMapper implements DomainExceptionMapper {
    @Override
    public BaseException toHttpException(DomainException ex) {
        return switch (ex) {
            case HabitNotFoundException -> new BaseException(HabitErrorCode.HABIT_NOT_FOUND);
            case InvalidHabitNameException -> new BaseException(HabitErrorCode.INVALID_HABIT_NAME);
            // ...
            default -> new BaseException(HabitErrorCode.UNKNOWN_ERROR);
        };
    }
}

// Service에서 사용
@Transactional
public void execute(DeleteHabitCommand command) {
    try {
        // ...
        habitRepository.delete(habitId);
    } catch (DomainException ex) {
        throw exceptionMapper.toHttpException(ex);
    }
}
```

**장점:**
- DDD 원칙 준수 (Domain ← HTTP 의존성 없음)
- 명시적 매핑으로 의도가 명확
- 테스트 용이

**단점:**
- 보일러플레이트 코드 증가
- 매핑 로직을 빠뜨릴 수 있음

---

## 6️⃣ 현재 코드 개선 전 체크리스트

현재 코드를 즉시 사용 가능하게 만들려면:

- [ ] **HabitExceptionHandler에 로깅 추가**
  ```java
  private ResponseEntity<ProblemDetail> toResponse(HabitErrorCode errorCode, Exception cause) {
      log.warn("습관 예외 발생: errorCode={}, cause={}", errorCode.name(), cause.getClass().getSimpleName(), cause);
      BaseException baseException = new BaseException(errorCode, cause);
      return ResponseEntity.status(baseException.getStatusCode()).body(baseException.getBody());
  }
  ```

- [ ] **HabitExceptionHandler를 GlobalExceptionHandler로 병합 (선택)**
  ```java
  // GlobalExceptionHandler에 habit 예외 핸들러 추가
  // 또는 habit-service의 ExceptionHandler를 extends
  ```

- [ ] **도메인 예외 이름 정규화**
  ```
  HabitNotFoundException ✓
  InvalidHabitNameException ✓
  HabitAccessDeniedException ✓
  → 모두 "Habit"으로 시작하는 명확한 네이밍
  ```

- [ ] **ErrorCode enum에 주석 추가**
  ```java
  /**
   * 사용처: DeleteHabitService, GetHabitDetailService에서 ownership validation 실패 시
   * 보안: 타인의 습관 존재 여부를 감추기 위해 의도적으로 404로 응답
   */
  HABIT_NOT_FOUND(HttpStatus.NOT_FOUND, "..."),
  ```

- [ ] **GlobalExceptionHandler의 DomainException 처리 개선**
  ```java
  // 현재: 모든 DomainException을 400으로 강제
  // 개선: 서비스별 ExceptionHandler가 처리하지 못한 예외만 캐치
  @ExceptionHandler(DomainException.class)
  public ResponseEntity<Object> handleDomainException(DomainException exception, HttpServletRequest request) {
      log.warn("처리되지 않은 도메인 예외: uri={}, exception={}", request.getRequestURI(), exception.getClass().getName(), exception);
      return ResponseEntity
              .status(HttpStatus.INTERNAL_SERVER_ERROR)  // 500으로 변경
              .body(createProblemDetail(...));
  }
  ```

---

## 7️⃣ 장기 구조 개선 방향

### Phase 1: 단기 개선 (1-2주)
```
- HabitExceptionHandler에 로깅 추가
- ErrorCode enum에 주석/문서 추가
- GlobalExceptionHandler 안전장치 개선
```

### Phase 2: 중기 개선 (1개월)
```
- 다른 서비스(auth-service, user-service)에서도 같은 패턴 적용 확인
- 서비스별 ExceptionHandler 통합 규칙 수립
- API 문서에 예외 응답 정의
```

### Phase 3: 장기 개선 (분기)
```
- 권장안 3 (명시적 매핑 계층) 도입 검토
- DDD, Hexagonal을 더 엄격히 준수하는 구조로 리팩토링
- 공통 예외 처리 모듈 고도화
```

---

## 📌 핵심 요약

| 평가 | 내용 |
|------|------|
| ✅ **잘한 점** | 도메인 예외를 별도로 정의해서 비즈니스 의미를 명확히 함 |
| ✅ **잘한 점** | BaseException으로 ProblemDetail을 자동 생성해서 HTTP 응답을 표준화함 |
| ❌ **문제점** | DomainException과 BaseException의 목적이 불명확함 |
| ❌ **문제점** | 예외 추가 시 3곳 수정 필요 (확장성 낮음) |
| ❌ **문제점** | 서비스마다 ExceptionHandler를 작성해야 함 (중복) |
| 🔧 **개선안** | ErrorCode 기반으로 통합하면서 DDD 영역 분리 |

**현재 상태:** 기본은 갖춰졌으나, 확장성과 일관성에서 개선 필요 (5~6점/10점 수준의 실무 코드)
