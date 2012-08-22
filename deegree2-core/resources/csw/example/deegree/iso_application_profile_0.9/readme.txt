What's it?
----------

csw/

Complete CSW example configuration. Uses PostGIS as backend. The necessary DDL (table generation) 
scripts are in "/scripts/sql/csw/postgis". Don't forget to edit db connection information in
"csw/featuretypes/csw.xsd". 

It's also possible to use Oracle as backende - the required scripts to create the tables can be 
find in "/scripts/sql/csw/oracle". Don't forget to adjust the db connection in 
"csw/featuretypes/csw_oracle.xsd_ignore" and activate this file. 