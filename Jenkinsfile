pipeline {
    agent any

    parameters {
        choice(
            name: 'MODULE_NAME',
            choices: [
                'auth-service',
                'config-server',
                'eureka-server',
                'gateway',
                'task-service',
                'user-service'
            ],
            description: 'Docker image to build and push'
        )
        string(
            name: 'IMAGE_REPOSITORY',
            defaultValue: 'dochiri0916/haru-simgi',
            description: 'Docker Hub repository'
        )
    }

    environment {
        DOCKERHUB_CREDENTIALS = 'dockerhub-creds'
        IMAGE_TAG = "${env.BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Test') {
            steps {
                sh 'chmod +x ./gradlew'
                sh './gradlew test --no-daemon'
            }
        }

        stage('Build Image') {
            steps {
                script {
                    env.FULL_IMAGE_NAME = "${params.IMAGE_REPOSITORY}:${params.MODULE_NAME}-${env.IMAGE_TAG}"
                }
                sh '''
                    docker build \
                      -f Dockerfile.service \
                      --build-arg MODULE_NAME=${MODULE_NAME} \
                      -t ${FULL_IMAGE_NAME} \
                      .
                '''
            }
        }

        stage('Push Image') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: env.DOCKERHUB_CREDENTIALS,
                    usernameVariable: 'DOCKERHUB_USER',
                    passwordVariable: 'DOCKERHUB_TOKEN'
                )]) {
                    sh '''
                        echo "${DOCKERHUB_TOKEN}" | docker login -u "${DOCKERHUB_USER}" --password-stdin
                        docker push ${FULL_IMAGE_NAME}
                        docker logout
                    '''
                }
            }
        }
    }

    post {
        success {
            echo "Pushed image: ${env.FULL_IMAGE_NAME}"
        }
        always {
            sh 'docker image prune -f || true'
        }
    }
}
