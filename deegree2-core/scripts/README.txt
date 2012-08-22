deegree scripts ${deegree.version} - demo

The scripts directory offers starter scripts to some of the tools available in
the deegree project. Currently scripts for windows based systems (batch) and
linux based systems (shell) are supplied.

In most case you have to edit these scripts to adapt them to your local system.
To do so, just use a text editor such as JEdit or other. 

With some services (CSW/WFS) sql scripts are supplied which help you to create
databases directly and sometimes fill them with appropriate data.


* Common scripts for wfs/wms demos

Following scripts create featuretype definitions (GML application schemata)
which enable the access of the (local) WFS to the data source (databases or
files). 
The CRS of all the sample *.shp files and featuretypes is EPSG:26912.
Before executing the scripts, check the path's and adjust them to your local configuration.

To setup a usable wfs you should do the following steps

1) create_spatial_database_postgis:
Script containing the steps to set up a spatial postgis database.

2) shp2pgsql:
script to insert shape based data to a postgis database.

3) DBtoFeatureTypeDef:
Script to create a feature type definition from a postgis database table.

(or if you are using hsql db) 
3) HSQLDBtoFeatureTypeDef:
Script to create a feature type definition from a hsql database table.

(or if you are using shape files)
3) ShapetoFeatureTypeDef:
Script to create a feature type definition from a shape file.

IndexShapefiles:
Script that creates a spatial index on a shape file to increase speed of 
data access. In the given example files, the Geometry attribute is the unique one.


* WMS specific

AVL2SLDtool:
Script to transform an ESRI AVL style definition to SLD style. AVL and
corresponding Shape file is needed.


* WFS specific 

0) create_wfs_demo_db_postgis
calls the postgis tools to build a spatial enabled postgres db.

create_digitizer_table_postgis:
creates necessary postgis tables to support for digitizing.

* WCS specific 

The RTB (Raster Tree Builder) processes an input raster data to enable an fast
access via tiling and indexing.

rastertreebuilder-help:
calls the help of the application describing the desired input parameters.

rtb_saltlakecity:
directly processes the example data including an aerial image of saltlake city.
Use this script as template for own data.


* common tools

SupportedSRS:
Script that gives information, whether a certain CRS is supported. 


* Fill HSQL db with shape files:

Pleas note for hsql sql files, TABLE and INDEX are to be written in UPPERCASE!!!

Start the hsql database manager with:
1) java -cp ../../../../lib/hsqldb.jar org.hsqldb.util.DatabaseManager
2) Insert the content of sql/quatree_hsqldb.sql in the opening window. 
3) fill the db with data from shape files by running the script: shape2GenericSQLdatabase_hlsqldb

Please refer to all services documentation for details.
In the deegree wiki, there is a special page, 
explaining these tools (http://wiki.deegree.org/deegreeWiki/deegreeTools).

Bonn, ${release.date}
