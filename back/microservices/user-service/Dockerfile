# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy parent POM first (from root of build context)
COPY pom.xml .
COPY user-service/pom.xml user-service/pom.xml
COPY user-service/src user-service/src

RUN mvn clean package -DskipTests -f user-service/pom.xml

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/user-service/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]