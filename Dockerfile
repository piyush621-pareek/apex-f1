# ==========================================
# APEX F1 — Multi-Stage Production Dockerfile
# Build & run: docker compose up --build
# ==========================================

FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /workspace/target/apex-f1-backend.jar /app/apex-f1-backend.jar
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["java", "-jar", "/app/apex-f1-backend.jar"]
