#!/bin/bash

# Build all microservices
echo "Building all microservices..."
for service in eureka-server user-service post-service category-service chat-service purchase-service api-gateway; do
  echo "Building $service..."
  cd back/microservices/$service
  mvn clean install -DskipTests
  cd -
done
echo "All microservices built."

# Start everything in Docker
echo "Starting all services in Docker..."
docker-compose -f docker-compose.yml -f back/docker-compose.yml up --build -d

# Wait for startup
sleep 60

# Start frontend locally (or containerize if needed)
echo "Starting frontend locally..."
cd front
npm run dev &
cd -

echo "Production environment started. Access at http://localhost:8080 (Gateway), http://localhost:5173 (Frontend)."