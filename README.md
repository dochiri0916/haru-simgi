# haru-simgi

GitHub 컨트리뷰션 그래프처럼 습관 완료 이력을 잔디로 시각화하는 백엔드 프로젝트입니다.  
1인 개발로 진행한 프로젝트이며, Spring Cloud 기반 MSA를 학습 목적으로 구현했고, 인증, 사용자, 습관 도메인을 서비스별로 분리했습니다.

## 한눈에 보기

- 개발 형태: 1인 개발
- 목적: 습관 기록을 누적하고 날짜별 완료 이력을 잔디 형태로 조회
- 형태: Gradle 멀티 모듈 + Spring Cloud MSA
- 진입점: `gateway`
- 운영 URL: `https://www.harusimgi.com`
- 핵심 도메인 분리:
  - `auth-service`: 카카오 OAuth 로그인, JWT 발급/재발급/로그아웃
  - `user-service`: 사용자 생성 및 내 정보 조회
  - `habit-service`: 습관 CRUD, 기록 관리, 잔디 집계
- 인프라:
  - `config-server`: 외부 Git 저장소 기반 설정 관리
  - `eureka-server`: 서비스 디스커버리
  - Redis: 인증 세션/리프레시 토큰 저장
  - MySQL: 서비스별 데이터 저장소 분리
  - Jenkins: CI 파이프라인 실행

## 아키텍처

```text
Client
  -> Gateway (8080)
      -> Auth Service (8082)
      -> User Service (8081)
      -> Habit Service (8083)

Config Server (9000) -> external config repo
Eureka Server (8761) -> service registry
Redis -> auth session / refresh token
MySQL -> service database
Jenkins -> build / test / image pipeline
Kubernetes -> home server deployment
```

### 서비스 책임

| 서비스 | 포트 | 역할 |
|---|---:|---|
| `gateway` | 8080 | 단일 진입점, 라우팅, 인증 필요한 요청 필터링, Swagger 집계 |
| `auth-service` | 8082 | 카카오 OAuth, JWT 발급/재발급, 로그아웃, 관리자 권한 변경 |
| `user-service` | 8081 | 내부 사용자 생성, 내 정보 조회 |
| `habit-service` | 8083 | 습관 생성/수정/삭제, 기록 CRUD, 잔디 데이터 집계 |
| `config-server` | 9000 | 외부 Git 설정 저장소에서 환경별 설정 로드 |
| `eureka-server` | 8761 | 서비스 등록/검색 |

## 코드 구조

이 레포는 백엔드만 포함합니다. 프론트엔드는 별도 레포 또는 클라이언트에서 API를 호출하는 구조를 전제로 합니다.

```text
.
├── auth-service/         # 인증 도메인
├── user-service/         # 사용자 도메인
├── habit-service/        # 습관/기록/잔디 도메인
├── gateway/              # API Gateway
├── config-server/        # Spring Cloud Config Server
├── eureka-server/        # Eureka Server
├── modules/
│   ├── error-handling/   # 공통 예외 처리
│   ├── jpa/              # JPA/감사/QueryDSL 공통 설정
│   ├── kafka/            # Kafka 공통 설정
│   ├── redis/            # Redis 공통 설정
│   ├── security/         # JWT/Spring Security 공통 설정
│   ├── swagger/          # Swagger/OpenAPI 공통 설정
│   └── time/             # 시간 관련 공통 설정
└── infra/docker/         # Dockerfile, compose, Jenkins용 Dockerfile
```

## 설계 포인트

### 1. 서비스별 도메인 분리

- 인증, 사용자, 습관을 별도 애플리케이션으로 분리했습니다.
- 각 서비스는 자기 데이터와 책임만 갖고, Gateway를 통해 외부에 노출됩니다.
- `auth-service`는 로그인 직후 `user-service`에 내부 사용자 생성을 요청합니다.

### 2. 공통 모듈 재사용

- `modules/security`, `modules/error-handling`, `modules:jpa` 같은 공통 모듈을 분리했습니다.
- 서비스 간 중복되는 인증, 예외 처리, JPA 설정을 모듈 수준에서 공유합니다.
- 공통 설정은 Spring Boot AutoConfiguration 형태로 제공됩니다.

### 3. 포트-어댑터 성향 패키지 구조

주요 도메인 서비스는 다음 흐름으로 구성되어 있습니다.

- `application/port/in`: 유스케이스 인터페이스
- `application/service`: 유스케이스 구현
- `application/port/out`: 외부 시스템 의존 인터페이스
- `infrastructure/adapter/in`: 웹 컨트롤러
- `infrastructure/adapter/out`: JPA, Redis, JWT, 외부 HTTP 어댑터
- `domain`: 핵심 도메인 모델과 정책

