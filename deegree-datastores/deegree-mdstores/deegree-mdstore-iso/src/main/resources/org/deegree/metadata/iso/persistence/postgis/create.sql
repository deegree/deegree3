CREATE TABLE IDXTB_MAIN ( 
	id integer NOT NULL,
	version integer,   
	status numeric(1),
	title varchar(500) NOT NULL, -- can occure multiple times, considering multi languages
	alternatetitles varchar(500),
	abstract text NOT NULL, -- can occure multiple times, considering multi languages
	anytext text NOT NULL,
	fileidentifier varchar(150) NOT NULL,
	modified timestamp NOT NULL,
	type varchar(25),
	topicCategories varchar(1000), 
	revisiondate timestamp,
	creationdate timestamp,
	publicationdate timestamp,
	OrganisationName varchar(250),
	HasSecurityConstraints boolean,
	Language char(3),
	ResourceId varchar(150),-- NOT NULL,
	ParentId varchar(150),
	ResourceLanguage char(3),
	GeographicDescriptionCode varchar(500),
	Denominator integer,
	DistanceValue decimal(10,2),
	DistanceUOM varchar(250),
	TempExtent_begin timestamp,
	TempExtent_end timestamp,
	ServiceType varchar(150),
	ServiceTypeVersion varchar(1000),
	CouplingType varchar(10),
	formats varchar(2000),
	Operations varchar(2000),
	degree boolean,
	lineage text,
	RespPartyRole varchar(25),
	SpecDate timestamp,
	SpecDateType varchar(15),
	SpecTitle varchar(500),
	recordfull bytea NOT NULL
);

