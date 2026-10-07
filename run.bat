@echo off
REM ============================================================
REM  APEX F1 - one-click run (Windows)
REM  Uses the portable JDK 17 + Maven kept on the D drive, so
REM  nothing needs to be installed system-wide.
REM ============================================================
setlocal
set "JAVA_HOME=D:\AOOPS Project\.tools\jdk-17.0.20.1+1"
set "PATH=%JAVA_HOME%\bin;%PATH%"
set "MVN=D:\AOOPS Project\.tools\apache-maven-3.9.9\bin\mvn.cmd"

cd /d "%~dp0"
echo Using JAVA_HOME=%JAVA_HOME%
echo Starting APEX F1 on http://localhost:8080  (press Ctrl+C to stop)
call "%MVN%" -DskipTests spring-boot:run
endlocal
