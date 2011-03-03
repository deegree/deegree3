CREATE TABLE Datasets ( 
    ID integer NOT NULL,
    version integer,    -- version of the record 
    status numeric(1),
    AnyText nvarchar(max),    -- common queryable property (ISO AP 1.0): Whole resource text. 
    Modified date,    -- common queryable property (ISO AP 1.0): MD_Metadata.dateStamp .Date 
    HasSecurityConstraints bit,    -- additional queryable property (ISO AP 1.0): MD_Metadata.AbstractMD_Identification.resourceConstraints.MD_securityConstraints (If an instance of the class MD_SecurityConstraint exists for a resource, the "HasSecurityConstraints" is "true", otherwise "false") 
    Language char(3),    -- additional queryable property (ISO AP 1.0): MD_Metadata.language 
    ParentIdentifier varchar(50),    -- additional queryable property (ISO AP 1.0): MD_Metadata.parentIdentifier
    source varchar(250),
    association varchar(50),
    recordfull varbinary(MAX) NOT NULL
);
   
-- index on anytext field must be tested    
-- CREATE INDEX anytext_idx ON Datasets (anytext);
CREATE INDEX modified_idx ON Datasets (modified);    
CREATE INDEX language_idx ON Datasets (language);
CREATE INDEX parentidentifier_idx ON Datasets (parentidentifier);

CREATE TABLE ISOQP_Abstract ( 
	ID integer NOT NULL,
	fk_datasets integer NOT NULL,
	Abstract nvarchar(max) NOT NULL    -- MD_Metadata.identificationInfo.AbstractMD_Identification.abstract 
);
    
CREATE NONCLUSTERED INDEX abstract_idx ON ISOQP_Abstract (ID) include (abstract);  

CREATE TABLE ISOQP_AlternateTitle ( 
	ID integer NOT NULL,
	fk_datasets integer NOT NULL,
	AlternateTitle varchar(500) NOT NULL    -- MD_Metadata.identificationInfo.AbstractMD_Identification.citation.CI_Citation.alternateTitle 
);
    
CREATE INDEX alternatetitle_idx ON ISOQP_AlternateTitle (alternatetitle);    

CREATE TABLE ISOQP_BoundingBox ( 
	ID integer NOT NULL,
	fk_datasets integer NOT NULL,
	bbox geometry NOT NULL,
	Authority varchar(250),    -- MD_Metadata.referenceSystemInfo.MD_ReferenceSystem.referenceSystemIdentifier.RS_Identifier.codeSpace 
	ID_CRS varchar(50),    -- MD_Metadata.referenceSystemInfo.MD_ReferenceSystem.referenceSystemIdentifier.RS_Identifier.code 
	Version varchar(50)    -- MD_Metadata.referenceSystemInfo.MD_ReferenceSystem.referenceSystemIdentifier.RS_Identifier.version 
);
 
ALTER TABLE ISOQP_BoundingBox ADD CONSTRAINT PK_ISOQP_BoundingBox PRIMARY KEY (ID);
CREATE INDEX id_crs_idx ON ISOQP_BoundingBox (id_crs);
CREATE SPATIAL INDEX [bbox_spx] ON [dbo].[ISOQP_BoundingBox] ([bbox] )USING  GEOMETRY_GRID 
WITH (BOUNDING_BOX =(-180, -90, 180, 90), GRIDS =(LEVEL_1 = MEDIUM,LEVEL_2 = MEDIUM,LEVEL_3 = MEDIUM,LEVEL_4 = MEDIUM), 
CELLS_PER_OBJECT = 16, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON);

CREATE TABLE ISOQP_CouplingType ( 
	ID integer NOT NULL,
	fk_datasets integer NOT NULL,
	CouplingType varchar(5) NOT NULL    -- MD_Metadata.identificationInfo.SV_ServiceIdentification.couplingType.SV_CouplingType.code@codeListValue 
);
    
CREATE INDEX couplingtype_idx ON ISOQP_CouplingType (couplingtype);

CREATE TABLE ISOQP_CreationDate ( 
	fk_datasets integer NOT NULL,
	CreationDate date NOT NULL,    -- MD_Identification.citation.CI_Citation.date.CI_Date[dateType.CI_DateTypeCode.@codeListValue='creation'].date.Date 
	ID integer NOT NULL
);
    
