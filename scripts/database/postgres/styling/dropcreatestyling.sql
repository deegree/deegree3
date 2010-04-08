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

-- sequences
create sequence layers_seq;
create sequence styles_seq;
create sequence fills_seq;
create sequence strokes_seq;
create sequence graphics_seq;
create sequence points_seq;
create sequence lines_seq;
create sequence polygons_seq;

-- main tables
create table layers (
id integer not null default nextval('layers_seq'),
name varchar,
title varchar not null,
connectionid varchar,
sourcetable varchar,
sourcequery varchar,
crs varchar,
bboxquery varchar,
namespace varchar,
symbolcodes varchar,
symbolfield varchar
);

create table styles (
id integer not null default nextval('layers_seq'),
type varchar, -- should be one of 'point', 'line', 'polygon'
name varchar,
fk integer,
minscale float,
maxscale float,
sld varchar
);

-- component tables
create table fills (
id integer not null default nextval('strokes_seq'),
color varchar,
graphic_id integer
);

create table strokes (
id integer not null default nextval('strokes_seq'),
color varchar,
width float,
linejoin varchar,
linecap varchar,
dasharray varchar,
dashoffset float,
stroke_graphic_id integer,
fill_graphic_id integer,
strokegap float,
strokeinitialgap float,
positionpercentage float
);

create table graphics (
id integer not null default nextval('graphics_seq'),
size float,
rotation float,
anchorx float,
anchory float,
displacementx float,
displacementy float,
wellknownname varchar,
svg varchar,
base64raster varchar,
fill_id integer,
stroke_id integer
);

-- symbolizer tables
create table points (
id integer not null default nextval('points_seq'),
uom varchar,
graphic_id integer
);

create table lines (
id integer not null default nextval('lines_seq'),
uom varchar,
stroke_id integer,
perpendicularoffset float
);

create table polygons (
id integer not null default nextval('polygons_seq'),
uom varchar,
fill_id integer,
stroke_id integer,
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
