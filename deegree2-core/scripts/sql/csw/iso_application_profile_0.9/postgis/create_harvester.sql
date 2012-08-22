ALTER TABLE jt_source_responsehandler DROP CONSTRAINT FK_jt_source_responsehandler_harvestsource;
ALTER TABLE jt_source_responsehandler DROP CONSTRAINT FK_jt_source_responsehandler_responsehandler;
ALTER TABLE metadatacache DROP CONSTRAINT FK_metadatacache_harvestsource;

DROP TABLE harvestsource;
DROP TABLE jt_source_responsehandler;
DROP TABLE metadatacache;
DROP TABLE responsehandler;

CREATE TABLE harvestsource ( 
	ID integer DEFAULT NEXTVAL('harvestsource_ID_seq'::TEXT) NOT NULL,
	source varchar(500) NOT NULL,
	harvestinterval int8 NOT NULL,
	lastTimeHarvested timestamp,
	nextHartvestTime timestamp NOT NULL,
	status boolean,
	sourceType varchar(50) NOT NULL
) ;

CREATE TABLE jt_source_responsehandler ( 
	fk_harvestsource integer NOT NULL,
	fk_responsehandler integer NOT NULL
) ;

CREATE TABLE metadatacache ( 
	ID integer DEFAULT NEXTVAL('metadatacache_ID_seq'::TEXT) NOT NULL,
	fk_harvestsource integer NOT NULL,
	fileidentifier varchar(150) NOT NULL,
	datestamp timestamp NOT NULL
) ;

CREATE TABLE responsehandler ( 
	ID integer DEFAULT NEXTVAL('responsehandler_ID_seq'::TEXT) NOT NULL,
	address varchar(500) NOT NULL,
	isMailAddress boolean NOT NULL
) ;


ALTER TABLE harvestsource ADD CONSTRAINT PK_harvestsource  PRIMARY KEY (ID);

ALTER TABLE metadatacache ADD CONSTRAINT PK_metadatacache PRIMARY KEY (ID);

ALTER TABLE responsehandler ADD CONSTRAINT PK_responsehandler PRIMARY KEY (ID);

ALTER TABLE harvestsource
	ADD CONSTRAINT UQ_harvestsource_source UNIQUE (source);
	
ALTER TABLE responsehandler
	ADD CONSTRAINT UQ_responsehandler_ID UNIQUE (ID);


DROP SEQUENCE harvestsource_ID_seq;

DROP SEQUENCE metadatacache_ID_seq;

DROP SEQUENCE responsehandler_ID_seq;

CREATE SEQUENCE harvestsource_ID_seq;

CREATE SEQUENCE metadatacache_ID_seq;

CREATE SEQUENCE responsehandler_ID_seq;

ALTER TABLE jt_source_responsehandler ADD CONSTRAINT FK_jt_source_responsehandler_harvestsource 
	FOREIGN KEY (fk_harvestsource) REFERENCES harvestsource (ID);

ALTER TABLE jt_source_responsehandler ADD CONSTRAINT FK_jt_source_responsehandler_responsehandler 
	FOREIGN KEY (fk_responsehandler) REFERENCES responsehandler (ID);

ALTER TABLE metadatacache ADD CONSTRAINT FK_metadatacache_harvestsource 
	FOREIGN KEY (fk_harvestsource) REFERENCES harvestsource (ID);
