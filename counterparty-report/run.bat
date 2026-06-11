@echo off
set JAVA_HOME=C:\Program Files\Java\jdk-24
set PATH=%JAVA_HOME%\bin;%PATH%

echo ========================================
echo   Counterparty Report Application
echo ========================================
echo.
echo Using Java: %JAVA_HOME%
echo.

cd /d %~dp0

echo Starting application...
echo.

java -jar target/counterparty-report-0.0.1-SNAPSHOT.jar

echo.
echo Application stopped.
pause