
CREATE TABLE ADDQP_Degree ( 
	ID integer NOT NULL,
	fk_datasets integer NOT NULL,
	Degree boolean NOT NULL    -- MD_Metadata.dataQualityInfo.DQ_DataQuality.report.DQ_DomainConsistency.result.DQ_ConformanceResult.pass.Boolean
	
);
COMMENT ON TABLE ADDQP_Degree
    IS 'additional queryable property (ISO AP 1.0 INSPIRE)';
COMMENT ON COLUMN ADDQP_Degree.Degree
    IS 'MD_Metadata.dataQualityInfo.DQ_DataQuality.report.DQ_DomainConsistency.result.DQ_ConformanceResult.pass.Boolean';


CREATE TABLE ADDQP_Specification ( 
	ID integer NOT NULL,
	fk_datasets integer NOT NULL,
	SpecificationTitle text NOT NULL,    -- MD_Metadata.dataQualityInfo.DQ_DataQuality.report.DQ_DomainConsistency.result.DQ_ConformanceResult.specification.CI_Citation.title
	SpecificationDateType character varying(50) NOT NULL, -- MD_Metadata.dataQualityInfo.DQ_DataQuality.report.DQ_DomainConsistency.result.DQ_ConformanceResult.specification.CI_Citation.date.CI_Date.dateType.CI_DateTypeCode
	SpecificationDate timestamp NOT NULL -- MD_Metadata.dataQualityInfo.DQ_DataQuality.report.DQ_DomainConsistency.result.DQ_ConformanceResult.specification.CI_Citation.date.CI_Date.date
);
COMMENT ON TABLE ADDQP_Specification
    IS 'additional queryable property (ISO AP 1.0 INSPIRE)';
COMMENT ON COLUMN ADDQP_Specification.SpecificationTitle
    IS 'MD_Metadata.dataQualityInfo.DQ_DataQuality.report.DQ_DomainConsistency.result.DQ_ConformanceResult.specification.CI_Citation.title';
COMMENT ON COLUMN ADDQP_Specification.SpecificationDateType
    IS 'MD_Metadata.dataQualityInfo.DQ_DataQuality.report.DQ_DomainConsistency.result.DQ_ConformanceResult.specification.CI_Citation.date.CI_Date.dateType.CI_DateTypeCode';
COMMENT ON COLUMN ADDQP_Specification.SpecificationDate
    IS 'MD_Metadata.dataQualityInfo.DQ_DataQuality.report.DQ_DomainConsistency.result.DQ_ConformanceResult.specification.CI_Citation.date.CI_Date.date';

CREATE TABLE ADDQP_Limitation ( 
	ID integer NOT NULL,
	fk_datasets integer NOT NULL,
	limitation character varying(200)  -- MD_Metadata.identificationInfo.MD_DataIdentification.resourceConstraints.MD_Constraints.useLimitation

);
COMMENT ON TABLE ADDQP_Limitation
    IS 'additional queryable property (ISO AP 1.0 INSPIRE)';
COMMENT ON COLUMN ADDQP_Limitation.limitation
    IS 'MD_Metadata.identificationInfo.MD_DataIdentification.resourceConstraints.MD_Constraints.useLimitation';


CREATE TABLE ADDQP_Lineage ( 
	ID integer NOT NULL,
	fk_datasets integer NOT NULL,
	lineage text NOT NULL    -- MD_Metadata.dataQualityInfo.DQ_DataQuality.lineage.LI_Lineage.statement
	
);
COMMENT ON TABLE ADDQP_Lineage
    IS 'additional queryable property (ISO AP 1.0 INSPIRE)';
COMMENT ON COLUMN ADDQP_Lineage.lineage
    IS 'MD_Metadata.dataQualityInfo.DQ_DataQuality.lineage.LI_Lineage.statement';


CREATE TABLE ADDQP_AccessConstraint ( 
	ID integer NOT NULL,
	fk_datasets integer NOT NULL,
	accessConstraint character varying(50) NOT NULL    -- MD_Metadata.identificationInfo.MD_DataIdentification.resourceConstraints.MD_LegalConstraints.accessConstraints.MD_RestrictionCode
	
);
COMMENT ON TABLE ADDQP_AccessConstraint
    IS 'additional queryable property (ISO AP 1.0 INSPIRE)';
