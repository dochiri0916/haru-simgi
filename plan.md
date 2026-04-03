# Docker Compose 환경변수 정리

## 왜 이 문서를 쓰는가

지금 헷갈리는 포인트는 이거다.

- 로컬에서 앱을 직접 실행할 때는 `localhost`를 쓴다.
- `docker compose` 안에서 서비스끼리 통신할 때는 `localhost`를 쓰면 안 된다.
- 매번 `export ...`로 환경변수를 넣는 방식은 귀찮다.

그래서 `docker compose`를 쓸 때는 보통 `env_file`이나 `.env` 파일로 값을 관리한다.

핵심은:

- 개발자는 `docker compose up`만 치고
- 환경변수는 파일에서 읽게 만들고
- 서비스는 그 값을 자동으로 받아서 실행되게 하는 것이다.

## `localhost`가 왜 문제인가

예를 들어 `user-service`가 도커 컨테이너 안에서 실행 중이라고 하자.

이때 컨테이너 안에서 `localhost:9092`는

- 내 맥북의 Kafka가 아니라
- `user-service` 컨테이너 자기 자신

을 뜻한다.

즉 도커 네트워크 안에서는 `localhost` 대신 서비스 이름을 써야 한다.

예:

- Kafka: `kafka:9092`
- Eureka: `eureka-server:8761`
- Config Server: `config-server:9000`

정리:

- 로컬에서 IDE로 직접 실행: `localhost`
- 도커 컴포즈 안에서 실행: `서비스 이름`

## 어떤 파일로 관리하나

보통 아래처럼 잡는다.

```text
docker-compose.yml
.env
env/
  common.env
  config-server.env
  auth-service.env
  user-service.env
  gateway.env
```

역할은 이렇다.

- `.env`
  `docker-compose.yml` 자체에서 치환할 값
- `env/common.env`
  여러 서비스가 같이 쓰는 값
- `env/config-server.env`
  config-server만 쓰는 값
- `env/auth-service.env`
  auth-service만 쓰는 값
- `env/user-service.env`
  user-service만 쓰는 값

## `env_file`이 뭔가

`docker-compose.yml`에서 컨테이너에 환경변수를 넣는 방법 중 하나다.

예:

```yaml
services:
  auth-service:
    env_file:
      - ./env/common.env
      - ./env/auth-service.env
```

이 뜻은:

- `common.env` 읽고
- `auth-service.env`도 읽어서
- 그 안의 값을 `auth-service` 컨테이너 환경변수로 넣어라

라는 뜻이다.

즉 매번 터미널에서 `export JWT_SECRET=...` 이런 걸 안 해도 된다.

## 예시로 보면

### 1. `env/common.env`

```env
KAFKA_BOOTSTRAP_SERVERS=kafka:9092
EUREKA_DEFAULT_ZONE=http://eureka-server:8761/eureka/
USER_REGISTERED_TOPIC=user.registered
```

여기에는 여러 서비스가 공통으로 쓰는 값을 넣는다.

### 2. `env/config-server.env`

```env
CONFIG_GIT_URI=https://github.com/your-id/msa-config.git
CONFIG_GIT_USERNAME=your-id
CONFIG_GIT_PASSWORD=your-token
```

여기에는 config-server만 쓰는 값을 넣는다.

### 3. `env/auth-service.env`

```env
JWT_SECRET=your-very-long-secret
AUTH_KAFKA_GROUP_ID=auth-service
```

### 4. `env/user-service.env`

```env
JWT_SECRET=your-very-long-secret
```

## Compose 파일에서는 어떻게 쓰나

예시:

```yaml
services:
  kafka:
    image: bitnami/kafka:latest
    ports:
      - "9092:9092"

  config-server:
    build: ./config-server
    env_file:
      - ./env/common.env
      - ./env/config-server.env

  eureka-server:
    build: ./eureka-server
    env_file:
      - ./env/common.env

  auth-service:
    build: ./auth-service
    env_file:
      - ./env/common.env
      - ./env/auth-service.env

  user-service:
    build: ./user-service
    env_file:
      - ./env/common.env
      - ./env/user-service.env

  gateway:
    build: ./gateway
    env_file:
      - ./env/common.env
```

이렇게 하면 각 서비스는 컨테이너 시작 시 자동으로 환경변수를 받는다.

## Spring Boot에서는 어떻게 연결되나

Spring 설정 파일에 이렇게 쓰면 된다.

```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
```

뜻:

- 환경변수 `KAFKA_BOOTSTRAP_SERVERS`가 있으면 그 값을 사용
- 없으면 기본값 `localhost:9092` 사용

즉:

- IDE에서 직접 실행할 때는 기본값 사용 가능
- Docker Compose에서는 `env_file` 값 사용 가능

같은 코드로 둘 다 커버할 수 있다.

## 로컬 직접 실행 vs Compose 실행 차이

### 로컬에서 직접 실행

네가 IntelliJ나 `./gradlew bootRun`으로 직접 띄우면:

- Kafka 주소: `localhost:9092`
- Eureka 주소: `localhost:8761`

처럼 로컬 기준 값이 맞다.

### Docker Compose 실행

컨테이너 안에서는:

- Kafka 주소: `kafka:9092`
- Eureka 주소: `eureka-server:8761`

처럼 서비스 이름 기준이 맞다.

즉 같은 앱이라도 실행 환경에 따라 값만 바뀌면 된다.

## 서버에서는 어떻게 하나

서버에서도 원리는 같다.

- Docker Compose를 쓰면 `env_file` 사용
- 혹은 배포 서버의 CI/CD에서 환경변수 주입
- 민감한 값은 Git에 직접 올리지 않음

즉 서버에서도 매번 `ssh 접속 -> export -> 실행` 이렇게 하지 않는다.

보통은:

- `env/prod/*.env`
- CI secret
- 서버 secret manager

중 하나로 관리한다.

## 결론

정리하면 이렇게 이해하면 된다.

1. 앱 코드는 `${ENV_NAME:default}` 형태로 작성한다.
2. 로컬 직접 실행은 기본값이나 로컬 환경변수를 쓴다.
3. Docker Compose 실행은 `env_file`로 값을 넣는다.
4. 컨테이너 안에서는 `localhost` 대신 서비스 이름을 쓴다.
5. 민감한 값은 코드나 yml에 하드코딩하지 않는다.

## 지금 프로젝트에 적용하면

이 프로젝트에서는 대략 아래 값을 Compose로 넘기게 된다.

- `CONFIG_GIT_URI`
- `CONFIG_GIT_USERNAME`
- `CONFIG_GIT_PASSWORD`
- `KAFKA_BOOTSTRAP_SERVERS`
- `USER_REGISTERED_TOPIC`
- `AUTH_KAFKA_GROUP_ID`
- `JWT_SECRET`

즉 다음 단계는 보통 이거다.

1. `docker-compose.yml` 작성
2. `env/common.env` 작성
3. `env/config-server.env`, `env/auth-service.env`, `env/user-service.env` 작성
4. 각 서비스가 그 환경변수를 읽게 연결

원하면 다음 작업으로 내가 실제 `docker-compose.yml`과 `env/*.env.example`까지 바로 만들어줄 수 있다.
