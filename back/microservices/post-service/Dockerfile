# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy parent POM first (from root of build context)
COPY pom.xml .
COPY post-service/pom.xml post-service/pom.xml
COPY post-service/src post-service/src

RUN mvn clean package -DskipTests -f post-service/pom.xml

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/post-service/target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]