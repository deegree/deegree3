-- Create Tables 
CREATE TABLE Datasets ( 
	ID integer NOT NULL,
	version integer,    -- version of the record 
	status numeric(1),
	AnyText text,    -- common queryable property (ISO AP 1.0): Whole resource text. 
	Modified timestamp,    -- common queryable property (ISO AP 1.0): MD_Metadata.dateStamp .Date 
	HasSecurityConstraints boolean,    -- additional queryable property (ISO AP 1.0): MD_Metadata.AbstractMD_Identification.resourceConstraints.MD_securityConstraints (If an instance of the class MD_SecurityConstraint exists for a resource, the "HasSecurityConstraints" is "true", otherwise "false") 
	Language varchar(50),    -- additional queryable property (ISO AP 1.0): MD_Metadata.language 
	ParentIdentifier varchar(50),    -- additional queryable property (ISO AP 1.0): MD_Metadata.parentIdentifier 
    recordfull bytea NOT NULL
	);
COMMENT ON COLUMN Datasets.version
    IS 'version of the record';
COMMENT ON COLUMN Datasets.AnyText
    IS 'common queryable property (ISO AP 1.0): Whole resource text.';
COMMENT ON COLUMN Datasets.Modified
    IS 'common queryable property (ISO AP 1.0): MD_Metadata.dateStamp .Date';
COMMENT ON COLUMN Datasets.HasSecurityConstraints
    IS 'additional queryable property (ISO AP 1.0): MD_Metadata.AbstractMD_Identification.resourceConstraints.MD_securityConstraints (If an instance of the class MD_SecurityConstraint exists for a resource, the "HasSecurityConstraints" is "true", otherwise "false")';
COMMENT ON COLUMN Datasets.Language
    IS 'additional queryable property (ISO AP 1.0): MD_Metadata.language';
COMMENT ON COLUMN Datasets.ParentIdentifier
    IS 'additional queryable property (ISO AP 1.0): MD_Metadata.parentIdentifier';

CREATE TABLE ISOQP_Abstract ( 
	ID integer NOT NULL,
	fk_datasets integer NOT NULL,
	Abstract text NOT NULL    -- MD_Metadata.identificationInfo.AbstractMD_Identification.abstract 
);
COMMENT ON TABLE ISOQP_Abstract
    IS 'common queryable property (ISO AP 1.0)';
COMMENT ON COLUMN ISOQP_Abstract.Abstract
    IS 'MD_Metadata.identificationInfo.AbstractMD_Identification.abstract';

CREATE TABLE ISOQP_AlternateTitle ( 
	ID integer NOT NULL,
	fk_datasets integer NOT NULL,
	AlternateTitle varchar(500) NOT NULL    -- MD_Metadata.identificationInfo.AbstractMD_Identification.citation.CI_Citation.alternateTitle 
);
COMMENT ON TABLE ISOQP_AlternateTitle
    IS 'additional queryable property (ISO AP 1.0)';
COMMENT ON COLUMN ISOQP_AlternateTitle.AlternateTitle
    IS 'MD_Metadata.identificationInfo.AbstractMD_Identification.citation.CI_Citation.alternateTitle';

CREATE TABLE ISOQP_BoundingBox ( 
	ID integer NOT NULL,
	fk_datasets integer NOT NULL,
	Authority varchar(250),    -- MD_Metadata.referenceSystemInfo.MD_ReferenceSystem.referenceSystemIdentifier.RS_Identifier.codeSpace 
	ID_CRS varchar(50),    -- MD_Metadata.referenceSystemInfo.MD_ReferenceSystem.referenceSystemIdentifier.RS_Identifier.code 
	Version varchar(50)    -- MD_Metadata.referenceSystemInfo.MD_ReferenceSystem.referenceSystemIdentifier.RS_Identifier.version 
);
COMMENT ON TABLE ISOQP_BoundingBox
    IS 'common queryable property (ISO AP 1.0): MD_Metadata.identificationInfo.MD_DataIdentification.extent.EX_Extent.geographicElement.EX_GeographicBoundingBox.westBoundLongitude,  MD_Metadata.identificationInfo.MD_DataIdentification.extent.EX_Extent.geographicElement.EX_GeographicBoundingBox.southBoundLatitude, MD_Metadata.identificationInfo.MD_DataIdentification.extent.EX_Extent.geographicElement.EX_GeographicBoundingBox.eastBoundLongitude, MD_Metadata.identificationInfo.MD_DataIdentification.extent.EX_Extent.geographicElement.EX_GeographicBoundingBox.northBoundLatitude 
 as BoundingBox';

