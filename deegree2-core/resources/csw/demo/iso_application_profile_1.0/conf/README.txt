deegree CSW ${deegree.version} - demo 
CSW 2.0.2 profile
----------

Complete CSW example configuration. Uses PostGIS as backend. The necessary DDL (table generation) 
scripts are in "/scripts/sql/csw/iso_application_profile_1.0/postgis". 
* Don't forget to edit db connection information in "csw/featuretypes/csw_postgres.xsd" *. 

It's also possible to use Oracle as backende - the required scripts to create the tables can be 
found in "/scripts/sql/csw/oracle". 
* Don't forget to adjust the db connection in "csw/featuretypes/csw_oracle.xsd.ignore" * 
Afterwards activate this file by renaming it to csw_oracle.xsd (and renaming the postgis version to .ignore).

Bonn, ${release.date} 