# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy parent POM first (from root of build context)
COPY pom.xml .
COPY purchase-service/pom.xml purchase-service/pom.xml
COPY purchase-service/src purchase-service/src

RUN mvn clean package -DskipTests -f purchase-service/pom.xml

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/purchase-service/target/*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]