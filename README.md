# haru-simgi

잔디가 쌓이는 형태의 투두 리스트를 MSA로 구현한 학습 프로젝트다. 사용자는 할 일을 만들고, 완료한 작업이 날짜별로 집계되며, 그 결과가 GitHub 컨트리뷰션 그래프처럼 `level 0~4` 강도로 표현된다. 서비스 핵심은 단순한 할 일 저장이 아니라, "오늘 무엇을 끝냈는지"를 누적해 성취를 시각화하는 데 있다.

현재 저장소는 프론트엔드가 아니라 백엔드 중심 구조다. 외부 진입점은 `gateway`, 인증은 `auth-service`, 사용자 정보는 `user-service`, 할 일과 잔디 집계는 `task-service`가 담당한다. 여기에 `config-server`, `eureka-server`, 공통 모듈들이 붙는 전형적인 Spring Cloud 기반 MSA 구성을 학습 목적에 맞게 단순화했다.

## 서비스 개요

- 카카오 로그인으로 사용자 인증
- JWT Access/Refresh Token 발급 및 재발급
- 사용자별 할 일 생성과 완료 처리
- 완료된 할 일을 날짜 단위로 집계한 잔디 데이터 조회
- Gateway, Discovery, Config Server를 포함한 MSA 구조 학습

## 핵심 사용자 경험

이 서비스의 중심 흐름은 아래처럼 단순하다.

1. 사용자가 카카오 로그인으로 인증한다.
2. 오늘 할 일을 등록한다.
3. 할 일을 완료하면 완료 시각이 기록된다.
4. `from ~ to` 기간의 완료 건수를 날짜별로 집계한다.
5. 완료 수를 잔디 강도(`0~4`)로 변환해 클라이언트에 내려준다.

잔디 강도는 현재 아래 기준으로 계산된다.

- `0`: 완료 0건
- `1`: 완료 1건
- `2`: 완료 2건
- `3`: 완료 3~4건
- `4`: 완료 5건 이상

## 아키텍처

### 서비스 구성

- `gateway`
  - 외부 클라이언트의 단일 진입점
  - `/api/auth/**`, `/api/users/**`, `/api/tasks/**`, `/api/admin/**` 라우팅
- `auth-service`
  - 카카오 OAuth 로그인
  - JWT 발급, 재발급, 로그아웃
  - 소셜 계정과 내부 사용자 계정 연결
- `user-service`
  - 사용자 프로필 생성 및 현재 로그인 사용자 조회
- `task-service`
  - 할 일 생성
  - 본인 소유 할 일 완료 처리
  - 기간별 잔디 집계
- `config-server`
  - 외부 설정 저장소에서 공통 설정 로드
- `eureka-server`
  - 서비스 등록 및 디스커버리
- `modules/*`
  - 보안, JPA, Kafka, 시간, 공통 에러 처리 등 재사용 모듈

### 아키텍처 흐름

```text
Client
  -> Gateway
    -> Auth Service
    -> User Service
    -> Task Service

Auth Service
  -> Kakao API
  -> User Service (최초 로그인 시 사용자 생성)

All Services
  -> Eureka Server
  -> Config Server
```

### 기술적 특징

- Java 21
- Spring Boot 4
- Spring Cloud Gateway
- Eureka Service Discovery
- Spring Cloud Config
- JWT 기반 인증/인가
- MySQL 사용 (`auth-service`, `user-service`는 docker-compose 기준 확인 가능)
- 동기 HTTP + JSON 중심의 단순한 서비스 간 통신
- `auth-service`, `user-service`, `task-service`에 Virtual Thread 활성화

## 도메인 흐름

### 1. 로그인 플로우

```text
Client
  -> GET /api/auth/login/kakao/authorize
  -> Kakao 로그인 완료
  -> GET /api/auth/login/kakao/callback?code=...
Gateway
  -> Auth Service
Auth Service
  -> Kakao 토큰/사용자 정보 조회
  -> 기존 인증 계정 조회
  -> 없으면 User Service에 소셜 사용자 생성 요청
  -> Access/Refresh Token 발급
  -> HttpOnly 쿠키 설정
```

핵심 포인트:

- 외부 OAuth 제공자는 카카오를 사용한다.
- 최초 로그인 시 `auth-service`가 `user-service`를 호출해 내부 사용자 프로필을 만든다.
- 이후 보호 API는 `Authorization: Bearer <token>` 또는 쿠키 기반으로 호출할 수 있다.

