rem @echo off

rem ---------------------------------------------------------------------------
rem Start script for the ADExporter (in deegree2)
rem ---------------------------------------------------------------------------

set EXPORTER_HOME=D:\Programme\deegree2\tools
set LIB=%EXPORTER_HOME%\lib
set JAVA=D:\Programme\Java\jdk1.5.0_05\bin\java
set CLASSPATH=%LIB%\deegree2.jar;%LIB%\ojdbc14.jar;%LIB%\log4j-1.2.9.jar
set MAINCLASS=org.deegree.tools.security.ActiveDirectoryImporter
set CONFIGFILE=%EXPORTER_HOME%\adexporter.properties

rem Start exporter main class
%JAVA% -classpath "%CLASSPATH%" %MAINCLASS% %CONFIGFILE%
:end
pause
