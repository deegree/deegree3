rem example for calling 'DBSchemaToDatastoreConf' for creating a deegree 
rem WFS configuration from a shape 

java -classpath .;..\..\classes;..\..\lib\postgis\postgresql-8.3-603.jdbc3.jar;..\..\lib\postgis\postgis1.3.3.jar;..\..\lib\xml\jaxen-1.1-beta-8.jar;..\..\lib\log4j\log4j-1.2.9.jar;..\..\lib\commons\commons-logging.jar org.deegree.tools.datastore.DBSchemaToDatastoreConf -output e:/temp/schema.xsd -driver org.postgresql.Driver -url "jdbc:postgresql://localhost:5432/igeodesktop" -user postgres -password postgres -tables zipcodes -srs EPSG:26912
pause