COMMENT ON COLUMN ADDQP_AccessConstraint.accessConstraint
    IS 'MD_Metadata.identificationInfo.MD_DataIdentification.resourceConstraints.MD_LegalConstraints.accessConstraints.MD_RestrictionCode';



CREATE TABLE ADDQP_OtherConstraint ( 
	ID integer NOT NULL,
	fk_datasets integer NOT NULL,
	otherConstraint text NOT NULL    -- MD_Metadata.identificationInfo.MD_DataIdentification.resourceConstraints.MD_LegalConstraints.otherConstraints
	
);
COMMENT ON TABLE ADDQP_OtherConstraint
    IS 'additional queryable property (ISO AP 1.0 INSPIRE)';
COMMENT ON COLUMN ADDQP_OtherConstraint.otherConstraint
    IS 'MD_Metadata.identificationInfo.MD_DataIdentification.resourceConstraints.MD_LegalConstraints.otherConstraints';



CREATE TABLE ADDQP_Classification ( 
	ID integer NOT NULL,
	fk_datasets integer NOT NULL,
	classification text NOT NULL    -- MD_Metadata.identificationInfo.MD_DataIdentification.resourceConstraints.MD_Constraints.classification
	
);
COMMENT ON TABLE ADDQP_Classification
    IS 'additional queryable property (ISO AP 1.0 INSPIRE)';
COMMENT ON COLUMN ADDQP_Classification.classification
    IS 'MD_Metadata.identificationInfo.MD_DataIdentification.resourceConstraints.MD_Constraints.classification';



ALTER TABLE ADDQP_Degree ADD CONSTRAINT PK_ADDQP_Degree
	PRIMARY KEY (ID);

ALTER TABLE ADDQP_Specification ADD CONSTRAINT PK_ADDQP_Specification
	PRIMARY KEY (ID);

ALTER TABLE ADDQP_Limitation ADD CONSTRAINT PK_ADDQP_Limitation
	PRIMARY KEY (ID);

ALTER TABLE ADDQP_Lineage ADD CONSTRAINT PK_ADDQP_Lineage
	PRIMARY KEY (ID);

ALTER TABLE ADDQP_AccessConstraint ADD CONSTRAINT PK_ADDQP_AccessConstraint
	PRIMARY KEY (ID);

ALTER TABLE ADDQP_OtherConstraint ADD CONSTRAINT PK_ADDQP_OtherConstraint
	PRIMARY KEY (ID);

ALTER TABLE ADDQP_Classification ADD CONSTRAINT PK_ADDQP_Classification
	PRIMARY KEY (ID);



ALTER TABLE ADDQP_Degree
	ADD CONSTRAINT UQ_ADDQP_Degree_ID UNIQUE (ID);

ALTER TABLE ADDQP_Specification
	ADD CONSTRAINT UQ_ADDQP_Specification_ID UNIQUE (ID);

ALTER TABLE ADDQP_Limitation
	ADD CONSTRAINT UQ_ADDQP_Limitation_ID UNIQUE (ID);

ALTER TABLE ADDQP_Lineage
	ADD CONSTRAINT UQ_ADDQP_Lineage_ID UNIQUE (ID);

ALTER TABLE ADDQP_AccessConstraint
	ADD CONSTRAINT UQ_ADDQP_AccessConstraint_ID UNIQUE (ID);

ALTER TABLE ADDQP_OtherConstraint
	ADD CONSTRAINT UQ_ADDQP_OtherConstraint_ID UNIQUE (ID);

ALTER TABLE ADDQP_Classification
	ADD CONSTRAINT UQ_ADDQP_Classification_ID UNIQUE (ID);



ALTER TABLE ADDQP_Degree ADD CONSTRAINT FK_ADDQP_Degree 
	FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;

ALTER TABLE ADDQP_Specification ADD CONSTRAINT FK_ADDQP_Specification 
	FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;

ALTER TABLE ADDQP_Limitation ADD CONSTRAINT FK_ADDQP_Limitation 
	FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;

ALTER TABLE ADDQP_Lineage ADD CONSTRAINT FK_ADDQP_Lineage 
	FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;

ALTER TABLE ADDQP_AccessConstraint ADD CONSTRAINT FK_ADDQP_AccessConstraint 
	FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;

ALTER TABLE ADDQP_OtherConstraint ADD CONSTRAINT FK_ADDQP_OtherConstraint
	FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;

ALTER TABLE ADDQP_Classification ADD CONSTRAINT FK_ADDQP_Classification 
	FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;

