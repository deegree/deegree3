CREATE TABLE ADDQP_Degree ( 
	ID integer NOT NULL,
	fk_datasets integer NOT NULL,
	Degree bit NOT NULL    -- MD_Metadata.dataQualityInfo.DQ_DataQuality.report.DQ_DomainConsistency.result.DQ_ConformanceResult.pass.boolean
	
);

CREATE INDEX degree_idx ON ADDQP_Degree (degree);

CREATE TABLE ADDQP_Specification ( 
	ID integer NOT NULL,
	fk_datasets integer NOT NULL,
	SpecificationTitle varchar(900) NOT NULL,    -- MD_Metadata.dataQualityInfo.DQ_DataQuality.report.DQ_DomainConsistency.result.DQ_ConformanceResult.specification.CI_Citation.title
	SpecificationDateType varchar(50) NOT NULL, -- MD_Metadata.dataQualityInfo.DQ_DataQuality.report.DQ_DomainConsistency.result.DQ_ConformanceResult.specification.CI_Citation.date.CI_Date.dateType.CI_DateTypeCode
	SpecificationDate datetime NOT NULL -- MD_Metadata.dataQualityInfo.DQ_DataQuality.report.DQ_DomainConsistency.result.DQ_ConformanceResult.specification.CI_Citation.date.CI_Date.date
);
    
CREATE INDEX specificationtitle_idx ON ADDQP_Specification (specificationtitle);
CREATE INDEX specificationdate_idx ON ADDQP_Specification (specificationdate);
CREATE INDEX specificationdatetype_idx ON ADDQP_Specification (specificationdatetype);

CREATE TABLE ADDQP_Limitation ( 
	ID integer NOT NULL,
	fk_datasets integer NOT NULL,
	limitation varchar(500)  -- MD_Metadata.identificationInfo.MD_DataIdentification.resourceConstraints.MD_Constraints.useLimitation
);

CREATE INDEX limitation_idx ON ADDQP_Limitation (limitation);

CREATE TABLE ADDQP_Lineage ( 
	ID integer NOT NULL,
	fk_datasets integer NOT NULL,
	lineage varchar(900) NOT NULL    -- MD_Metadata.dataQualityInfo.DQ_DataQuality.lineage.LI_Lineage.statement
);

CREATE INDEX lineage_idx ON ADDQP_Lineage (lineage);

CREATE TABLE ADDQP_AccessConstraint ( 
	ID integer NOT NULL,
	fk_datasets integer NOT NULL,
	accessConstraint varchar(50) NOT NULL    -- MD_Metadata.identificationInfo.MD_DataIdentification.resourceConstraints.MD_LegalConstraints.accessConstraints.MD_RestrictionCode	
);
CREATE INDEX accessconstraint_idx ON ADDQP_AccessConstraint (accessconstraint);


CREATE TABLE ADDQP_OtherConstraint ( 
	ID integer NOT NULL,
	fk_datasets integer NOT NULL,
	otherConstraint varchar(900) NOT NULL    -- MD_Metadata.identificationInfo.MD_DataIdentification.resourceConstraints.MD_LegalConstraints.otherConstraints
	
);

CREATE INDEX otherconstraint_idx ON ADDQP_OtherConstraint (otherconstraint);


CREATE TABLE ADDQP_Classification ( 
	ID integer NOT NULL,
	fk_datasets integer NOT NULL,
	classification varchar(900) NOT NULL    -- MD_Metadata.identificationInfo.MD_DataIdentification.resourceConstraints.MD_Constraints.classification
	
);

CREATE INDEX classification_idx ON ADDQP_Classification (classification);


ALTER TABLE ADDQP_Degree ADD CONSTRAINT PK_ADDQP_Degree PRIMARY KEY (ID);

ALTER TABLE ADDQP_Specification ADD CONSTRAINT PK_ADDQP_Specification PRIMARY KEY (ID);

ALTER TABLE ADDQP_Limitation ADD CONSTRAINT PK_ADDQP_Limitation PRIMARY KEY (ID);

ALTER TABLE ADDQP_Lineage ADD CONSTRAINT PK_ADDQP_Lineage PRIMARY KEY (ID);

ALTER TABLE ADDQP_AccessConstraint ADD CONSTRAINT PK_ADDQP_AccessConstraint PRIMARY KEY (ID);

ALTER TABLE ADDQP_OtherConstraint ADD CONSTRAINT PK_ADDQP_OtherConstraint PRIMARY KEY (ID);

ALTER TABLE ADDQP_Classification ADD CONSTRAINT PK_ADDQP_Classification PRIMARY KEY (ID);



ALTER TABLE ADDQP_Degree ADD CONSTRAINT FK_ADDQP_Degree FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;

ALTER TABLE ADDQP_Specification ADD CONSTRAINT FK_ADDQP_Specification  FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;

ALTER TABLE ADDQP_Limitation ADD CONSTRAINT FK_ADDQP_Limitation  FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;

ALTER TABLE ADDQP_Lineage ADD CONSTRAINT FK_ADDQP_Lineage  FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;

ALTER TABLE ADDQP_AccessConstraint ADD CONSTRAINT FK_ADDQP_AccessConstraint  FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;

ALTER TABLE ADDQP_OtherConstraint ADD CONSTRAINT FK_ADDQP_OtherConstraint FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;

ALTER TABLE ADDQP_Classification ADD CONSTRAINT FK_ADDQP_Classification  FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;


