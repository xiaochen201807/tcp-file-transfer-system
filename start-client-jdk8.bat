@echo off
echo Starting TCP Client with JDK 1.8...
echo Checking Java version...
java -version
echo.
cd tcp-client
set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_XXX
set PATH=%JAVA_HOME%\bin;%PATH%
mvn spring-boot:run
pause
