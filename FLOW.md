# 배포 흐름

이 문서는 `haru-simgi`의 목표 배포 흐름을 정리한다. 전체 방향은 `develop` 브랜치에 푸시하면 홈서버 Kubernetes에 자동 배포해 검증하고, 검증된 변경을 `main`에 반영하면 Jenkins 승인 버튼을 거쳐 AWS EC2에 `docker-compose`로 배포하는 구조다.

## 포트폴리오 목적과 비용 전략

이 배포 구조의 1차 목적은 상용 대규모 운영 최적화보다 취업용 포트폴리오에서 백엔드와 인프라 경험을 명확하게 보여주는 것이다. 따라서 CI/CD, Docker, Kubernetes, GitOps, Redis, Kafka, AWS 배포 경험이 실제 흐름 안에 드러나도록 구성한다.

포트폴리오에서 보여줄 핵심 경험은 다음과 같다.

```text
Jenkins 기반 CI/CD
Docker image build/push
Docker Compose 운영 배포
Kubernetes 배포
Argo CD 기반 GitOps
Redis 사용
Kafka 기반 이벤트 처리
AWS EC2 배포
```

동시에 AWS 비용은 최소화한다. AWS EC2는 프리티어로 운용 가능한 `t3.micro`를 사용하고, Kubernetes와 Kafka를 포함한 무거운 검증 환경은 홈서버에서 담당한다.

```text
home server
  -> Kubernetes, Argo CD, Redis, Kafka, MSA 검증 환경
  -> develop 브랜치 기준 자동 배포
  -> 기술 경험 증명 목적

AWS EC2 t3.micro
  -> Docker Compose 기반 최종 공개 배포 환경
  -> main 브랜치 기준 승인 후 배포
  -> 비용 최소화와 포트폴리오 공개 URL 제공 목적
```

즉, 홈서버는 기술 경험을 보여주는 DevOps 검증 환경이고, AWS EC2는 비용을 통제하면서 외부에서 접근 가능한 최종 배포 환경이다.

## 목표 구조

```text
local
  -> push to develop
  -> Jenkins develop pipeline
  -> Docker image push
  -> k8s manifest repo update
  -> Argo CD sync
  -> home server k8s deploy
  -> smoke test
  -> merge develop to main
  -> Jenkins main pipeline
  -> Jenkins manual approval
  -> AWS EC2 docker-compose deploy
```

홈서버는 `develop` 브랜치 기준의 Kubernetes/Argo CD 검증 환경으로 사용한다. AWS EC2는 `main` 브랜치 기준의 최종 배포 환경으로 사용한다. EC2는 `t3.micro` 기준으로 Kubernetes, Kafka, 다수의 Spring Boot 서비스, 다수의 DB를 모두 안정적으로 운영하기에는 리소스가 제한적이므로 `docker-compose`로 단순하게 유지한다.

## 환경별 책임

| 환경 | 역할 | 배포 방식 |
|---|---|---|
| local | 코드 작성, 커밋, Git push | Git |
| Jenkins | 테스트, 이미지 빌드, 이미지 푸시, manifest 업데이트, 승인 게이트, EC2 배포 실행 | Pipeline |
| Docker Registry | 서비스별 Docker 이미지 저장 | Docker Hub 또는 GHCR |
| home server | Kubernetes, Argo CD, Redis, Kafka 기반 사전 배포 및 기술 검증 | k3s + Argo CD |
| k8s manifest repo | 홈서버 Kubernetes 배포 선언 저장 | GitOps |
| AWS EC2 `t3.micro` | 비용 최소화된 최종 공개 배포 환경 | Docker Compose |

## 브랜치 전략

배포 대상은 브랜치 기준으로 분리한다.

| 브랜치 | 역할 | 배포 대상 | 배포 방식 |
|---|---|---|---|
| `develop` | 개발 통합, 사전 검증 | home server k3s | Argo CD 자동 배포 |
| `main` | 운영 기준 브랜치 | AWS EC2 `t3.micro` | Jenkins 승인 후 Docker Compose 배포 |

`develop`에 push된 변경은 홈서버에만 자동 배포한다. EC2에는 `develop` 변경을 직접 배포하지 않는다.

