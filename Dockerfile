# ---------- Build stage ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy only the POM first so dependencies get their own layer and stay cached
# until pom.xml changes
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Copy the source and build the executable JAR (tests run in CI, not here)
COPY src ./src
RUN mvn -B clean package -DskipTests

# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create an unprivileged system user and group to run the app
RUN addgroup -S spring && adduser -S spring -G spring

# Copy only the built JAR from the build stage, owned by the app user
COPY --from=build --chown=spring:spring /app/target/*.jar app.jar

# Drop root privileges
USER spring

# Port the app listens on (server.port in application.yaml)
EXPOSE 8081

# Exec form so java runs as PID 1 and receives stop signals directly
ENTRYPOINT ["java", "-jar", "app.jar"]
