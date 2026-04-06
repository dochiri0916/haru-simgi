# 고객용 카카오 로그인 우선 인증 전환 계획

## 문서 상태

- 현재 기준 문서
- 현재 채택안은 `Auth0 Free + Kakao 로그인 only + Gateway JWT 검증`이다.
- 직접 운영형 인증 서버 계획은 보류하고, 필요 시 별도 참고 문서로만 사용한다.

## 목표

- 1인 개발 기준으로 인증 복잡도를 최소화하고 빠르게 출시한다.
- 초기 로그인 수단은 `카카오 로그인` 하나만 제공한다.
- 일반 회원가입, 이메일 인증, 휴대폰 인증, 비밀번호 재설정은 초기 범위에서 제외한다.
- 인증 서버를 직접 운영하지 않고 `Auth0 Free`를 사용한다.
- 기존 MSA 구조에서는 `Gateway`가 JWT를 검증하고, 내부 서비스는 도메인 데이터만 처리하게 만든다.

## 최종 방향

- 초기 출시 기준 권장안은 `Auth0 Free + Kakao 로그인 단일 채널`이다.
- 현재 단계에서는 `Naver`, `Google`, `Apple`, 일반 회원가입을 모두 후순위로 미룬다.
- `AWS`에 배포하더라도 인증은 `Auth0`가 담당하고, 애플리케이션은 `Gateway + user-service + 기타 내부 서비스` 구조를 유지한다.
- 현재 `auth-service`는 장기적으로 제거하거나, 남기더라도 자체 로그인 서버가 아니라 내부 회원 동기화 보조 역할로 축소한다.

## 왜 이렇게 범위를 줄이는가

### 1. 가장 중요한 것은 출시 속도다

- 초기 제품에서 가장 비싼 것은 인증 기능의 개수보다 개발 시간이다.
- 카카오 로그인 하나만 먼저 붙이면 로그인 UX, 토큰 검증, 회원 동기화에 집중할 수 있다.
- 인증 수단을 여러 개 열수록 QA 범위와 예외 처리가 급격히 늘어난다.

### 2. 일반 회원가입은 생각보다 일이 많다

- 이메일/비밀번호 회원가입을 열면 아래 항목이 함께 따라온다.
  - 이메일 인증
  - 비밀번호 정책
  - 비밀번호 찾기 및 재설정
  - 계정 탈취 대응
  - 스팸 가입 차단
  - 중복 이메일 병합
- 1인 개발 MVP에서는 이 범위를 피하는 것이 맞다.

### 3. 카카오 하나만으로도 초기 검증은 가능하다

- 국내 B2C에서는 카카오 로그인만으로도 충분히 초기 전환율을 볼 수 있다.
- 로그인 성공률, 회원 전환율, 가입 이탈률, 재방문율을 먼저 확인한 뒤 다른 로그인 수단을 붙이는 것이 더 합리적이다.

## 권장 아키텍처

```text
사용자 앱
  -> Auth0 Universal Login
  -> Kakao
  <- ID Token / Access Token

사용자 앱
  -> Gateway
  -> JWT 검증(Auth0 issuer, audience, JWKS)
  -> user-service / todo-service / 기타 내부 서비스
```

핵심 원칙:

- 로그인과 자격 증명 관리는 `Auth0`가 담당한다.
- 초기 로그인 공급자는 `Kakao` 하나만 사용한다.
- `Gateway`는 Resource Server로서 JWT를 검증한다.
- 내부 서비스는 `sub`, `email`, `name`, `picture`, `provider` 같은 클레임을 사용한다.
- 사용자 프로필과 앱 데이터는 `user-service`가 관리한다.

## 서비스 역할 재정의

### Gateway

- `Auth0`의 `issuer`, `audience`, `JWKS`를 기준으로 액세스 토큰을 검증한다.
- 공개 API와 인증이 필요한 API를 라우팅 기준으로 분리한다.
- 필요 시 사용자 식별 클레임을 내부 보안 컨텍스트로 전달한다.

### user-service