-- ISO AP CQPs
COMMENT ON COLUMN IDXTB_MAIN.version IS 'version of the record';
COMMENT ON COLUMN IDXTB_MAIN.title IS 'A name given to the resource: MD_Metadata.identificationInfo[1].AbstractMD_Identification.citation.CI_Citation.title';
COMMENT ON COLUMN IDXTB_MAIN.abstract IS 'A summary of the content of the resource: MD_Metadata.identificationInfo[1].AbstractMD_Identification.abstract';
COMMENT ON COLUMN IDXTB_MAIN.anytext IS 'A target for full-text search of character data types in a catalogue';
COMMENT ON COLUMN IDXTB_MAIN.fileidentifier IS 'A unique reference to the record within the catalogue: MD_Metadata.fileIdentifier';
COMMENT ON COLUMN IDXTB_MAIN.modified IS 'Date on which the record was created or updated within the catalogue: MD_Metadata.dateStamp.Date';
COMMENT ON COLUMN IDXTB_MAIN.type IS 'The nature or genre of the content of the resource. Type can include general categories, genres or aggregation levels of content: MD_Metadata.hierarchyLevel.MD_ScopeCode/@codeListValue. If MD_Metadata .hierarchyLevel is missing, Type will be considered as Dataset (default).';
COMMENT ON COLUMN IDXTB_MAIN.topicCategories IS 'The topic of the content of the resource: this field will be addesser by CQP subject and TopicCategory!: MD_Metadata.identificationInfo[1].MD_DataIdentification.topicCategory (also MD_Metadata.identificationInfo.AbstractMD_Identification.descriptiveKeywords.MD_Keywords.keyword is mapped to CQP subject)';
COMMENT ON COLUMN IDXTB_MAIN.revisiondate IS 'Revision date of the resource: MD_Metadata.identificationInfo[1].AbstractMD_Identification.citation.CI_Citation.date.CI_Date[dateType.CI_DateTypeCode.@codeListValue="revision"].date.Date';
COMMENT ON COLUMN IDXTB_MAIN.creationdate IS 'Creation date of the resource: MD_Metadata.identificationInfo[1].AbstractMD_Identification.citation.CI_Citation.date.CI_Date[dateType.CI_DateTypeCode.@codeListValue="creation"].date.Date';
COMMENT ON COLUMN IDXTB_MAIN.publicationdate IS 'Publication date of the resource: MD_Metadata.identificationInfo[1].AbstractMD_Identification.citation.CI_Citation.date.CI_Date[dateType.CI_DateTypeCode.@codeListValue="publication"].date.Date';
COMMENT ON COLUMN IDXTB_MAIN.OrganisationName IS 'Name of the organisation providing the resource: MD_Metadata.identificationInfo[1].AbstractMD_Identification.pointOfContact.CI_ResponsibleParty.organisationName';
COMMENT ON COLUMN IDXTB_MAIN.HasSecurityConstraints IS 'Are there any security constraints?: MD_Metadata.identificationInfo[1].AbstractMD_Identification.resourceConstraints.MD_SecurityConstraints';
COMMENT ON COLUMN IDXTB_MAIN.Language IS 'Language of the metadata: MD_Metadata.language';
COMMENT ON COLUMN IDXTB_MAIN.ResourceId IS 'Identifier of the resource: MD_Metadata.identificationInfo[1].AbstractMD_Identification.citation.CI_Citation.identifier.MD_Identifier.code';
COMMENT ON COLUMN IDXTB_MAIN.ParentId IS 'Fileidentifier of the metadata to which this metadata is a subset (child): MD_Metadata.parentIdentifier';
COMMENT ON COLUMN IDXTB_MAIN.ResourceLanguage IS 'Language(s) used within the dataset: MD_Metadata.identificationInfo[1].MD_DataIdentification.language';
COMMENT ON COLUMN IDXTB_MAIN.GeographicDescriptionCode IS 'Description of the geographic area using identifiers. MD_Metadata.identificationInfo[1].MD_DataIdentification.extent.EX_Extent.geographicElement.EX_GeographicDescription.geographicIdentifier.MD_Identifier.code or MD_Metadata.identificationInfo[1].SV_ServiceIdentification.extent.EX_Extent.geographicElement.EX_GeographicDescription.geographicIdentifier.MD_Identifier.code';
COMMENT ON COLUMN IDXTB_MAIN.Denominator IS 'Level of detail expressed as a scale factor or a ground distance. Here: the number below the line in a vulgar fraction. Only used, if DistanceValue and DistanceUOM are not used: MD_Metadata.identificationInfo[1].MD_DataIdentification.spatialResolution.MD_Resolution.equivalentScale.MD_RepresentativeFraction.denominator';
COMMENT ON COLUMN IDXTB_MAIN.DistanceValue IS 'Sample ground distance. Here: the distance as decimal value. Only used, if Denominator is not used. MD_Metadata.identificationInfo[1].MD_DataIdentification.spatialResolution.MD_Resolution.distance.gco:Distance';
COMMENT ON COLUMN IDXTB_MAIN.DistanceUOM IS 'Sample ground distance. Here: the name of the unit of measure. Only used, if Denominator is not used. MD_Metadata.identificationInfo[1].MD_DataIdentification.spatialResolution.MD_Resolution.distance.gco:Distance@uom';
COMMENT ON COLUMN IDXTB_MAIN.TempExtent_begin IS 'Temporal extent information begin: MD_Metadata.identificationInfo[1].MD_DataIdentification.extent.EX_Extent.temporalElement.EX_TemporalExtent.extent.TimePeriod.beginPosition';
COMMENT ON COLUMN IDXTB_MAIN.TempExtent_end IS 'Temporal extent information end: MD_Metadata.identificationInfo[1].MD_DataIdentification.extent.EX_Extent.temporalElement.EX_TemporalExtent.extent.TimePeriod.endPosition';
COMMENT ON COLUMN IDXTB_MAIN.ServiceType IS 'Name of a service type. MD_Metadata.identificationInfo[1].SV_ServiceIdentification.serviceType';
COMMENT ON COLUMN IDXTB_MAIN.ServiceTypeVersion IS 'The version of a service type. MD_Metadata.identificationInfo[1].SV_ServiceIdentification.serviceTypeVersion';
COMMENT ON COLUMN IDXTB_MAIN.CouplingType IS 'The coupling type of thisservice: MD_Metadata.identificationInfo[1].SV_ServiceIdentification.couplingType.SV_CouplingType.code@codeListValue';
COMMENT ON COLUMN IDXTB_MAIN.Operations IS 'Name of a service operation.: MD_Metadata.identificationInfo[1].SV_ServiceIdentification.containsOperations.SV_OperationMetadata.operationName';
COMMENT ON COLUMN IDXTB_MAIN.formats IS 'The physical or digital manifestation of the resource: MD_Metadata.distributionInfo.MD_Distribution.distributionFormat.MD_Format.name';
-- INSPIRE CQPs
COMMENT ON COLUMN IDXTB_MAIN.degree IS 'This is the degree of conformity of the resource to the related specification: dataQualityInfo/*/report/*/result/*/pass';
COMMENT ON COLUMN IDXTB_MAIN.lineage IS 'This is a statement on process history and/or overall quality of the spatial dataset. dataQualityInfo/*/lineage/*/statement';
COMMENT ON COLUMN IDXTB_MAIN.RespPartyRole IS 'The function performed by the responsible party. identificationInfo[1]/*/pointOfContact/*/role';
COMMENT ON COLUMN IDXTB_MAIN.SpecDate IS 'Reference date of specification: dataQualityInfo/*/report/*/result/*/specification/*/date/*/date';
COMMENT ON COLUMN IDXTB_MAIN.SpecDateType IS 'Type reference date of specification: dataQualityInfo/*/report/*/result/*/specification/*/date/*/dateType';
COMMENT ON COLUMN IDXTB_MAIN.SpecTitle IS 'Title of the specification dataQualityInfo/*/report/*/result/*/specification/*/title';


