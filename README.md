# msa-todo

JWT 기반 인증 서버를 직접 구현한 MSA 학습 프로젝트다. `auth-service`가 로그인, 토큰 재발급, 로그아웃, 권한 변경을 담당하고, `gateway`는 외부 진입점, `user-service`와 `task-service`는 보호된 리소스 서버 역할을 맡는다. 웹 배포를 위해 access/refresh token을 HttpOnly 쿠키로도 내려준다.

## 서비스 구성

- `gateway`: `/api/auth/**`, `/api/users/**`, `/api/tasks/**` 라우팅
- `auth-service`: 이메일/비밀번호 로그인, access token 발급, refresh token 재발급, 로그아웃, 사용자 권한 변경
- `user-service`: 회원가입, 현재 로그인 사용자 조회
- `task-service`: 인증 사용자 기준 할 일 생성, 완료, 잔디 조회
- `eureka-server`, `config-server`: 서비스 디스커버리와 설정 관리

## 인증 흐름

1. `POST /api/users`로 회원가입
2. `user-service`가 회원 저장 후 `auth-service` 내부 API를 동기 호출해 인증 계정 생성
4. `POST /api/auth/login`으로 access token, refresh token 발급과 HttpOnly 쿠키 설정
5. 보호 API 호출 시 `Authorization: Bearer <accessToken>` 또는 access token 쿠키 사용
6. access token 만료 시 `POST /api/auth/refresh`로 재발급하고 쿠키 갱신
7. `POST /api/auth/logout`으로 refresh token 폐기와 인증 쿠키 삭제

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

- `POST /api/users`
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
curl -X POST http://localhost:8080/api/users \
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
