rem set environment variables
set lib=..\..\lib

java -Xms300m -Xmx1400m -classpath %lib%\deegree2.jar;%lib%\postgresql-8.0-311.jdbc3.jar;%lib%\log4j-1.2.9.jar;%lib%\jaxen-1.1-beta-8.jar;%lib%\commons-logging.jar;%lib%\ojdbc14_10g.jar org.deegree.tools.security.WMSLayerImporter -WMSAddress http://demo.deegree.org/deegree-wms/services -Driver org.postgresql.Driver -URL jdbc:postgresql://localhost:5432/security -DBUserName postgres -DBUserPassword postgres -SecAdminPassword JOSE67
pause