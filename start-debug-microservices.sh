#!/bin/bash

# Start infrastructure containers (DB, Keycloak)
echo "Starting infrastructure (DB, Keycloak)..."
docker-compose up -d postgres keycloak-db keycloak

# Wait for infrastructure
echo "Waiting for infrastructure..."
sleep 20

# Start Eureka server locally
echo "Starting Eureka server locally..."
cd back/microservices/eureka-server
mvn clean install -DskipTests
mvn spring-boot:run &
cd -
echo "Eureka server started locally"

# Wait for Eureka
sleep 15

# Start microservices locally (with build)
echo "Starting microservices locally..."
cd back/microservices/user-service
mvn clean install -DskipTests
mvn spring-boot:run &
cd -

cd back/microservices/post-service
mvn clean install -DskipTests
mvn spring-boot:run &
cd -

cd back/microservices/category-service
mvn clean install -DskipTests
mvn spring-boot:run &
cd -

cd back/microservices/chat-service
mvn clean install -DskipTests
mvn spring-boot:run &
cd -

cd back/microservices/purchase-service
mvn clean install -DskipTests
mvn spring-boot:run &
cd -

# Wait for services
sleep 30

# Stop any running API Gateway
echo "Stopping any running API Gateway..."
pkill -f "api-gateway" || true

# Start API Gateway locally
echo "Starting API Gateway locally..."
cd back/microservices/api-gateway
mvn clean install -DskipTests
mvn spring-boot:run &
cd -
echo "API Gateway started locally"

# Start frontend locally
echo "Starting frontend locally..."
cd front
npm run dev &
cd -
echo "Frontend started locally"

echo "All services started for debugging. Check logs."

echo "All services started for debugging. Check logs in terminals."
echo "To stop: kill background processes or use Ctrl+C in each terminal."