CREATE TABLE ISOQP_CouplingType ( 
	ID integer NOT NULL,
	fk_datasets integer NOT NULL,
	CouplingType varchar(5) NOT NULL    -- MD_Metadata.identificationInfo.SV_ServiceIdentification.couplingType.SV_CouplingType.code@codeListValue 
);
COMMENT ON TABLE ISOQP_CouplingType
    IS 'additional queryable property (ISO AP 1.0) - service';
COMMENT ON COLUMN ISOQP_CouplingType.CouplingType
    IS 'MD_Metadata.identificationInfo.SV_ServiceIdentification.couplingType.SV_CouplingType.code@codeListValue';

CREATE TABLE ISOQP_CreationDate ( 
	fk_datasets integer NOT NULL,
	CreationDate timestamp NOT NULL,    -- MD_Identification.citation.CI_Citation.date.CI_Date[dateType.CI_DateTypeCode.@codeListValue='creation'].date.Date 
	ID integer NOT NULL
);
COMMENT ON TABLE ISOQP_CreationDate
    IS 'additional queryable property (ISO AP 1.0)';
COMMENT ON COLUMN ISOQP_CreationDate.CreationDate
    IS 'MD_Identification.citation.CI_Citation.date.CI_Date[dateType.CI_DateTypeCode.@codeListValue=''creation''].date.Date';

CREATE TABLE ISOQP_Format ( 
	ID integer NOT NULL,
	fk_datasets integer NOT NULL,
	Format varchar(500) NOT NULL    -- MD_Metadata.distributionInfo.MD_Distribution.distributionFormat.MD_Format.name 
);
COMMENT ON TABLE ISOQP_Format
    IS 'common queryable property (ISO AP 1.0)';
COMMENT ON COLUMN ISOQP_Format.Format
    IS 'MD_Metadata.distributionInfo.MD_Distribution.distributionFormat.MD_Format.name';

CREATE TABLE ISOQP_GeographicDescriptionCode ( 
	ID integer NOT NULL,
	fk_datasets integer NOT NULL,
	GeographicDescriptionCode varchar(50)    -- dataset, datasetcollection, application: MD_Metadata.identificationInfo.MD_DataIdentification.extent.EX_Extent.geographicElement.EX_GeographicDescription.geographicIdentifier.MD_Identifier.code 
-- service: MD_Metadata.identificationInfo.SV_ServiceIdentification.extent.EX_Extent.geographicElement.EX_GeographicDescription.geographicIdentifier.MD_Identifier.code 
);
COMMENT ON TABLE ISOQP_GeographicDescriptionCode
    IS 'additional queryable property (ISO AP 1.0) - dataset, datasetcollection, application, service';
COMMENT ON COLUMN ISOQP_GeographicDescriptionCode.GeographicDescriptionCode
    IS 'dataset, datasetcollection, application: MD_Metadata.identificationInfo.MD_DataIdentification.extent.EX_Extent.geographicElement.EX_GeographicDescription.geographicIdentifier.MD_Identifier.code 
 service: MD_Metadata.identificationInfo.SV_ServiceIdentification.extent.EX_Extent.geographicElement.EX_GeographicDescription.geographicIdentifier.MD_Identifier.code';

CREATE TABLE ISOQP_Keyword ( 
	ID integer NOT NULL,
	fk_datasets integer NOT NULL,
	KeywordType varchar(50),    -- MD_Metadata.identificationInfo.AbstractMD_Identification.descriptiveKeywords.MD_Keywords.type 
	Keyword varchar(500),    -- MD_Metadata.identificationInfo.AbstractMD_Identification.descriptiveKeywords.MD_Keywords.keyword 
	Thesaurus varchar(500)    -- MD_Metadata.identificationInfo.AbstractMD_Identification.descriptiveKeywords.MD_Keywords.thesaurusName.CI_Citation.title 
);
COMMENT ON TABLE ISOQP_Keyword
    IS 'contains the queryable properties:  KeywordType - additional queryable property (ISO AP 1.0) part of Subject (TopicCategory is missing) - common queryable property (ISO AP 1.0)';
