# Calculator DevOps

A Calculator REST API built with Spring Boot 4 on Java 21 and Maven. It has `add`, `subtract`, `multiply` and `divide` endpoints under `/api/v1`. Each one takes two numeric query parameters and returns the result as JSON. Invalid input returns HTTP 400 with a structured error body. Invalid input means a missing or non-numeric parameter, `NaN`/`Infinity`, division by zero, or a result that overflows. A Spring Boot Actuator health endpoint is also exposed.

## Prerequisites

- JDK 21
- Maven 3.9+ (or use the bundled wrapper: `./mvnw` / `mvnw.cmd`)

## Build, test and run

```bash
# Build the jar (also runs the tests)
mvn clean package

# Run the tests only
mvn test
```

Test reports are written to `target/surefire-reports/`.

Run the app with either command. Both serve on **port 8081**.

```bash
mvn spring-boot:run
java -jar target/calculator-0.0.1-SNAPSHOT.jar
```

## API

All calculator endpoints take the required query parameters `a` and `b`, both finite numbers.

| Method | Endpoint             | Description                    |
|--------|----------------------|--------------------------------|
| GET    | `/api/v1/add`        | `a + b`                        |
| GET    | `/api/v1/subtract`   | `a - b`                        |
| GET    | `/api/v1/multiply`   | `a * b`                        |
| GET    | `/api/v1/divide`     | `a / b` (`b` must not be `0`)  |
| GET    | `/actuator/health`   | Application health (`UP`/`DOWN`) |

### Success

```bash
curl "http://localhost:8081/api/v1/add?a=2&b=3"
```
```json
{"operation":"add","a":2.0,"b":3.0,"result":5.0}
```

### Errors (HTTP 400)

Every error uses the same shape: `timestamp`, `status`, `error`, `message`, `path`.

**Division by zero**
```bash
curl "http://localhost:8081/api/v1/divide?a=1&b=0"
```
```json
{"timestamp":"2026-10-23T15:23:31.513599600Z","status":400,"error":"Bad Request","message":"Division by zero is not allowed","path":"/api/v1/divide"}
```

**Missing parameter**
```bash
curl "http://localhost:8081/api/v1/add?a=1"
```
```json
{"timestamp":"2026-10-23T15:23:31.589168500Z","status":400,"error":"Bad Request","message":"Missing required parameter 'b'","path":"/api/v1/add"}
```

**Non-numeric parameter**
```bash
curl "http://localhost:8081/api/v1/add?a=abc&b=1"
```
```json
{"timestamp":"2026-10-23T15:23:31.658272900Z","status":400,"error":"Bad Request","message":"Parameter 'a' must be a number, but was 'abc'","path":"/api/v1/add"}
```

**NaN / Infinity input**
```bash
curl "http://localhost:8081/api/v1/subtract?a=1&b=NaN"
```
```json
{"timestamp":"2026-10-23T15:23:31.727929Z","status":400,"error":"Bad Request","message":"Parameter 'b' must be a finite number, but was 'NaN'","path":"/api/v1/subtract"}
```

**Result overflow**
```bash
curl "http://localhost:8081/api/v1/multiply?a=1e308&b=10"
```
```json
{"timestamp":"2026-10-23T15:23:31.800834600Z","status":400,"error":"Bad Request","message":"Result of multiply is too large to represent as a finite number","path":"/api/v1/multiply"}
```

## Running with Docker

The multi-stage [Dockerfile](Dockerfile) builds the JAR with Maven. It then runs the JAR on a slim Java 21 runtime image as a non-root user.

```bash
docker build -t calculator .
docker run -d --name calculator -p 8081:8081 calculator
curl "http://localhost:8081/api/v1/add?a=2&b=3"
```

## Running with Docker Compose

[docker-compose.yml](docker-compose.yml) runs **2 replicas** of the app behind an **nginx** reverse proxy on **port 80**. nginx shares requests between the replicas. The replicas publish no ports of their own, so all traffic goes through nginx.

```bash
docker compose up -d        # build (if needed) and start the stack
docker compose ps           # all containers should show (healthy)
curl "http://localhost/api/v1/add?a=2&b=3"
docker compose down         # stop and remove the stack
```

- **Health checks:** each app replica checks `/actuator/health`. nginx starts only after the replicas are healthy, and it has its own health check.
- **Image:** `${DOCKERHUB_USER:-suryans77}/calculator:${IMAGE_TAG:-latest}`. Set these variables to run a specific build.

## CI/CD Pipeline (Jenkins)

The declarative [Jenkinsfile](Jenkinsfile) runs on every push to GitHub:

| Stage        | What it does |
|--------------|--------------|
| Checkout     | Checks out the pushed commit |
| Build        | `mvn -B clean package -DskipTests` |
| Test         | `mvn -B test`; publishes JUnit reports |
| Docker Build | Builds `suryans77/calculator`, tagged with the build number and `latest` |
| Push         | Pushes both tags to [Docker Hub](https://hub.docker.com/r/suryans77/calculator) |
| Deploy       | `docker compose up -d` with the new tag; waits up to 120s for every container to be healthy, then checks `/actuator/health` through nginx |

- **Trigger:** a GitHub webhook reaches the local Jenkins through an ngrok tunnel (`ngrok http 8080`). The webhook URL is `https://<ngrok-host>/github-webhook/`, and the job has *GitHub hook trigger for GITScm polling* enabled.
- **Credentials:** Docker Hub credentials are stored in Jenkins as a username/password credential with the ID `dockerhub-creds`, using an access token as the password. Nothing is hard-coded in the repo.
- **Notifications:** an email with the job name, build number, status and build URL is sent on every success or failure. This uses the Email Extension plugin.

## Project structure

```
.
├── Dockerfile                                   # Multi-stage image build
├── docker-compose.yml                           # 2 app replicas + nginx
├── nginx/nginx.conf                             # Reverse proxy / load balancer
├── Jenkinsfile                                  # CI/CD pipeline
└── src/
    ├── main/
    │   ├── java/com/suryans/calculator/
    │   │   ├── CalculatorApplication.java       # Spring Boot entry point
    │   │   ├── controller/CalculatorController.java  # REST endpoints
    │   │   ├── service/CalculatorService.java   # Arithmetic and validation
    │   │   ├── dto/                             # CalculationResponse, ErrorResponse
    │   │   └── exception/                       # Custom exceptions and GlobalExceptionHandler
    │   └── resources/application.yaml           # Port 8081, actuator config
    └── test/java/com/suryans/calculator/
        ├── CalculatorApplicationTests.java      # Context load test
        ├── controller/CalculatorControllerTest.java  # MockMvc (@WebMvcTest) tests
        └── service/CalculatorServiceTest.java   # JUnit 5 unit tests
```

checking for build automation