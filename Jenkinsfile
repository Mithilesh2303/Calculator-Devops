pipeline {
    agent any

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
    }

    triggers {
        // Automatic trigger: GitHub push webhook or Poll SCM fallback
        githubPush()
        pollSCM('H/5 * * * *')
    }

    environment {
        // Ensure Homebrew and standard binaries (mvn, docker) are accessible in Jenkins agent environment
        PATH = "/opt/homebrew/bin:/usr/local/bin:${env.PATH}"
        JAVA_HOME = '/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home'

        // Docker Hub repository: <username>/<repository>
        DOCKER_IMAGE = 'mithileshsai2303/calculator-api'
        DOCKER_CRED_ID = 'dockerhub-credentials'
        
        // Notification email recipient
        RECIPIENT_EMAIL = 'Mithilesh.Sai@iiitb.ac.in'
        
        // Tag with Jenkins build number for traceability
        IMAGE_TAG = "${env.BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                echo '=== STAGE 1: CHECKOUT ==='
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo '=== STAGE 2: BUILD ==='
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Test') {
            steps {
                echo '=== STAGE 3: TEST ==='
                sh 'mvn test'
            }
            post {
                always {
                    // Publish JUnit XML test results for Jenkins reporting
                    junit testResults: '**/target/surefire-reports/*.xml', allowEmptyResults: false
                }
            }
        }

        stage('Docker Build') {
            steps {
                echo '=== STAGE 4: DOCKER BUILD ==='
                sh """
                    docker build -t ${DOCKER_IMAGE}:${IMAGE_TAG} -t ${DOCKER_IMAGE}:latest .
                """
            }
        }

        stage('Push') {
            steps {
                echo '=== STAGE 5: DOCKER PUSH ==='
                withCredentials([usernamePassword(
                    credentialsId: "${DOCKER_CRED_ID}",
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh """
                        echo "\$DOCKER_PASS" | docker login -u "\$DOCKER_USER" --password-stdin
                        docker push ${DOCKER_IMAGE}:${IMAGE_TAG}
                        docker push ${DOCKER_IMAGE}:latest
                        docker logout
                    """
                }
            }
        }

        stage('Deploy') {
            steps {
                echo '=== STAGE 6: DEPLOY (DOCKER COMPOSE) ==='
                sh """
                    export DOCKER_IMAGE=${DOCKER_IMAGE}
                    export IMAGE_TAG=${IMAGE_TAG}
                    docker compose down --remove-orphans || true
                    docker compose up -d --build
                """
                echo 'Verifying stack health...'
                script {
                    timeout(time: 2, unit: 'MINUTES') {
                        sh '''
                            echo "Waiting for services to become healthy..."
                            until [ "$(curl -s -o /dev/null -w '%{http_code}' http://localhost:80/actuator/health)" = "200" ]; do
                                echo "Waiting for http://localhost:80/actuator/health to return 200..."
                                sleep 3
                            done
                            echo "Stack successfully deployed and verified healthy!"
                        '''
                    }
                }
            }
        }
    }

    post {
        success {
            echo "Build #${env.BUILD_NUMBER} SUCCEEDED!"
            script {
                try {
                    emailext (
                        to: "${RECIPIENT_EMAIL}",
                        subject: "SUCCESS: Jenkins Build #${env.BUILD_NUMBER} - ${env.JOB_NAME}",
                        body: """
                            <p>Hello,</p>
                            <p>Jenkins Build <b>#${env.BUILD_NUMBER}</b> for <b>${env.JOB_NAME}</b> completed successfully!</p>
                            <p>All tests passed, Docker image <code>${DOCKER_IMAGE}:${IMAGE_TAG}</code> was pushed to Docker Hub, and stack was deployed cleanly.</p>
                            <p>View Console Output: <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>
                        """,
                        mimeType: 'text/html'
                    )
                } catch (Exception e) {
                    echo "Email alert notice: ${e.message} (Configure SMTP in Jenkins to receive live emails)"
                }
            }
        }
        failure {
            echo "Build #${env.BUILD_NUMBER} FAILED!"
            script {
                try {
                    emailext (
                        to: "${RECIPIENT_EMAIL}",
                        subject: "FAILURE: Jenkins Build #${env.BUILD_NUMBER} - ${env.JOB_NAME}",
                        body: """
                            <p>Attention,</p>
                            <p>Jenkins Build <b>#${env.BUILD_NUMBER}</b> for <b>${env.JOB_NAME}</b> failed.</p>
                            <p>Please inspect the build logs to diagnose the issue.</p>
                            <p>View Console Output: <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>
                        """,
                        mimeType: 'text/html'
                    )
                } catch (Exception e) {
                    echo "Email alert notice: ${e.message} (Configure SMTP in Jenkins to receive live emails)"
                }
            }
        }
        always {
            cleanWs()
        }
    }
}
