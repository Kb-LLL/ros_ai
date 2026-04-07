@echo off
echo Starting development environment...

echo Starting backend server...
start "Backend" cmd /k "mvn spring-boot:run"

echo Starting frontend development server...
cd frontend
start "Frontend" cmd /k "npm run dev"
cd ..

echo Development environment started!
echo Frontend: http://localhost:3000
echo Backend: http://localhost:8080