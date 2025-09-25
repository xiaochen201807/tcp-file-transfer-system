@echo off
chcp 65001 >nul
echo Starting TCP Server...
cd tcp-server
mvn spring-boot:run
pause
