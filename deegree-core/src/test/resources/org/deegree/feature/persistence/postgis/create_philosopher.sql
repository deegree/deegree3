-- This file is part of deegree, for copyright/license information, please visit http://www.deegree.org/license.
-- This script creates the database tables for the philosopher example.
CREATE TABLE COUNTRY (
    ID              INT8          PRIMARY KEY, 
    NAME            VARCHAR (100)
);
SELECT ADDGEOMETRYCOLUMN('','country','geom','4326','MULTIPOLYGON',2);
ALTER TABLE COUNTRY ADD CONSTRAINT COUNTRY_GEOM_CHECK CHECK (isvalid(geom));
CREATE INDEX COUNTRY_SPATIAL_IDX ON COUNTRY USING GIST ( geom GIST_GEOMETRY_OPS );
VACUUM ANALYZE COUNTRY;

CREATE TABLE PLACE (
    ID              INT8          PRIMARY KEY, 
    COUNTRY_ID      INT8          REFERENCES COUNTRY, 
    NAME            VARCHAR (100)
);

CREATE TABLE PHILOSOPHER (
    ID              INT8           PRIMARY KEY, 
    NAME            VARCHAR (100),
    SEX             CHAR(1),
    DATE_OF_BIRTH   DATE,
    PLACE_OF_BIRTH  INT8           REFERENCES PLACE,
    DATE_OF_DEATH   DATE,
    PLACE_OF_DEATH  INT8           REFERENCES PLACE
);

CREATE TABLE IS_FRIEND_OF (
    PHILOSOPHER1_ID INT8,
    PHILOSOPHER2_ID INT8,
    PRIMARY KEY  (PHILOSOPHER1_ID, PHILOSOPHER2_ID)
);

CREATE TABLE SUBJECT (
    PHILOSOPHER_ID  INT8           REFERENCES PHILOSOPHER,
    NAME            VARCHAR (100),
    PRIMARY KEY  (PHILOSOPHER_ID, NAME)
);

CREATE TABLE BOOK (
    ID              INT8          PRIMARY KEY, 
    TITLE           VARCHAR (200),
    PUB_DATE        DATE
);

CREATE TABLE IS_AUTHOR_OF (
    PHILOSOPHER_ID INT8,
    BOOK_ID INT8,
    PRIMARY KEY  (PHILOSOPHER_ID, BOOK_ID)
);

CREATE SEQUENCE FID_seq;


CREATE TABLE feature_types (
    id smallint PRIMARY KEY,
    qname text NOT NULL
);
COMMENT ON TABLE feature_types IS 'Ids and bboxes of concrete feature types';

SELECT ADDGEOMETRYCOLUMN('public', 'feature_types','bbox','4326','GEOMETRY',2);
ALTER TABLE feature_types ADD CONSTRAINT feature_types_check_bbox CHECK (isvalid(bbox));
/* (no spatial index needed, as envelope is only used for keeping track of feature type extents) */
INSERT INTO feature_types (id,qname) VALUES (0,'{http://www.deegree.org/app}Book');
INSERT INTO feature_types (id,qname) VALUES (1,'{http://www.deegree.org/app}Country');
INSERT INTO feature_types (id,qname) VALUES (2,'{http://www.deegree.org/app}Place');
INSERT INTO feature_types (id,qname) VALUES (3,'{http://www.deegree.org/app}Philosopher');

CREATE TABLE gml_objects (
    id SERIAL PRIMARY KEY,
    gml_id text UNIQUE NOT NULL,
    gml_description text,
    ft_type smallint REFERENCES feature_types,
    binary_object bytea
);
COMMENT ON TABLE gml_objects IS 'All objects (features and geometries)';
SELECT ADDGEOMETRYCOLUMN('public', 'gml_objects','gml_bounded_by','-1','GEOMETRY',2);
ALTER TABLE gml_objects ADD CONSTRAINT gml_objects_geochk CHECK (isvalid(gml_bounded_by));
CREATE INDEX gml_objects_sidx ON gml_objects USING GIST ( gml_bounded_by GIST_GEOMETRY_OPS );

CREATE TABLE gml_names (
    gml_object_id integer REFERENCES GML_OBJECTS,
    name text NOT NULL,
    codespace text,
    prop_idx smallint NOT NULL
);
