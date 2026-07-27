pipeline {

    agent any


    environment {

        IMAGE_NAME = "cloudops-demo"

        IMAGE_TAG = "v1.0.${BUILD_NUMBER}"

        ACR_REGISTRY = "crpi-38urml8fe00gm6pl.cn-shenzhen.personal.cr.aliyuncs.com"

        ACR_NAMESPACE = "cloudopsczq"

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
                    ${ACR_REGISTRY}/${ACR_NAMESPACE}/${IMAGE_NAME}:${IMAGE_TAG}



                    docker push \
                    ${ACR_REGISTRY}/${ACR_NAMESPACE}/${IMAGE_NAME}:${IMAGE_TAG}


                    '''

                }

            }

        }



        stage('Deploy Kubernetes') {

                     environment {
                     KUBECONFIG = "/var/lib/jenkins/.kube/config"


            steps {


                echo "开始Kubernetes部署"


                sh '''


                kubectl apply -f k8s/deployment.yaml


                kubectl apply -f k8s/service.yaml



                kubectl set image deployment/cloudops-demo \
                cloudops-demo=${ACR_REGISTRY}/${ACR_NAMESPACE}/${IMAGE_NAME}:${IMAGE_TAG}



                kubectl rollout status deployment/cloudops-demo



                '''

            }

        }


    }



    post {


        success {

            echo "SUCCESS"

        }


        failure {

            echo "FAILED"

        }


    }


}
