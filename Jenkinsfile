pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
        skipDefaultCheckout(true)
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 40, unit: 'MINUTES')
    }

    environment {
        K8S_NAMESPACE = 'smart-study'
        OPENAI_BASE_URL = 'https://studyplanneragent-resource.openai.azure.com/openai/v1'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm

                sh '''
                    set +x
                    echo "Source code checked out."
                    git rev-parse --short HEAD
                '''
            }
        }

        stage('Test Backend Services') {
            steps {
                withCredentials([
                    string(credentialsId: 'user-db-password',
                           variable: 'USER_DB_PASSWORD'),
                    string(credentialsId: 'study-db-password',
                           variable: 'STUDY_DB_PASSWORD'),
                    string(credentialsId: 'quiz-db-password',
                           variable: 'QUIZ_DB_PASSWORD'),
                    string(credentialsId: 'smart-study-jwt-secret',
                           variable: 'JWT_SECRET'),
                    string(credentialsId: 'azure-openai-key',
                           variable: 'AZURE_OPENAI_KEY')
                ]) {
                    sh '''
                        set +x
                        set -eu

                        cleanup_test_databases() {
                            docker rm -f \
                                smart-study-ci-user-db \
                                smart-study-ci-study-db \
                                smart-study-ci-quiz-db \
                                >/dev/null 2>&1 || true
                        }

                        cleanup_test_databases
                        trap cleanup_test_databases EXIT

                        echo "Starting temporary PostgreSQL databases..."

                        docker run -d \
                            --name smart-study-ci-user-db \
                            -p 15433:5432 \
                            -e POSTGRES_DB=user_db \
                            -e POSTGRES_USER=postgres \
                            -e POSTGRES_PASSWORD="$USER_DB_PASSWORD" \
                            postgres:17 >/dev/null

                        docker run -d \
                            --name smart-study-ci-study-db \
                            -p 15434:5432 \
                            -e POSTGRES_DB=study_db \
                            -e POSTGRES_USER=postgres \
                            -e POSTGRES_PASSWORD="$STUDY_DB_PASSWORD" \
                            postgres:17 >/dev/null

                        docker run -d \
                            --name smart-study-ci-quiz-db \
                            -p 15435:5432 \
                            -e POSTGRES_DB=quiz_db \
                            -e POSTGRES_USER=postgres \
                            -e POSTGRES_PASSWORD="$QUIZ_DB_PASSWORD" \
                            postgres:17 >/dev/null

                        wait_for_database() {
                            container="$1"
                            database="$2"

                            for attempt in $(seq 1 60); do
                                if docker exec "$container" \
                                    pg_isready -U postgres -d "$database" \
                                    >/dev/null 2>&1; then
                                    echo "$database is ready."
                                    return 0
                                fi

                                sleep 1
                            done

                            docker logs "$container"
                            echo "Database did not become ready."
                            return 1
                        }

                        wait_for_database smart-study-ci-user-db user_db
                        wait_for_database smart-study-ci-study-db study_db
                        wait_for_database smart-study-ci-quiz-db quiz_db

                        echo "Testing User Service..."
                        (
                            cd services/user-service

                            DB_URL="jdbc:postgresql://host.docker.internal:15433/user_db" \
                            DB_USERNAME=postgres \
                            DB_PASSWORD="$USER_DB_PASSWORD" \
                            JWT_SECRET="$JWT_SECRET" \
                            mvn -B -Dmaven.repo.local=/var/jenkins_home/.m2/repository clean test
                        )

                        echo "Testing Study Service..."
                        (
                            cd services/study-service

                            DB_URL="jdbc:postgresql://host.docker.internal:15434/study_db" \
                            DB_USERNAME=postgres \
                            DB_PASSWORD="$STUDY_DB_PASSWORD" \
                            mvn -B -Dmaven.repo.local=/var/jenkins_home/.m2/repository clean test
                        )

                        echo "Testing Quiz Service..."
                        (
                            cd services/quiz-service

                            DB_URL="jdbc:postgresql://host.docker.internal:15435/quiz_db" \
                            DB_USERNAME=postgres \
                            DB_PASSWORD="$QUIZ_DB_PASSWORD" \
                            AZURE_OPENAI_KEY="$AZURE_OPENAI_KEY" \
                            OPENAI_BASE_URL="$OPENAI_BASE_URL" \
                            mvn -B -Dmaven.repo.local=/var/jenkins_home/.m2/repository clean test
                        )

                        echo "Testing API Gateway..."
                        (
                            cd services/api-gateway

                            JWT_SECRET="$JWT_SECRET" \
                            mvn -B -Dmaven.repo.local=/var/jenkins_home/.m2/repository clean test
                        )

                        echo "All backend service tests passed."
                    '''
                }
            }
        }

        stage('Build and Push Docker Images') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'dockerhub-creds',
                        usernameVariable: 'DOCKERHUB_USER',
                        passwordVariable: 'DOCKERHUB_TOKEN'
                    )
                ]) {
                    sh '''
                        set +x
                        set -eu

                        export DOCKER_CONFIG="$(mktemp -d)"

                        cleanup_docker_credentials() {
                            docker logout >/dev/null 2>&1 || true
                            rm -rf "$DOCKER_CONFIG"
                        }

                        trap cleanup_docker_credentials EXIT

                        printf '%s' "$DOCKERHUB_TOKEN" |
                            docker login \
                                --username "$DOCKERHUB_USER" \
                                --password-stdin

                        build_and_push() {
                            repository="$1"
                            context="$2"
                            image="$DOCKERHUB_USER/$repository"

                            echo "Building $repository..."

                            docker build \
                                -t "$image:$BUILD_NUMBER" \
                                -t "$image:latest" \
                                "$context"

                            docker push "$image:$BUILD_NUMBER"
                            docker push "$image:latest"
                        }

                        build_and_push \
                            smart-study-user-service \
                            services/user-service

                        build_and_push \
                            smart-study-study-service \
                            services/study-service

                        build_and_push \
                            smart-study-quiz-service \
                            services/quiz-service

                        build_and_push \
                            smart-study-api-gateway \
                            services/api-gateway

                        build_and_push \
                            smart-study-frontend \
                            frontend

                        echo "All Docker images built and pushed."
                    '''
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                withCredentials([
                    file(credentialsId: 'docker-desktop-kubeconfig',
                         variable: 'KUBECONFIG_FILE'),

                    usernamePassword(
                        credentialsId: 'dockerhub-creds',
                        usernameVariable: 'DOCKERHUB_USER',
                        passwordVariable: 'DOCKERHUB_TOKEN'
                    ),

                    string(credentialsId: 'user-db-password',
                           variable: 'USER_DB_PASSWORD'),
                    string(credentialsId: 'study-db-password',
                           variable: 'STUDY_DB_PASSWORD'),
                    string(credentialsId: 'quiz-db-password',
                           variable: 'QUIZ_DB_PASSWORD'),
                    string(credentialsId: 'smart-study-jwt-secret',
                           variable: 'JWT_SECRET'),
                    string(credentialsId: 'azure-openai-key',
                           variable: 'AZURE_OPENAI_KEY')
                ]) {
                    sh '''
                        set +x
                        set -eu

                        export KUBECONFIG="$KUBECONFIG_FILE"

                        echo "Checking Kubernetes connection..."
                        kubectl get nodes

                        echo "Creating application namespace..."
                        kubectl apply -f kubernetes/namespace.yaml

                        echo "Applying application configuration..."
                        kubectl apply -f kubernetes/configmap.yaml

                        echo "Creating database and application Secrets..."

                        kubectl create secret generic user-postgres-secret \
                            --namespace "$K8S_NAMESPACE" \
                            --from-literal=password="$USER_DB_PASSWORD" \
                            --dry-run=client -o yaml |
                            kubectl apply -f -

                        kubectl create secret generic study-postgres-secret \
                            --namespace "$K8S_NAMESPACE" \
                            --from-literal=password="$STUDY_DB_PASSWORD" \
                            --dry-run=client -o yaml |
                            kubectl apply -f -

                        kubectl create secret generic quiz-postgres-secret \
                            --namespace "$K8S_NAMESPACE" \
                            --from-literal=password="$QUIZ_DB_PASSWORD" \
                            --dry-run=client -o yaml |
                            kubectl apply -f -

                        kubectl create secret generic smart-study-jwt-secret \
                            --namespace "$K8S_NAMESPACE" \
                            --from-literal=jwt-secret="$JWT_SECRET" \
                            --dry-run=client -o yaml |
                            kubectl apply -f -

                        kubectl create secret generic azure-openai-secret \
                            --namespace "$K8S_NAMESPACE" \
                            --from-literal=api-key="$AZURE_OPENAI_KEY" \
                            --dry-run=client -o yaml |
                            kubectl apply -f -

                        echo "Creating Docker Hub pull credentials..."

                        kubectl create secret docker-registry dockerhub-pull-secret \
                            --namespace "$K8S_NAMESPACE" \
                            --docker-server=https://index.docker.io/v1/ \
                            --docker-username="$DOCKERHUB_USER" \
                            --docker-password="$DOCKERHUB_TOKEN" \
                            --docker-email=jenkins@localhost \
                            --dry-run=client -o yaml |
                            kubectl apply -f -

                        kubectl patch serviceaccount default \
                            --namespace "$K8S_NAMESPACE" \
                            --type=merge \
                            -p '{"imagePullSecrets":[{"name":"dockerhub-pull-secret"}]}'

                        echo "Deploying PostgreSQL databases..."

                        kubectl apply -f kubernetes/user-postgres.yaml
                        kubectl apply -f kubernetes/study-postgres.yaml
                        kubectl apply -f kubernetes/quiz-postgres.yaml

                        kubectl rollout status deployment/user-postgres \
                            --namespace "$K8S_NAMESPACE" --timeout=240s

                        kubectl rollout status deployment/study-postgres \
                            --namespace "$K8S_NAMESPACE" --timeout=240s

                        kubectl rollout status deployment/quiz-postgres \
                            --namespace "$K8S_NAMESPACE" --timeout=240s

                        echo "Preparing versioned application manifests..."

                        TEMP_MANIFESTS="$(mktemp -d)"
                        trap 'rm -rf "$TEMP_MANIFESTS"' EXIT

                        sed "s|image: smart-study-user-service:latest|image: $DOCKERHUB_USER/smart-study-user-service:$BUILD_NUMBER|" \
                            kubernetes/user-service.yaml \
                            > "$TEMP_MANIFESTS/user-service.yaml"

                        sed "s|image: smart-study-study-service:latest|image: $DOCKERHUB_USER/smart-study-study-service:$BUILD_NUMBER|" \
                            kubernetes/study-service.yaml \
                            > "$TEMP_MANIFESTS/study-service.yaml"

                        sed "s|image: smart-study-quiz-service:k8s-v3|image: $DOCKERHUB_USER/smart-study-quiz-service:$BUILD_NUMBER|" \
                            kubernetes/quiz-service.yaml \
                            > "$TEMP_MANIFESTS/quiz-service.yaml"

                        sed "s|image: smart-study-api-gateway:k8s-v2|image: $DOCKERHUB_USER/smart-study-api-gateway:$BUILD_NUMBER|" \
                            kubernetes/api-gateway.yaml \
                            > "$TEMP_MANIFESTS/api-gateway.yaml"

                        sed "s|image: smart-study-frontend:latest|image: $DOCKERHUB_USER/smart-study-frontend:$BUILD_NUMBER|" \
                            kubernetes/frontend.yaml \
                            > "$TEMP_MANIFESTS/frontend.yaml"

                        echo "Deploying application services..."

                        kubectl apply -f "$TEMP_MANIFESTS/user-service.yaml"
                        kubectl apply -f "$TEMP_MANIFESTS/study-service.yaml"
                        kubectl apply -f "$TEMP_MANIFESTS/quiz-service.yaml"
                        kubectl apply -f "$TEMP_MANIFESTS/api-gateway.yaml"
                        kubectl apply -f "$TEMP_MANIFESTS/frontend.yaml"

                        echo "Waiting for application rollouts..."

                        kubectl rollout status deployment/user-service \
                            --namespace "$K8S_NAMESPACE" --timeout=300s

                        kubectl rollout status deployment/study-service \
                            --namespace "$K8S_NAMESPACE" --timeout=300s

                        kubectl rollout status deployment/quiz-service \
                            --namespace "$K8S_NAMESPACE" --timeout=300s

                        kubectl rollout status deployment/api-gateway \
                            --namespace "$K8S_NAMESPACE" --timeout=300s

                        kubectl rollout status deployment/frontend \
                            --namespace "$K8S_NAMESPACE" --timeout=180s

                        echo "Final Kubernetes deployment status:"
                        kubectl get deployments,services,pods \
                            --namespace "$K8S_NAMESPACE"
                    '''
                }
            }
        }

        stage('Deploy to AWS EC2') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'aws-jenkins-deploy',
                    usernameVariable: 'AWS_ACCESS_KEY_ID',
                    passwordVariable: 'AWS_SECRET_ACCESS_KEY'
                )]) {
                    withEnv(['AWS_DEFAULT_REGION=us-east-1', 'AWS_PAGER=']) {
                        sh '''
                            set -eu
                            set +x

                            INSTANCE_ID="i-0e980509fecf1cd16"
                            PARAM_FILE="$(mktemp)"
                            trap 'rm -f "$PARAM_FILE"' EXIT

                            printf '{"ImageTag":["%s"]}' "$BUILD_NUMBER" > "$PARAM_FILE"

                            COMMAND_ID="$(aws ssm send-command \
                                --document-name SmartStudyDeploy \
                                --instance-ids "$INSTANCE_ID" \
                                --parameters "file://$PARAM_FILE" \
                                --query 'Command.CommandId' \
                                --output text)"

                            echo "AWS deployment command submitted."

                            for attempt in $(seq 1 60); do
                                STATUS="$(aws ssm get-command-invocation \
                                    --command-id "$COMMAND_ID" \
                                    --instance-id "$INSTANCE_ID" \
                                    --query 'Status' \
                                    --output text 2>/dev/null || true)"

                                case "$STATUS" in
                                    Success)
                                        echo "AWS deployment completed successfully."
                                        aws ssm get-command-invocation \
                                            --command-id "$COMMAND_ID" \
                                            --instance-id "$INSTANCE_ID" \
                                            --query 'StandardOutputContent' \
                                            --output text
                                        exit 0
                                        ;;
                                    Failed|Cancelled|TimedOut|Cancelling)
                                        echo "AWS deployment failed with status: $STATUS"
                                        aws ssm get-command-invocation \
                                            --command-id "$COMMAND_ID" \
                                            --instance-id "$INSTANCE_ID" \
                                            --query 'StandardErrorContent' \
                                            --output text || true
                                        exit 1
                                        ;;
                                    *)
                                        sleep 5
                                        ;;
                                esac
                            done

                            echo "Timed out waiting for AWS deployment."
                            exit 1
                        '''
                    }
                }
            }
        }
    }

    post {
        always {
            sh '''
                set +x
                docker rm -f \
                    smart-study-ci-user-db \
                    smart-study-ci-study-db \
                    smart-study-ci-quiz-db \
                    >/dev/null 2>&1 || true
            '''
        }

        success {
            echo 'CI/CD pipeline completed successfully.'
        }

        failure {
            echo 'Pipeline failed. Check the stage logs to identify the cause.'
        }
    }
}