COMMENT ON COLUMN ISOQP_Keyword.KeywordType
    IS 'MD_Metadata.identificationInfo.AbstractMD_Identification.descriptiveKeywords.MD_Keywords.type';
COMMENT ON COLUMN ISOQP_Keyword.Keyword
    IS 'MD_Metadata.identificationInfo.AbstractMD_Identification.descriptiveKeywords.MD_Keywords.keyword';
COMMENT ON COLUMN ISOQP_Keyword.Thesaurus
    IS 'MD_Metadata.identificationInfo.AbstractMD_Identification.descriptiveKeywords.MD_Keywords.thesaurusName.CI_Citation.title';

CREATE TABLE ISOQP_OperatesOnData ( 
	ID integer NOT NULL,
	fk_datasets integer NOT NULL,
	OperatesOn varchar(50),    -- MD_Metadata.identificationInfo.SV_ServiceIdentification.operatesOn.MD_DataIdentification.citation.CI_Citation.identifier 
	OperatesOnIdentifier varchar(50),    -- MD_Metadata.identificationInfo.SV_ServiceIdentification.coupledResource.SV_CoupledResource.identifier 
	OperatesOnName varchar(50)    -- MD_Metadata.identificationInfo.SV_ServiceIdentification.coupledResource.SV_CoupledResource.operationName 
);
COMMENT ON TABLE ISOQP_OperatesOnData
    IS 'additional queryable property (ISO AP 1.0) - service';
COMMENT ON COLUMN ISOQP_OperatesOnData.OperatesOn
    IS 'MD_Metadata.identificationInfo.SV_ServiceIdentification.operatesOn.MD_DataIdentification.citation.CI_Citation.identifier';
COMMENT ON COLUMN ISOQP_OperatesOnData.OperatesOnIdentifier
    IS 'MD_Metadata.identificationInfo.SV_ServiceIdentification.coupledResource.SV_CoupledResource.identifier';
COMMENT ON COLUMN ISOQP_OperatesOnData.OperatesOnName
    IS 'MD_Metadata.identificationInfo.SV_ServiceIdentification.coupledResource.SV_CoupledResource.operationName';

CREATE TABLE ISOQP_Operation ( 
	ID integer NOT NULL,
	fk_datasets integer NOT NULL,
	Operation varchar(50) NOT NULL    -- MD_Metadata.identificationInfo.SV_ServiceIdentification.containsOperations.SV_OperationMetadata.operationName 
);
COMMENT ON TABLE ISOQP_Operation
    IS 'additional queryable property (ISO AP 1.0) - service';
COMMENT ON COLUMN ISOQP_Operation.Operation
    IS 'MD_Metadata.identificationInfo.SV_ServiceIdentification.containsOperations.SV_OperationMetadata.operationName';

CREATE TABLE ISOQP_OrganisationName ( 
	ID integer NOT NULL,
	fk_datasets integer NOT NULL,
	OrganisationName varchar(500) NOT NULL    -- MD_Metadata.identificationInfo.AbstractMD_Identification.pointOfContact.CI_ResponsibleParty.organisationName 
);
COMMENT ON TABLE ISOQP_OrganisationName
    IS 'additional queryable property (ISO AP 1.0)';
COMMENT ON COLUMN ISOQP_OrganisationName.OrganisationName
    IS 'MD_Metadata.identificationInfo.AbstractMD_Identification.pointOfContact.CI_ResponsibleParty.organisationName';

CREATE TABLE ISOQP_PublicationDate ( 
	ID integer NOT NULL,
	fk_datasets integer NOT NULL,
	PublicationDate timestamp NOT NULL    -- MD_Metadata.identificationInfo.AbstractMD_Identification.citation.CI_Citation.date.CI_Date[dateType.CI_DateTypeCode.@codeListValue='publication'].date.Date 
);
COMMENT ON TABLE ISOQP_PublicationDate
    IS 'additional queryable property (ISO AP 1.0)';
COMMENT ON COLUMN ISOQP_PublicationDate.PublicationDate
    IS 'MD_Metadata.identificationInfo.AbstractMD_Identification.citation.CI_Citation.date.CI_Date[dateType.CI_DateTypeCode.@codeListValue=''publication''].date.Date';

