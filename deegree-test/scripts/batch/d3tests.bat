REM SET LD_LIBRARY_PATH=
REM SET JAVA_OPTS=-Xmx1000M -XX:MaxDirectMemorySize=500M
java %JAVA_OPTS% -cp deegree-tests-3.0-pre.jar:. org.deegree.test.TestingToolBox %*