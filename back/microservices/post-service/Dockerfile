# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY microservices/post-service/pom.xml pom.xml
COPY microservices/post-service/src src

RUN mvn clean package -DskipTests -Dmaven.test.skip=true -Dmaven.wagon.http.retryHandler.count=5 -Dmaven.wagon.http.retryHandler.requestSentEnabled=true -Dmaven.wagon.httpconnectionManager.ttlSeconds=120 -Dhttp.keepAlive=false -Dmaven.wagon.http.timeout=180000

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]