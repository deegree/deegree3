CREATE TABLE CQP_Main (
    ID integer DEFAULT NEXTVAL('CQP_MAIN_ID_seq'::TEXT),
	subject text,
	title varchar(200),
	abstract text,
	anyText text,
	identifier varchar(250),
	modified timestamp,
	type varchar(50),
	extentBegin timestamp,
	extentEnd timestamp,
	revisionDate timestamp,
	creationDate timestamp,
	publicationDate timestamp,
	alternateTitle text,
	resourceIdentifier text,
	resourceLanguage text,
	geographicDescripionCode text,
	serviceType varchar(50),
	topicCategory text,
	parentId varchar(150),
	language varchar(100),
	lineage text,
	condAppToAccAndUse text,
	accessConstraints text,
	otherConstraints text,
	classification text,
	couplingType text,
	operation text,
	operatesOn text,
	operatesOnIdentifier text,
	operatesOnName text,
	metadataset text
);

CREATE TABLE CQP_BBOX ( 
	FK_CQP_MAIN integer,
	ID integer DEFAULT NEXTVAL('CQP_BBOX_ID_seq'::TEXT)
);

CREATE TABLE CQP_DomainConsistency ( 
	FK_CQP_MAIN integer,
	specificationTitle varchar(200),
	degree boolean,
	ID integer DEFAULT NEXTVAL('CQP_DomainConsistency_ID_seq'::TEXT)
);

CREATE TABLE CQP_SpecificationDate (
	FK_CQP_DomainConsistency integer,
	dateStamp timestamp,
	datetype varchar(20), 
	ID integer DEFAULT NEXTVAL('CQP_SpecificationDate_ID_seq'::TEXT)
);

SELECT AddGeometryColumn('', 'cqp_bbox','geom', 4326,'POLYGON',2);

CREATE SEQUENCE CQP_MAIN_ID_seq;
CREATE SEQUENCE CQP_BBOX_ID_seq;
CREATE SEQUENCE CQP_DomainConsistency_ID_seq;
CREATE SEQUENCE CQP_SpecificationDate_ID_seq;

ALTER TABLE CQP_Main ADD CONSTRAINT UQ_CQP_Main_identifier UNIQUE (identifier);
ALTER TABLE CQP_Main ADD CONSTRAINT UQ_CQP_Main_resIdentifier UNIQUE (resourceIdentifier );