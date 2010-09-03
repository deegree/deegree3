@ECHO OFF

::IF "%JAVA_HOME%" == "" (
::SET JAVA_HOME=""
::ECHO "JAVA_HOME not set. Looking up in registry."
::set KeyName=HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Java Development Kit
::set Cmd=reg query "%KeyName%" /s
::for /f "tokens=2*" %%i in ('%Cmd% ^| find "JavaHome"') do set JAVA_HOME=%%j
::)

IF "%JAVA_HOME%" == "" (
  ECHO ******************************************************
  ECHO "No Java Development Kit (JDK) installation found.   "
  ECHO "Please install Oracle (Sun) JDK 1.6.0_04 or greater."
  ECHO ******************************************************
  PAUSE
  START http://www.oracle.com/technetwork/java/javase/downloads/index.html
) ELSE (
  ECHO "Using %JAVA_HOME%"
  SET PATH=%JAVA_HOME%\bin;%PATH%
  bin\catalina.bat start
  START http://localhost:8080
)
::EXIT