CREATE TABLE ISOQP_ResourceIdentifier ( 
	ID integer NOT NULL,
	fk_datasets integer NOT NULL,
	ResourceIdentifier varchar(500) NOT NULL    -- MD_Metadata.identificationInfo.AbstractMD_Identification.citation.CI_Citation.identifier.MD_Identifier.code 
);
COMMENT ON TABLE ISOQP_ResourceIdentifier
    IS 'additional queryable property (ISO AP 1.0)';
COMMENT ON COLUMN ISOQP_ResourceIdentifier.ResourceIdentifier
    IS 'MD_Metadata.identificationInfo.AbstractMD_Identification.citation.CI_Citation.identifier.MD_Identifier.code';

CREATE TABLE ISOQP_ResourceLanguage ( 
	ID integer NOT NULL,
	fk_datasets integer NOT NULL,
	ResourceLanguage varchar(50) NOT NULL    -- MD_Metadata.identificationInfo.MD_DataIdentification.language 
);
COMMENT ON TABLE ISOQP_ResourceLanguage
    IS ' 	1) additional queryable property (ISO AP 1.0) - dataset, datasetcollection, application';
COMMENT ON COLUMN ISOQP_ResourceLanguage.ResourceLanguage
    IS 'MD_Metadata.identificationInfo.MD_DataIdentification.language';

CREATE TABLE ISOQP_RevisionDate ( 
	ID integer NOT NULL,
	fk_datasets integer NOT NULL,
	RevisionDate timestamp NOT NULL    -- MD_Metadata.identificationInfo.AbstractMD_Identification.citation.CI_Citation.date.CI_Date[dateType.CI_DateTypeCode.@codeListValue='revision'].date.Date 
);
COMMENT ON TABLE ISOQP_RevisionDate
    IS 'additional queryable property (ISO AP 1.0)';
COMMENT ON COLUMN ISOQP_RevisionDate.RevisionDate
    IS 'MD_Metadata.identificationInfo.AbstractMD_Identification.citation.CI_Citation.date.CI_Date[dateType.CI_DateTypeCode.@codeListValue=''revision''].date.Date';

CREATE TABLE ISOQP_ServiceType ( 
	ID integer NOT NULL,
	fk_datasets integer NOT NULL,
	ServiceType varchar(50) NOT NULL    -- MD_Metadata.identificationInfo.SV_ServiceIdentification.serviceType 
);
COMMENT ON TABLE ISOQP_ServiceType
    IS 'additional queryable property (ISO AP 1.0) - service';
COMMENT ON COLUMN ISOQP_ServiceType.ServiceType
    IS 'MD_Metadata.identificationInfo.SV_ServiceIdentification.serviceType';

CREATE TABLE ISOQP_ServiceTypeVersion ( 
	ID integer NOT NULL,
	fk_datasets integer NOT NULL,
	ServiceTypeVersion varchar(15) NOT NULL    -- MD_Metadata.identificationInfo.SV_ServiceIdentification.serviceTypeVersion 
);
COMMENT ON TABLE ISOQP_ServiceTypeVersion
    IS 'additional queryable property (ISO AP 1.0) - service';
COMMENT ON COLUMN ISOQP_ServiceTypeVersion.ServiceTypeVersion
    IS 'MD_Metadata.identificationInfo.SV_ServiceIdentification.serviceTypeVersion';

CREATE TABLE ISOQP_SpatialResolution ( 
	ID integer NOT NULL,
	fk_datasets integer NOT NULL,
	Denominator integer,    -- MD_Metadata.identificationInfo.MD_DataIdentification.spatialResolution.MD_Resolution.equivalentScale.MD_RepresentativeFraction.denominator 
	DistanceValue decimal(10,4),    -- MD_Metadata.identificationInfo.MD_DataIdentification.spatialResolution.MD_Resolution.distance.gco:Distance 
	DistanceUOM varchar(50)    -- MD_Metadata.identificationInfo.MD_DataIdentification.spatialResolution.MD_Resolution.distance.gco:Distance@uom 
);
COMMENT ON TABLE ISOQP_SpatialResolution
    IS 'additional queryable property (ISO AP 1.0) - dataset, datasetcollection, application';
COMMENT ON COLUMN ISOQP_SpatialResolution.Denominator
    IS 'MD_Metadata.identificationInfo.MD_DataIdentification.spatialResolution.MD_Resolution.equivalentScale.MD_RepresentativeFraction.denominator';
