# security

## 역할

공통 보안 기술 모듈이다.
Spring Security, JWT, 감사, 보안 예외 응답 같은 인프라를 재사용 가능하게 제공한다.

## 제공 기능

- Security auto-configuration
- JWT 생성 및 검증 유틸리티
- 기본 `SecurityFilterChain`
- 인증 실패 / 인가 실패 응답 처리
- 보안 감사용 `AuditorAware`
- 보안 관련 typed properties

## 방향성

- 이 모듈은 `port-adapter` 대상이 아니라 공통 기술 모듈이다.
- 패키지는 기술 역할 기준으로 유지한다.
- 인증 비즈니스 정책은 `auth-service` 내부에서 결정한다.

## 넣지 않을 것

- 로그인 유스케이스
- 사용자 자격정보 소유 정책
- 서비스별 인증/인가 비즈니스 로직
- `port`, `adapter`, `usecase` 네이밍

## 사용 대상

- JWT 인증이 필요한 서비스
- 공통 보안 필터/에러 응답/감사가 필요한 서비스