CREATE INDEX creationdate_idx ON ISOQP_CreationDate (creationdate);

CREATE TABLE ISOQP_Format ( 
	ID integer NOT NULL,
	fk_datasets integer NOT NULL,
	Format varchar(500) NOT NULL    -- MD_Metadata.distributionInfo.MD_Distribution.distributionFormat.MD_Format.name 
);
    
CREATE INDEX format_idx ON ISOQP_Format (format);

CREATE TABLE ISOQP_GeographicDescriptionCode ( 
	ID integer NOT NULL,
	fk_datasets integer NOT NULL,
	GeographicDescriptionCode varchar(500)    -- dataset, datasetcollection, application: MD_Metadata.identificationInfo.MD_DataIdentification.extent.EX_Extent.geographicElement.EX_GeographicDescription.geographicIdentifier.MD_Identifier.code 
-- service: MD_Metadata.identificationInfo.SV_ServiceIdentification.extent.EX_Extent.geographicElement.EX_GeographicDescription.geographicIdentifier.MD_Identifier.code 
);
 
CREATE INDEX geographicdescriptioncode_idx ON ISOQP_GeographicDescriptionCode (geographicdescriptioncode);

CREATE TABLE ISOQP_Keyword ( 
	ID integer NOT NULL,
	fk_datasets integer NOT NULL,
	KeywordType varchar(500),    -- MD_Metadata.identificationInfo.AbstractMD_Identification.descriptiveKeywords.MD_Keywords.type 
	Keyword varchar(500),    -- MD_Metadata.identificationInfo.AbstractMD_Identification.descriptiveKeywords.MD_Keywords.keyword 
	Thesaurus varchar(500)    -- MD_Metadata.identificationInfo.AbstractMD_Identification.descriptiveKeywords.MD_Keywords.thesaurusName.CI_Citation.title 
);
    
CREATE INDEX keyword_idx ON ISOQP_Keyword (keyword);    
CREATE INDEX thesaurus_idx ON ISOQP_Keyword (thesaurus);
CREATE INDEX keywordtype_idx ON ISOQP_Keyword (keywordtype);

CREATE TABLE ISOQP_OperatesOnData ( 
	ID integer NOT NULL,
	fk_datasets integer NOT NULL,
	OperatesOn varchar(50),    -- MD_Metadata.identificationInfo.SV_ServiceIdentification.operatesOn.MD_DataIdentification.citation.CI_Citation.identifier 
	OperatesOnIdentifier varchar(50),    -- MD_Metadata.identificationInfo.SV_ServiceIdentification.coupledResource.SV_CoupledResource.identifier 
	OperatesOnName varchar(50)    -- MD_Metadata.identificationInfo.SV_ServiceIdentification.coupledResource.SV_CoupledResource.operationName 
);
    
CREATE INDEX operateson_idx ON ISOQP_OperatesOnData (operateson);
CREATE INDEX operatesonname_idx ON ISOQP_OperatesOnData (operatesonname);
CREATE INDEX operatesonidentifier_idx ON ISOQP_OperatesOnData (operatesonidentifier);

CREATE TABLE ISOQP_Operation ( 
	ID integer NOT NULL,
	fk_datasets integer NOT NULL,
	Operation varchar(50) NOT NULL    -- MD_Metadata.identificationInfo.SV_ServiceIdentification.containsOperations.SV_OperationMetadata.operationName 
);
    
CREATE INDEX operation_idx ON ISOQP_Operation (Operation);

CREATE TABLE ISOQP_OrganisationName ( 
	ID integer NOT NULL,
	fk_datasets integer NOT NULL,
	OrganisationName varchar(500) NOT NULL    -- MD_Metadata.identificationInfo.AbstractMD_Identification.pointOfContact.CI_ResponsibleParty.organisationName 
);
    
CREATE INDEX organisationname_idx ON ISOQP_OrganisationName (organisationname);

CREATE TABLE ISOQP_PublicationDate ( 
	ID integer NOT NULL,
	fk_datasets integer NOT NULL,
	PublicationDate date NOT NULL    -- MD_Metadata.identificationInfo.AbstractMD_Identification.citation.CI_Citation.date.CI_Date[dateType.CI_DateTypeCode.@codeListValue='publication'].date.Date 
);
    
