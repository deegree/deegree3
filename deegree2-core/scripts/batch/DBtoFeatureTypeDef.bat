rem This file is part of deegree, for copyright/license information, please visit http://www.deegree.org/license.
rem You have to set several parameters to create a featuretypedefinition:
rem each row starts with a java call including several library references and method calls
rem -tables tablename -> if more than one table than table_1,table_2
rem -user DB user must be specified
rem -password '' -> passwort in single quote
rem -driver org.postgresql.Driver -> the needed driver
rem -url  jdbc:postgresql://localhost:5432/deegreetest -> the url to the DB server with port number an DB name
rem -output ../../conf/wms/featuretypes/sgid024_countyboundariespre2003.xsd -> output directory 
rem         and featuretype denfinition file name.

rem SCRIPT EXAMPLE

rem java -classpath ..\..\lib\deegree2.jar org.deegree.tools.datastore.DBSchemaToDatastoreConf -tables table_1,table_2 -user someuser -password '' -driver org.postgresql.Driver -url  jdbc:postgresql://server:5432/someDB -output ../../conf/wms/featuretypes/featuretypedefinition.xsd

rem SCRIPT BEGIN

rem java -classpath ..\..\lib\deegree2.jar org.deegree.tools.datastore.DBSchemaToDatastoreConf -tables sgid024_springs -user deegreetest -password 'deegreetest' -driver org.postgresql.Driver -url  jdbc:postgresql://localhost:5432/deegreetest -output ../../conf/wms/featuretypes/sgid024_springs.xsd
rem java -classpath ..\..\lib\deegree2.jar org.deegree.tools.datastore.DBSchemaToDatastoreConf -tables sgid100_railroadsdlg100 -user deegreetest -password 'deegreetest' -driver org.postgresql.Driver -url  jdbc:postgresql://localhost:5432/deegreetest -output ../../conf/wms/featuretypes/sgid100_railroadsdlg100.xsd


rem SCRIPT END