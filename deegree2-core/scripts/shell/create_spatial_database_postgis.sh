#!/bin/sh
# This file is part of deegree, for copyright/license information, please visit http://www.deegree.org/license.
# This script creates a spatially enabled Postgis Database. You might have to adapt the paths to the
# Postgresql bin directory to your system.
# please specify 'deegreetest' for password if asked for it.
# After creation of user deegreetest and database deegreetest with this script, please follow up with
# by executing the scripts 02_shp2pgsql.sh and 03 DBtoFeaturetypeDef.sh
createuser -s -d deegreetest -P -U postgres
createdb deegreetest -U deegreetest -E UTF8 -T template0
createlang -U deegreetest plpgsql deegreetest
psql -U deegreetest -d deegreetest -f /usr/local/pgsql/share/contrib/lwpostgis.sql
psql -U deegreetest -d deegreetest -f /usr/local/pgsql/share/contrib/spatial_ref_sys.sql