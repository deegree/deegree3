CREATE TABLE harvestsource ( 
	ID NUMBER(38) NOT NULL,
	source VARCHAR2(500 char) NOT NULL,
	harvestinterval NUMBER(38) NOT NULL,
	lastTimeHarvested DATE,
	nextHartvestTime DATE NOT NULL,
	status NUMBER(1),
	sourceType VARCHAR2(50 char) NOT NULL,
	forceharvesting NUMBER(1) DEFAULT 0 NOT NULL
) NOLOGGING  NOMONITORING;

CREATE TABLE jt_source_responsehandler ( 
	fk_harvestsource NUMBER(38) NOT NULL,
	fk_responsehandler NUMBER(38) NOT NULL
) NOLOGGING  NOMONITORING;

CREATE TABLE metadatacache ( 
	ID NUMBER(38) NOT NULL,
	fk_harvestsource NUMBER(38) NOT NULL,
	fileidentifier VARCHAR2(150 char) NOT NULL,
	datestamp DATE NOT NULL
) NOLOGGING  NOMONITORING;


CREATE TABLE responsehandler ( 
	ID NUMBER(38) NOT NULL,
	address VARCHAR2(500 char) NOT NULL,
	isMailAddress NUMBER(1) NOT NULL
) NOLOGGING  NOMONITORING;

CREATE SEQUENCE harvestsource_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE SEQUENCE metadatacache_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE SEQUENCE responsehandler_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;



ALTER TABLE harvestsource ADD CONSTRAINT PK_harvestsource  PRIMARY KEY (ID) ;
ALTER TABLE metadatacache ADD CONSTRAINT PK_metadatacache  PRIMARY KEY (ID) ;
ALTER TABLE responsehandler ADD CONSTRAINT PK_responsehandler  PRIMARY KEY (ID) ;

commit;
