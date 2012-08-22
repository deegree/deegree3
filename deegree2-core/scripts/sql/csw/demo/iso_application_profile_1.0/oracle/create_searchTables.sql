CREATE TABLE CQP_Main ( 
    ID number(12),
    FK_METADATA number(12), 
    subject varchar2(2000 char),
    title varchar2(200 char),
    abstract varchar2(4000 char),
    anytext varchar2(4000 char),
    identifier varchar2(250 char),
    modified TIMESTAMP,
    type varchar2(50 char),
    revisionDate TIMESTAMP,
    creationDate TIMESTAMP,
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
    operatesOnName varchar2(400 char)
)NOLOGGING  NOMONITORING;

CREATE TABLE CQP_PublicationDate (
	ID number (12),
	FK_CQP_MAIN number(12),
	publicationDate TIMESTAMP
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

CREATE SEQUENCE CQP_MAIN_ID_seq increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE SEQUENCE CQP_BBOX_ID_seq increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE SEQUENCE CQP_PublicationDate_ID_seq increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE SEQUENCE CQP_DomainConsistency_ID_seq increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE SEQUENCE CQP_SpecificationDate_ID_seq increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

commit;
