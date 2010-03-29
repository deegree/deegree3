--  -------------------------------------------------- 
--  DBMS       : PostgreSQL 
--  -------------------------------------------------- 

-- main tables
drop table layers;
drop table styles;

-- component tables
drop table fills;
drop table strokes;
drop table graphics;

-- symbolizer tables
drop table points;
drop table lines;
drop table polygons;

-- main tables
create table layers (
id varchar,
name varchar,
title varchar not null,
connectionid varchar,
sourcetable varchar,
sourcequery varchar,
symbolcodes varchar,
symbolfield varchar
);

create table styles (
id varchar,
type varchar, -- should be one of 'point', 'line', 'polygon'
fk varchar,
minscale float,
maxscale float,
sld varchar
);

-- component tables
create table fills (
id varchar,
color varchar,
graphic_id varchar
);

create table strokes (
id varchar,
color varchar,
width float,
linejoin varchar,
linecap varchar,
dasharray varchar,
dashoffset float,
stroke_graphic_id varchar,
fill_graphic_id varchar,
strokegap float,
strokeinitialgap float,
positionpercentage float
);

create table graphics (
id varchar,
size float,
rotation float,
anchorx float,
anchory float,
displacementx float,
displacementy float,
wellknownname varchar,
svg varchar,
base64raster varchar,
fill_id varchar,
stroke_id varchar
);

-- symbolizer tables
create table points (
id varchar,
uom varchar,
graphic_id varchar
);

create table lines (
id varchar,
uom varchar,
stroke_id varchar,
perpendicularoffset float
);

create table polygons (
id varchar,
uom varchar,
fill_id varchar,
stroke_id varchar,
displacementx float,
displacementy float,
perpendicularoffset float
);

create index pk_id_layers on layers(id);
create index pk_id_styles on styles(id);
create index pk_id_fills on fills(id);
create index pk_id_strokes on strokes(id);
create index pk_id_graphics on graphics(id);
create index pk_id_points on points(id);
create index pk_id_lines on lines(id);
create index pk_id_polygons on polygons(id);
