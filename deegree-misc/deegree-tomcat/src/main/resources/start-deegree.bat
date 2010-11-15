@ECHO OFF

if defined %JAVA_HOME% %JAVA_HOME%\bin\java -classpath deegree-javacheck.jar org.deegree.JavaCheck else java -classpath deegree-javacheck.jar org.deegree.JavaCheck

IF ERRORLEVEL 5 GOTO End

SET JAVA_OPTS=-Xmx1024M
bin\catalina.bat start
START http://localhost:8080
TIMEOUT /T -1

:End
