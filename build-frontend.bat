@echo off
echo Building Vue frontend...
cd frontend
npm install
npm run build
cd ..
echo Frontend build completed!