- 앱 내부 사용자 프로필의 원본을 소유한다.
- 예시: 닉네임, 온보딩 상태, 마케팅 동의, 앱 설정, 프로필 이미지 보조 정보
- 카카오 로그인 완료 후 받은 `sub`를 기준으로 내부 회원을 생성하거나 기존 회원과 연결한다.

### auth-service

- 초기 목표는 제거 또는 최소화다.
- 유지한다면 역할은 아래 수준으로 제한한다.
  - 최초 로그인 후 내부 회원 동기화
  - 앱 전용 인증 보조 메타데이터 관리
- 자체 회원가입, 자체 로그인, 자체 비밀번호 저장, 자체 소셜 로그인 브로커 역할은 더 이상 맡기지 않는다.

## 데이터 설계 원칙

- 인증의 원본 식별자는 `Auth0 user_id` 또는 OIDC `sub`다.
- 내부 서비스에서는 이를 `externalAuthId` 같은 이름으로 저장한다.
- 앱 내부에서 필요한 별도 공개 식별자가 있으면 `publicId`를 따로 둔다.

예시:

- `externalAuthId`: `oauth2|kakao|...` 또는 Auth0가 반환하는 사용자 식별자
- `publicId`: 앱 외부 노출용 UUID
- `internalUserId`: 내부 DB PK

권장 규칙:

- 인증 식별 원본은 `Auth0`
- 프로필 원본은 `user-service`
- 비밀번호 관련 데이터는 앱 DB에 저장하지 않음
- 이메일은 보조 정보로 저장하되, 초기에는 `sub`를 절대 기준으로 사용

## 초기 로그인 정책

초기 제공:

1. `Kakao 로그인`

초기 제외:

- 일반 회원가입
- 이메일/비밀번호 로그인
- 이메일 인증
- 휴대폰 인증
- `Naver`
- `Google`
- `Apple`

후속 검토 대상:

- `Naver` 추가
- iOS 출시 시 `Apple` 추가
- 글로벌 확장 시 `Google` 추가
- 정말 필요할 때만 일반 회원가입 추가

## 구현 단계

### 1단계. Auth0 테넌트 및 카카오 로그인 설정

- `Auth0 Free` 테넌트 생성
- 애플리케이션 생성
- 콜백 URL, 로그아웃 URL, 허용 오리진 설정
- `Kakao` 연결
- 필요한 사용자 클레임 확인

완료 기준:

- 카카오 로그인 성공 후 `sub`, `email`, `name`을 안정적으로 수신한다.

### 2단계. Gateway JWT 검증 도입

- `Gateway`를 `Auth0` Resource Server 검증 구조로 전환
- `issuer`, `audience`, `jwks-uri` 설정
- 공개 경로와 보호 경로 정리

완료 기준:

- 유효 토큰만 보호 API에 접근 가능
- 잘못된 토큰은 일관된 `401/403`을 반환

### 3단계. user-service 회원 동기화 도입

- 로그인 완료 후 받은 `sub`를 기준으로 내부 회원 upsert
- 최초 로그인 시 내부 회원 생성
- 재로그인 시 기존 회원과 연결

저장 예시:

- `externalAuthId`
- `email`
- `name`
- `profileImageUrl`
- `provider`
- `lastLoginAt`

완료 기준:

- 같은 카카오 계정으로 다시 로그인해도 같은 내부 회원으로 연결된다.

### 4단계. auth-service 축소 또는 제거

- 자체 회원가입 API 제거 대상 검토
- 자체 로그인 API 제거 대상 검토
- 리프레시 토큰 저장소 제거 여부 검토
- 외부 IdP 사용 시 필요 없는 책임 삭제

완료 기준:

- 로그인 경로가 `Auth0 + Kakao` 중심으로 단일화된다.

### 5단계. 앱 클라이언트 로그인 흐름 연결

- 앱에서 `카카오로 시작하기` 버튼 제공
- `Universal Login` 진입
- 로그인 완료 후 액세스 토큰 저장
- API 호출 시 `Authorization: Bearer <token>` 전달
- 로그아웃 시 앱 세션과 Auth0 세션 정책 정리

