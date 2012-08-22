#!/bin/sh
# This file is part of deegree, for copyright/license information, please visit http://www.deegree.org/license.
# This tool sets up the database tables for the digitizer.
psql -d deegreetest -U deegreetest -f ../sql/create_digitizer_table_postgis.sql

