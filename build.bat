@echo off
REM Compiles the Shelf Side app into the build\classes folder.
setlocal

REM Use the project's portable JDK if it is present, otherwise use the JDK on PATH.
set "JAVAC=javac"
for /d %%D in (".tools\jdk\jdk-*") do if exist "%%D\bin\javac.exe" set "JAVAC=%%D\bin\javac.exe"

if not exist "build\classes" mkdir "build\classes"

echo Compiling app...
"%JAVAC%" -d build\classes src\shelfside\*.java
if errorlevel 1 (
    echo BUILD FAILED
    exit /b 1
)
echo BUILD OK
