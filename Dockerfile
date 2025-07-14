# Build backend
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x ./mvnw && ./mvnw dependency:go-offline -B
COPY src src
RUN ./mvnw package -DskipTests

# Runtime image
FROM eclipse-temurin:21-jre-jammy AS runtime
RUN groupadd -r appuser && useradd -r -g appuser appuser
WORKDIR /app
COPY --from=build --chown=appuser:appuser /app/target/*.jar app.jar
# VOLUME /app/logs
ARG SERVER_PORT=8080
EXPOSE ${SERVER_PORT}
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:${SERVER_PORT}/actuator/health || exit 1
USER appuser
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
LABEL maintainer="sagar.shrivastva6413@example.com" \
      version="1.0" \
      description="Spring Boot application"