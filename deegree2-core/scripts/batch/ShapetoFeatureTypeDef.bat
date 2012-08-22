rem This file is part of deegree, for copyright/license information, please visit http://www.deegree.org/license.
rem You have to set several parameters to create a featuretypedefinition:
rem each row starts with a java call including several library references and method calls
rem -driver SHAPE
rem -tables ..\..\data\shapename -> path to shapefile without the extension .shp
rem -output ..\..\conf\wms\featuretypes\SGID500_Contours500Ft_shp.xsd -> output directory 
rem         and featuretype denfinition file name.
rem Have a look at the  Commented_FeaturetypeDefiniton.xsd.txt in the outputdirectory 
rem $deegree-wms$/WEB-INF/conf/wms/featuretypes/ and the Philosopher example under $deegree-wms$/
rem WEB-INF/conf/wms/featuretypes/philosopher/Philosopher.xsd for details about the featuretype definition. 

rem SCRIPT EXAMPLE

rem java -classpath ..\..\lib\deegree2.jar org.deegree.tools.datastore.DBSchemaToDatastoreConf -driver SHAPE -tables ..\..\data\utah\vector\SGID500_Contours500Ft -output ..\..\conf\wms\featuretypes\SGID500_Contours500Ft_shp.xsd

rem Be aware that by executing this commands the existing featuretype definitions will be overwritten as long as you dont change the -output filename
rem SCRIPT BEGIN

rem java -classpath ..\..\lib\deegree2.jar org.deegree.tools.datastore.DBSchemaToDatastoreConf -driver SHAPE -tables ..\..\data\utah\vector\SGID024_Springs -output ..\..\conf\wms\featuretypes\SGID024_Springs_shp.xsd
rem java -classpath ..\..\lib\deegree2.jar org.deegree.tools.datastore.DBSchemaToDatastoreConf -driver SHAPE -tables ..\..\data\utah\vector\SGID100_RailroadsDLG100 -output ..\..\conf\wms\featuretypes\SGID100_RailroadsDLG100_shp.xsd

rem SCRIPT END