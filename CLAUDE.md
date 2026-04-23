# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 프로젝트 개요

GitHub 컨트리뷰션 그래프처럼 할 일 완료 이력을 잔디로 시각화하는 투두 서비스. Spring Cloud 기반 MSA 학습 프로젝트다.

## 빌드 및 실행 명령어

```bash
# 전체 빌드
./gradlew build

# 특정 모듈 빌드
./gradlew :{MODULE_NAME}:build   # 예: ./gradlew :auth-service:build

# 테스트 실행
./gradlew test --no-daemon

# 특정 모듈 테스트
./gradlew :{MODULE_NAME}:test

# 전체 스택 실행 (Kafka, MySQL x2, Config, Eureka, 3개 서비스, Gateway)
docker compose up --build
```

Docker Compose 실행 전 루트에 `config-server.env` 파일이 필요하다. `CONFIG_GIT_URI`, `KAKAO_REST_API_KEY`, `KAKAO_CLIENT_SECRET`, `KAKAO_REDIRECT_URI` 환경 변수를 설정해야 한다.

## 아키텍처

### 서비스 구성 및 포트

| 서비스 | 포트 | 역할 |
|---|---|---|
| `gateway` | 8080 | 단일 진입점, 라우팅 |
| `auth-service` | 8082 | 카카오 OAuth, JWT 발급/재발급/로그아웃 |
| `user-service` | 8081 | 사용자 프로필 관리 |
| `habit-service` | 8083 | 습관 CRUD, 잔디 집계 |
| `config-server` | 9000 | 외부 Git에서 설정 로드 |
| `eureka-server` | 8761 | 서비스 등록/디스커버리 |

### 공통 모듈 (`modules/`)

- `error-handling`: 공통 예외 처리
- `security`: JWT + Spring Security 설정
- `jpa`: JPA + QueryDSL 설정
- `kafka`: Kafka producer/consumer 설정
- `time`: 시간 유틸리티

각 서비스는 필요한 모듈만 선택적으로 의존한다.

### 서비스 간 통신

- 동기 HTTP + JSON 방식 사용
- Gateway → 각 서비스: Eureka 로드밸런싱(`lb://service-name`)
- Auth Service → User Service: 최초 로그인 시 내부 사용자 생성 요청
- 인프라 기동 순서: `config-server` → `eureka-server` → 나머지 서비스

### 인증 흐름

- 카카오 OAuth 로그인 → JWT Access/Refresh Token 발급
- Access Token: 짧은 만료, `Authorization: Bearer` 헤더 또는 HttpOnly 쿠키
- Refresh Token: 긴 만료, 저장소 기반 관리(폐기 가능), HttpOnly 쿠키

### 잔디 레벨 기준

완료 0건 → level 0, 1건 → 1, 2건 → 2, 3~4건 → 3, 5건 이상 → 4

## 기술 스택

- Java 21 (Virtual Thread 활성화: auth/user/habit-service)
- Spring Boot 4.0.5, Spring Cloud 2025.1.1
- Spring Cloud Gateway, Eureka, Config Server
- JJWT 0.12.6, QueryDSL 5.1.0, SpringDoc OpenAPI 3.0.0
- MySQL (auth-mysql: 3307, user-mysql: 3308), Kafka
- Gradle 멀티 프로젝트 구조

## API 엔드포인트

```
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
GET    /api/habits/{habitId}/records
POST   /api/habits/{habitId}/records
GET    /api/habits/grass

PATCH  /api/admin/users/{userId}/role
```

## CI/CD

`Jenkinsfile`에 파이프라인 정의. `infra/docker/Dockerfile.service`는 멀티스테이지 빌드로 특정 모듈을 빌드한다. 젠킨스 전용 이미지는 `infra/docker/jenkins/Dockerfile`에 둔다.

```bash
docker build -f infra/docker/Dockerfile.service --build-arg MODULE_NAME={module} .
```
