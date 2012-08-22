#!/bin/sh
# This file is part of deegree, for copyright/license information, please visit http://www.deegree.org/license.
# This script executes an sql script to generate the needed database tables for the osm example layers.
psql -d deegreeosm -U deegreetest -f ../sql/create_wmstable_from_osm.sql
