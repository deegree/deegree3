DROP TABLE harvestsource CASCADE CONSTRAINTS ;
DROP TABLE jt_source_responsehandler CASCADE CONSTRAINTS ;
DROP TABLE metadatacache CASCADE CONSTRAINTS ;
DROP TABLE responsehandler CASCADE CONSTRAINTS ;

DROP SEQUENCE harvestsource_ID_SEQ;
DROP SEQUENCE metadatacache_ID_SEQ;
DROP SEQUENCE responsehandler_ID_SEQ;


CREATE TABLE harvestsource ( 
	ID NUMBER(38) NOT NULL,
	source VARCHAR2(500) NOT NULL,
	harvestinterval NUMBER(38) NOT NULL,
	lastTimeHarvested DATE,
	nextHartvestTime DATE NOT NULL,
	status NUMBER(1),
	sourceType VARCHAR2(50) NOT NULL,
	forceharvesting NUMBER(1) DEFAULT 0 NOT NULL
) NOLOGGING  NOMONITORING;

CREATE TABLE jt_source_responsehandler ( 
	fk_harvestsource NUMBER(38) NOT NULL,
	fk_responsehandler NUMBER(38) NOT NULL
) NOLOGGING  NOMONITORING;

CREATE TABLE metadatacache ( 
	ID NUMBER(38) NOT NULL,
	fk_harvestsource NUMBER(38) NOT NULL,
	fileidentifier VARCHAR2(150) NOT NULL,
	datestamp DATE NOT NULL
) NOLOGGING  NOMONITORING;


CREATE TABLE responsehandler ( 
	ID NUMBER(38) NOT NULL,
	address VARCHAR2(500) NOT NULL,
	isMailAddress NUMBER(1) NOT NULL
) NOLOGGING  NOMONITORING;

CREATE SEQUENCE harvestsource_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE OR REPLACE TRIGGER SET_harvestsource_id
BEFORE INSERT
ON harvestsource
FOR EACH ROW
BEGIN
  SELECT harvestsource_id_SEQ.NEXTVAL
  INTO :NEW.id
  FROM DUAL;
END;
/

CREATE SEQUENCE metadatacache_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE OR REPLACE TRIGGER SET_metadatacache_id
BEFORE INSERT
ON metadatacache
FOR EACH ROW
BEGIN
  SELECT metadatacache_id_SEQ.NEXTVAL
  INTO :NEW.id
  FROM DUAL;
END;
/

CREATE SEQUENCE responsehandler_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE OR REPLACE TRIGGER SET_responsehandler_id
BEFORE INSERT
ON responsehandler
FOR EACH ROW
BEGIN
  SELECT responsehandler_id_SEQ.NEXTVAL
  INTO :NEW.id
  FROM DUAL;
END;
/

ALTER TABLE harvestsource ADD CONSTRAINT PK_harvestsource  PRIMARY KEY (ID) ;
ALTER TABLE metadatacache ADD CONSTRAINT PK_metadatacache  PRIMARY KEY (ID) ;
ALTER TABLE responsehandler ADD CONSTRAINT PK_responsehandler  PRIMARY KEY (ID) ;


ALTER TABLE harvestsource ADD CONSTRAINT UQ_harvestsource_ID UNIQUE (ID) ;
ALTER TABLE harvestsource ADD CONSTRAINT UQ_harvestsource_source UNIQUE (harvestinterval) ;
ALTER TABLE responsehandler ADD CONSTRAINT UQ_responsehandler_ID UNIQUE (ID) ;


