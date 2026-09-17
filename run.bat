@echo off
REM Compiles (if needed) and runs the Shelf Side app.
REM Optional: pass a path to use a different inventory file, e.g.  run.bat myfile.json
setlocal

set "JAVAC=javac"
set "JAVA=java"
for /d %%D in (".tools\jdk\jdk-*") do (
    if exist "%%D\bin\javac.exe" set "JAVAC=%%D\bin\javac.exe"
    if exist "%%D\bin\java.exe"  set "JAVA=%%D\bin\java.exe"
)

if not exist "build\classes" mkdir "build\classes"

echo Compiling app...
"%JAVAC%" -d build\classes src\shelfside\*.java
if errorlevel 1 ( echo BUILD FAILED & exit /b 1 )

"%JAVA%" -cp "build\classes" shelfside.ShelfSideWrapper %*