CREATE INDEX publicationdate_idx ON ISOQP_PublicationDate (publicationdate);    

CREATE TABLE ISOQP_ResourceIdentifier ( 
	ID integer NOT NULL,
	fk_datasets integer NOT NULL,
	ResourceIdentifier varchar(50) NOT NULL    -- MD_Metadata.identificationInfo.AbstractMD_Identification.citation.CI_Citation.identifier.MD_Identifier.code 
);
    
CREATE INDEX resourceidentifier_idx ON ISOQP_ResourceIdentifier (resourceidentifier);

CREATE TABLE ISOQP_ResourceLanguage ( 
	ID integer NOT NULL,
	fk_datasets integer NOT NULL,
	ResourceLanguage varchar(50) NOT NULL    -- MD_Metadata.identificationInfo.MD_DataIdentification.language 
);
    
CREATE INDEX resourcelanguage_idx ON ISOQP_ResourceLanguage (resourcelanguage);

CREATE TABLE ISOQP_RevisionDate ( 
	ID integer NOT NULL,
	fk_datasets integer NOT NULL,
	RevisionDate date NOT NULL    -- MD_Metadata.identificationInfo.AbstractMD_Identification.citation.CI_Citation.date.CI_Date[dateType.CI_DateTypeCode.@codeListValue='revision'].date.Date 
);
    
CREATE INDEX revisiondate_idx ON ISOQP_RevisionDate (revisiondate);    

CREATE TABLE ISOQP_ServiceType ( 
	ID integer NOT NULL,
	fk_datasets integer NOT NULL,
	ServiceType varchar(50) NOT NULL    -- MD_Metadata.identificationInfo.SV_ServiceIdentification.serviceType 
);
    
CREATE INDEX servicetype_idx ON ISOQP_ServiceType (servicetype);    

CREATE TABLE ISOQP_ServiceTypeVersion ( 
	ID integer NOT NULL,
	fk_datasets integer NOT NULL,
	ServiceTypeVersion varchar(15) NOT NULL    -- MD_Metadata.identificationInfo.SV_ServiceIdentification.serviceTypeVersion 
);
    
CREATE INDEX servicetypeversion_idx ON ISOQP_ServiceTypeVersion (servicetypeversion);    

CREATE TABLE ISOQP_SpatialResolution ( 
	ID integer NOT NULL,
	fk_datasets integer NOT NULL,
	Denominator integer,    -- MD_Metadata.identificationInfo.MD_DataIdentification.spatialResolution.MD_Resolution.equivalentScale.MD_RepresentativeFraction.denominator 
	DistanceValue decimal(10,4),    -- MD_Metadata.identificationInfo.MD_DataIdentification.spatialResolution.MD_Resolution.distance.gco:Distance 
	DistanceUOM varchar(50)    -- MD_Metadata.identificationInfo.MD_DataIdentification.spatialResolution.MD_Resolution.distance.gco:Distance@uom 
);
    
CREATE INDEX denominator_idx ON ISOQP_SpatialResolution (Denominator);    
CREATE INDEX distancevalue_idx ON ISOQP_SpatialResolution (distancevalue);
CREATE INDEX distanceuom_idx ON ISOQP_SpatialResolution (distanceuom);

CREATE TABLE ISOQP_TemporalExtent ( 
	ID integer NOT NULL,
	fk_datasets integer NOT NULL,
	TempExtent_begin datetime NOT NULL,    -- MD_Metadata.identificationInfo.MD_DataIdentification.extent.EX_Extent.temporalElement.EX_TemporalExtent.extent.TimePeriod.beginPosition 
	TempExtent_end datetime NOT NULL    -- MD_Metadata.identificationInfo.MD_DataIdentification.extent.EX_Extent.temporalElement.EX_TemporalExtent.extent.TimePeriod.endPosition 
);
    
CREATE INDEX tempExtent_begin_idx ON ISOQP_TemporalExtent (tempExtent_begin);    

CREATE TABLE ISOQP_Title ( 
	ID integer NOT NULL,
	fk_datasets integer NOT NULL,
	Title varchar(500) NOT NULL    -- MD_Metadata.identificationInfo.AbstractMD_Identification.citation.CI_Citation.title 
);
    
CREATE INDEX title_idx ON ISOQP_Title (title);    

