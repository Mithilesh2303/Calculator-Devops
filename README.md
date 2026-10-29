# Calculator REST API & End-to-End DevOps CI/CD Pipeline

[![Java 17](https://img.shields.io/badge/Java-17-orange.svg)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.9+-blue.svg)](https://maven.apache.org/)
[![Docker](https://img.shields.io/badge/Docker-Multi--stage-2496ED.svg)](https://www.docker.com/)
[![Jenkins](https://img.shields.io/badge/Jenkins-Declarative%20Pipeline-D24939.svg)](https://www.jenkins.io/)

Production-grade Calculator REST API built with **Spring Boot 3** and orchestrated via an automated DevOps pipeline leveraging **Git/GitHub**, **Jenkins**, **Docker (multi-stage build)**, and **Docker Compose (with Nginx reverse proxy)**.

---

## Architecture Overview

```
+-----------------------------------------------------------------------------------+
|                                 DevOps Workflow                                   |
|                                                                                   |
|  [Developer] --- git push ---> [GitHub Repo]                                      |
|                                     |                                             |
|                               (Webhook / Poll)                                    |
|                                     v                                             |
|                             +---------------+                                     |
|                             |    Jenkins    |                                     |
|                             +---------------+                                     |
|                                     |                                             |
|        +----------------------------+----------------------------+                |
|        |              |             |              |             |                |
|    1. Checkout    2. Build       3. Test     4. Docker Build  5. Push             |
|                  (mvn clean)   (JUnit XML)   (Build # tag)   (Docker Hub)         |
|                                                            6. Deploy              |
|                                                                  v                |
|                                                       [Docker Compose Stack]      |
+------------------------------------------------------------------+----------------+
                                                                   |
                                                                   v
                                                    +-------------------------------+
                                                    |     Nginx Reverse Proxy       |
                                                    |          (Port 80)            |
                                                    +---------------+---------------+
                                                                    |
                                                            (calc-network)
                                                                    |
                                                                    v
                                                    +-------------------------------+
                                                    |     Calculator REST API       |
                                                    |          (Port 8080)          |
                                                    |      /actuator/health         |
                                                    +-------------------------------+
```

---

## 1. REST API Specification

### Endpoints
All calculation endpoints are exposed under `/api/v1` and take parameters `a` and `b`.

| Operation | HTTP Method | Endpoint | Sample Request | Expected Response |
| :--- | :--- | :--- | :--- | :--- |
| **Add** | `GET` | `/api/v1/add` | `/api/v1/add?a=2&b=3` | `{"operation":"add","a":2,"b":3,"result":5}` |
| **Subtract** | `GET` | `/api/v1/subtract` | `/api/v1/subtract?a=10&b=4` | `{"operation":"subtract","a":10,"b":4,"result":6}` |
| **Multiply** | `GET` | `/api/v1/multiply` | `/api/v1/multiply?a=5&b=6` | `{"operation":"multiply","a":5,"b":6,"result":30}` |
| **Divide** | `GET` | `/api/v1/divide` | `/api/v1/divide?a=10&b=2` | `{"operation":"divide","a":10,"b":2,"result":5}` |
| **Divide by Zero** | `GET` | `/api/v1/divide` | `/api/v1/divide?a=10&b=0` | HTTP 400 Bad Request with error JSON |
| **Health Check** | `GET` | `/actuator/health` | `/actuator/health` | `{"status":"UP"}` |

### Error Response Format (HTTP 400)
When invalid parameters are passed or division by zero is attempted:
```json
{
  "timestamp": "2026-10-20T17:21:00.000",
  "status": 400,
  "error": "Bad Request",
  "message": "Division by zero is not allowed.",
  "path": "/api/v1/divide"
}
```

---

## 2. Local Development & Testing

### Prerequisites
- Java 17+ (e.g. OpenJDK 17)
- Apache Maven 3.8+
- Docker & Docker Compose

### Run Unit & Integration Tests
```bash
mvn clean test
```
Test results and XML reports are generated in `target/surefire-reports/`.

### Run Spring Boot Application Locally
```bash
mvn spring-boot:run
```
The application starts at `http://localhost:8080`.

### Test Endpoints via cURL
```bash
# Add
curl "http://localhost:8080/api/v1/add?a=12&b=8"

# Subtract
curl "http://localhost:8080/api/v1/subtract?a=25&b=7"

# Multiply
curl "http://localhost:8080/api/v1/multiply?a=4&b=9"

# Divide
curl "http://localhost:8080/api/v1/divide?a=42&b=7"

# Divide by Zero (Validation Test)
curl -i "http://localhost:8080/api/v1/divide?a=42&b=0"

# Actuator Health Probe
curl "http://localhost:8080/actuator/health"
```

---

## 3. Containerization (Docker)

### Multi-Stage Dockerfile Highlights
- **Builder Stage**: Uses `maven:3.9.6-eclipse-temurin-17-alpine` to compile and package the JAR, with dependency caching.
- **Runtime Stage**: Uses lightweight `eclipse-temurin:17-jre-alpine` (~100MB footprint).
- **Security**: Runs under a non-root system user (`appuser`).
- **Healthcheck**: Built-in container health check hitting `/actuator/health`.

### Build & Run Docker Container Standalone
```bash
# Build the Docker image
docker build -t calculator-api:1.0.0 .

# Run the container
docker run -d --name calculator-app -p 8080:8080 calculator-api:1.0.0

# Verify health status
docker inspect --format='{{json .State.Health}}' calculator-app

# Clean up
docker stop calculator-app && docker rm calculator-app
```

---

## 4. Multi-Container Orchestration (Docker Compose)

The multi-container stack consists of:
1. `calculator-api`: Spring Boot backend service on custom bridge network `calc-network`.
2. `nginx-proxy`: Nginx reverse proxy listening on port 80, routing incoming traffic to the backend API after health check verification.

### Launch Stack
```bash
docker compose up -d --build
```

### Inspect Running Containers
```bash
docker compose ps
```

### Access via Nginx Reverse Proxy (Port 80)
```bash
curl "http://localhost:80/api/v1/add?a=100&b=250"
curl "http://localhost:80/actuator/health"
```

### Teardown Stack
```bash
docker compose down
```

---

## 5. Jenkins Declarative CI/CD Pipeline

The pipeline is defined in [Jenkinsfile](file:///Users/mithileshsaiyechuri/Acads/Sem%207/SPE/miniproject/Jenkinsfile) and includes 6 automated stages:

1. **Checkout**: Pulls the latest commit from GitHub upon push notification.
2. **Build**: Compiles the source code and packages the JAR via `mvn clean package -DskipTests`.
3. **Test**: Executes all JUnit 5 unit and integration tests via `mvn test` and publishes test reports to Jenkins (`junit '**/target/surefire-reports/*.xml'`).
4. **Docker Build**: Builds the container image tagged with the Jenkins `${BUILD_NUMBER}` and `latest`.
5. **Push**: Logs into Docker Hub using credentials securely retrieved from the Jenkins Credential Store (`dockerhub-credentials`) and pushes the image.
6. **Deploy**: Deploys or rolling-updates the Docker Compose stack using the newly pushed image and verifies container health at `http://localhost:80/actuator/health`.
7. **Post Actions**: Sends email notifications to the student upon pipeline success or failure.

### Jenkins Setup Steps
1. **Docker Hub Credentials**:
   - In Jenkins Dashboard $\rightarrow$ **Manage Jenkins** $\rightarrow$ **Credentials** $\rightarrow$ **System** $\rightarrow$ **Global credentials**.
   - Kind: `Username with password`.
   - ID: `dockerhub-credentials`.
   - Username / Password: your Docker Hub username and Access Token.
2. **Pipeline Job**:
   - Create a new **Pipeline** job named `calculator-ci-cd`.
   - Build Triggers: Select **GitHub hook trigger for GITScm polling** or **Poll SCM** (`H/5 * * * *`).
   - Definition: **Pipeline script from SCM** $\rightarrow$ Git $\rightarrow$ Enter repository URL and branch `main` $\rightarrow$ Script Path: `Jenkinsfile`.
3. **Email Notification Configuration**:
   - Configure SMTP server in **Manage Jenkins** $\rightarrow$ **System** $\rightarrow$ **Extended E-mail Notification**.

---

## 6. Project Structure

```
calculator-devops/
├── src/
│   ├── main/
│   │   ├── java/com/spe/calculator/
│   │   │   ├── CalculatorApplication.java
│   │   │   ├── controller/
│   │   │   │   └── CalculatorController.java
│   │   │   ├── dto/
│   │   │   │   ├── CalculationResponse.java
│   │   │   │   └── ErrorResponse.java
│   │   │   ├── exception/
│   │   │   │   ├── DivisionByZeroException.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   └── service/
│   │   │       └── CalculatorService.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/com/spe/calculator/
│           ├── controller/
│           │   └── CalculatorControllerIntegrationTest.java
│           └── service/
│               └── CalculatorServiceTest.java
├── nginx/
│   └── nginx.conf
├── Dockerfile
├── .dockerignore
├── docker-compose.yml
├── Jenkinsfile
├── pom.xml
├── .gitignore
└── README.md
```

---

## 7. Automated CI/CD Status
- **Pipeline**: Fully automated via Jenkins Declarative Pipeline and GitHub Webhook triggers.
- **Health Verification**: Continuous health check validation via Spring Boot Actuator (`/actuator/health`).

