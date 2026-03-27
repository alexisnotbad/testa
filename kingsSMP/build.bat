@echo off
setlocal enabledelayedexpansion
echo ============================================
echo  KingsSMP Build Script (no Maven needed)
echo ============================================

REM --- Config ---
set PAPER_VERSION=1.21.1
set PAPER_BUILD=132
set PAPER_JAR=paper-%PAPER_VERSION%-%PAPER_BUILD%.jar
set PAPER_URL=https://api.papermc.io/v2/projects/paper/versions/%PAPER_VERSION%/builds/%PAPER_BUILD%/downloads/%PAPER_JAR%
set OUT_JAR=kingsSMP-1.0.0.jar

REM --- Check Java ---
java -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java not found. Install Java 21 from https://adoptium.net
    pause & exit /b 1
)

REM --- Download Paper jar if needed ---
if not exist "%PAPER_JAR%" (
    echo Downloading Paper %PAPER_VERSION% build %PAPER_BUILD%...
    powershell -Command "Invoke-WebRequest -Uri '%PAPER_URL%' -OutFile '%PAPER_JAR%'"
    if not exist "%PAPER_JAR%" (
        echo ERROR: Failed to download Paper jar.
        echo Please download it manually from:
        echo   %PAPER_URL%
        echo and place it in this folder as: %PAPER_JAR%
        pause & exit /b 1
    )
    echo Downloaded %PAPER_JAR%
) else (
    echo Found existing %PAPER_JAR%
)

REM --- Collect all .java files ---
echo Collecting source files...
if exist sources.txt del sources.txt
for /r "src\main\java" %%f in (*.java) do (
    echo %%f >> sources.txt
)

REM --- Create output dirs ---
if not exist "build\classes" mkdir "build\classes"
if not exist "build\jar\com\kingssmp" mkdir "build\jar\com\kingssmp"

REM --- Compile ---
echo Compiling...
javac -source 21 -target 21 ^
    -cp "%PAPER_JAR%" ^
    -d "build\classes" ^
    @sources.txt

if errorlevel 1 (
    echo.
    echo ERROR: Compilation failed. See errors above.
    if exist sources.txt del sources.txt
    pause & exit /b 1
)
echo Compilation successful!

REM --- Copy resources ---
echo Copying resources...
xcopy /s /y "src\main\resources\*" "build\classes\" >nul

REM --- Package jar ---
echo Packaging jar...
cd build\classes
jar cfm "..\..\%OUT_JAR%" ..\..\MANIFEST.MF . >nul 2>&1
REM Try without manifest if that fails
jar cf "..\..\%OUT_JAR%" .
cd ..\..

if exist "%OUT_JAR%" (
    echo.
    echo ============================================
    echo  SUCCESS! Built: %OUT_JAR%
    echo  Copy this file to your server's plugins folder.
    echo ============================================
) else (
    echo ERROR: Jar packaging failed.
)

if exist sources.txt del sources.txt
pause