CREATE TABLE ISOQP_TopicCategory ( 
	ID integer NOT NULL,
	fk_datasets integer NOT NULL,
	TopicCategory varchar(50) NOT NULL    -- MD_Metadata.identificationInfo.MD_DataIdentification.topicCategory 
);
    
CREATE INDEX topiccategory_idx ON ISOQP_TopicCategory (topiccategory);    

CREATE TABLE ISOQP_Type ( 
	ID integer NOT NULL,
	fk_datasets integer NOT NULL,
	Type varchar(50) NOT NULL    -- MD_Metadata.hierarchyLevel.MD_ScopeCode/@codeListValue. If MD_Metadata.hierarchyLevel is missing, 'Type' will be considered as "Dataset" (default). 
);
    
CREATE INDEX type_idx ON ISOQP_Type (type);    

CREATE TABLE QP_Identifier ( 
ID integer NOT NULL,
fk_datasets integer NOT NULL,
identifier varchar(150) NOT NULL    -- MD_Metadata.fileIdentifier
);
    
CREATE INDEX identifier_idx ON QP_Identifier (identifier);    


CREATE TABLE DCQP_RIGHTS ( 
	ID integer NOT NULL,
	fk_datasets integer NOT NULL,
	rights varchar(100) NOT NULL   
);
    
CREATE INDEX rights_idx ON DCQP_RIGHTS (rights);    


-- Create Primary Key Constraints 
ALTER TABLE Datasets ADD CONSTRAINT PK_Datasets  PRIMARY KEY (ID);
	
ALTER TABLE QP_Identifier ADD CONSTRAINT PK_QP_Identifier PRIMARY KEY (ID);

ALTER TABLE ISOQP_Abstract ADD CONSTRAINT PK_ISOQP_Abstract PRIMARY KEY (ID);

ALTER TABLE ISOQP_AlternateTitle ADD CONSTRAINT PK_ISOQP_AlternateTitle PRIMARY KEY (ID);

ALTER TABLE ISOQP_CouplingType ADD CONSTRAINT PK_ISOQP_CouplingType PRIMARY KEY (ID);

ALTER TABLE ISOQP_CreationDate ADD CONSTRAINT PK_ISOQP_CreationDate PRIMARY KEY (ID);

ALTER TABLE ISOQP_Format ADD CONSTRAINT PK_ISOQP_Format PRIMARY KEY (ID);

ALTER TABLE ISOQP_GeographicDescriptionCode ADD CONSTRAINT PK_ISOQP_GeographicDescriptionCode PRIMARY KEY (ID);

ALTER TABLE ISOQP_Keyword ADD CONSTRAINT PK_ISOQP_Keyword PRIMARY KEY (ID);

ALTER TABLE ISOQP_OperatesOnData ADD CONSTRAINT PK_ISOQP_OperatesOnData PRIMARY KEY (ID);

ALTER TABLE ISOQP_Operation ADD CONSTRAINT PK_ISOQP_Operation PRIMARY KEY (ID);

ALTER TABLE ISOQP_OrganisationName ADD CONSTRAINT PK_ISOQP_OrganisationName PRIMARY KEY (ID);

ALTER TABLE ISOQP_PublicationDate ADD CONSTRAINT PK_ISOQP_PublicationDate PRIMARY KEY (ID);

ALTER TABLE ISOQP_ResourceIdentifier ADD CONSTRAINT PK_ISOQP_ResourceIdentifier PRIMARY KEY (ID);

ALTER TABLE ISOQP_ResourceLanguage ADD CONSTRAINT PK_ISOQP_ResourceLanguage PRIMARY KEY (ID);

ALTER TABLE ISOQP_RevisionDate ADD CONSTRAINT PK_ISOQP_RevisionDate PRIMARY KEY (ID);

ALTER TABLE ISOQP_ServiceType ADD CONSTRAINT PK_ISOQP_ServiceType PRIMARY KEY (ID);

ALTER TABLE ISOQP_ServiceTypeVersion ADD CONSTRAINT PK_ISOQP_ServiceTypeVersion PRIMARY KEY (ID);

ALTER TABLE ISOQP_SpatialResolution ADD CONSTRAINT PK_ISOQP_SpatialResolution PRIMARY KEY (ID);

