# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY microservices/api-gateway/pom.xml pom.xml
COPY microservices/api-gateway/src src

RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]