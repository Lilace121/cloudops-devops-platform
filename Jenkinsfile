pipeline {

    agent any

    environment {

        IMAGE_NAME = "cloudops-demo"

        IMAGE_TAG = "${BUILD_NUMBER}"

        ACR_REGISTRY = "crpi-38urml8fe00gm6pl.cn-shenzhen.personal.cr.aliyuncs.com"

        ACR_NAMESPACE = "cloudopsczq"

        IMAGE_FULL_NAME = "${ACR_REGISTRY}/${ACR_NAMESPACE}/${IMAGE_NAME}:${IMAGE_TAG}"

        GITOPS_REPO = "git@github.com:Lilace121/cloudops-gitops.git"

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

                withCredentials([usernamePassword(
                    credentialsId: 'aliyun-acr',
                    usernameVariable: 'ACR_USER',
                    passwordVariable: 'ACR_PASS'
                )]) {

                    sh '''

                    echo "$ACR_PASS" | docker login \
                      --username "$ACR_USER" \
                      --password-stdin ${ACR_REGISTRY}

                    docker tag \
                      ${IMAGE_NAME}:${IMAGE_TAG} \
                      ${IMAGE_FULL_NAME}

                    docker push ${IMAGE_FULL_NAME}

                    '''

                }

            }

        }

        stage('Update GitOps Repository') {

            steps {

                echo "更新 GitOps 仓库中的镜像版本"

                sh '''
                set -e

                GITOPS_DIR=$(mktemp -d)

                cleanup() {
                    rm -rf "$GITOPS_DIR"
                }

                trap cleanup EXIT

                export GIT_SSH_COMMAND="ssh -o BatchMode=yes -o StrictHostKeyChecking=accept-new"

                git clone ${GITOPS_REPO} "$GITOPS_DIR"

                cd "$GITOPS_DIR"

                git config user.name "Jenkins"
                git config user.email "jenkins@cloudops.local"

                python3 - "${IMAGE_FULL_NAME}" <<'PYUPDATE'
from pathlib import Path
import sys
import re

image = sys.argv[1]
path = Path("apps/cloudops/deployment.yaml")
text = path.read_text()

new_text, count = re.subn(
    r'^(\s*image:\s*).*$',
    lambda m: m.group(1) + image,
    text,
    count=1,
    flags=re.MULTILINE
)

if count != 1:
    raise SystemExit(f"expected to update exactly one image line, updated {count}")

path.write_text(new_text)
PYUPDATE

                echo "===== 新镜像 ====="
                grep -n "image:" apps/cloudops/deployment.yaml

                echo "===== GitOps Diff ====="
                git diff -- apps/cloudops/deployment.yaml

                git add apps/cloudops/deployment.yaml

                if git diff --cached --quiet; then
                    echo "GitOps 配置没有变化，无需提交"
                else
                    git commit -m "deploy cloudops-demo:${IMAGE_TAG}"
                    git push origin main
                fi
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
