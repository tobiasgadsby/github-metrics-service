FROM openjdk:21-jdk-slim

WORKDIR /app

COPY target/github-metrics-svc-1.0.1.jar github-metrics-svc-1.0.1.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "github-metrics-svc-1.0.1.jar"]