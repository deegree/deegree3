-- This file is part of deegree, for copyright/license information, please visit http://www.deegree.org/license.
-- This script drops the database tables for the philosopher example.
DROP INDEX COUNTRY_SPATIAL_IDX;
SELECT DropGeometryColumn ('','country', 'geom');
DROP TABLE SUBJECT CASCADE;
DROP TABLE IS_AUTHOR_OF CASCADE;
DROP TABLE BOOK CASCADE;
DROP TABLE PHILOSOPHER CASCADE;
DROP TABLE IS_FRIEND_OF CASCADE;
DROP TABLE PLACE CASCADE;
DROP TABLE COUNTRY CASCADE;
DROP SEQUENCE FID_seq;