COMMENT ON COLUMN ISOQP_SpatialResolution.DistanceValue
    IS 'MD_Metadata.identificationInfo.MD_DataIdentification.spatialResolution.MD_Resolution.distance.gco:Distance';
COMMENT ON COLUMN ISOQP_SpatialResolution.DistanceUOM
    IS 'MD_Metadata.identificationInfo.MD_DataIdentification.spatialResolution.MD_Resolution.distance.gco:Distance@uom';

CREATE TABLE ISOQP_TemporalExtent ( 
	ID integer NOT NULL,
	fk_datasets integer NOT NULL,
	TempExtent_begin timestamp NOT NULL,    -- MD_Metadata.identificationInfo.MD_DataIdentification.extent.EX_Extent.temporalElement.EX_TemporalExtent.extent.TimePeriod.beginPosition 
	TempExtent_end timestamp NOT NULL    -- MD_Metadata.identificationInfo.MD_DataIdentification.extent.EX_Extent.temporalElement.EX_TemporalExtent.extent.TimePeriod.endPosition 
);
COMMENT ON TABLE ISOQP_TemporalExtent
    IS 'additional queryable property (ISO AP 1.0) - dataset, datasetcollection, application';
COMMENT ON COLUMN ISOQP_TemporalExtent.TempExtent_begin
    IS 'MD_Metadata.identificationInfo.MD_DataIdentification.extent.EX_Extent.temporalElement.EX_TemporalExtent.extent.TimePeriod.beginPosition';
COMMENT ON COLUMN ISOQP_TemporalExtent.TempExtent_end
    IS 'MD_Metadata.identificationInfo.MD_DataIdentification.extent.EX_Extent.temporalElement.EX_TemporalExtent.extent.TimePeriod.endPosition';

CREATE TABLE ISOQP_Title ( 
	ID integer NOT NULL,
	fk_datasets integer NOT NULL,
	Title varchar(500) NOT NULL    -- MD_Metadata.identificationInfo.AbstractMD_Identification.citation.CI_Citation.title 
);
COMMENT ON TABLE ISOQP_Title
    IS 'common queryable property (ISO AP 1.0)';
COMMENT ON COLUMN ISOQP_Title.Title
    IS 'MD_Metadata.identificationInfo.AbstractMD_Identification.citation.CI_Citation.title';

CREATE TABLE ISOQP_TopicCategory ( 
	ID integer NOT NULL,
	fk_datasets integer NOT NULL,
	TopicCategory varchar(50) NOT NULL    -- MD_Metadata.identificationInfo.MD_DataIdentification.topicCategory 
);
COMMENT ON TABLE ISOQP_TopicCategory
    IS 'additional queryable property (ISO AP 1.0) - dataset, datasetcollection, application';
COMMENT ON COLUMN ISOQP_TopicCategory.TopicCategory
    IS 'MD_Metadata.identificationInfo.MD_DataIdentification.topicCategory';

CREATE TABLE ISOQP_Type ( 
	ID integer NOT NULL,
	fk_datasets integer NOT NULL,
	Type varchar(50) NOT NULL    -- MD_Metadata.hierarchyLevel.MD_ScopeCode/@codeListValue. If MD_Metadata.hierarchyLevel is missing, 'Type' will be considered as "Dataset" (default). 
);
COMMENT ON TABLE ISOQP_Type
    IS 'common queryable property (ISO AP 1.0)';
COMMENT ON COLUMN ISOQP_Type.Type
    IS 'MD_Metadata.hierarchyLevel.MD_ScopeCode/@codeListValue. If MD_Metadata.hierarchyLevel is missing, ''Type'' will be considered as "Dataset" (default).';

CREATE TABLE QP_Identifier ( 
ID integer NOT NULL,
fk_datasets integer NOT NULL,
identifier varchar(150) NOT NULL    -- MD_Metadata.fileIdentifier
);
COMMENT ON TABLE QP_Identifier
    IS 'common queryable property (ISO AP 1.0)';
COMMENT ON COLUMN QP_Identifier.identifier
    IS 'MD_Metadata.fileIdentifier';


CREATE TABLE DCQP_RIGHTS ( 
	ID integer NOT NULL,
	fk_datasets integer NOT NULL,
	rights varchar(100) NOT NULL   
);
COMMENT ON TABLE DCQP_RIGHTS
    IS 'dublin core queryable property (DC)';
