-- This file is part of deegree, for copyright/license information, please visit http://www.deegree.org/license.
-- Generates the tables for feature type si_gazetteer
drop table si_gazetteer;
CREATE TABLE si_gazetteer
(
  gazetteer_id int4,
  identifier varchar,
  scope varchar,
  territoryofuse int4,
  coordinatesystem varchar,
  isglobal bool,
  custodian varchar(2) DEFAULT '01'::character varying
) 
WITHOUT OIDS;
INSERT INTO si_gazetteer (gazetteer_id, identifier, scope, territoryofuse, coordinatesystem, isglobal, custodian) 
(select 1, 'My Gazetteer ', 'Gazetteer to search for several sites', 1, 'EPSG:26912', true, '01');

drop table si_geographicextent;
CREATE TABLE si_geographicextent
(
  id serial NOT NULL,
  description varchar,
  reference varchar,
  westboundlongitude float8,
  eastboundlongitude float8,
  southboundlatitude float8,
  northboundlatitude float8
) 
WITHOUT OIDS;
--inserts the min max lat lon BBox in WGS84  of the merged counties
INSERT INTO si_geographicextent (id, description, reference, westboundlongitude, eastboundlongitude, southboundlatitude, northboundlatitude) (select 1, 'Description', 'Reference', Xmin(transform(multi(geomunion(geographicextent)),'4326')),Xmax(transform(multi(geomunion(geographicextent)),'4326')),Ymin(transform(multi(geomunion(geographicextent)),'4326')), Ymax(transform(multi(geomunion(geographicextent)),'4326')) from counties);

drop table si_locationtype;
CREATE TABLE si_locationtype
(
  id int4,
  name varchar,
  theme varchar,
  srs_id int4,
  gaz_id int4,
  identifier varchar,
  definition varchar,
  "owner" varchar(2),
  territoryofuse int4
) 
WITHOUT OIDS;
INSERT INTO si_locationtype (id, name, theme, srs_id, gaz_id, identifier, definition, "owner", territoryofuse) (select 1, 'municipalities', 'Municipalities of Utah', 1, 1, 'name', 'Some kind of definition for Municipalities', '01', 1);
INSERT INTO si_locationtype (id, name, theme, srs_id, gaz_id, identifier, definition, "owner", territoryofuse) (select 2, 'counties', 'Counties of Utah', 1, 1, 'name', 'Some kind of definition for Counties', '01', 1);


drop table si_responsibleparty;
CREATE TABLE si_responsibleparty
(
  name varchar,
  role varchar,
  id varchar(2)
) 
WITHOUT OIDS;
INSERT INTO si_responsibleparty (name, role, id) (select 'name of institution', 'Owner of data', '01');

drop table si_spatialreferencesystem;
CREATE TABLE si_spatialreferencesystem
(
  id serial NOT NULL,
  name varchar,
  domainofvalidity int4,
  theme varchar,
  overallowner varchar(2)
) 
WITHOUT OIDS;
INSERT INTO si_spatialreferencesystem (id, name, domainofvalidity, theme, overallowner) (select 1, 'A', 1, 'Administrative Boundary Features', '01');
