@echo off
if "%JAVA_HOME%"=="" call:FIND_JAVA_HOME
"%JAVA_HOME%"\bin\java -classpath deegree-javacheck.jar org.deegree.JavaCheck else java -classpath deegree-javacheck.jar org.deegree.JavaCheck
IF ERRORLEVEL 5 GOTO WRONGJAVA
SET JAVA_OPTS=-Xmx1024M
.\catalina.bat run
goto:END

:FIND_JAVA_HOME
FOR /F "skip=2 tokens=2*" %%A IN ('REG QUERY "HKLM\Software\JavaSoft\Java Development Kit" /v CurrentVersion') DO set CurVer=%%B
FOR /F "skip=2 tokens=2*" %%A IN ('REG QUERY "HKLM\Software\JavaSoft\Java Development Kit\%CurVer%" /v JavaHome') DO set JAVA_HOME=%%B
if "%JAVA_HOME%"=="" GOTO NOJAVA
goto:EOF

:WRONGJAVA
pause
goto:END

:NOJAVA
echo ***********************************************************************
echo JAVA_HOME is not set and automatic detection failed.
echo Please install a suitable JDK, e.g. Oracle Java 7 JDK.
echo ***********************************************************************
pause

:END
