@echo off
setlocal
cd /d "%~dp0"
set "APP_JAR=target\BTL_OOP_desktop-1.0.0.jar"
if not exist "%APP_JAR%" set "APP_JAR=BTL_OOP_desktop.jar"
if not exist "%APP_JAR%" (
  echo Chua co file JAR. Hay chay Maven verify trong IntelliJ roi thu lai.
  pause
  exit /b 1
)
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" goto java_home
if exist "%ProgramFiles%\Apache NetBeans\jdk\bin\java.exe" (
  set "JAVA_HOME=%ProgramFiles%\Apache NetBeans\jdk"
  goto java_home
)
echo Can Java 17 tro len. Neu java -version la 1.8, hay dat JAVA_HOME toi JDK moi.
java -jar "%APP_JAR%"
if errorlevel 1 pause
exit /b
:java_home
"%JAVA_HOME%\bin\java.exe" -jar "%APP_JAR%"
if errorlevel 1 pause
