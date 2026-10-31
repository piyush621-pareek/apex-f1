@echo off
REM ============================================================
REM  APEX F1 — Build & Run Production Fat JAR (Windows)
REM  Packages target\apex-f1-backend.jar and launches with prod profile
REM ============================================================
setlocal
cd /d "%~dp0"

where mvn >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    set "MVN_CMD=mvn"
) else if exist "%USERPROFILE%\maven\apache-maven-3.9.9\bin\mvn.cmd" (
    set "MVN_CMD=%USERPROFILE%\maven\apache-maven-3.9.9\bin\mvn.cmd"
) else if exist "D:\AOOPS Project\.tools\apache-maven-3.9.9\bin\mvn.cmd" (
    set "JAVA_HOME=D:\AOOPS Project\.tools\jdk-17.0.20.1+1"
    set "PATH=%JAVA_HOME%\bin;%PATH%"
    set "MVN_CMD=D:\AOOPS Project\.tools\apache-maven-3.9.9\bin\mvn.cmd"
) else (
    echo Maven not found in PATH. Downloading portable Maven 3.9.9 to %USERPROFILE%\maven...
    powershell -NoProfile -Command "Invoke-WebRequest 'https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.9/apache-maven-3.9.9-bin.zip' -OutFile '$env:TEMP\maven.zip'; Expand-Archive '$env:TEMP\maven.zip' -DestinationPath '$env:USERPROFILE\maven' -Force"
    set "MVN_CMD=%USERPROFILE%\maven\apache-maven-3.9.9\bin\mvn.cmd"
)

echo Running JUnit 5 tests and packaging runnable JAR...
call "%MVN_CMD%" clean package
if %ERRORLEVEL% NEQ 0 exit /b %ERRORLEVEL%

echo Starting production JAR on http://localhost:8080 ...
java -jar target\apex-f1-backend.jar --spring.profiles.active=prod
endlocal
