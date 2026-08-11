pipeline {

    agent any

    environment {

        // ========================================================
        // APPLICATION
        // ========================================================

        APP_NAME = 'securebank'

        // ========================================================
        // DOCKER
        // Replace with your actual Docker Hub username
        // ========================================================

        DOCKER_IMAGE = 'devopsbyrushi/securebank'

        // Jenkins credential ID for Docker Hub
        DOCKER_CREDENTIALS = 'dockerhub-credentials'

        // ========================================================
        // SONARQUBE
        // Must match the SonarQube server name configured in Jenkins
        // ========================================================

        SONARQUBE_SERVER = 'SonarQube'

        SONAR_PROJECT_KEY = 'securebank'
        SONAR_PROJECT_NAME = 'SecureBank'
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
        // 5. SONARQUBE QUALITY GATE
        // ========================================================

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


        // ========================================================
        // 6. DOCKER BUILD
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
        // 7. DOCKER IMAGE VERIFY
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
        // 8. DOCKER HUB LOGIN
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
        // 9. PUSH TO DOCKER HUB
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
            SonarQube    : QUALITY GATE PASSED
            Docker       : IMAGE BUILD SUCCESS
            Docker Hub   : IMAGE PUSH SUCCESS

            Docker Image:
            ${DOCKER_IMAGE}:${BUILD_NUMBER}

            ==================================================
            '''
        }


        failure {

            echo '''
            ==================================================
                  SECUREBANK CI/CD FAILED
            ==================================================

            Check the failed Jenkins stage and console output.

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
