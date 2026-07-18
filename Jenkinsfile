pipeline {
    agent any

    stages {

        stage('Clone') {
            steps {
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: '*/main']],
                    userRemoteConfigs: [[
                        url: 'git@github.com:Lilace121/cloudops-devops-platform.git'
                    ]],
                    extensions: [
                        [$class: 'CloneOption',
                         shallow: true,
                         depth: 1,
                         timeout: 10]
                    ]
                ])
            }
        }


        stage('Build') {
            steps {
                echo "开始Maven构建"

                sh '''
                cd app/cloudops-demo
                mvn clean package -DskipTests
                '''
            }
        }


        stage('Docker Build') {
            steps {
                echo "开始Docker镜像构建"

                sh '''
                docker build \
                -f docker/Dockerfile \
                -t cloudops-demo:v1 .
                '''
            }
        }


        stage('Deploy') {
            steps {
                echo "开始部署"

                sh '''
                docker stop cloudops-demo || true
                docker rm cloudops-demo || true

                docker run -d \
                --name cloudops-demo \
                -p 8081:8081 \
                cloudops-demo:v1
                '''
            }
        }
    }
}
