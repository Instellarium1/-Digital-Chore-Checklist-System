@echo off
setlocal
if not exist out mkdir out
if exist sources.txt del sources.txt
dir /s /b src\*.java > sources.txt
javac -d out @sources.txt
if errorlevel 1 (
  echo.
  echo Compilation failed. Make sure the JDK is installed and check the source files.
  del sources.txt
  pause
  exit /b 1
)
del sources.txt
java -cp out chorechecklist.Main
pause
endlocal
