pipeline {

    agent any

    environment {

        // ========================================================
        // APPLICATION
        // ========================================================

        APP_NAME = 'securebank'

        // ========================================================
        // DOCKER HUB
        // ========================================================

        DOCKER_IMAGE = 'devopsbyrushi/securebank'

        // Jenkins Docker Hub credential ID
        DOCKER_CREDENTIALS = 'dockerhub-credentials'

        // ========================================================
        // SONARQUBE
        // ========================================================

        SONARQUBE_SERVER = 'SonarQube'

        SONAR_PROJECT_KEY = 'securebank'
        SONAR_PROJECT_NAME = 'SecureBank'

        // ========================================================
        // KUBERNETES
        // ========================================================

        K8S_NAMESPACE = 'devops-demo'
        K8S_DEPLOYMENT = 'securebank'
    }


    stages {

        // ========================================================
        // 1. CHECKOUT
        // ========================================================

        stage('Checkout') {

            steps {

                echo '=========================================='
                echo '        CHECKOUT SOURCE CODE'
                echo '=========================================='

                checkout scm
            }
        }


        // ========================================================
        // 2. MAVEN BUILD + TEST
        // ========================================================

        stage('Maven Build and Test') {

            steps {

                echo '=========================================='
                echo '       MAVEN BUILD AND UNIT TEST'
                echo '=========================================='

                sh '''
                    mvn clean package
                '''
            }

            post {

                always {

                    junit allowEmptyResults: true,
                          testResults: 'target/surefire-reports/*.xml'
                }
            }
        }


        // ========================================================
        // 3. VERIFY WAR
        // ========================================================

        stage('Verify WAR') {

            steps {

                echo '=========================================='
                echo '            VERIFY WAR FILE'
                echo '=========================================='

                sh '''
                    ls -lh target/
                    test -f target/securebank.war
                '''
            }
        }


        // ========================================================
        // 4. SONARQUBE ANALYSIS
        // ========================================================

        stage('SonarQube Analysis') {

            steps {

                echo '=========================================='
                echo '         SONARQUBE ANALYSIS'
                echo '=========================================='

                withSonarQubeEnv("${SONARQUBE_SERVER}") {

                    sh '''
                        mvn org.sonarsource.scanner.maven:sonar-maven-plugin:5.1.0.4751:sonar \
                        -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                        -Dsonar.projectName=${SONAR_PROJECT_NAME}
                    '''
                }
            }
        }


        // ========================================================
        // 5. DOCKER BUILD
        // ========================================================

        stage('Docker Build') {

            steps {

                echo '=========================================='
                echo '          DOCKER IMAGE BUILD'
                echo '=========================================='

                sh '''
                    docker build \
                    -t ${DOCKER_IMAGE}:${BUILD_NUMBER} \
                    -t ${DOCKER_IMAGE}:latest .
                '''
            }
        }


        // ========================================================
        // 6. DOCKER IMAGE VERIFY
        // ========================================================

        stage('Docker Image Verify') {

            steps {

                echo '=========================================='
                echo '        VERIFY DOCKER IMAGE'
                echo '=========================================='

                sh '''
                    docker images | grep ${DOCKER_IMAGE}
                '''
            }
        }


        // ========================================================
        // 7. DOCKER HUB LOGIN
        // ========================================================

        stage('Docker Hub Login') {

            steps {

                echo '=========================================='
                echo '          DOCKER HUB LOGIN'
                echo '=========================================='

                withCredentials([
                    usernamePassword(
                        credentialsId: "${DOCKER_CREDENTIALS}",
                        usernameVariable: 'DOCKER_USERNAME',
                        passwordVariable: 'DOCKER_PASSWORD'
                    )
                ]) {

                    sh '''
                        echo "$DOCKER_PASSWORD" | docker login \
                        -u "$DOCKER_USERNAME" \
                        --password-stdin
                    '''
                }
            }
        }


        // ========================================================
        // 8. PUSH TO DOCKER HUB
        // ========================================================

        stage('Docker Push') {

            steps {

                echo '=========================================='
                echo '          PUSH TO DOCKER HUB'
                echo '=========================================='

                sh '''
                    docker push ${DOCKER_IMAGE}:${BUILD_NUMBER}
                    docker push ${DOCKER_IMAGE}:latest
                '''
            }
        }


        // ========================================================
        // 9. KUBERNETES DEPLOY
        // ========================================================

        stage('Kubernetes Deploy') {

            steps {

                echo '=========================================='
                echo '       DEPLOY TO KUBERNETES'
                echo '=========================================='

                sh '''
                    echo "Using Docker Image:"
                    echo "${DOCKER_IMAGE}:${BUILD_NUMBER}"

                    echo "Updating Kubernetes image tag..."

                    sed -i "s|IMAGE_TAG|${BUILD_NUMBER}|g" k8s/deployment.yml

                    echo "Applying Deployment..."

                    kubectl apply \
                    -f k8s/deployment.yml \
                    -n ${K8S_NAMESPACE}

                    echo "Applying Service..."

                    kubectl apply \
                    -f k8s/service.yml \
                    -n ${K8S_NAMESPACE}
                '''
            }
        }


        // ========================================================
        // 10. VERIFY KUBERNETES
        // ========================================================

        stage('Verify Kubernetes') {

            steps {

                echo '=========================================='
                echo '       VERIFY KUBERNETES PODS'
                echo '=========================================='

                sh '''
                    echo "Waiting for SecureBank pods..."

                    kubectl wait \
                    --for=condition=Ready \
                    pod \
                    -l app=securebank \
                    -n ${K8S_NAMESPACE} \
                    --timeout=5m

                    echo "=========================================="
                    echo "       KUBERNETES PODS"
                    echo "=========================================="

                    kubectl get pods \
                    -n ${K8S_NAMESPACE} \
                    -l app=securebank \
                    -o wide

                    echo "=========================================="
                    echo "       KUBERNETES SERVICE"
                    echo "=========================================="

                    kubectl get svc \
                    ${K8S_DEPLOYMENT} \
                    -n ${K8S_NAMESPACE}
                '''
            }
        }
     }


    // ============================================================
    // POST ACTIONS
    // ============================================================

    post {

        success {

            echo '''
==================================================
          SECUREBANK CI/CD SUCCESSFUL
==================================================

GitHub       : CHECKOUT SUCCESS
Maven        : BUILD + TEST SUCCESS
SonarQube    : ANALYSIS SUCCESS
Docker       : IMAGE BUILD SUCCESS
Docker Hub   : IMAGE PUSH SUCCESS
Kubernetes   : DEPLOYMENT SUCCESS

Docker Image:
devopsbyrushi/securebank:${BUILD_NUMBER}

Kubernetes:
Namespace  : devops-demo
Deployment : securebank

==================================================
'''
        }


        failure {

            echo '''
==================================================
            SECUREBANK CI/CD FAILED
==================================================

Check the failed Jenkins stage
and console output.

==================================================
'''
        }


        always {

            echo '=========================================='
            echo '       PIPELINE EXECUTION COMPLETED'
            echo '=========================================='

            sh 'docker logout || true'
        }
    }
}