COMMENT ON COLUMN DCQP_RIGHTS.Rights
    IS 'Rights is not in the ISO AP but in DC';


-- Create Primary Key Constraints 
ALTER TABLE Datasets ADD CONSTRAINT PK_Datasets 
	PRIMARY KEY (ID);
	
ALTER TABLE QP_Identifier ADD CONSTRAINT PK_QP_Identifier 
	PRIMARY KEY (ID);


ALTER TABLE ISOQP_Abstract ADD CONSTRAINT PK_ISOQP_Abstract 
	PRIMARY KEY (ID);


ALTER TABLE ISOQP_AlternateTitle ADD CONSTRAINT PK_ISOQP_AlternateTitle 
	PRIMARY KEY (ID);


ALTER TABLE ISOQP_BoundingBox ADD CONSTRAINT PK_ISOQP_BoundingBox 
	PRIMARY KEY (ID);


ALTER TABLE ISOQP_CouplingType ADD CONSTRAINT PK_ISOQP_CouplingType 
	PRIMARY KEY (ID);


ALTER TABLE ISOQP_CreationDate ADD CONSTRAINT PK_ISOQP_CreationDate 
	PRIMARY KEY (ID);


ALTER TABLE ISOQP_Format ADD CONSTRAINT PK_ISOQP_Format 
	PRIMARY KEY (ID);


ALTER TABLE ISOQP_GeographicDescriptionCode ADD CONSTRAINT PK_ISOQP_GeographicDescriptionCode 
	PRIMARY KEY (ID);


ALTER TABLE ISOQP_Keyword ADD CONSTRAINT PK_ISOQP_Keyword 
	PRIMARY KEY (ID);


ALTER TABLE ISOQP_OperatesOnData ADD CONSTRAINT PK_ISOQP_OperatesOnData 
	PRIMARY KEY (ID);


ALTER TABLE ISOQP_Operation ADD CONSTRAINT PK_ISOQP_Operation 
	PRIMARY KEY (ID);


ALTER TABLE ISOQP_OrganisationName ADD CONSTRAINT PK_ISOQP_OrganisationName 
	PRIMARY KEY (ID);


ALTER TABLE ISOQP_PublicationDate ADD CONSTRAINT PK_ISOQP_PublicationDate 
	PRIMARY KEY (ID);


ALTER TABLE ISOQP_ResourceIdentifier ADD CONSTRAINT PK_ISOQP_ResourceIdentifier 
	PRIMARY KEY (ID);


ALTER TABLE ISOQP_ResourceLanguage ADD CONSTRAINT PK_ISOQP_ResourceLanguage 
	PRIMARY KEY (ID);


ALTER TABLE ISOQP_RevisionDate ADD CONSTRAINT PK_ISOQP_RevisionDate 
	PRIMARY KEY (ID);


ALTER TABLE ISOQP_ServiceType ADD CONSTRAINT PK_ISOQP_ServiceType 
	PRIMARY KEY (ID);


ALTER TABLE ISOQP_ServiceTypeVersion ADD CONSTRAINT PK_ISOQP_ServiceTypeVersion 
	PRIMARY KEY (ID);


ALTER TABLE ISOQP_SpatialResolution ADD CONSTRAINT PK_ISOQP_SpatialResolution 
	PRIMARY KEY (ID);


ALTER TABLE ISOQP_TemporalExtent ADD CONSTRAINT PK_ISOQP_TemporalExtent 
	PRIMARY KEY (ID);


ALTER TABLE ISOQP_Title ADD CONSTRAINT PK_ISOQP_Title 
	PRIMARY KEY (ID);


ALTER TABLE ISOQP_TopicCategory ADD CONSTRAINT PK_ISOQP_TopicCategory 
	PRIMARY KEY (ID);


ALTER TABLE ISOQP_Type ADD CONSTRAINT PK_ISOQP_Type 
	PRIMARY KEY (ID);


--ALTER TABLE UserDefinedQueryableProperties ADD CONSTRAINT PK_SelfQueryableProperties 
--	PRIMARY KEY (fk_datasets);


ALTER TABLE DCQP_Rights ADD CONSTRAINT PK_DCQP_Rights 
	PRIMARY KEY (ID);