`main`에 반영된 변경만 EC2 배포 대상이 된다. 일반적인 흐름은 `develop`에서 홈서버 검증을 끝낸 뒤 `develop -> main` PR 또는 merge를 진행하고, Jenkins의 승인 버튼을 눌러 EC2에 배포하는 방식이다.

```text
feature branch
  -> merge to develop
  -> home server automatic deploy
  -> smoke test
  -> merge develop to main
  -> Jenkins approval
  -> AWS EC2 deploy
```

## 상세 흐름

### 1. develop push

개발자는 로컬에서 변경 사항을 커밋하고 `develop` 브랜치에 푸시한다.

```text
developer
  -> git push origin develop
  -> app repository develop branch
```

이 저장소에는 Spring Boot MSA 소스, `Jenkinsfile`, `Dockerfile.service`, `docker-compose` 설정이 포함된다.

### 2. Jenkins develop build + docker push

Jenkins는 `develop` push를 기준으로 개발/검증 파이프라인을 시작한다.

현재 `Jenkinsfile`은 아래 흐름을 수행한다.

```text
Checkout
  -> ./gradlew test --no-daemon
  -> Dockerfile.service로 선택한 모듈 이미지 빌드
  -> Docker Hub push
```

현재 이미지 태그 형식은 다음과 같다.

```text
dochiri0916/haru-simgi:{moduleName}-{buildNumber}
```

예시:

```text
dochiri0916/haru-simgi:auth-service-15
dochiri0916/haru-simgi:gateway-15
```

장기적으로는 `buildNumber`만 사용하기보다 Git commit SHA를 함께 남기는 방식이 추적에 유리하다.

```text
dochiri0916/haru-simgi:auth-service-develop-{gitSha}
dochiri0916/haru-simgi:gateway-develop-{gitSha}
```

### 3. k8s manifest repo 업데이트

`develop` 이미지 푸시가 끝나면 Jenkins가 별도의 Kubernetes manifest 저장소를 업데이트한다.

애플리케이션 소스 저장소와 Kubernetes manifest 저장소를 분리한다.

```text
app repo
  - service source code
  - Jenkinsfile
  - Dockerfile.service
  - docker-compose files

k8s manifest repo
  - namespace
  - deployment
  - service
  - ingress
  - config
  - develop image tag
```

Jenkins는 manifest repo의 `Deployment` 이미지 태그만 새 이미지로 변경하고 커밋한다.

```text
Jenkins
  -> clone k8s manifest repo
  -> update image tag
  -> git commit
  -> git push
```

예시 변경 대상:

```yaml
containers:
  - name: gateway
    image: dochiri0916/haru-simgi:gateway-develop-a1b2c3d
```

### 4. Argo CD -> 홈서버 자동 배포

홈서버에는 가벼운 Kubernetes 배포판인 k3s를 사용한다. Argo CD는 manifest repo의 `develop` 배포 선언을 감시하다가 변경 사항이 생기면 홈서버 Kubernetes에 자동으로 동기화한다.

```text
k8s manifest repo
  -> Argo CD
  -> home server k3s
  -> haru-simgi services
```

홈서버는 최종 운영 환경이라기보다 EC2 배포 전 검증 환경이다. 따라서 여기서는 배포 성공 여부, 서비스 기동 여부, Gateway 라우팅, 인증/습관 API의 기본 동작을 확인한다.

포트폴리오 관점에서는 홈서버가 Kubernetes, Argo CD, Redis, Kafka를 실제로 묶어 운영해 본 경험을 보여주는 핵심 환경이다. AWS 프리티어 EC2에 모든 인프라를 무리하게 올리기보다, 홈서버에서 무거운 DevOps 검증을 수행하고 EC2는 공개 배포 환경으로 가볍게 유지한다.

### 5. 검증

Argo CD 배포가 끝난 뒤 Jenkins는 홈서버 배포 결과를 확인한다.

검증 대상은 아래 정도로 시작한다.

```text
Argo CD application sync status
Kubernetes rollout status
Gateway health check
Eureka 등록 상태
주요 API smoke test
```

예시 smoke test:

