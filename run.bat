@echo off
REM ============================================================
REM  APEX F1 — One-Click Run (Windows)
REM  Auto-detects system Maven, %USERPROFILE%\maven, or downloads Maven 3.9.9 automatically.
REM ============================================================
setlocal
cd /d "%~dp0"

if exist "D:\AOOPS Project\.tools\jdk-17.0.20.1+1" (
    set "JAVA_HOME=D:\AOOPS Project\.tools\jdk-17.0.20.1+1"
    set "PATH=%JAVA_HOME%\bin;%PATH%"
)

where mvn >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    set "MVN=mvn"
) else if exist "%USERPROFILE%\maven\apache-maven-3.9.9\bin\mvn.cmd" (
    set "MVN=%USERPROFILE%\maven\apache-maven-3.9.9\bin\mvn.cmd"
) else if exist "D:\AOOPS Project\.tools\apache-maven-3.9.9\bin\mvn.cmd" (
    set "MVN=D:\AOOPS Project\.tools\apache-maven-3.9.9\bin\mvn.cmd"
) else (
    echo Maven not found in PATH. Downloading portable Maven 3.9.9 to %USERPROFILE%\maven...
    powershell -NoProfile -Command "Invoke-WebRequest 'https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.9/apache-maven-3.9.9-bin.zip' -OutFile '$env:TEMP\maven.zip'; Expand-Archive '$env:TEMP\maven.zip' -DestinationPath '$env:USERPROFILE\maven' -Force"
    set "MVN=%USERPROFILE%\maven\apache-maven-3.9.9\bin\mvn.cmd"
)

echo Starting APEX F1 on http://localhost:8080  (press Ctrl+C to stop)
call "%MVN%" -DskipTests spring-boot:run
endlocal
