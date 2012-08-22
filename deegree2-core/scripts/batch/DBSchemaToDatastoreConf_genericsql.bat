rem example for calling 'DBSchemaToDatastoreConf' for creating a deegree 
rem WFS configuration from a shape 

C:\Programme\Java\jre1.5.0_06\bin\java -classpath .;..\..\classes;..\..\lib\sqlserver\msbase.jar;..\..\lib\sqlserver\mssqlserver.jar;..\..\lib\sqlserver\msutil.jar;..\..\lib\jts\jts-1.8.jar;..\..\lib\xml\jaxen-1.1-beta-8.jar;..\..\lib\log4j\log4j-1.2.9.jar;..\..\lib\commons\commons-logging.jar org.deegree.tools.datastore.DBSchemaToDatastoreConf -output e:/temp/schema.xsd -driver com.microsoft.jdbc.sqlserver.SQLServerDriver -url jdbc:microsoft:sqlserver://localhost:1433;DatabaseName=testwfs -user www2 -password www2 -tables flaechennutzung
pause