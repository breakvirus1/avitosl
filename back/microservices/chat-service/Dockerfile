# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy parent POM first (from root of build context)
COPY pom.xml .
COPY chat-service/pom.xml chat-service/pom.xml
COPY chat-service/src chat-service/src

RUN mvn clean package -DskipTests -f chat-service/pom.xml

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/chat-service/target/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]