--Geospatial column in idxtb_main.bbox
SELECT AddGeometryColumn('public','idxtb_main','bbox',-1,'GEOMETRY','2');

CREATE TABLE IDXTB_Constraint ( 
	id integer NOT NULL,
	fk_main integer NOT NULL,
	ConditionAppToAcc text,
	AccessConstraints varchar(500),
	OtherConstraints text,
	Classification varchar(20)
);

COMMENT ON COLUMN IDXTB_Constraint.ConditionAppToAcc IS 'This metadata element defines the conditions for access and use of spatial datasets and services, and where applicable, corresponding fees as required by Articles 5-2 (b) and 11-2 (f). identificationInfo[1]/*/resourceConstraints/*/useLimitation';
COMMENT ON COLUMN IDXTB_Constraint.AccessConstraints IS 'Access constraints applied to assure the protection of privacy or intellectual property, and any special restrictions or limitations on obtaining the resource. identificationInfo[1]/*/resourceConstraints/*/accessConstraints';
COMMENT ON COLUMN IDXTB_Constraint.OtherConstraints IS 'other restrictions and legal prerequisites for accessing and using the resource: identificationInfo[1]/*/reso urceConstraints/*/otherC onstraints';
COMMENT ON COLUMN IDXTB_Constraint.Classification IS 'name of the handling restrictions on the resource. identificationInfo[1]/*/resourceConstraints/*/classification';

CREATE TABLE IDXTB_CRS ( 
	id integer NOT NULL,
	fk_main integer NOT NULL,
	authority varchar(500),
	crsid varchar(500) NOT NULL,
	version varchar(50)
);

COMMENT ON COLUMN IDXTB_CRS.authority IS 'Authority of the CRS: MD_Metadata.referenceSystemInfo.MD_ReferenceSystem.referenceSystemIdentifier.RS_Identifier.codeSpace';
COMMENT ON COLUMN IDXTB_CRS.crsid IS 'ID of the CRS: MD_Metadata.referenceSystemInfo.MD_ReferenceSystem.referenceSystemIdentifier.RS_Identifier.code';
COMMENT ON COLUMN IDXTB_CRS.version IS 'Version to which the CRS encoding refers to: MD_Metadata.referenceSystemInfo.MD_ReferenceSystem.referenceSystemIdentifier.RS_Identifier.version';

CREATE TABLE IDXTB_KEYWORD ( 
	id integer NOT NULL,
	fk_main integer NOT NULL,
	keywords text NOT NULL,
	keywordtype varchar(250)
);

COMMENT ON COLUMN IDXTB_KEYWORD.keywords IS 'The topic of the content of the resource: MD_Metadata.identificationInfo[1].AbstractMD_Identification.descriptiveKeywords.MD_Keywords.keyword';
COMMENT ON COLUMN IDXTB_KEYWORD.keywordtype IS 'Methods used to group similar keywords: MD_Metadata.identificationInfo.Abstract.MD_Identification.descriptiveKeywords.MD_Keywords.type';

