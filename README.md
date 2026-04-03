# msa-todo

Spring Boot 기반 MSA 학습 프로젝트입니다. 현재 구성은 `config-server`, `eureka-server`, `gateway`, `user-service`, `auth-service`와 공통 모듈들로 나뉘어 있습니다.

## 서비스 구성

- `config-server`
  Git 저장소에 있는 설정 파일을 읽어 각 서비스에 제공합니다.
- `eureka-server`
  서비스 디스커버리 서버입니다.
- `gateway`
  외부 요청 진입점입니다.
- `user-service`
  사용자 등록을 담당합니다.
- `auth-service`
  로그인, 토큰 재발급, 인증용 사용자 저장을 담당합니다.

## 현재 흐름

- 외부 요청은 `gateway`를 통해 들어옵니다.
- `user-service`는 회원가입 후 `user.registered` Kafka 이벤트를 발행합니다.
- `auth-service`는 해당 이벤트를 구독해서 인증용 사용자를 저장합니다.

## MSA 초기 설정 순서

보통 아래 순서로 잡으면 됩니다.

1. 멀티모듈 프로젝트 생성
2. `config-server` 구성
3. 설정 전용 Git 저장소 생성
4. `eureka-server` 구성
5. 각 서비스에서 Eureka 클라이언트 설정
6. `gateway` 라우팅 설정
7. 서비스별 비즈니스 구현
8. 서비스 간 통신 방식 선택
   이 프로젝트는 회원가입 후속 동기화에 Kafka 이벤트를 사용합니다.

## 설정 저장소 준비

`config-server`는 별도 Git 저장소의 설정 파일을 읽습니다. 예를 들면 `msa-config` 같은 저장소를 하나 만들고 아래처럼 파일을 둡니다.

```text
msa-config
├── auth-service.yml
├── user-service.yml
├── gateway.yml
├── eureka-server.yml
└── config-server.yml
```

예시:

```yaml
# user-service.yml
server:
  port: 8081

eureka:
  client:
    service-url:
      defaultZone: http://admin:1234@localhost:8761/eureka/
```

```yaml
# auth-service.yml
server:
  port: 8082

eureka:
  client:
    service-url:
      defaultZone: http://admin:1234@localhost:8761/eureka/
```

설정 저장소에는 서비스별 `port`, `eureka`, `datasource`, `jwt`, `kafka` 같은 운영 설정을 넣고, 애플리케이션 코드 저장소에는 최소 설정만 두는 식으로 운영하는 편이 좋습니다.

## Config Server와 GitHub 연동

`config-server`는 현재 [`config-server/src/main/resources/application.yml`](/Users/songseongbin/programming/study/msa-todo/config-server/src/main/resources/application.yml)에 Git 저장소 URI를 읽도록 되어 있습니다.

중요:

- GitHub 토큰을 `application.yml`에 직접 하드코딩하지 않는 것이 좋습니다.
- `PAT` 또는 fine-grained token은 환경변수로 주입하는 방식이 안전합니다.

현재 프로젝트는 아래 환경변수를 사용하도록 맞춰져 있습니다.

```bash
export CONFIG_GIT_URI=https://github.com/<your-id>/<your-config-repo>.git
export CONFIG_GIT_USERNAME=<your-github-id>
export CONFIG_GIT_PASSWORD=<your-github-token>
```

private 저장소를 쓸 때는 `username/password` 방식으로 두고, public 저장소면 `username/password` 없이 `uri`만 둬도 됩니다.

## 서비스 설정 연동

각 서비스는 최종적으로 아래 항목들을 외부 설정에서 받아오게 되는 구조를 권장합니다.

- `server.port`
- `eureka.client.service-url.defaultZone`
- `spring.datasource.*`
- `jwt.*`
- `spring.kafka.*`

지금 리포지토리 안에는 최소한의 로컬 개발 설정만 들어 있습니다.

- [`user-service/src/main/resources/application.yaml`](/Users/songseongbin/programming/study/msa-todo/user-service/src/main/resources/application.yaml)
- [`auth-service/src/main/resources/application.yaml`](/Users/songseongbin/programming/study/msa-todo/auth-service/src/main/resources/application.yaml)
- [`gateway/src/main/resources/application.yml`](/Users/songseongbin/programming/study/msa-todo/gateway/src/main/resources/application.yml)

## Gateway 라우팅

현재 게이트웨이는 아래 경로를 라우팅합니다.

- `/api/auth/**` -> `auth-service`
- `/api/users/**` -> `user-service`

라우팅 설정은 [`gateway/src/main/resources/application.yml`](/Users/songseongbin/programming/study/msa-todo/gateway/src/main/resources/application.yml)에 있습니다.

## Kafka 연동

현재 회원가입 이벤트 토픽은 `user.registered`입니다.

- `user-service`
  producer 역할
- `auth-service`
  consumer 역할

관련 설정 위치:

- [`user-service/src/main/resources/application.yaml`](/Users/songseongbin/programming/study/msa-todo/user-service/src/main/resources/application.yaml)
- [`auth-service/src/main/resources/application.yaml`](/Users/songseongbin/programming/study/msa-todo/auth-service/src/main/resources/application.yaml)

기본 환경변수 예시:

```bash
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
export USER_REGISTERED_TOPIC=user.registered
export AUTH_KAFKA_GROUP_ID=auth-service
```

## 실행 순서

로컬에서 띄울 때는 아래 순서를 권장합니다.

1. Kafka
2. Config Server
3. Eureka Server
4. Auth Service
5. User Service
6. Gateway

예시:

```bash
./gradlew :config-server:bootRun
./gradlew :eureka-server:bootRun
./gradlew :auth-service:bootRun
./gradlew :user-service:bootRun
./gradlew :gateway:bootRun
```

## 로컬에서 우선 필요한 환경변수

최소 예시는 아래 정도입니다.

```bash
export CONFIG_GIT_URI=https://github.com/<your-id>/<your-config-repo>.git
export CONFIG_GIT_USERNAME=<your-github-id>
export CONFIG_GIT_PASSWORD=<your-github-token>
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
export JWT_SECRET=12345678901234567890123456789012
```

## 테스트

서비스 테스트 실행:

```bash
./gradlew :user-service:test :auth-service:test
```

테스트 중 Eureka 서버가 떠 있지 않으면 경고 로그가 보일 수 있습니다. 테스트 자체가 실패한 것은 아니고, 로컬 등록 시도 로그입니다.

## 패키지 기준

현재 프로젝트는 adapter 스타일을 기준으로 정리하고 있습니다.

- `application`
  유스케이스, 포트, 애플리케이션 에러
- `domain`
  도메인 모델
- `infrastructure.adapter.in`
  웹, 메시징 consumer 같은 입력 어댑터
- `infrastructure.adapter.out`
  persistence, Kafka producer 같은 출력 어댑터

에러 코드는 `web` 전용이 아니라 유스케이스 전반에서 사용되므로 서비스별 `application.error`에 두는 기준으로 맞췄습니다.
