pipeline {

    agent any

    environment {

        // =====================================================
        // APPLICATION
        // =====================================================

        APP_NAME = 'securebank'

        // =====================================================
        // DOCKER HUB
        // =====================================================

        DOCKER_IMAGE = 'devopsbyrushi/securebank'

        // Jenkins Docker Hub credential ID
        DOCKER_CREDENTIALS = 'dockerhub-credentials'

        // =====================================================
        // SONARQUBE
        // =====================================================

        SONARQUBE_SERVER = 'SonarQube'

        SONAR_PROJECT_KEY = 'securebank'

        SONAR_PROJECT_NAME = 'SecureBank'

        // =====================================================
        // KUBERNETES / GKE
        // =====================================================

        K8S_NAMESPACE = 'devops-demo'

        K8S_DEPLOYMENT = 'securebank'

        K8S_CONTAINER = 'securebank'

        K8S_DEPLOYMENT_FILE = 'k8s/deployment.yml'

        K8S_SERVICE_FILE = 'k8s/service.yml'

        // Jenkins user's kubeconfig
        KUBECONFIG = '/var/lib/jenkins/.kube/config'
    }


    stages {

        // =====================================================
        // 1. CHECKOUT
        // =====================================================

        stage('Checkout') {

            steps {

                echo '=========================================='
                echo '        CHECKOUT SOURCE CODE'
                echo '=========================================='

                checkout scm
            }
        }


        // =====================================================
        // 2. MAVEN BUILD + TEST
        // =====================================================

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


        // =====================================================
        // 3. VERIFY WAR
        // =====================================================

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


        // =====================================================
        // 4. SONARQUBE ANALYSIS
        // =====================================================

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


        // =====================================================
        // 5. SONARQUBE QUALITY GATE
        // =====================================================

        stage('SonarQube Quality Gate') {

            steps {

                echo '=========================================='
                echo '       SONARQUBE QUALITY GATE'
                echo '=========================================='

                timeout(time: 5, unit: 'MINUTES') {

                    waitForQualityGate abortPipeline: true
                }
            }
        }


        // =====================================================
        // 6. DOCKER BUILD
        // =====================================================

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


        // =====================================================
        // 7. DOCKER IMAGE VERIFY
        // =====================================================

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


        // =====================================================
        // 8. DOCKER HUB LOGIN
        // =====================================================

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


        // =====================================================
        // 9. PUSH IMAGE TO DOCKER HUB
        // =====================================================

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


        // =====================================================
        // 10. KUBERNETES DEPLOYMENT
        // =====================================================

        stage('Deploy to Kubernetes') {

            steps {

                echo '=========================================='
                echo '        DEPLOY TO GKE KUBERNETES'
                echo '=========================================='

                sh '''
                    echo "Checking Kubernetes connection..."

                    kubectl cluster-info

                    echo "Checking namespace..."

                    kubectl get namespace ${K8S_NAMESPACE}

                    echo "Applying Kubernetes Deployment..."

                    kubectl apply \
                    -f ${K8S_DEPLOYMENT_FILE} \
                    -n ${K8S_NAMESPACE}

                    echo "Applying Kubernetes Service..."

                    kubectl apply \
                    -f ${K8S_SERVICE_FILE} \
                    -n ${K8S_NAMESPACE}
                '''
            }
        }


        // =====================================================
        // 11. UPDATE IMAGE
        // =====================================================

        stage('Update Kubernetes Image') {

            steps {

                echo '=========================================='
                echo '       UPDATE KUBERNETES IMAGE'
                echo '=========================================='

                sh '''
                    kubectl set image deployment/${K8S_DEPLOYMENT} \
                    ${K8S_CONTAINER}=${DOCKER_IMAGE}:${BUILD_NUMBER} \
                    -n ${K8S_NAMESPACE}
                '''
            }
        }


        // =====================================================
        // 12. ROLLOUT STATUS
        // =====================================================

        stage('Kubernetes Rollout') {

            steps {

                echo '=========================================='
                echo '        KUBERNETES ROLLOUT STATUS'
                echo '=========================================='

                sh '''
                    kubectl rollout status \
                    deployment/${K8S_DEPLOYMENT} \
                    -n ${K8S_NAMESPACE} \
                    --timeout=5m
                '''
            }
        }


        // =====================================================
        // 13. KUBERNETES HEALTH CHECK
        // =====================================================

        stage('Kubernetes Health Check') {

            steps {

                echo '=========================================='
                echo '       KUBERNETES HEALTH CHECK'
                echo '=========================================='

                sh '''
                    echo "=========================================="
                    echo "DEPLOYMENTS"
                    echo "=========================================="

                    kubectl get deployments \
                    -n ${K8S_NAMESPACE}

                    echo "=========================================="
                    echo "PODS"
                    echo "=========================================="

                    kubectl get pods \
                    -n ${K8S_NAMESPACE} \
                    -o wide

                    echo "=========================================="
                    echo "SERVICES"
                    echo "=========================================="

                    kubectl get services \
                    -n ${K8S_NAMESPACE}
                '''
            }
        }
    }


    // =========================================================
    // POST ACTIONS
    // =========================================================

    post {

        success {

            echo '''
            ==================================================
                 SECUREBANK CI/CD SUCCESSFUL
            ==================================================

            GitHub       : CHECKOUT SUCCESS
            Maven        : BUILD + TEST SUCCESS
            SonarQube    : QUALITY GATE PASSED
            Docker       : IMAGE BUILD SUCCESS
            Docker Hub   : IMAGE PUSH SUCCESS
            Kubernetes   : DEPLOYMENT SUCCESS
            GKE          : ROLLOUT SUCCESS

            Docker Image:
            ${DOCKER_IMAGE}:${BUILD_NUMBER}

            Namespace:
            ${K8S_NAMESPACE}

            ==================================================
            '''
        }


        failure {

            echo '''
            ==================================================
                 SECUREBANK CI/CD FAILED
            ==================================================

            Check the failed Jenkins stage
            and Jenkins Console Output.

            ==================================================
            '''
        }


        always {

            echo '=========================================='
            echo '       PIPELINE EXECUTION COMPLETED'
            echo '=========================================='

            sh '''
                docker logout || true
            '''
        }
    }
}
