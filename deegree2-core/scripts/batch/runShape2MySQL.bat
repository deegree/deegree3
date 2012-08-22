set JAVA=C:\Programme\Java\jdk1.5.0_06\bin\java
set CP=.;..\..\classes;..\..\lib\xml\jaxen-1.1-beta-8.jar;..\..\lib\log4j\log4j-1.2.9.jar;..\..\lib\commons\commons-logging.jar;..\..\lib\mysql\mysql-connector-java-5.1.7-bin.jar;..\..\lib\jts\jts-1.8.jar
set ARGS=-f D:\data\shapes\utah\adminstration\SGID500_ZipCodes.shp -url jdbc:mysql://localhost:3306/bfs -user root -password mysql
%JAVA% -classpath %CP% org.deegree.tools.shape.Shp2MySQL %ARGS%