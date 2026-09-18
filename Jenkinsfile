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

        stage('Trivy Security Scan') {

        steps {

            sh '''
            set -e

            echo "===== Trivy Security Gate ====="
            echo "Scanning image: ${IMAGE_NAME}:${IMAGE_TAG}"

            PROXY_SCHEME=http
            PROXY_HOST=192.168.88.1:7897
            PROXY="${PROXY_SCHEME}://${PROXY_HOST}"

            HTTPS_PROXY="$PROXY" \
            HTTP_PROXY="$PROXY" \
            trivy image \
              --timeout 20m \
              --scanners vuln \
              --severity HIGH,CRITICAL \
              --ignore-unfixed \
              --exit-code 1 \
              ${IMAGE_NAME}:${IMAGE_TAG}

            echo "===== Trivy Security Gate PASSED ====="
            '''

        }

    }

    stage('Generate SBOM') {

        steps {

            sh '''
            set -e

            echo "===== Generate CycloneDX SBOM ====="
            echo "Image: ${IMAGE_NAME}:${IMAGE_TAG}"

            rm -rf sbom
            mkdir -p sbom

            trivy image \
              --cache-dir /var/lib/jenkins/.cache/trivy \
              --skip-db-update \
              --skip-java-db-update \
              --format cyclonedx \
              --output "sbom/${IMAGE_NAME}-${IMAGE_TAG}.cdx.json" \
              ${IMAGE_NAME}:${IMAGE_TAG}

            echo "===== SBOM Generated ====="
            ls -lh "sbom/${IMAGE_NAME}-${IMAGE_TAG}.cdx.json"
            '''

            archiveArtifacts(
                artifacts: 'sbom/*.cdx.json',
                fingerprint: true
            )

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

image = sys.argv[1]
path = Path("apps/cloudops/deployment.yaml")

lines = path.read_text().splitlines()
matches = 0

for index, line in enumerate(lines):
    stripped = line.lstrip()

    if stripped.startswith("image:") and "cloudops-demo" in stripped:
        indent = line[:len(line) - len(stripped)]
        lines[index] = f"{indent}image: {image}"
        matches += 1

if matches != 1:
    raise SystemExit(
        f"expected exactly one cloudops-demo image line, found {matches}"
    )

path.write_text(chr(10).join(lines) + chr(10))
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
