@echo off
REM Compiles the app and the tests, then runs all tests with JUnit.
setlocal

REM Use the project's portable JDK if it is present, otherwise use the JDK on PATH.
set "JAVAC=javac"
set "JAVA=java"
for /d %%D in (".tools\jdk\jdk-*") do (
    if exist "%%D\bin\javac.exe" set "JAVAC=%%D\bin\javac.exe"
    if exist "%%D\bin\java.exe"  set "JAVA=%%D\bin\java.exe"
)

set "JUNIT=.tools\jars\junit-4.13.2.jar"
set "HAMCREST=.tools\jars\hamcrest-core-1.3.jar"

if not exist "build\classes" mkdir "build\classes"
if not exist "build\test-classes" mkdir "build\test-classes"

echo Compiling app...
"%JAVAC%" -d build\classes src\shelfside\*.java
if errorlevel 1 ( echo BUILD FAILED & exit /b 1 )

echo Compiling tests...
"%JAVAC%" -cp "build\classes;%JUNIT%;%HAMCREST%" -d build\test-classes test\shelfside\*.java
if errorlevel 1 ( echo TEST BUILD FAILED & exit /b 1 )

echo Running tests...
"%JAVA%" -cp "build\classes;build\test-classes;%JUNIT%;%HAMCREST%" org.junit.runner.JUnitCore ^
    shelfside.ItemTest ^
    shelfside.StorageTest ^
    shelfside.LoginProcessorTest ^
    shelfside.RetailSideTest ^
    shelfside.ClientSideTest
