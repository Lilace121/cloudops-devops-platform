pipeline {

    agent any


    environment {

        IMAGE_NAME = "cloudops-demo"

        IMAGE_TAG = "v1"

        ACR_REGISTRY = "crpi-38urml8fe00gm6pl.cn-shenzhen.personal.cr.aliyuncs.com"

        ACR_IMAGE = "crpi-38urml8fe00gm6pl.cn-shenzhen.personal.cr.aliyuncs.com/cloudopsczq/cloudops-demo:v1"

    }


    stages {


        stage('Build') {

            steps {

                sh '''
                cd app/cloudops-demo

                mvn clean package -DskipTests
                '''

            }

        }



        stage('Docker Build') {

            steps {

                sh '''

                docker build \
                -f docker/Dockerfile \
                -t ${IMAGE_NAME}:${IMAGE_TAG} .

                '''

            }

        }



        stage('Docker Push') {

            steps {


                withCredentials([

                    usernamePassword(

                        credentialsId: 'aliyun-acr',

                        usernameVariable: 'ACR_USER',

                        passwordVariable: 'ACR_PASS'

                    )

                ]) {


                    sh '''

                    echo $ACR_PASS | docker login \
                    --username $ACR_USER \
                    --password-stdin \
                    ${ACR_REGISTRY}



                    docker tag \
                    ${IMAGE_NAME}:${IMAGE_TAG} \
                    ${ACR_IMAGE}



                    docker push ${ACR_IMAGE}

                    '''

                }

            }

        }



        stage('Deploy Kubernetes') {

    steps {

        sh '''

        kubectl apply -f k8s/deployment.yaml

        kubectl apply -f k8s/service.yaml


        kubectl rollout restart deployment cloudops-demo


        '''

    }

}

    }


    post {

        success {

            echo 'SUCCESS'

        }


        failure {

            echo 'FAILED'

        }

    }

}
