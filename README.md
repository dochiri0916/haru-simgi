# msa-todo

JWT 기반 인증 서버를 직접 구현한 MSA 학습 프로젝트다. `auth-service`가 로그인, 토큰 재발급, 로그아웃, 권한 변경을 담당하고, `gateway`는 외부 진입점, `user-service`와 `task-service`는 보호된 리소스 서버 역할을 맡는다. 웹 배포를 위해 access/refresh token을 HttpOnly 쿠키로도 내려준다.

현재 단계에서는 학습용 + MVP 출시용 웹을 기준으로, 서비스 간 통신과 서버 구현을 가장 단순한 동기 HTTP MVC로 유지한다. 대신 Java 21 버추얼 스레드를 켜서 블로킹 I/O 기반 코드의 구조를 유지하면서 동시성 비용을 낮추는 방향을 선택했다.

## 서비스 구성

- `gateway`: `/api/auth/**`, `/api/users/**`, `/api/tasks/**` 라우팅
- `auth-service`: 회원가입, 이메일/비밀번호 로그인, access token 발급, refresh token 재발급, 로그아웃, 사용자 권한 변경
- `user-service`: 현재 로그인 사용자 조회와 사용자 프로필 생성/조회
- `task-service`: 인증 사용자 기준 할 일 생성, 완료, 잔디 조회
- `eureka-server`, `config-server`: 서비스 디스커버리와 설정 관리

## 현재 기술 선택

- 외부 API와 서비스 간 호출은 우선 `HTTP + JSON`으로 구현
- `user-service`, `auth-service`, `task-service`는 Spring MVC 기반 동기 처리
- Java 21 버추얼 스레드를 활성화해 동기 코드의 단순함을 유지하면서 요청 대기 비용을 낮춤
- `gateway`는 라우팅 계층이라 Spring Cloud Gateway의 WebFlux 모델을 그대로 사용

## 버추얼 스레드 적용

- 적용 대상: `user-service`, `auth-service`, `task-service`
- 설정: `spring.threads.virtual.enabled=true`
- 기대 효과:
  - JDBC, 내부 HTTP 호출, 인증 처리처럼 블로킹 I/O가 많은 API에서 동시 요청 처리 여유 증가
  - 비동기 체인 없이 동기 코드 구조 유지
  - 학습 초기 단계에서 디버깅과 장애 분석이 단순함
- 한계:
  - DB 응답 속도, 커넥션 풀, 네트워크 지연 자체를 줄여주지는 않음
  - 리액티브 아키텍처의 backpressure 모델을 대체하지는 않음

## 학습 로드맵

이 프로젝트는 아래 순서로 확장하며 공부한다.

1. `HTTP`: 현재 단계. 브라우저와 서버, 서비스 간 호출을 가장 이해하기 쉬운 형태로 구현
2. `gRPC`: 서비스 간 내부 통신을 바이너리 프로토콜과 명시적 계약 기반으로 전환하며 비교
3. `Virtual Thread`: 동기 코드 구조를 유지하면서 JVM 레벨 동시성 비용 절감 효과 확인
4. `Reactive`: WebFlux, 논블로킹 I/O, backpressure까지 포함한 완전한 리액티브 모델 학습

즉 현재 목표는 "처음부터 가장 복잡한 구조"가 아니라, "출시 가능한 MVP를 유지하면서 단계별로 왜 다음 기술이 필요한지 비교 가능한 상태"를 만드는 것이다.

## 인증 흐름

1. `POST /api/auth/register`로 회원가입
2. `auth-service`가 비밀번호를 해시하고 `user-service` 내부 API를 동기 호출해 사용자 프로필 생성
3. `auth-service`가 인증 계정을 저장하고 access token, refresh token을 발급하며 HttpOnly 쿠키를 설정
4. 보호 API 호출 시 `Authorization: Bearer <accessToken>` 또는 access token 쿠키 사용
5. access token 만료 시 `POST /api/auth/refresh`로 재발급하고 쿠키 갱신
6. `POST /api/auth/logout`으로 refresh token 폐기와 인증 쿠키 삭제

## 토큰 전략

- Access Token
  - 짧은 만료 시간
  - `sub=userId`, `role`, `category=access`
  - HttpOnly 쿠키로도 전달되어 브라우저에서 자동 전송 가능
- Refresh Token
  - 긴 만료 시간
  - `jti`, `sub=userId`, `category=refresh`
  - DB 저장 후 재발급과 로그아웃 시 폐기
  - 권한 변경 시 기존 refresh token 즉시 폐기
  - HttpOnly 쿠키로도 전달되어 `refresh/logout` 요청 본문 없이 사용 가능

## 주요 API

### 공개 API

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`

### 보호 API

- `GET /api/users/me`
- `POST /api/tasks`
- `PATCH /api/tasks/{taskId}/complete`
- `GET /api/tasks/grass`
- `PATCH /api/admin/users/{userId}/role` (`ADMIN` 전용)

## 예시 호출

### 회원가입

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "alice@example.com",
    "password": "secret123!"
  }'
```

### 로그인

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -c cookies.txt \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "alice@example.com",
    "password": "secret123!"
  }'
```

### 내 정보 조회

```bash
curl http://localhost:8080/api/users/me \
  -b cookies.txt
```

또는

```bash
curl http://localhost:8080/api/users/me \
  -H "Authorization: Bearer ${ACCESS_TOKEN}"
```

### 사용자 권한 변경

```bash
curl -X PATCH http://localhost:8080/api/admin/users/1/role \
  -H "Authorization: Bearer ${ADMIN_ACCESS_TOKEN}" \
  -H 'Content-Type: application/json' \
  -d '{
    "role": "ADMIN"
  }'
```

### 할 일 생성

```bash
curl -X POST http://localhost:8080/api/tasks \
  -b cookies.txt \
  -H 'Content-Type: application/json' \
  -d '{
    "title": "JWT 인증 서버 포트폴리오 정리"
  }'
```

또는

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  -H 'Content-Type: application/json' \
  -d '{
    "title": "JWT 인증 서버 포트폴리오 정리"
  }'
```

### 로그아웃

```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -b cookies.txt
```

## 포트폴리오 포인트

- 인증 서버를 외부 SaaS에 위임하지 않고 직접 구현
- Access/Refresh 토큰 분리
- Refresh Token 저장소 관리
- `USER`, `ADMIN` 역할 모델과 관리자 전용 권한 변경 API
- Gateway, Auth Service, Resource Service 책임 분리
- 회원가입 데이터와 인증 데이터 분리
- 사용자 본인 소유의 리소스만 수정 가능하도록 서비스 레벨 권한 검증
