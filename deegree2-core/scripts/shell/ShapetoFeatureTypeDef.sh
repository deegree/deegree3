#!/bin/sh
# This file is part of deegree, for copyright/license information, please visit http://www.deegree.org/license.
# This script generates FeatureTypeDefinitions from Shapefiles.
#You have to set several parameters to create a featuretypedefinition:
#-driver SHAPE
#-tables ..\..\data\utah\vector\shapename -> path to shapefile without the extension .shp
#-output ..\..\conf\wms\featuretypes\SGID500_Contours500Ft_shp.xsd -> output directory 
#        and featuretype denfinition file name.
#Have a look at the  Commented_FeaturetypeDefiniton.xsd.txt in the outputdirectory 
#$deegree-wms$/WEB-INF/conf/wms/featuretypes/ and the Philosopher example under $deegree-wms$/
# WEB-INF/conf/wms/featuretypes/philosopher/Philosopher.xsd for details about the featuretype definition. 

#SCRIPT EXAMPLE

#java -classpath ../../lib/deegree2.jar org.deegree.tools.datastore.DBSchemaToDatastoreConf -driver SHAPE -tables ../../data/utah/vectorSGID500_Contours500Ft -output ../../conf/wms/featuretypes/SGID500_Contours500Ft_shp.xsd

#Be aware that by executing this commands the existing featuretype definitions will be overwritten as long as you dont change the -output filename
#SCRIPT BEGIN
#java -classpath ../../lib/deegree2.jar org.deegree.tools.datastore.DBSchemaToDatastoreConf -driver SHAPE -tables ../../data/utah/vector/SGID500_Contours500Ft -output ../../conf/wms/featuretypes/SGID500_Contours500Ft_shp.xsd
#java -classpath ../../lib/deegree2.jar org.deegree.tools.datastore.DBSchemaToDatastoreConf -driver SHAPE -tables ../../data/utah/vector/SGID500_Contours1000Ft -output ../../conf/wms/featuretypes/SGID500_Contours1000Ft_shp.xsd
#java -classpath ../../lib/deegree2.jar org.deegree.tools.datastore.DBSchemaToDatastoreConf -driver SHAPE -tables ../../data/utah/vector/SGID500_Contours2500Ft -output ../../conf/wms/featuretypes/SGID500_Contours2500Ft_shp.xsd
#java -classpath ../../lib/deegree2.jar org.deegree.tools.datastore.DBSchemaToDatastoreConf -driver SHAPE -tables ../../data/utah/vector/SGID500_EnergyResourcesPoly -output ../../conf/wms/featuretypes/SGID500_EnergyResourcesPoly_shp.xsd
#java -classpath ../../lib/deegree2.jar org.deegree.tools.datastore.DBSchemaToDatastoreConf -driver SHAPE -tables ../../data/utah/vector/SGID024_Springs -output ../../conf/wms/featuretypes/SGID024_Springs_shp.xsd
#java -classpath ../../lib/deegree2.jar org.deegree.tools.datastore.DBSchemaToDatastoreConf -driver SHAPE -tables ../../data/utah/vector/SGID100_LakesDLG100 -output ../../conf/wms/featuretypes/SGID100_LakesDLG100_shp.xsd
#java -classpath ../../lib/deegree2.jar org.deegree.tools.datastore.DBSchemaToDatastoreConf -driver SHAPE -tables ../../data/utah/vector/SGID100_Airports -output ../../conf/wms/featuretypes/SGID100_Airports_shp.xsd
#java -classpath ../../lib/deegree2.jar org.deegree.tools.datastore.DBSchemaToDatastoreConf -driver SHAPE -tables ../../data/utah/vector/SGID100_RailroadsDLG100 -output ../../conf/wms/featuretypes/SGID100_RailroadsDLG100_shp.xsd
#java -classpath ../../lib/deegree2.jar org.deegree.tools.datastore.DBSchemaToDatastoreConf -driver SHAPE -tables ../../data/utah/vector/SGID100_RailroadsTIGER1990 -output ../../conf/wms/featuretypes/SGID100_RailroadsTIGER1990_shp.xsd
#java -classpath ../../lib/deegree2.jar org.deegree.tools.datastore.DBSchemaToDatastoreConf -driver SHAPE -tables ../../data/utah/vector/SGID100_RoadsDLG100 -output ../../conf/wms/featuretypes/SGID100_RoadsDLG100_shp.xsd
#java -classpath ../../lib/deegree2.jar org.deegree.tools.datastore.DBSchemaToDatastoreConf -driver SHAPE -tables ../../data/utah/vector/SGID500_DominantVegetation -output ../../conf/wms/featuretypes/SGID500_DominantVegetation_shp.xsd
#SCRIPT END
