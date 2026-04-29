pipeline {
    agent any

    triggers {
        githubPush()
    }

    environment {
        DOCKERHUB_REPO = "dochiri"
        APP_REPO_URL = "https://github.com/dochiri0916/haru-simgi.git"
        DEPLOY_REPO_URL = "https://github.com/dochiri0916/haru-simgi-deploy.git"
        DEPLOY_BRANCH = "main"
        BASE_VERSION = "0.1"
    }

    stages {
        stage('Checkout') {
            steps {
                deleteDir()
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: '*/main']],
                    userRemoteConfigs: [[
                        url: "${APP_REPO_URL}",
                        credentialsId: 'github'
                    ]]
                ])

                script {
                    env.GIT_SHORT_SHA = sh(
                        script: "git rev-parse --short HEAD",
                        returnStdout: true
                    ).trim()

                    env.IMAGE_TAG = "${BASE_VERSION}.${BUILD_NUMBER}-${env.GIT_SHORT_SHA}"

                    echo "Image tag: ${env.IMAGE_TAG}"
                }
            }
        }

        stage('Test') {
            steps {
                sh 'chmod +x ./gradlew'
                sh './gradlew test --no-daemon'
            }
        }

        stage('Detect Changed Services') {
            steps {
                script {
                    def changedOutput = sh(
                        script: "git diff --name-only HEAD~1 HEAD || true",
                        returnStdout: true
                    ).trim()

                    def changedFiles = changedOutput ? changedOutput.split("\n") : []
                    def services = [] as Set

                    def allServices = [
                        "gateway",
                        "auth-service",
                        "user-service",
                        "habit-service",
                        "config-server",
                        "eureka-server"
                    ]

                    def buildAll = false

                    changedFiles.each { file ->
                        if (
                            file.startsWith("modules/") ||
                            file.startsWith("infra/docker/") ||
                            file == "settings.gradle" ||
                            file == "build.gradle" ||
                            file == "gradlew" ||
                            file == "gradlew.bat"
                        ) {
                            buildAll = true
                        }

                        if (file.startsWith("gateway/")) services.add("gateway")
                        if (file.startsWith("auth-service/")) services.add("auth-service")
                        if (file.startsWith("user-service/")) services.add("user-service")
                        if (file.startsWith("habit-service/")) services.add("habit-service")
                        if (file.startsWith("config-server/")) services.add("config-server")
                        if (file.startsWith("eureka-server/")) services.add("eureka-server")
                    }

                    if (buildAll) {
                        services = allServices as Set
                    }

                    env.CHANGED_SERVICES = services.join(",")
                    echo "Changed services: ${env.CHANGED_SERVICES}"
                }
            }
        }

        stage('Build & Push') {
            when {
                expression { return env.CHANGED_SERVICES?.trim() }
            }
            steps {
                script {
                    def services = env.CHANGED_SERVICES.split(",")

                    withCredentials([usernamePassword(
                        credentialsId: 'dockerhub',
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS'
                    )]) {
                        sh '''
                        echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                        '''

                        for (svc in services) {
                            if (svc?.trim()) {
                                def imageName = "${DOCKERHUB_REPO}/harusimgi-${svc}"

                                sh """
                                docker build \
                                  -t ${imageName}:${IMAGE_TAG} \
                                  -f infra/docker/Dockerfile.service \
                                  --build-arg MODULE_NAME=${svc} \
                                  .

                                docker push ${imageName}:${IMAGE_TAG}
                                """
                            }
                        }
                    }
                }
            }
        }

        stage('Update Deploy Manifests') {
            when {
                expression { return env.CHANGED_SERVICES?.trim() }
            }
            steps {
                dir('haru-simgi-deploy') {
                    checkout([
                        $class: 'GitSCM',
                        branches: [[name: "*/${DEPLOY_BRANCH}"]],
                        extensions: [[
                            $class: 'LocalBranch',
                            localBranch: "${DEPLOY_BRANCH}"
                        ]],
                        userRemoteConfigs: [[
                            url: "${DEPLOY_REPO_URL}",
                            credentialsId: 'github'
                        ]]
                    ])

                    script {
                        def services = env.CHANGED_SERVICES.split(",")

                        for (svc in services) {
                            if (svc?.trim()) {
                                def imageName = "${DOCKERHUB_REPO}/harusimgi-${svc}"
                                def deploymentFile = "manifests/${svc}/deployment.yaml"

                                sh """
                                test -f ${deploymentFile}
                                sed -i 's|image: ${imageName}:.*|image: ${imageName}:${IMAGE_TAG}|g' ${deploymentFile}
                                """
                            }
                        }

                        withCredentials([usernamePassword(
                            credentialsId: 'github',
                            usernameVariable: 'GITHUB_USER',
                            passwordVariable: 'GITHUB_TOKEN'
                        )]) {
                            sh '''
                            git config user.name "jenkins"
                            git config user.email "jenkins@harusimgi.local"

                            git add manifests
                            git commit -m "deploy: update image tag ${IMAGE_TAG}" || echo "No manifest changes"

                            git push https://${GITHUB_USER}:${GITHUB_TOKEN}@github.com/dochiri0916/haru-simgi-deploy.git HEAD:${DEPLOY_BRANCH}
                            '''
                        }
                    }
                }
            }
        }
    }

    post {
        success {
            echo "CI 성공 -> ${env.CHANGED_SERVICES} 이미지 생성 및 배포 manifest 갱신 완료"
        }
        failure {
            echo "빌드 실패"
        }
    }
}
