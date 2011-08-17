--  -------------------------------------------------- 
--  DBMS       : PostgreSQL 
--  -------------------------------------------------- 

SELECT DropGeometryColumn('', 'dobj_buildings','envelope');
SELECT DropGeometryColumn('', 'dobj_buildings','footprint');
SELECT DropGeometryColumn('', 'dobj_trees','envelope');
SELECT DropGeometryColumn('', 'dobj_trees','footprint');
SELECT DropGeometryColumn('', 'dobj_prototypes','envelope');
SELECT DropGeometryColumn('', 'dobj_prototypes','footprint');

DROP TABLE model_info;
DROP TABLE dobj_buildings;
DROP TABLE dobj_trees;
DROP TABLE dobj_prototypes;


--#######################
--Info table hold informations on the number of vertices ordinates of the models.
--#######################
CREATE TABLE model_info ( 
    model_type varchar(150) not null,
    ordinates integer,
    texture_ordinates integer
) WITHOUT OIDS;
ALTER TABLE model_info OWNER TO postgres;

ALTER TABLE model_info ADD CONSTRAINT PK_model_info PRIMARY KEY ( model_type );

--#######################
--Buildings
--#######################
CREATE TABLE dobj_buildings ( 
	id serial not null,
	uuid varchar(50) not null,
	name varchar(150),
	externalRef varchar(150),
	model_type varchar(150),
	data bytea,
	semanticdata bytea,
	lastupdate timestamp
) WITHOUT OIDS;
ALTER TABLE dobj_buildings OWNER TO postgres;

SELECT AddGeometryColumn('', 'dobj_buildings','envelope', 31466,'POLYGON',3);
SELECT AddGeometryColumn('', 'dobj_buildings','footprint', 31466,'POLYGON',2);

ALTER TABLE dobj_buildings ADD CONSTRAINT PK_dobj_buildings PRIMARY KEY (ID);
--building indizes
CREATE INDEX IDX_buildings_id ON dobj_buildings (id);
CREATE INDEX idx_buildings_uuid ON dobj_buildings (uuid);
CREATE INDEX IDX_buildings_name ON dobj_buildings (name);
CREATE INDEX IDX_buildings_type ON dobj_buildings (model_type);
-- spatial index
CREATE INDEX spx_buildings_envelope ON dobj_buildings USING gist( envelope );
CREATE INDEX spx_buildings_footprint ON dobj_buildings USING gist( footprint );



--#######################
-- Trees 
--#######################
CREATE TABLE dobj_trees ( 
	id serial not null,
	uuid varchar(50) not null,
	name varchar(150),
	externalRef varchar(150),
	model_type varchar(150),
	data bytea,
	semanticdata bytea,
	lastupdate timestamp
) WITHOUT OIDS;
ALTER TABLE dobj_trees OWNER TO postgres;

SELECT AddGeometryColumn('', 'dobj_trees','envelope', 31466,'POLYGON',3);
SELECT AddGeometryColumn('', 'dobj_trees','footprint', 31466,'POLYGON',2);

ALTER TABLE dobj_trees ADD CONSTRAINT PK_dobj_tree PRIMARY KEY (ID);
--trees indizes
CREATE INDEX idx_trees_id ON dobj_trees (id);
CREATE INDEX idx_trees_uuid ON dobj_trees (uuid);
CREATE INDEX idx_trees_name ON dobj_trees (name);
CREATE INDEX idx_trees_type ON dobj_trees (model_type);

-- spatial index
CREATE INDEX spx_trees_envelope ON dobj_trees USING gist( envelope );
CREATE INDEX spx_trees_footprint ON dobj_trees USING gist( footprint );


--####################### 
-- Prototypes are buildings which can be refered to.
--#######################
CREATE TABLE dobj_prototypes ( 
	id serial not null,
	uuid varchar(50) not null,
	name varchar(150),
	externalRef varchar(150),
	model_type varchar(150),
	data bytea,
	semanticdata bytea,
	lastupdate timestamp
) WITHOUT OIDS;
ALTER TABLE dobj_prototypes OWNER TO postgres;

SELECT AddGeometryColumn('', 'dobj_prototypes','envelope', 31466,'POLYGON',3);
SELECT AddGeometryColumn('', 'dobj_prototypes','footprint', 31466,'POLYGON',2);

ALTER TABLE dobj_prototypes ADD CONSTRAINT PK_dobj_prototype PRIMARY KEY (ID);

-- normal index
CREATE INDEX idx_prototypes_id ON dobj_prototypes (id);
CREATE INDEX idx_prototypes_uuid ON dobj_prototypes (uuid);
CREATE INDEX idx_prototypes_name ON dobj_prototypes (name);
CREATE INDEX idx_prototypes_type ON dobj_prototypes (model_type);
--spatial index
CREATE INDEX spx_prototypes_envelope ON dobj_prototypes USING gist( envelope );
CREATE INDEX spx_prototypes_footprint ON dobj_prototypes USING gist( footprint );