CREATE TABLE IDXTB_OperatesOnData ( 
	id integer NOT NULL,
	fk_main integer NOT NULL,
	OperatesOn varchar(150),
	OperatesOnId varchar(150) NOT NULL,
	OperatesOnName varchar(150) NOT NULL
);

COMMENT ON COLUMN IDXTB_OperatesOnData.OperatesOn IS 'Identifier of a dataset tightly coupled with the service instance: MD_Metadata.identificationInfo[1].SV_ServiceIdentification.operatesOn.MD_DataIdentification.citation.CI_Citation.identifier';
COMMENT ON COLUMN IDXTB_OperatesOnData.OperatesOnId IS 'Identifier of a tightly coupled dataset on which the service operates with a specific operation: MD_Metadata.identificationInfo[1].SV_ServiceIdentification.coupledResource.SV_CoupledResource.identifier';
COMMENT ON COLUMN IDXTB_OperatesOnData.OperatesOnName IS 'Name of an operation with which the service operates on a tightly coupled dataset with a specific identifier: MD_Metadata.identificationInfo[0].SV_ServiceIdentification.coupledResource.SV_CoupledResource.operationName';


-- set primary keys including unique constraints
ALTER TABLE IDXTB_MAIN ADD CONSTRAINT PK_IDXTB_MAIN PRIMARY KEY (id);
ALTER TABLE IDXTB_Constraint ADD CONSTRAINT PK_Constraint PRIMARY KEY (id);
ALTER TABLE IDXTB_CRS ADD CONSTRAINT PK_IDXTB_CRS  PRIMARY KEY (id);
ALTER TABLE IDXTB_KEYWORD ADD CONSTRAINT PK_IDXTB_KEYWORD PRIMARY KEY (id);
ALTER TABLE IDXTB_OperatesOnData ADD CONSTRAINT PK_OperatesOnData PRIMARY KEY (id);

ALTER TABLE IDXTB_MAIN ADD CONSTRAINT UQ_IDXTB_MAIN_id UNIQUE (id);
ALTER TABLE IDXTB_Constraint ADD CONSTRAINT UQ_Constraint_id UNIQUE (id);
ALTER TABLE IDXTB_CRS ADD CONSTRAINT UQ_IDXTB_CRS_id UNIQUE (id);
ALTER TABLE IDXTB_KEYWORD ADD CONSTRAINT UQ_IDXTB_KEYWORD_id UNIQUE (id);
ALTER TABLE IDXTB_OperatesOnData ADD CONSTRAINT UQ_OperatesOnData_id UNIQUE (id);

-- set unique constraint
ALTER TABLE IDXTB_MAIN ADD CONSTRAINT UQ_IDXTB_MAIN_fileidentifier UNIQUE (fileidentifier);

-- Create Foreign Key Constraints 
ALTER TABLE IDXTB_Constraint ADD CONSTRAINT FK_IDXTB_Constraint_IDXTB_MAIN FOREIGN KEY (fk_main) REFERENCES IDXTB_MAIN (ID) ON DELETE CASCADE;
ALTER TABLE IDXTB_CRS ADD CONSTRAINT FK_IDXTB_CRS_IDXTB_MAIN FOREIGN KEY (fk_main) REFERENCES IDXTB_MAIN (ID) ON DELETE CASCADE;
ALTER TABLE IDXTB_KEYWORD ADD CONSTRAINT FK_IDXTB_KEYWORD_IDXTB_MAIN FOREIGN KEY (fk_main) REFERENCES IDXTB_MAIN (ID) ON DELETE CASCADE;
ALTER TABLE IDXTB_OperatesOnData ADD CONSTRAINT FK_IDXTB_OperatesOnData_IDXTB_MAIN FOREIGN KEY (fk_main) REFERENCES IDXTB_MAIN (ID) ON DELETE CASCADE;

