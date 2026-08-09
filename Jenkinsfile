pipeline {

    agent any

    environment {

        // Application
        APP_NAME = 'securebank'
        CONTAINER_NAME = 'securebank'

        // Docker
        IMAGE_NAME = 'securebank'

        // Application port
        HOST_PORT = '8081'
        CONTAINER_PORT = '8080'

        // SonarQube server name configured in Jenkins
        SONARQUBE_SERVER = 'SonarQube'
    }

    stages {

        // ============================================================
        // 1. CHECKOUT
        // ============================================================

        stage('Checkout') {

            steps {

                echo '=========================================='
                echo '        CHECKOUT SOURCE CODE'
                echo '=========================================='

                checkout scm
            }
        }


        // ============================================================
        // 2. MAVEN BUILD
        // ============================================================

        stage('Maven Build') {

            steps {

                echo '=========================================='
                echo '             MAVEN BUILD'
                echo '=========================================='

                sh '''
                    mvn clean package
                '''
            }
        }


        // ============================================================
        // 3. UNIT TEST
        // ============================================================

        stage('Unit Test') {

            steps {

                echo '=========================================='
                echo '              UNIT TEST'
                echo '=========================================='

                sh '''
                    mvn test
                '''
            }

            post {

                always {

                    junit allowEmptyResults: true,
                          testResults: 'target/surefire-reports/*.xml'
                }
            }
        }


        // ============================================================
        // 4. VERIFY WAR FILE
        // ============================================================

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


        // ============================================================
        // 5. SONARQUBE ANALYSIS
        // ============================================================

        stage('SonarQube Analysis') {

            steps {

                echo '=========================================='
                echo '         SONARQUBE ANALYSIS'
                echo '=========================================='

                withSonarQubeEnv("${SONARQUBE_SERVER}") {

                    sh '''
                        mvn sonar:sonar \
                        -Dsonar.projectKey=securebank \
                        -Dsonar.projectName=SecureBank
                    '''
                }
            }
        }


        // ============================================================
        // 6. QUALITY GATE
        // ============================================================

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


        // ============================================================
        // 7. DOCKER BUILD
        // ============================================================

        stage('Docker Build') {

            steps {

                echo '=========================================='
                echo '          DOCKER IMAGE BUILD'
                echo '=========================================='

                sh '''
                    sudo docker build \
                        -t ${IMAGE_NAME}:${BUILD_NUMBER} .
                '''
            }
        }


        // ============================================================
        // 8. DOCKER IMAGE VERIFY
        // ============================================================

        stage('Docker Image Verify') {

            steps {

                echo '=========================================='
                echo '        VERIFY DOCKER IMAGE'
                echo '=========================================='

                sh '''
                    sudo docker images | grep ${IMAGE_NAME}
                '''
            }
        }


        // ============================================================
        // 9. STOP OLD CONTAINER
        // ============================================================

        stage('Stop Old Container') {

            steps {

                echo '=========================================='
                echo '         STOP OLD CONTAINER'
                echo '=========================================='

                sh '''
                    sudo docker rm -f ${CONTAINER_NAME} || true
                '''
            }
        }


        // ============================================================
        // 10. DEPLOY APPLICATION
        // ============================================================

        stage('Deploy Application') {

            steps {

                echo '=========================================='
                echo '        DEPLOY SECUREBANK'
                echo '=========================================='

                sh '''
                    sudo docker run -d \
                        --name ${CONTAINER_NAME} \
                        --restart unless-stopped \
                        -p ${HOST_PORT}:${CONTAINER_PORT} \
                        ${IMAGE_NAME}:${BUILD_NUMBER}
                '''
            }
        }


        // ============================================================
        // 11. APPLICATION STATUS
        // ============================================================

        stage('Application Status') {

            steps {

                echo '=========================================='
                echo '       APPLICATION STATUS'
                echo '=========================================='

                sh '''
                    sleep 15

                    sudo docker ps \
                        --filter "name=${CONTAINER_NAME}"
                '''
            }
        }


        // ============================================================
        // 12. APPLICATION HEALTH CHECK
        // ============================================================

        stage('Health Check') {

            steps {

                echo '=========================================='
                echo '         APPLICATION HEALTH CHECK'
                echo '=========================================='

                sh '''
                    sleep 5

                    curl -f \
                        --max-time 30 \
                        http://localhost:${HOST_PORT}/health
                '''
            }
        }


        // ============================================================
        // 13. DOCKER LOGS
        // ============================================================

        stage('Application Logs') {

            steps {

                echo '=========================================='
                echo '          APPLICATION LOGS'
                echo '=========================================='

                sh '''
                    sudo docker logs \
                        --tail 50 \
                        ${CONTAINER_NAME}
                '''
            }
        }
    }


    // ================================================================
    // POST ACTIONS
    // ================================================================

    post {

        success {

            echo '''
            ==================================================
                  SECUREBANK CI/CD SUCCESSFUL
            ==================================================

            GitHub       : Banking-3Tier-Warfile
            Application  : SecureBank
            SonarQube    : PASSED
            Docker       : SUCCESS
            Deployment   : SUCCESS
            Health Check : PASSED

            Application:
            http://<JENKINS-EXTERNAL-IP>:8081

            ==================================================
            '''
        }


        failure {

            echo '''
            ==================================================
                  SECUREBANK CI/CD FAILED
            ==================================================

            Please check the failed Jenkins stage.

            ==================================================
            '''
        }


        always {

            echo '=========================================='
            echo '       PIPELINE EXECUTION COMPLETED'
            echo '=========================================='
        }
    }
}