```text
GET /actuator/health
GET /api/users/me
GET /api/habits/grass?from=2026-04-01&to=2026-04-30
```

인증이 필요한 API는 테스트용 토큰 또는 테스트 계정을 별도로 준비해야 한다.

### 6. develop -> main 반영

홈서버 검증이 통과하면 `develop`의 변경을 `main`에 반영한다.

```text
develop
  -> pull request or merge
  -> main
```

EC2는 `main` 브랜치 기준으로만 배포한다. 따라서 홈서버 검증이 실패한 `develop` 변경은 `main`으로 올리지 않는다.

### 7. Jenkins main build + docker push

`main`에 변경이 들어오면 Jenkins는 운영 배포용 파이프라인을 시작한다. 이 단계에서도 테스트와 이미지 빌드를 다시 수행한다.

운영 이미지 태그는 `main` 기준임을 드러내면서 Git commit을 추적할 수 있게 잡는다.

```text
dochiri0916/haru-simgi:auth-service-main-{gitSha}
dochiri0916/haru-simgi:gateway-main-{gitSha}
```

홈서버에서 검증한 커밋과 EC2에 배포한 커밋을 비교할 수 있도록 Jenkins 로그에 Git SHA를 남긴다.

### 8. Jenkins 승인 버튼

`main` 파이프라인은 EC2 배포 직전에 수동 승인 단계를 둔다.

```groovy
stage('Approve EC2 Deploy') {
    when {
        branch 'main'
    }
    steps {
        input message: 'Deploy main to EC2?', ok: 'Deploy'
    }
}
```

이 단계의 목적은 `main`에 merge된 변경이라도 외부 노출 가능성이 있는 EC2 반영은 사람이 한 번 확인하고 진행하는 것이다.

### 9. EC2 docker-compose 배포

승인 후 Jenkins는 EC2에 SSH로 접속해 `main` 파이프라인에서 만든 이미지 태그를 `docker-compose`로 배포한다.

```text
Jenkins
  -> SSH EC2
  -> docker compose pull
  -> docker compose up -d
  -> docker image prune
```

EC2는 `t3.micro` 기준으로 운영하므로 Kubernetes 대신 Docker Compose를 사용한다. 이 환경은 대규모 트래픽이나 고가용성 운영을 목표로 하지 않고, AWS 배포 경험과 외부 접근 가능한 포트폴리오 URL 제공을 목표로 한다.

```text
AWS EC2 t3.micro
  - Docker
  - Docker Compose
  - docker-compose.yml
  - docker-compose.prod.yml
  - .env
```

EC2 운영 구성은 비용 최소화가 우선이다. Kafka, Kubernetes, Argo CD 같은 무거운 구성은 홈서버 검증 환경에서 경험을 증명하고, EC2에는 main 브랜치 기준의 애플리케이션을 Compose로 배포한다.

필요하면 EC2에서는 아래처럼 최소 운영 구성을 우선한다.

```text
gateway
auth-service
user-service
habit-service
redis
mysql
```

Kafka 기반 이벤트 처리 경험은 홈서버 Kubernetes 환경에서 검증한다. EC2에서 Kafka까지 실행해야 하는 경우에는 `t3.micro` 리소스 제약을 고려해 JVM 메모리 제한, swap, 컨테이너 메모리 제한을 함께 적용한다.

배포 스크립트 예시:

```bash
ssh ec2-user@EC2_HOST <<EOF
  cd /opt/haru-simgi
  export IMAGE_TAG=${IMAGE_TAG}
  docker compose -f docker-compose.yml -f docker-compose.prod.yml pull
  docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d
  docker image prune -f
EOF
```

현재 compose 파일은 로컬 빌드 중심의 `build` 설정을 포함하고 있다. EC2 운영 배포에서는 빌드 대신 `main` 이미지 태그를 registry에서 pull하도록 운영용 override 파일을 분리하는 것이 좋다.

예시:

```yaml
services:
  gateway:
    image: dochiri0916/haru-simgi:gateway-main-${IMAGE_TAG}
    build: null

  auth-service:
    image: dochiri0916/haru-simgi:auth-service-main-${IMAGE_TAG}
    build: null

  user-service:
    image: dochiri0916/haru-simgi:user-service-main-${IMAGE_TAG}
    build: null

  habit-service:
    image: dochiri0916/haru-simgi:habit-service-main-${IMAGE_TAG}
    build: null
```

