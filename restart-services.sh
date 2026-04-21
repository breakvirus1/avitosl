#!/bin/bash

# Script to kill processes on ports 8081 (backend) and 5173 (frontend) and restart both services
echo "🔪 Killing existing processes on ports 8081 and 5173..."

# Kill processes on port 8081 (backend)
PID_8081=$(lsof -ti:8081 2>/dev/null)
if [ ! -z "$PID_8081" ]; then
    echo "Killing process $PID_8081 on port 8081"
    kill -9 $PID_8081 2>/dev/null
fi

# Kill processes on port 5173 (frontend)
PID_5173=$(lsof -ti:5173 2>/dev/null)
if [ ! -z "$PID_5173" ]; then
    echo "Killing process $PID_5173 on port 5173"
    kill -9 $PID_5173 2>/dev/null
fi

# Wait a moment for processes to die
sleep 2

echo "🚀 Starting backend..."
cd back/avito
mvn spring-boot:run &
BACKEND_PID=$!
echo "Backend started with PID: $BACKEND_PID"

echo "🚀 Starting frontend..."
cd ../../front
npm run dev &
FRONTEND_PID=$!
echo "Frontend started with PID: $FRONTEND_PID"

echo "✅ Both services started!"
echo "Backend (PID: $BACKEND_PID) running on http://localhost:8081"
echo "Frontend (PID: $FRONTEND_PID) running on http://localhost:5173"
echo ""
echo "To stop services, run: kill $BACKEND_PID $FRONTEND_PID"