-- This file is part of deegree, for copyright/license information, please visit http://www.deegree.org/license.
-- This script contains SQL commands to create usable database relations for the OpenStreetMap.Bonn and OpenStreetMap.NRW Layers in demo deegree-WMS
create table railwaystation as select osm_id, railway, name, way from osm_point where "railway" = 'station';
alter table railwaystation rename column way to location;
insert into geometry_columns values ('', 'public', 'railwaystation', 'location', 2, 4326, 'POINT');
CREATE INDEX idx_railwaystation ON railwaystation USING GIST (location);

create table roads as select osm_id, highway, ref, name, way from osm_line;
alter table roads rename column way to centerline; 
insert into geometry_columns values ('', 'public', 'roads', 'centerline', 2, 4326, 'LINESTRING');
alter table roads rename column highway to classification;
delete from roads where "classification" is null;
CREATE INDEX idx_roads ON roads USING GIST (centerline);

create table airport as select osm_id, aeroway, name, way from osm_point where "aeroway" = 'aerodrome';
alter table airport rename column way to location;
insert into geometry_columns values ('', 'public', 'airport', 'location', 2, 4326, 'POINT');
CREATE INDEX idx_airport ON airport USING GIST (location);

create table peak as select osm_id, "natural", tourism, name, way from osm_point where "natural" = 'peak';
alter table peak rename column way to location;
insert into geometry_columns values ('', 'public', 'peak', 'location', 2, 4326, 'POINT');
CREATE INDEX idx_peak ON peak USING GIST (location);

create table bonn_buildings as select osm_id, amenity, building, name, way from osm_polygon;
alter table bonn_buildings rename column way to geometry;
insert into geometry_columns values ('', 'public', 'bonn_buildings', 'geometry', 2, 4326, 'POLYGON');
delete from bonn_buildings where "building" is null;
select isvalid(geometry) as valid,osm_id from bonn_buildings order by valid;
delete  from bonn_buildings where isvalid(geometry) = 'f';
CREATE INDEX idx_bonn_buildings ON bonn_buildings USING GIST (geometry);

create table bonn_placeofworship as select osm_id, amenity, religion, name, way from osm_point;
alter table bonn_placeofworship rename column way to location;  
insert into geometry_columns values ('', 'public', 'bonn_placeofworship', 'location', 2, 4326, 'POINT');
delete from bonn_placeofworship where "religion" is null;
CREATE INDEX idx_bonn_placeofworship ON bonn_placeofworship USING GIST (location);

create table bonn_roads as select osm_id, highway, ref, name, way from osm_line;
alter table bonn_roads rename column way to centerline;  
insert into geometry_columns values ('', 'public', 'bonn_roads', 'centerline', 2, 4326, 'LINESTRING');
alter table bonn_roads rename column highway to classification;
delete from bonn_roads where "classification" is null;
CREATE INDEX idx_bonn_roads ON bonn_roads USING GIST (centerline);

create table bonn_gastro as select osm_id, amenity, cuisine, tourism, name, way from osm_point where "amenity" = 'restaurant' or "amenity" = 'pub' or "amenity" = 'cafe' or "amenity" = 'fastfood';
alter table bonn_gastro rename column way to location;
alter table bonn_gastro rename column amenity to type; 
insert into geometry_columns values ('', 'public', 'bonn_gastro', 'location ', 2, 4326, 'POINT');
delete from bonn_gastro where "type" is null;
CREATE INDEX idx_bonn_gastro ON bonn_gastro USING GIST (location);

create table bodyofwaters as select osm_id, "natural", tourism, name, way from osm_polygon where "natural" = 'water';
alter table bodyofwaters rename column way to geometry;  
insert into geometry_columns values ('', 'public', 'bodyofwaters', 'geometry', 2, 4326, 'POLYGON');
select isvalid(geometry) as valid,osm_id from bodyofwaters order by valid;
delete  from bodyofwaters where isvalid(geometry) = 'f';
CREATE INDEX idx_bodyofwaters ON bodyofwaters USING GIST (geometry);

create table adminboundary as select osm_id, admin_level, boundary, way from osm_line;
alter table adminboundary rename column way to centerline; 
insert into geometry_columns values ('', 'public', 'adminboundary', 'centerline', 2, 4326, 'LINESTRING');
alter table adminboundary rename column admin_level to adminLevel;
delete from adminboundary where "boundary" is null;
delete from adminboundary where "adminlevel" is null;
CREATE INDEX idx_adminboundary ON adminboundary USING GIST (centerline);

create table cycleway as select osm_id, name, ncn, ncn_ref, rcn, rcn_ref, lcn, lcn_ref, route_name, way from osm_line;
alter table cycleway rename column way to centerline;  
insert into geometry_columns values ('', 'public', 'cycleway', 'centerline', 2, 4326, 'LINESTRING');
delete from cycleway where "ncn" is null and "rcn" is null and "lcn" is null;
CREATE INDEX idx_cycleway ON cycleway USING GIST (centerline);

create table hikingway as select osm_id, name, nwn, nwn_ref, rwn, rwn_ref, lwn, lwn_ref, route_name, way from osm_line;
alter table hikingway rename column way to centerline;  
insert into geometry_columns values ('', 'public', 'hikingway', 'centerline', 2, 4326, 'LINESTRING');
delete from hikingway where "nwn" is null and "rwn" is null and "lwn" is null;
CREATE INDEX idx_hikingway ON hikingway USING GIST (centerline);

create table railway as select osm_id, name, railway, way from osm_line where "railway" = 'rail';
alter table railway rename column way to centerline;  
insert into geometry_columns values ('', 'public', 'railway', 'centerline', 2, 4326, 'LINESTRING');
delete from railway where "railway" is null;
CREATE INDEX idx_railway ON railway USING GIST (centerline);

create table populatedplace as select osm_id, name, place, way from osm_point;
alter table populatedplace rename column way to location;  
insert into geometry_columns values ('', 'public', 'populatedplace', 'location', 2, 4326, 'POINT');
delete from populatedplace where "place" is null;
CREATE INDEX idx_populatedplace ON populatedplace USING GIST (location);

create table watercourse as select osm_id, waterway, way from osm_line;
alter table watercourse rename column way to centerline;  
insert into geometry_columns values ('', 'public', 'watercourse', 'centerline', 2, 4326, 'LINESTRING');
delete from watercourse where "waterway" is null;
CREATE INDEX idx_watercourse ON watercourse USING GIST (centerline);

