rem This file is part of deegree, for copyright/license information, please visit http://www.deegree.org/license.
rem  This script creates a spatially enabled Postgis Database. You might have to adapt the paths to the
rem  Postgresql directory to your system.
rem  please specify 'deegreetest' for password if asked for it.
createuser -s -d deegreetest -P -U postgres
createdb deegreetest -U deegreetest -E UTF8 -T template0
createlang -U deegreetest plpgsql deegreetest
psql -U deegreetest -d deegreetest -f C:\Programme\PostgreSQL\8.1\share\contrib\lwpostgis.sql
psql -U deegreetest -d deegreetest -f C:\Programme\PostgreSQL\8.1\share\contrib\spatial_ref_sys.sql