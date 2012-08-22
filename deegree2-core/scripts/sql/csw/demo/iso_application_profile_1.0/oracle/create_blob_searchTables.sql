DROP TABLE CQP_Main CASCADE CONSTRAINT PURGE;
DROP TABLE CQP_BBOX  CASCADE CONSTRAINT PURGE;
DROP TABLE CQP_SpecificationDate CASCADE CONSTRAINT PURGE;
DROP TABLE CQP_DomainConsistency CASCADE CONSTRAINT PURGE;


CREATE TABLE CQP_Main ( 
    ID number(12),
    subject varchar2(2000 char),
    title varchar2(200 char),
    abstract varchar2(4000 char),
    anytext varchar2(4000 char),
    identifier varchar2(250 char),
    modified TIMESTAMP,
    type varchar2(50 char),
    beginExtent TIMESTAMP,
    endExtent TIMESTAMP,
    revisionDate TIMESTAMP,
    creationDate TIMESTAMP,
    publicationDate TIMESTAMP,
    alternateTitle varchar2(400 char),
    resourceIdentifier varchar2(100 char),
    resourceLanguage varchar2(40 char),
    geographicDescripionCode varchar2(4000 char),
    serviceType varchar2(50 char),
    topicCategory varchar2(4000 char),
    parentId varchar2(150 char),
    language varchar2(100 char),
    lineage varchar2(4000 char),
    condAppToAccAndUse varchar2(2000 char),
    accessConstraints varchar2(2000 char),
    otherConstraints varchar2(2000 char),
    classification varchar2(2000 char),
    couplingType varchar2(15 char),
    operation varchar2(100 char),
    operatesOn varchar2(250 char),
    operatesOnIdentifier varchar2(250 char),
    operatesOnName varchar2(400 char),
    metadataset CLOB
)NOLOGGING  NOMONITORING;

CREATE TABLE CQP_BBOX (
	ID number(12),
	FK_CQP_MAIN number(12),
	geom sdo_geometry	
)NOLOGGING  NOMONITORING;

CREATE TABLE CQP_DomainConsistency ( 
	ID number(12),
	FK_CQP_MAIN number(12),
	specificationTitle varchar2(200 char),
	degree CHAR
)NOLOGGING  NOMONITORING;

CREATE TABLE CQP_SpecificationDate (
	ID number(12),
	FK_CQP_DomainConsistency number(12),
	dateStamp TIMESTAMP,
	datetype varchar2(20 char)
)NOLOGGING  NOMONITORING;


DROP SEQUENCE CQP_MAIN_ID_seq;
DROP SEQUENCE CQP_BBOX_ID_seq;
DROP SEQUENCE CQP_DomainConsistency_ID_seq;
DROP SEQUENCE CQP_SpecificationDate_ID_seq;

CREATE SEQUENCE CQP_MAIN_ID_seq increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE CQP_BBOX_ID_seq increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE CQP_DomainConsistency_ID_seq increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE CQP_SpecificationDate_ID_seq increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

drop INDEX IDX_CQP_MAIN1;
drop INDEX IDX_CQP_MAIN2;
drop INDEX IDX_CQP_MAIN3;
drop INDEX IDX_CQP_MAIN4;
drop INDEX IDX_CQP_MAIN5;
drop INDEX IDX_CQP_MAIN6;
drop INDEX IDX_CQP_MAIN7;
drop INDEX IDX_CQP_MAIN8;
drop INDEX IDX_CQP_MAIN9;
drop INDEX IDX_CQP_DOMAINCONSISTENCY;
drop INDEX IDX_CQP_SPECIFICATIONDATE;

CREATE INDEX IDX_CQP_MAIN1 ON CQP_MAIN (identifier, title, abstract);
CREATE INDEX IDX_CQP_MAIN2 ON CQP_MAIN (subject, serviceType, topicCategory);
CREATE INDEX IDX_CQP_MAIN3 ON CQP_MAIN (modified, type, revisionDate, creationDate);
CREATE INDEX IDX_CQP_MAIN4 ON CQP_MAIN (geographicDescripionCode, parentId);
CREATE INDEX IDX_CQP_MAIN5 ON CQP_MAIN (language, lineage);
CREATE INDEX IDX_CQP_MAIN6 ON CQP_MAIN (condAppToAccAndUse, accessConstraints);
CREATE INDEX IDX_CQP_MAIN7 ON CQP_MAIN (otherConstraints, classification);
CREATE INDEX IDX_CQP_MAIN8 ON CQP_MAIN (couplingType, operation, operatesOn);
CREATE INDEX IDX_CQP_MAIN9 ON CQP_MAIN (operatesOnIdentifier, operatesOnName);
CREATE INDEX IDX_CQP_DOMAINCONSISTENCY ON CQP_DomainConsistency (specificationTitle, degree);
CREATE INDEX IDX_CQP_SPECIFICATIONDATE ON CQP_SpecificationDate (dateStamp, datetype);

ALTER TABLE CQP_Main ADD CONSTRAINT UQ_CQP_Main_identifier UNIQUE (identifier);
ALTER TABLE CQP_Main ADD CONSTRAINT UQ_CQP_Main_resIdentifier UNIQUE (resourceIdentifier );

DROP INDEX SPX_CQP_BBOX;
delete from USER_SDO_GEOM_METADATA where TABLE_NAME = 'CQP_BBOX' AND COLUMN_NAME = 'GEOM';
INSERT INTO USER_SDO_GEOM_METADATA (TABLE_NAME, COLUMN_NAME, DIMINFO, SRID) 
  VALUES ('CQP_BBOX', 'GEOM', 
    MDSYS.SDO_DIM_ARRAY 
    (
       MDSYS.SDO_DIM_ELEMENT('X', -180, 180, 0.0005), 
       MDSYS.SDO_DIM_ELEMENT('Y', -90, 90, 0.0005)
    ), 4978
  );
CREATE INDEX SPX_CQP_BBOX on CQP_BBOX(GEOM) INDEXTYPE is MDSYS.SPATIAL_INDEX;
commit;