### 2. 할 일 생성 플로우

```text
Client
  -> POST /api/tasks
Gateway
  -> Task Service
Task Service
  -> JWT 사용자 식별
  -> 사용자 소유 Task 생성
  -> 저장 후 응답 반환
```

현재 할 일은 인증된 사용자 기준으로 생성된다. 제목은 공백만 허용되지 않으며, 길이 제한도 검증한다.

### 3. 할 일 완료 플로우

```text
Client
  -> PATCH /api/tasks/{taskId}/complete
Gateway
  -> Task Service
Task Service
  -> Task 조회
  -> 요청 사용자와 소유자 일치 검증
  -> completedAt 기록
  -> 완료 상태 저장
```

본인이 소유한 할 일만 완료할 수 있도록 서비스 레벨에서 검증한다.

### 4. 잔디 조회 플로우

```text
Client
  -> GET /api/tasks/grass?from=2026-04-01&to=2026-04-30
Gateway
  -> Task Service
Task Service
  -> 기간 검증
  -> 완료된 Task 조회
  -> 날짜별 완료 건수 집계
  -> count -> level 변환
  -> 잔디 응답 반환
```

응답은 전체 완료 수와 일별 데이터 목록을 포함한다.

```json
{
  "from": "2026-04-01",
  "to": "2026-04-30",
  "totalCompletedCount": 12,
  "days": [
    {
      "date": "2026-04-01",
      "completedCount": 0,
      "level": 0
    },
    {
      "date": "2026-04-02",
      "completedCount": 3,
      "level": 3
    }
  ]
}
```

## API 요약

### 인증

- `GET /api/auth/login/kakao/authorize`
- `POST /api/auth/login/kakao`
- `GET /api/auth/login/kakao/callback`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`

### 사용자

- `GET /api/users/me`

### 할 일 / 잔디

- `POST /api/tasks`
- `PATCH /api/tasks/{taskId}/complete`
- `GET /api/tasks/grass?from=YYYY-MM-DD&to=YYYY-MM-DD`

### 관리자

- `PATCH /api/admin/users/{userId}/role`

## 토큰 전략

### Access Token

- 짧은 만료 시간
- 사용자 식별과 권한 정보 포함
- `Authorization` 헤더 또는 HttpOnly 쿠키로 전달 가능

### Refresh Token

- 긴 만료 시간
- 재발급과 로그아웃에 사용
- 저장소 기반으로 관리되어 폐기 가능
- HttpOnly 쿠키 기반 사용 가능

## 실행 구조

루트 `docker-compose.yml` 기준으로 아래 컴포넌트가 함께 올라간다.

- `kafka`
- `auth-mysql`
- `user-mysql`
- `config-server`
- `eureka-server`
- `auth-service`
- `user-service`
- `task-service`
- `gateway`

실행 전에 확인할 점:

- Java 21 필요
- `config-server`는 외부 Git 설정 저장소가 필요하므로 `CONFIG_GIT_URI` 등 환경 변수가 준비되어야 한다
- 카카오 로그인 테스트를 하려면 `KAKAO_REST_API_KEY`, `KAKAO_CLIENT_SECRET`, `KAKAO_REDIRECT_URI` 설정이 맞아야 한다
- 저장소 루트에서 `config-server.env`를 참조하므로 해당 파일 또는 동등한 환경 구성이 필요하다

예시 실행:

```bash
docker compose up --build
```

## 왜 이 구조인가

이 프로젝트는 단순히 투두 API를 만드는 것이 아니라, 아래를 같이 검증하기 위한 학습 저장소다.

- 인증 서버를 직접 분리했을 때의 책임 경계
- Resource Service와 Auth Service 분리 구조
- Gateway + Discovery + Config Server를 포함한 전형적인 MSA 운영 형태
- 동기식 서비스 간 통신을 유지하면서도 Virtual Thread로 구조 복잡도를 낮추는 방식
- "작업 완료 이벤트를 집계해 시각화 데이터로 바꾸는 서비스"라는 도메인 분리

즉, 이 저장소의 핵심은 "할 일을 완료하면 잔디가 심어지는 투두 서비스"를 MSA 환경에서 어떻게 나누고 연결할지에 있다.
