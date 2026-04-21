# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY microservices/user-service/pom.xml pom.xml
COPY microservices/user-service/src src

RUN mvn clean package -DskipTests -Dmaven.test.skip=true -Dmaven.wagon.http.retryHandler.count=5 -Dmaven.wagon.http.retryHandler.requestSentEnabled=true -Dmaven.wagon.httpconnectionManager.ttlSeconds=120 -Dhttp.keepAlive=false -Dmaven.wagon.http.timeout=180000

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8081
RUN echo '#!/bin/sh' > /app/start.sh && \
    echo 'sleep 10' >> /app/start.sh && \
    echo 'exec java -jar app.jar' >> /app/start.sh && \
    chmod +x /app/start.sh
ENTRYPOINT ["/app/start.sh"]