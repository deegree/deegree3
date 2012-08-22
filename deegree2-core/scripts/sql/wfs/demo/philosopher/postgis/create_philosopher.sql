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