-- set index for alpha numeric columns 
-- IDXTB_MAIN
CREATE INDEX title_idx ON IDXTB_MAIN (title);
CREATE INDEX abstract_idx ON IDXTB_MAIN (abstract);
CREATE INDEX anytext_idx ON IDXTB_MAIN (anytext);
CREATE INDEX fileidentifier_idx ON IDXTB_MAIN (fileidentifier);
CREATE INDEX modified_idx ON IDXTB_MAIN (modified);
CREATE INDEX type_idx ON IDXTB_MAIN (type);
CREATE INDEX topicCategories_idx ON IDXTB_MAIN (topicCategories);
CREATE INDEX publicationdate_idx ON IDXTB_MAIN (publicationdate);
CREATE INDEX creationdate_idx ON IDXTB_MAIN (creationdate);
CREATE INDEX revisiondate_idx ON IDXTB_MAIN (revisiondate);
CREATE INDEX OrganisationName_idx ON IDXTB_MAIN (OrganisationName);
CREATE INDEX Language_idx ON IDXTB_MAIN (Language);
CREATE INDEX ResourceId_idx ON IDXTB_MAIN (ResourceId);
CREATE INDEX ParentId_idx ON IDXTB_MAIN (ParentId);
CREATE INDEX ResourceLanguage_idx ON IDXTB_MAIN (ResourceLanguage);
CREATE INDEX Denominator_idx ON IDXTB_MAIN (Denominator);
CREATE INDEX DistanceValue_idx ON IDXTB_MAIN (DistanceValue);
CREATE INDEX DistanceUOM_idx ON IDXTB_MAIN (DistanceUOM);
CREATE INDEX TempExtent_begin_idx ON IDXTB_MAIN (TempExtent_begin);
CREATE INDEX TempExtent_end_idx ON IDXTB_MAIN (TempExtent_end);
CREATE INDEX ServiceType_idx ON IDXTB_MAIN (ServiceType);
CREATE INDEX ServiceTypeVersion_idx ON IDXTB_MAIN (ServiceTypeVersion);
CREATE INDEX CouplingType_idx ON IDXTB_MAIN (CouplingType);
CREATE INDEX lineage_idx ON IDXTB_MAIN (lineage);
CREATE INDEX RespPartyRole_idx ON IDXTB_MAIN (RespPartyRole);
CREATE INDEX SpecDate_idx ON IDXTB_MAIN (SpecDate);
CREATE INDEX SpecDateType_idx ON IDXTB_MAIN (SpecDateType);
CREATE INDEX SpecTitle_idx ON IDXTB_MAIN (SpecTitle);
CREATE INDEX formats_idx ON IDXTB_MAIN (formats);
CREATE INDEX Operations_idx ON IDXTB_MAIN (Operations);
-- IDXTB_Constraint
CREATE INDEX fk_main_constraint_idx ON IDXTB_Constraint (fk_main);
CREATE INDEX ConditionAppToAcc_idx ON IDXTB_Constraint (ConditionAppToAcc);
CREATE INDEX AccessConstraints_idx ON IDXTB_Constraint (AccessConstraints);
CREATE INDEX OtherConstraints_idx ON IDXTB_Constraint (OtherConstraints);
CREATE INDEX Classification_idx ON IDXTB_Constraint (Classification);
-- IDXTB_CRS
CREATE INDEX fk_main_crs_idx ON IDXTB_CRS (fk_main);
CREATE INDEX crsid_idx ON IDXTB_CRS (crsid);
CREATE INDEX authority_idx ON IDXTB_CRS (authority);
CREATE INDEX version_idx ON IDXTB_CRS (version);
-- IDXTB_KEYWORD
CREATE INDEX fk_main_keyword_idx ON IDXTB_KEYWORD (fk_main);
CREATE INDEX keywords_idx ON IDXTB_KEYWORD (keywords);
CREATE INDEX keywordtype_idx ON IDXTB_KEYWORD (keywordtype);
-- IDXTB_OperatesOnData
CREATE INDEX fk_main_OperatesOnData_idx ON IDXTB_OperatesOnData (fk_main);
CREATE INDEX OperatesOnId_idx ON IDXTB_OperatesOnData (OperatesOnId);
CREATE INDEX OperatesOnName_idx ON IDXTB_OperatesOnData (OperatesOnName);
CREATE INDEX OperatesOn_idx ON IDXTB_OperatesOnData (OperatesOn);

-- set spatial index 
CREATE INDEX IDXTB_MAIN_spx ON IDXTB_MAIN USING GIST ( bbox );
