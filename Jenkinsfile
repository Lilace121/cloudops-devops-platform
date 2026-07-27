pipeline {

    agent any


    environment {

        IMAGE_NAME = "cloudops-demo"

        IMAGE_TAG = "v1.0.${BUILD_NUMBER}"

        ACR_REGISTRY = "crpi-38urml8fe00gm6pl.cn-shenzhen.personal.cr.aliyuncs.com"

        ACR_NAMESPACE = "cloudopsczq"

        KUBECONFIG = "/var/lib/jenkins/.kube/config"

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

            steps {


                echo "开始Kubernetes部署"



                sh '''

                echo "检查Kubernetes连接"



                kubectl --kubeconfig=/var/lib/jenkins/.kube/config get nodes



                echo "应用Deployment"



                kubectl --kubeconfig=/var/lib/jenkins/.kube/config apply \
                -f k8s/deployment.yaml



                echo "应用Service"



                kubectl --kubeconfig=/var/lib/jenkins/.kube/config apply \
                -f k8s/service.yaml



                echo "更新镜像"



                kubectl --kubeconfig=/var/lib/jenkins/.kube/config \
                set image deployment/cloudops-demo \
                cloudops-demo=${ACR_REGISTRY}/${ACR_NAMESPACE}/${IMAGE_NAME}:${IMAGE_TAG}



                echo "等待滚动发布完成"



                kubectl --kubeconfig=/var/lib/jenkins/.kube/config \
                rollout status deployment/cloudops-demo



                echo "Kubernetes部署完成"



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