-- Create Indexes 
ALTER TABLE Datasets
	ADD CONSTRAINT UQ_Datasets_ID UNIQUE (ID);
ALTER TABLE QP_Identifier
	ADD CONSTRAINT UQ_QP_Identifier_ID UNIQUE (ID);
ALTER TABLE QP_Identifier
	ADD CONSTRAINT UQ_QP_Identifier_Identifier UNIQUE (identifier);
ALTER TABLE ISOQP_Abstract
	ADD CONSTRAINT UQ_ISOQP_Abstract_ID UNIQUE (ID);
ALTER TABLE ISOQP_AlternateTitle
	ADD CONSTRAINT UQ_ISOQP_AlternateTitle_ID UNIQUE (ID);
ALTER TABLE ISOQP_BoundingBox
	ADD CONSTRAINT UQ_ISOQP_BoundingBox_ID UNIQUE (ID);
ALTER TABLE ISOQP_CouplingType
	ADD CONSTRAINT UQ_ISOQP_CouplingType_ID UNIQUE (ID);
ALTER TABLE ISOQP_CreationDate
	ADD CONSTRAINT UQ_ISOQP_CreationDate_ID UNIQUE (ID);
ALTER TABLE ISOQP_Format
	ADD CONSTRAINT UQ_ISOQP_Format_ID UNIQUE (ID);
ALTER TABLE ISOQP_GeographicDescriptionCode
	ADD CONSTRAINT UQ_ISOQP_GeographicDescriptionCode_ID UNIQUE (ID);
ALTER TABLE ISOQP_Keyword
	ADD CONSTRAINT UQ_ISOQP_Keyword_ID UNIQUE (ID);
ALTER TABLE ISOQP_OperatesOnData
	ADD CONSTRAINT UQ_ISOQP_OperatesOnData_ID UNIQUE (ID);
ALTER TABLE ISOQP_Operation
	ADD CONSTRAINT UQ_ISOQP_Operation_ID UNIQUE (ID);
ALTER TABLE ISOQP_OrganisationName
	ADD CONSTRAINT UQ_ISOQP_OrganisationName_ID UNIQUE (ID);
ALTER TABLE ISOQP_PublicationDate
	ADD CONSTRAINT UQ_ISOQP_PublicationDate_ID UNIQUE (ID);
ALTER TABLE ISOQP_ResourceIdentifier
	ADD CONSTRAINT UQ_ISOQP_ResourceIdentifier_ID UNIQUE (ID);
ALTER TABLE ISOQP_ResourceLanguage
	ADD CONSTRAINT UQ_ISOQP_ResourceLanguage_ID UNIQUE (ID);
ALTER TABLE ISOQP_RevisionDate
	ADD CONSTRAINT UQ_ISOQP_RevisionDate_ID UNIQUE (ID);
ALTER TABLE ISOQP_ServiceType
	ADD CONSTRAINT UQ_ISOQP_ServiceType_ID UNIQUE (ID);
ALTER TABLE ISOQP_ServiceTypeVersion
	ADD CONSTRAINT UQ_ISOQP_ServiceTypeVersion_ID UNIQUE (ID);
ALTER TABLE ISOQP_SpatialResolution
	ADD CONSTRAINT UQ_ISOQP_SpatialResolution_ID UNIQUE (ID);
ALTER TABLE ISOQP_TemporalExtent
	ADD CONSTRAINT UQ_ISOQP_TemporalExtent_ID UNIQUE (ID);
ALTER TABLE ISOQP_Title
	ADD CONSTRAINT UQ_ISOQP_Title_ID UNIQUE (ID);
ALTER TABLE ISOQP_TopicCategory
	ADD CONSTRAINT UQ_ISOQP_TopicCategory_ID UNIQUE (ID);
ALTER TABLE ISOQP_Type
	ADD CONSTRAINT UQ_ISOQP_Type_ID UNIQUE (ID);
--ALTER TABLE UserDefinedQueryableProperties
--	ADD CONSTRAINT UQ_SelfQueryableProperties_fk_datasets UNIQUE (fk_datasets);
ALTER TABLE DCQP_Rights
	ADD CONSTRAINT UQ_DCQP_Rights_ID UNIQUE (ID);

-- Create Foreign Key Constraints 
ALTER TABLE QP_Identifier ADD CONSTRAINT FK_QP_Identifier_Datasets 
	FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;
	