## 저장소 분리 기준

이 구조에서는 저장소를 최소 두 개로 나누는 것이 좋다.

```text
haru-simgi
  - application source
  - Jenkinsfile
  - Dockerfile.service
  - docker-compose files

haru-simgi-manifest
  - home server Kubernetes manifests for develop
  - Argo CD Application
  - develop service image tags
```

EC2용 compose 파일은 애플리케이션 저장소에 둘 수 있다. EC2는 GitOps 대상이 아니라 `main` 브랜치 기준 Jenkins 승인 후 SSH 배포 대상이기 때문이다.

## 이미지 태그 전략

홈서버와 EC2에는 같은 소스 변경에서 나온 이미지를 배포해야 한다. 다만 브랜치별 배포 대상이 다르므로 태그에는 브랜치 정보를 포함하는 편이 운영상 구분하기 쉽다.

좋은 기준:

```text
develop 이미지는 홈서버 검증에 사용한다.
main 이미지는 EC2 운영 배포에 사용한다.
각 이미지 태그에는 Git SHA를 포함한다.
latest는 운영 배포 기준 태그로 사용하지 않는다.
```

권장 태그:

```text
{moduleName}-{branchName}-{gitSha}
```

예시:

```text
dochiri0916/haru-simgi:gateway-develop-a1b2c3d
dochiri0916/haru-simgi:gateway-main-d4e5f6a
dochiri0916/haru-simgi:auth-service-develop-a1b2c3d
dochiri0916/haru-simgi:auth-service-main-d4e5f6a
```

현재 Jenkins의 `{moduleName}-{buildNumber}` 방식도 동작은 하지만, Git commit과 바로 연결되지 않기 때문에 장애 분석이나 롤백 시 추적성이 떨어진다.

## Jenkins 브랜치 조건 예시

Jenkinsfile은 브랜치에 따라 배포 단계를 분리한다.

```groovy
stage('Deploy Home Server') {
    when {
        branch 'develop'
    }
    steps {
        sh './scripts/update-k8s-manifest.sh'
        sh './scripts/wait-home-deploy.sh'
        sh './scripts/smoke-test-home.sh'
    }
}

stage('Approve EC2 Deploy') {
    when {
        branch 'main'
    }
    steps {
        input message: 'Deploy main to EC2?', ok: 'Deploy'
    }
}

stage('Deploy EC2') {
    when {
        branch 'main'
    }
    steps {
        sh './scripts/deploy-ec2.sh'
    }
}
```

## 최종 권장 플로우

```text
1. local에서 feature 작업
2. develop에 merge 또는 push
3. Jenkins develop pipeline 실행
4. Jenkins가 테스트 실행
5. Jenkins가 develop Docker image build/push
6. Jenkins가 k8s manifest repo의 develop image tag 업데이트
7. Argo CD가 홈서버 k3s에 자동 sync
8. Jenkins가 홈서버 배포 상태와 smoke test 검증
9. 검증이 통과하면 develop을 main에 반영
10. Jenkins main pipeline 실행
11. Jenkins가 main Docker image build/push
12. Jenkins input으로 EC2 배포 승인
13. Jenkins가 EC2에 SSH 접속
14. EC2에서 docker compose pull/up -d
15. EC2 health check로 최종 검증
```

핵심은 `develop`은 홈서버 Kubernetes 검증 환경으로 자동 배포하고, `main`은 EC2 `t3.micro` Docker Compose 운영 환경으로 승인 후 배포하는 것이다.

이 구조에서는 Jenkins가 CI/CD를 담당하고, 홈서버가 Kubernetes, Argo CD, Redis, Kafka를 포함한 기술 경험 증명 환경이 되며, AWS EC2는 비용을 최소화하면서 외부 공개 배포를 담당한다. 따라서 포트폴리오에서는 단순히 서비스를 배포했다는 점뿐 아니라, 비용 제약을 고려해 환경별 책임을 분리하고 현실적인 배포 전략을 설계했다는 점을 함께 보여줄 수 있다.
