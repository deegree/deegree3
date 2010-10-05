@ECHO OFF

java -classpath deegree-javacheck.jar org.deegree.JavaCheck
IF ERRORLEVEL 5 GOTO End

bin\catalina.bat start
START http://localhost:8080
TIMEOUT /T -1

:End