ALTER TABLE ISOQP_Abstract ADD CONSTRAINT FK_ISOQP_Abstract_Datasets 
	FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;

ALTER TABLE ISOQP_AlternateTitle ADD CONSTRAINT FK_ISOQP_AlternateTitle_Datasets 
	FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;

ALTER TABLE ISOQP_BoundingBox ADD CONSTRAINT FK_ISOQP_BoundingBox_Datasets 
	FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;

ALTER TABLE ISOQP_CouplingType ADD CONSTRAINT FK_ISOQP_CouplingType_Datasets 
	FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;

ALTER TABLE ISOQP_CreationDate ADD CONSTRAINT FK_ISOQP_CreationDate_Datasets 
	FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;

ALTER TABLE ISOQP_Format ADD CONSTRAINT FK_ISOQP_Format_Datasets 
	FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;

ALTER TABLE ISOQP_GeographicDescriptionCode ADD CONSTRAINT FK_ISOQP_GeographicDescriptionCode_Datasets 
	FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;

ALTER TABLE ISOQP_Keyword ADD CONSTRAINT FK_ISOQP_KeywordType_Datasets 
	FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;

ALTER TABLE ISOQP_OperatesOnData ADD CONSTRAINT FK_ISOQP_OperatesOnData_Datasets 
	FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;

ALTER TABLE ISOQP_Operation ADD CONSTRAINT FK_ISOQP_Operation_Datasets 
	FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;

ALTER TABLE ISOQP_OrganisationName ADD CONSTRAINT FK_ISOQP_OrganisationName_Datasets 
	FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;

ALTER TABLE ISOQP_PublicationDate ADD CONSTRAINT FK_ISOQP_PublicationDate_Datasets 
	FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;

ALTER TABLE ISOQP_ResourceIdentifier ADD CONSTRAINT FK_ISOQP_ResourceIdentifier_Datasets 
	FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;

ALTER TABLE ISOQP_ResourceLanguage ADD CONSTRAINT FK_ISOQP_ResourceLanguage_Datasets 
	FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;

ALTER TABLE ISOQP_RevisionDate ADD CONSTRAINT FK_ISOQP_RevisionDate_Datasets 
	FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;

ALTER TABLE ISOQP_ServiceType ADD CONSTRAINT FK_ISOQP_ServiceType_Datasets 
	FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;

ALTER TABLE ISOQP_ServiceTypeVersion ADD CONSTRAINT FK_ISOQP_ServiceTypeVersion_Datasets 
	FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;

ALTER TABLE ISOQP_SpatialResolution ADD CONSTRAINT FK_ISOQP_SpatialResolution_Datasets 
	FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;

ALTER TABLE ISOQP_TemporalExtent ADD CONSTRAINT FK_ISOQP_TemporalExtent_Datasets 
	FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;

ALTER TABLE ISOQP_Title ADD CONSTRAINT FK_ISOQP_Title_Datasets 
	FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;

ALTER TABLE ISOQP_TopicCategory ADD CONSTRAINT FK_ISOQP_TopicCategory_Datasets 
	FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;

ALTER TABLE ISOQP_Type ADD CONSTRAINT FK_ISOQP_Type_Datasets 
	FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;
	
ALTER TABLE DCQP_Rights ADD CONSTRAINT FK_DCQP_Rights_Datasets 
	FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID);

	
	
	
--Source column in datasets
ALTER TABLE datasets ADD COLUMN source character varying(250);
ALTER TABLE datasets ALTER COLUMN source SET STORAGE EXTENDED;
COMMENT ON COLUMN datasets.source IS 'common queryable property in DC, but is not supported in ISO AP';

--Association column in datasets
ALTER TABLE datasets ADD COLUMN association character varying(50);
ALTER TABLE datasets ALTER COLUMN association SET STORAGE EXTENDED;
COMMENT ON COLUMN datasets.association IS 'common queryable property in DC, but is not supported in ISO AP';

--Geospatial column in isoqp_boundingbox
SELECT AddGeometryColumn('public','isoqp_boundingbox','bbox','-1','POLYGON','2');

--INSPIRE

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
	limitation character varying(500)  -- MD_Metadata.identificationInfo.MD_DataIdentification.resourceConstraints.MD_Constraints.useLimitation

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