완료 기준:

- 카카오 로그인 후 앱 내 보호 API 호출이 정상 동작한다.

## 백엔드 변경 체크리스트

### Gateway

- `JWT issuer` 설정 추가
- `audience` 검증 추가
- `JWKS` 기반 서명 검증 추가
- `/api/public/**`와 `/api/private/**` 경로 정책 정리

### user-service

- `externalAuthId` 컬럼 추가
- 로그인 후 회원 생성/동기화 API 추가
- 동일 이메일 충돌 정책 정리
- `provider=KAKAO` 또는 유사 필드 추가 검토

### auth-service

- 직접 로그인 API 제거 여부 결정
- 자체 회원가입 제거 여부 결정
- 회원 동기화 보조 역할만 남길지 판단

## 클라이언트 변경 체크리스트

- `카카오 로그인` 버튼 추가
- 로그인 성공 후 토큰 저장
- 앱 시작 시 로그인 상태 복원
- 보호 API 호출 공통 인터셉터 추가
- 로그아웃 처리 추가

초기에는 하지 않을 것:

- 이메일 회원가입 UI
- 비밀번호 입력 UI
- 비밀번호 찾기 UI
- SMS 인증 UI

## 운영 기준

### 지금 당장 하지 않을 것

- 일반 회원가입
- 이메일 인증
- 휴대폰 인증
- 자체 비밀번호 로그인 재구축
- 자체 OAuth 서버 구현
- 자체 소셜 로그인 브로커 개발
- `Keycloak` 자체 운영

### 1차 출시 후 확인할 지표

- 카카오 로그인 성공률
- 로그인 전환율
- 회원 생성 성공률
- 인증 실패율
- 월간 활성 사용자 수

### 다음 로그인 수단을 추가할 조건

- 카카오만으로 로그인 이탈이 높을 때
- 특정 사용자층에서 네이버 요구가 강할 때
- iOS 앱 심사 또는 애플 정책 대응이 필요할 때
- 글로벌 사용자 유입으로 구글 요구가 생길 때

## 리스크와 대응

### 리스크 1. 카카오 단일 로그인 의존

- 대응: 초기에는 수용한다.
- 대응: 내부 DB에서는 `externalAuthId`와 앱 `publicId`를 분리해 향후 공급자 추가에 대비한다.

### 리스크 2. 이메일 정보 누락 또는 변경

- 대응: 초기에는 `sub`를 절대 기준으로 사용한다.
- 대응: 이메일은 보조 정보로 저장하고, 회원 식별 키로 직접 사용하지 않는다.

### 리스크 3. 무료 플랜 한계

- 대응: 제품 검증 전에는 수용한다.
- 대응: 성장 시점에 `Cognito`, `Keycloak`, 사내 인증 서비스로 이전 가능하도록 경계를 느슨하게 유지한다.

## 최종 판단

- 현재 프로젝트에서 가장 현실적인 방향은 `Auth0 Free + 카카오 로그인 + Gateway JWT 검증 + user-service 프로필 관리`다.
- `auth-service`를 계속 확장하거나 일반 회원가입을 같이 여는 방향은 초기 출시 전략과 맞지 않는다.
- 지금 목표는 인증 옵션을 많이 만드는 것이 아니라 제품을 실제로 내는 것이다.

## 참고 자료

- Auth0 가격: https://auth0.com/pricing
- Auth0 소셜 로그인: https://auth0.com/docs/authenticate/identity-providers/social-identity-providers
- Auth0 Custom OAuth2 connection: https://auth0.com/docs/authenticate/identity-providers/social-identity-providers/oauth2
- Kakao 로그인 문서: https://developers.kakao.com/docs/latest/ko/tutorial/login
- 오늘의집 API Gateway 사례: https://www.bucketplace.com/post/2021-12-30-msa-phase-1-api-gateway/
- MSA School API Gateway / Auth 서버 예시: https://www.msaschool.io/operation/design/design-seven/
- 관련 학습 자료: https://wikidocs.net/298426
