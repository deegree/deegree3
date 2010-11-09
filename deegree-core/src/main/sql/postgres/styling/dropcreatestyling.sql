--  -------------------------------------------------- 
--  DBMS       : PostgreSQL 
--  -------------------------------------------------- 

-- indexes
drop index pk_id_layers;
drop index pk_id_styles;

drop index pk_id_fills;
drop index pk_id_strokes;
drop index pk_id_graphics;
drop index pk_id_fonts;
drop index pk_id_lineplacements;
drop index pk_id_halos;

drop index pk_id_points;
drop index pk_id_lines;
drop index pk_id_polygons;
drop index pk_id_texts;

-- main tables
drop table layers;
drop table styles;

-- component tables
drop table fills;
drop table strokes;
drop table graphics;
drop table fonts;
drop table lineplacements;
drop table halos;

-- symbolizer tables
drop table points;
drop table lines;
drop table polygons;
drop table texts;

-- drop sequences

drop sequence layers_seq;
drop sequence styles_seq;

drop sequence fills_seq;
drop sequence strokes_seq;
drop sequence graphics_seq;
drop sequence fonts_seq;
drop sequence lineplacements_seq;
drop sequence halos_seq;

drop sequence points_seq;
drop sequence lines_seq;
drop sequence polygons_seq;
drop sequence texts_seq;

-- sequences
create sequence layers_seq;
create sequence styles_seq;

create sequence fills_seq;
create sequence strokes_seq;
create sequence graphics_seq;
create sequence fonts_seq;
create sequence lineplacements_seq;
create sequence halos_seq;

create sequence points_seq;
create sequence lines_seq;
create sequence polygons_seq;
create sequence texts_seq;

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
id integer not null default nextval('styles_seq'),
type varchar, -- should be one of 'point', 'line', 'polygon'
name varchar,
fk integer,
minscale float,
maxscale float,
sld varchar
);

-- component tables
create table fills (
id integer not null default nextval('fills_seq'),
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

create table fonts (
id integer not null default nextval('fonts_seq'),
family varchar,
style varchar,
bold boolean,
size integer
);

create table lineplacements (
id integer not null default nextval('lineplacements_seq'),
perpendicularoffset float,
repeat boolean,
initialgap float,
gap float,
isaligned boolean,
generalizeline boolean
);

create table halos (
id integer not null default nextval('halos_seq'),
fill_id integer,
radius float
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

create table texts (
id integer not null default nextval('texts_seq'),
labelexpr varchar,
uom varchar,
font_id integer,
fill_id integer,
rotation float,
displacementx float,
displacementy float,
anchorx float,
anchory float,
lineplacement_id integer,
halo_id integer
);

create unique index pk_id_layers on layers(id);
create unique index pk_id_styles on styles(id);

create unique index pk_id_fills on fills(id);
create unique index pk_id_strokes on strokes(id);
create unique index pk_id_graphics on graphics(id);
create unique index pk_id_fonts on fonts(id);
create unique index pk_id_lineplacements on lineplacements(id);
create unique index pk_id_halos on halos(id);

create unique index pk_id_points on points(id);
create unique index pk_id_lines on lines(id);
create unique index pk_id_polygons on polygons(id);
create unique index pk_id_texts on texts(id);
