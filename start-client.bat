@echo off
chcp 65001 >nul
echo Starting TCP Client...
cd tcp-client
mvn spring-boot:run
pause
