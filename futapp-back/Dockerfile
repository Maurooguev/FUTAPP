FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn --batch-mode dependency:go-offline
COPY src ./src
RUN mvn --batch-mode -DskipTests package

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S futapp && adduser -S futapp -G futapp \
    && mkdir -p /var/lib/futapp/uploads && chown -R futapp:futapp /var/lib/futapp
WORKDIR /app
COPY --from=build /workspace/target/futapp-0.0.1-SNAPSHOT.jar app.jar
USER futapp
EXPOSE 8080
HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=5 \
  CMD wget -q -O - http://127.0.0.1:8080/api/v1/status >/dev/null || exit 1
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
