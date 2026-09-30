@echo off
REM ---------------------------------------------------------------------------
REM Quiz Master - local start (Windows).
REM Maven install karne ki zaroorat nahi: Maven Wrapper (mvnw.cmd) khud Maven
REM download kar leta hai (pehli baar internet chahiye).
REM   Chalane ke liye: is file par double-click karo
REM   Rukne ke liye   : iss window me Ctrl + C
REM ---------------------------------------------------------------------------
cd /d "%~dp0"
title Quiz Master - local server

where java >nul 2>nul
if errorlevel 1 (
  echo.
  echo   Java nahi mila.
  echo   JDK 17 install karo ^(LOCAL-SETUP.md dekho^), phir naya Command Prompt khol ke dobara try karo.
  echo.
  pause
  exit /b 1
)

echo Java version:
java -version 2>&1 | findstr /i "version"
echo.
echo App start ho raha hai  -^>  http://localhost:8080
echo Admin login: admin@quizmaster.app / Admin@12345
echo.

call mvnw.cmd spring-boot:run
echo.
echo Server band ho gaya.
pause
