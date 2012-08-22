#!/bin/sh
# This file is part of deegree, for copyright/license information, please visit http://www.deegree.org/license.
# This script sets up the needed database tables for the gazetteer example
shp2pgsql -s 26912 -c ../../data/utah/vector/SGID024_Municipalities2004_edited imp_municipalities > ../../data/sql/imp_municipalities.sql
psql -f ../../data/sql/imp_municipalities.sql -d deegreetest -U deegreetest
shp2pgsql -s 26912  -c ../../data/utah/vector/SGID100_CountyBoundaries_edited imp_counties > ../../data/sql/imp_counties.sql
psql -f ../../data/sql/imp_counties.sql -d deegreetest -U deegreetest
psql -d deegreetest -U deegreetest -f ../sql/gazetteer/02_postgis_counties_create-table.sql
psql -d deegreetest -U deegreetest -f ../sql/gazetteer/02_postgis_municipalities_create-table.sql
psql -d deegreetest -U deegreetest -f ../sql/gazetteer/03_postgis_counties_insert-and-update.sql
psql -d deegreetest -U deegreetest -f ../sql/gazetteer/03_postgis_municipalities_insert-and-update.sql
psql -d deegreetest -U deegreetest -f ../sql/gazetteer/04_postgis_counties_create-index.sql
psql -d deegreetest -U deegreetest -f ../sql/gazetteer/04_postgis_municipalities_create-index.sql
psql -d deegreetest -U deegreetest -f ../sql/gazetteer/05_create_tables_for_ft_si_gazeteer_metadata.sql