#!/bin/sh
# This file is part of deegree, for copyright/license information, please visit http://www.deegree.org/license.
# This tool generates FeatureTypeDefinitions from Database tables.
# You have to set several parameters to create a featuretypedefinition:
# the following lines are just comments
# each row starts with a java call
# -tables tablename -> if more than one table than table_1,table_2
# -user DB user must be specified
# -password 'deegreetest' passwort in single quote
# -driver org.postgresql.Driver the needed driver
# -url  jdbc:postgresql://localhost:5432/deegreetest the url to the DB server with port number an DB name
# -output ../../conf/wms/featuretypes/sgid024_countyboundariespre2003.xsd output directory 
#         and featuretype denfinition file name.

# SCRIPT EXAMPLE

# java -classpath ../../lib/deegree2.jar org.deegree.tools.datastore.DBSchemaToDatastoreConf -tables table_1,table_2 -user someuser -password 'deegreetest' -driver org.postgresql.Driver -url  jdbc:postgresql://server:5432/someDB -output ../../conf/wms/featuretypes/featuretypedefinition.xsd

# SCRIPT BEGIN

#java -classpath ../../lib/deegree2.jar org.deegree.tools.datastore.DBSchemaToDatastoreConf -tables sgid024_springs -user deegreetest -password 'deegreetest' -driver org.postgresql.Driver -url  jdbc:postgresql://localhost:5432/deegreetest -output ../../conf/wms/featuretypes/sgid024_springs.xsd
#java -classpath ../../lib/deegree2.jar org.deegree.tools.datastore.DBSchemaToDatastoreConf -tables sgid024_stateboundary -user deegreetest -password 'deegreetest' -driver org.postgresql.Driver -url  jdbc:postgresql://localhost:5432/deegreetest -output ../../conf/wms/featuretypes/sgid024_stateboundary.xsd
#java -classpath ../../lib/deegree2.jar org.deegree.tools.datastore.DBSchemaToDatastoreConf -tables sgid100_airports -user deegreetest -password 'deegreetest' -driver org.postgresql.Driver -url  jdbc:postgresql://localhost:5432/deegreetest -output ../../conf/wms/featuretypes/sgid100_airports.xsd
#java -classpath ../../lib/deegree2.jar org.deegree.tools.datastore.DBSchemaToDatastoreConf -tables sgid100_lakesdlg100 -user deegreetest -password 'deegreetest' -driver org.postgresql.Driver -url  jdbc:postgresql://localhost:5432/deegreetest -output ../../conf/wms/featuretypes/sgid100_lakesdlg100.xsd
#java -classpath ../../lib/deegree2.jar org.deegree.tools.datastore.DBSchemaToDatastoreConf -tables sgid100_railroadsdlg100 -user deegreetest -password 'deegreetest' -driver org.postgresql.Driver -url  jdbc:postgresql://localhost:5432/deegreetest -output ../../conf/wms/featuretypes/sgid100_railroadsdlg100.xsd
#java -classpath ../../lib/deegree2.jar org.deegree.tools.datastore.DBSchemaToDatastoreConf -tables sgid100_railroadstiger1990 -user deegreetest -password 'deegreetest' -driver org.postgresql.Driver -url  jdbc:postgresql://localhost:5432/deegreetest -output ../../conf/wms/featuretypes/sgid100_railroadstiger1990.xsd
#java -classpath ../../lib/deegree2.jar org.deegree.tools.datastore.DBSchemaToDatastoreConf -tables sgid100_roadsdlg100 -user deegreetest -password 'deegreetest' -driver org.postgresql.Driver -url  jdbc:postgresql://localhost:5432/deegreetest -output ../../conf/wms/featuretypes/sgid100_roadsdlg100.xsd
#java -classpath ../../lib/deegree2.jar org.deegree.tools.datastore.DBSchemaToDatastoreConf -tables sgid500_contours1000ft -user deegreetest -password 'deegreetest' -driver org.postgresql.Driver -url  jdbc:postgresql://localhost:5432/deegreetest -output ../../conf/wms/featuretypes/sgid500_contours1000ft.xsd
#java -classpath ../../lib/deegree2.jar org.deegree.tools.datastore.DBSchemaToDatastoreConf -tables sgid500_contours2500ft -user deegreetest -password 'deegreetest' -driver org.postgresql.Driver -url  jdbc:postgresql://localhost:5432/deegreetest -output ../../conf/wms/featuretypes/sgid500_contours2500ft.xsd
#java -classpath ../../lib/deegree2.jar org.deegree.tools.datastore.DBSchemaToDatastoreConf -tables sgid500_contours500ft -user deegreetest -password 'deegreetest' -driver org.postgresql.Driver -url  jdbc:postgresql://localhost:5432/deegreetest -output ../../conf/wms/featuretypes/sgid500_contours500ft.xsd
#java -classpath ../../lib/deegree2.jar org.deegree.tools.datastore.DBSchemaToDatastoreConf -tables sgid500_dominantvegetation -user deegreetest -password 'deegreetest' -driver org.postgresql.Driver -url  jdbc:postgresql://localhost:5432/deegreetest -output ../../conf/wms/featuretypes/sgid500_dominantvegetation.xsd
#java -classpath ../../lib/deegree2.jar org.deegree.tools.datastore.DBSchemaToDatastoreConf -tables sgid500_energyresourcespoly -user deegreetest -password 'deegreetest' -driver org.postgresql.Driver -url  jdbc:postgresql://localhost:5432/deegreetest -output ../../conf/wms/featuretypes/sgid500_energyresourcespoly.xsd
#java -classpath ../../lib/deegree2.jar org.deegree.tools.datastore.DBSchemaToDatastoreConf -tables sgid500_roadsdlg500 -user deegreetest -password 'deegreetest' -driver org.postgresql.Driver -url  jdbc:postgresql://localhost:5432/deegreetest -output ../../conf/wms/featuretypes/sgid500_roadsdlg500.xsd
#java -classpath ../../lib/deegree2.jar org.deegree.tools.datastore.DBSchemaToDatastoreConf -tables sgid500_zipcodes -user deegreetest -password 'deegreetest' -driver org.postgresql.Driver -url  jdbc:postgresql://localhost:5432/deegreetest -output ../../conf/wms/featuretypes/sgid500_zipcodes.xsd
#java -classpath ../../lib/deegree2.jar org.deegree.tools.datastore.DBSchemaToDatastoreConf -tables sgid100_countyboundaries_edited -user deegreetest -password 'deegreetest' -driver org.postgresql.Driver -url  jdbc:postgresql://localhost:5432/deegreetest -output ../../conf/wms/featuretypes/sgid100_countyboundaries_edited.xsd
#java -classpath ../../lib/deegree2.jar org.deegree.tools.datastore.DBSchemaToDatastoreConf -tables sgid024_municipalities2004_edited -user deegreetest -password 'deegreetest' -driver org.postgresql.Driver -url  jdbc:postgresql://localhost:5432/deegreetest -output ../../conf/wms/featuretypes/sgid024_municipalities2004_edited.xsd




#java -classpath ../../lib/deegree2.jar org.deegree.tools.datastore.DBSchemaToDatastoreConf -tables table_1,table_2 -user someuser -password 'deegreetest' -driver org.postgresql.Driver -url  jdbc:postgresql://server:5432/someDB -output /tmp/datastore.xsd
