deegree WMS ${deegree.version} - demo 

This directory contains several configuration files such as 
the central wms_configuration.xml, style definitions (SLD documents),
configurations to access local WFS and WCS and XSLT scripts to 
transform GetFeatureInfo.
You find additional wms configuration files to enable WMS 1.3.0 or 
a service version to validate against the  OGC cite test engine.  


In the following, this ReadMe file describes, how to get the OpenStreetMap layers working

+++++++++++++++++++++++++++++++++++++++++
++++ OpenStreetMap Slippy Map Layers ++++
+++++++++++++++++++++++++++++++++++++++++

The OSM Slippy Map layers just need an active internet connection. They will not work behind a proxy.
If there is an active internet connection, you can activate the OSM Slippy Map Layers by deleting the commentation tags
in the wms_configuration.xml.
For further information, have a look at the deegree WMS documentation.

+++++++++++++++++++++++++++++++++++++++++
++++ OpenStreetMap Bonn & NRW Layers ++++
+++++++++++++++++++++++++++++++++++++++++

For these layers you need the geodata from OpenStreetMap.org.
How to get it, is described in the deegree wiki at : http://wiki.deegree.org/deegreeWiki/HowToSetUpDeegreeWMSWithOpenStreetMap

We used two OSM xml files for our example Layers. One for the Bonn dataset and one for the NRW dataset. The NRW file should include the data for Bonn.
So it is adequate to import the NRW file.

BoundingBox for the Bonn Dataset: 7.0137 7.2313 50.6415 50.795

BoundingBox for the NRW Dataset: 5.77 9.69 50.31 52.53

As described on the wiki page mentioned above, create a postgreSQL database with PostGIS. 
For creation of the database, the tool osm2pgsql is needed. How to get it is described in the wiki.
Before executing the command for osm2pgsql, please change the default.style file in osm2pgsql root location with the following:

enable
#way       lcn_ref      text     linear
#way       rcn_ref      text     linear
#way       ncn_ref      text     linear
#way       lcn          text     linear
#way       rcn          text     linear
#way       ncn          text     linear
#way       lwn_ref      text     linear
#way       rwn_ref      text     linear
#way       nwn_ref      text     linear
#way       lwn          text     linear
#way       rwn          text     linear
#way       nwn          text     linear
#way       route_name   text     linear

by deleting the # in every line.
and add the line

node       cuisine      text     nocache

Now the following command for osm2pgsql can be executed:
Linux: ./osm2pgsql -h localhost -P 5432 -d deegreeosm -U deegreetest -W -p osm -l -s -C 1000 -S default.style  /datadirectory/nrw.osm.bz2 
Windows: osm2pgsql.exe -h localhost -P 5432 -d deegreeosm -U deegreetest -W -p osm -l -s -C 1000 -S default.style  /datadirectory/nrw.osm.bz2 
PLEASE NOTE:
For our example it is recommended, to use the slim mode of osm2pgsql by adding "-s" to the command. 
A help for osm2pgsql is available by typing osm2pgsql -h , explaining the different command options.
The database's name should be deegreeosm; user and password deegreetest.

The next step is to run the shell/batch script "create_wmstable_from_osm.sh/bat" from /wms-root/WEB-INF/scripts/shell(batch)
Please check the source relations(please check the sql script /wms-root/WEB-INF/scripts/create_wmstable_from_osm.sql, too)  in this script before running it! 

After that, the data is represented in the postgreSQL database.

To get a better overview about the layer structure, the table below gives information about the layers and their corresponding database relations:

Bonn dataset:

Layername      | database relation  
_______________|___________________
gastrobonn     | bonn_gastro
_______________|___________________
placeofworship | bonn_placeofworship
_______________|___________________
roadbonn       | bonn_roads
_______________|___________________
expressway1b   | bonn_roads
_______________|___________________
expressway2b   | bonn_roads
_______________|___________________
motorway1b     | bonn_roads
_______________|___________________
motorway2b     | bonn_roads
_______________|___________________
roadlabelb     | bonn_roads
_______________|___________________
building       | bonn_buildings
_______________|___________________
label          | bonn_buildings
_______________|___________________


NRW Dataset:

Layername      | database relation
_______________|___________________
railwaystation | railwaystation
_______________|___________________
peak           | peak
_______________|___________________
airport        | airport
_______________|___________________
populatedplace | populatedplace
_______________|___________________
adminboundaries| adminboundary
_______________|___________________
motorway       | roads
_______________|___________________
railway        | railway
_______________|___________________
railway2       | railway
_______________|___________________
watercourse    | watercourse
_______________|___________________
bodyofwaters   | bodyofwaters
_______________|___________________
landuse        | landuse
_______________|___________________
hikingway      | hikingway
_______________|___________________
cycleway       | cycleway
_______________|___________________
 
Finally, deleting the commentation tags from the layers in the wms_configuration.xml might make the layers working.
For further information, have a look at the deegree WMS documentation.

Bonn, ${release.date}
