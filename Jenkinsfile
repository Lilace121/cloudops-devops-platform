pipeline {

    agent any


    environment {

        IMAGE_NAME="cloudops-demo:v1"

        CONTAINER_NAME="cloudops-demo"

    }


    stages {


        stage('Clone') {

            steps {

                echo "拉取代码"

            }

        }



        stage('Build') {

            steps {

                echo "Maven构建"

                sh '''
                cd app/cloudops-demo
                mvn clean package
                '''

            }

        }



        stage('Docker Build') {

            steps {

                echo "构建Docker镜像"

                sh '''
                docker build \
                -f docker/Dockerfile \
                -t $IMAGE_NAME .
                '''

            }

        }



        stage('Deploy') {

            steps {

                echo "部署应用"


                sh '''

                docker stop $CONTAINER_NAME || true

                docker rm $CONTAINER_NAME || true


                docker run -d \
                -p 8081:8081 \
                --name $CONTAINER_NAME \
                $IMAGE_NAME

                '''

            }

        }


    }


}
