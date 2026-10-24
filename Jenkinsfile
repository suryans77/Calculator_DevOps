pipeline {
    agent any

    environment {
        DOCKERHUB_USER = 'suryans77'
        IMAGE_NAME = "${DOCKERHUB_USER}/calculator"
        IMAGE_TAG = "${BUILD_NUMBER}"
    }

    options {
        disableConcurrentBuilds()
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    stages {
        // Fetch the revision that triggered this build
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        // Compile and package the JAR; tests run in their own stage
        stage('Build') {
            steps {
                sh 'mvn -B clean package -DskipTests'
            }
        }

        // Run unit and MockMvc tests and publish the results, even on failure
        stage('Test') {
            steps {
                sh 'mvn -B test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        // Build the image with the build-number tag and also tag it as latest
        stage('Docker Build') {
            steps {
                sh 'docker build -t "$IMAGE_NAME:$IMAGE_TAG" -t "$IMAGE_NAME:latest" .'
            }
        }

        // Push both tags to Docker Hub using the Jenkins-stored credentials
        stage('Push') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-creds',
                        usernameVariable: 'DOCKERHUB_USERNAME', passwordVariable: 'DOCKERHUB_PASSWORD')]) {
                    // Single-quoted so the shell, not Groovy, expands the secrets
                    sh '''
                        echo "$DOCKERHUB_PASSWORD" | docker login -u "$DOCKERHUB_USERNAME" --password-stdin
                        docker push "$IMAGE_NAME:$IMAGE_TAG"
                        docker push "$IMAGE_NAME:latest"
                        docker logout
                    '''
                }
            }
        }

        // Start the compose stack on this build's image, then wait for it to be healthy
        stage('Deploy') {
            steps {
                sh 'IMAGE_TAG=${IMAGE_TAG} docker compose up -d --no-build --remove-orphans'
                sh '''
                    deadline=$(( $(date +%s) + 120 ))
                    while true; do
                        # -a includes exited containers so a crashed one counts as unhealthy
                        ids=$(docker compose ps -a -q)
                        total=$(echo "$ids" | wc -w)
                        healthy=0
                        for id in $ids; do
                            if [ "$(docker inspect -f '{{.State.Health.Status}}' "$id" 2>/dev/null)" = "healthy" ]; then
                                healthy=$((healthy + 1))
                            fi
                        done
                        echo "Healthy containers: $healthy/$total"

                        if [ "$total" -gt 0 ] && [ "$healthy" -eq "$total" ]; then
                            break
                        fi
                        if [ "$(date +%s)" -ge "$deadline" ]; then
                            echo "Stack did not become healthy within 120s"
                            docker compose ps -a
                            docker compose logs --no-color --tail=200
                            exit 1
                        fi
                        sleep 5
                    done

                    # End-to-end check through nginx
                    curl -fsS http://localhost/actuator/health
                '''
            }
        }
    }

    post {
        success {
            emailext(
                to: 'suryansdash005@gmail.com',
                subject: "SUCCESS: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: """Job: ${env.JOB_NAME}
Build: #${env.BUILD_NUMBER}
Status: ${currentBuild.currentResult}
URL: ${env.BUILD_URL}"""
            )
        }
        failure {
            emailext(
                to: 'suryansdash005@gmail.com',
                subject: "FAILURE: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: """Job: ${env.JOB_NAME}
Build: #${env.BUILD_NUMBER}
Status: ${currentBuild.currentResult}
URL: ${env.BUILD_URL}

Console: ${env.BUILD_URL}console"""
            )
        }
    }
}