ALTER TABLE ISOQP_TemporalExtent ADD CONSTRAINT PK_ISOQP_TemporalExtent PRIMARY KEY (ID);

ALTER TABLE ISOQP_Title ADD CONSTRAINT PK_ISOQP_Title PRIMARY KEY (ID);

ALTER TABLE ISOQP_TopicCategory ADD CONSTRAINT PK_ISOQP_TopicCategory PRIMARY KEY (ID);

ALTER TABLE ISOQP_Type ADD CONSTRAINT PK_ISOQP_Type PRIMARY KEY (ID);

--ALTER TABLE UserDefinedQueryableProperties ADD CONSTRAINT PK_SelfQueryableProperties 
--	PRIMARY KEY (fk_datasets);

ALTER TABLE DCQP_Rights ADD CONSTRAINT PK_DCQP_Rights  PRIMARY KEY (ID);


-- Create Foreign Key Constraints 
ALTER TABLE QP_Identifier ADD CONSTRAINT FK_QP_Identifier_Datasets FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;
	
ALTER TABLE ISOQP_Abstract ADD CONSTRAINT FK_ISOQP_Abstract_Datasets  FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;

ALTER TABLE ISOQP_AlternateTitle ADD CONSTRAINT FK_ISOQP_AlternateTitle_Datasets  FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;

ALTER TABLE ISOQP_BoundingBox ADD CONSTRAINT FK_ISOQP_BoundingBox_Datasets  FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;

ALTER TABLE ISOQP_CouplingType ADD CONSTRAINT FK_ISOQP_CouplingType_Datasets  FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;

ALTER TABLE ISOQP_CreationDate ADD CONSTRAINT FK_ISOQP_CreationDate_Datasets  FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;

ALTER TABLE ISOQP_Format ADD CONSTRAINT FK_ISOQP_Format_Datasets  FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;

ALTER TABLE ISOQP_GeographicDescriptionCode ADD CONSTRAINT FK_ISOQP_GeographicDescriptionCode_Datasets  FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;

ALTER TABLE ISOQP_Keyword ADD CONSTRAINT FK_ISOQP_KeywordType_Datasets  FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;

ALTER TABLE ISOQP_OperatesOnData ADD CONSTRAINT FK_ISOQP_OperatesOnData_Datasets  FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;

ALTER TABLE ISOQP_Operation ADD CONSTRAINT FK_ISOQP_Operation_Datasets  FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;

ALTER TABLE ISOQP_OrganisationName ADD CONSTRAINT FK_ISOQP_OrganisationName_Datasets  FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;

ALTER TABLE ISOQP_PublicationDate ADD CONSTRAINT FK_ISOQP_PublicationDate_Datasets  FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;

ALTER TABLE ISOQP_ResourceIdentifier ADD CONSTRAINT FK_ISOQP_ResourceIdentifier_Datasets  FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;

ALTER TABLE ISOQP_ResourceLanguage ADD CONSTRAINT FK_ISOQP_ResourceLanguage_Datasets  FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;

ALTER TABLE ISOQP_RevisionDate ADD CONSTRAINT FK_ISOQP_RevisionDate_Datasets  FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;

ALTER TABLE ISOQP_ServiceType ADD CONSTRAINT FK_ISOQP_ServiceType_Datasets  FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;

ALTER TABLE ISOQP_ServiceTypeVersion ADD CONSTRAINT FK_ISOQP_ServiceTypeVersion_Datasets  FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;

ALTER TABLE ISOQP_SpatialResolution ADD CONSTRAINT FK_ISOQP_SpatialResolution_Datasets  FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;

ALTER TABLE ISOQP_TemporalExtent ADD CONSTRAINT FK_ISOQP_TemporalExtent_Datasets  FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;

ALTER TABLE ISOQP_Title ADD CONSTRAINT FK_ISOQP_Title_Datasets  FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;

ALTER TABLE ISOQP_TopicCategory ADD CONSTRAINT FK_ISOQP_TopicCategory_Datasets  FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;

ALTER TABLE ISOQP_Type ADD CONSTRAINT FK_ISOQP_Type_Datasets  FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID) ON DELETE CASCADE;
	
ALTER TABLE DCQP_Rights ADD CONSTRAINT FK_DCQP_Rights_Datasets  FOREIGN KEY (fk_datasets) REFERENCES Datasets (ID);


