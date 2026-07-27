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
                -t ${IMAGE_NAME}:${IMAGE_TAG} .

                '''

            }

        }



        stage('Docker Push') {

            steps {


                echo "推送镜像到阿里云ACR"


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

                export KUBECONFIG=${KUBECONFIG}


                echo "检查Kubernetes集群状态"

                kubectl get nodes



                echo "应用Kubernetes资源"


                kubectl apply -f k8s/deployment.yaml

                kubectl apply -f k8s/service.yaml




                echo "更新镜像版本"



                kubectl set image deployment/cloudops-demo \
                cloudops-demo=${ACR_REGISTRY}/${ACR_NAMESPACE}/${IMAGE_NAME}:${IMAGE_TAG}




                echo "等待滚动发布完成"



                kubectl rollout status deployment/cloudops-demo




                echo "查看Pod状态"


                kubectl get pods -o wide



                echo "查看Service"


                kubectl get svc


                '''

            }

        }


    }



    post {


        success {

            echo "SUCCESS: CI/CD部署完成"

        }


        failure {

            echo "FAILED: CI/CD部署失败"

        }


    }


}