예를 들어 `auth-service`는 카카오 OAuth, JWT, Redis, User Service 호출을 각각 외부 어댑터로 분리해 두었습니다.

## 요청 흐름

### 인증이 필요한 API

1. 클라이언트가 `gateway`로 요청
2. `gateway`가 JWT/세션 정보를 검사
3. Eureka를 통해 대상 서비스로 라우팅
4. 각 서비스는 공통 `security` 모듈 기반으로 사용자 정보를 해석

### 카카오 로그인

1. 클라이언트가 카카오 인가 URL 요청
2. `auth-service`가 카카오 인증 후 사용자 정보 조회
3. 내부 사용자 없으면 `user-service`에 생성 요청
4. Access/Refresh Token 발급
5. Refresh Token과 세션은 Redis에 저장

## 기술 스택

- Java 21
- Spring Boot 4.0.5
- Spring Cloud 2025.1.1
- Spring Cloud Gateway
- Spring Cloud Config
- Eureka
- Spring Data JPA
- QueryDSL
- Spring Security + JWT
- Redis
- MySQL
- Docker Compose
- Gradle Multi Project

추가로 `auth-service`, `user-service`, `habit-service`는 Virtual Thread를 활성화해 두었습니다.

## 주요 API

```text
GET  /api/auth/login/kakao/authorize
GET  /api/auth/login/kakao/callback
POST /api/auth/login/kakao
POST /api/auth/refresh
POST /api/auth/logout

GET  /api/users/me

GET    /api/habits
POST   /api/habits
GET    /api/habits/{habitId}
PATCH  /api/habits/{habitId}
DELETE /api/habits/{habitId}
GET    /api/habits/{habitId}/records
POST   /api/habits/{habitId}/records
PATCH  /api/habits/{habitId}/records/{recordId}
DELETE /api/habits/{habitId}/records/{recordId}
GET    /api/habits/grass
```

## 로컬 실행

### 1. 빌드

```bash
./gradlew build
```

### 2. Docker Compose 실행

```bash
docker compose -f infra/docker/docker-compose.yml up --build
```

로컬 포트를 열어서 실행하려면:

```bash
docker compose \
  -f infra/docker/docker-compose.yml \
  -f infra/docker/docker-compose.local.yml \
  up --build
```

### 3. 필요한 환경 변수

`config-server.env` 파일이 루트에 필요합니다.

예시:

```env
CONFIG_GIT_URI=https://github.com/{your-org}/{config-repo}.git
CONFIG_GIT_USERNAME={username}
CONFIG_GIT_PASSWORD={token}
KAKAO_REST_API_KEY={kakao-rest-api-key}
KAKAO_CLIENT_SECRET={kakao-client-secret}
```

추가로 실행 환경에 따라 다음 값들을 사용합니다.

- `JWT_SECRET`
- `KAKAO_REDIRECT_URI`
- `KAKAO_FRONTEND_REDIRECT_URI`
- `CONFIG_SERVER_USERNAME`
- `CONFIG_SERVER_PASSWORD`
- `EUREKA_SERVER_USERNAME`
- `EUREKA_SERVER_PASSWORD`

## 배포

- 도메인: `www.harusimgi.com`
- DNS: AWS Route 53에서 도메인 구매 및 관리
- CI: Jenkins로 빌드 및 배포 파이프라인 구성
- 운영 환경: 홈서버 Kubernetes 클러스터에 배포
- 외부 접근: `https://www.harusimgi.com`

프로젝트를 로컬 학습용으로만 끝내지 않고, 실제 도메인을 연결해 외부에서 접속 가능한 형태로 운영 중입니다.

## Swagger

Gateway를 통해 각 서비스 문서를 한 곳에서 볼 수 있게 구성했습니다.

- `http://localhost:8080/swagger-ui.html`
- 또는 `http://localhost:8080/swagger-ui/index.html`

## 인프라 파일 위치

- Compose: [`infra/docker/docker-compose.yml`](./infra/docker/docker-compose.yml)
- 로컬 오버레이: [`infra/docker/docker-compose.local.yml`](./infra/docker/docker-compose.local.yml)
- 운영 오버레이: [`infra/docker/docker-compose.prod.yml`](./infra/docker/docker-compose.prod.yml)
- 서비스 이미지 빌드용 Dockerfile: [`infra/docker/Dockerfile.service`](./infra/docker/Dockerfile.service)
- Jenkins 이미지용 Dockerfile: [`infra/docker/jenkins/Dockerfile`](./infra/docker/jenkins/Dockerfile)
