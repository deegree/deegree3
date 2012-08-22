-- TABLES
CREATE TABLE AlternateTitle ( 
	FK_Citation number(12),
	alternateTitle varchar2(250 char),
	ID number(12)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE CI_Address ( 
	ID number(12),
	city varchar2(200 char),
	administrativeArea varchar2(200 char),
	postalCode varchar2(50 char),
	country varchar2(50 char)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE CI_Citation ( 
	ID number(12),
	title varchar2(200 char),
	edition varchar2(2000 char),
	editionDate TIMESTAMP,
	identifier varchar2(250 char),
	ISBN varchar2(200 char),
	ISSN varchar2(200 char),
	revisionDate TIMESTAMP,
	creationDate TIMESTAMP,
	fk_fileidentifier number(12),
	otherCitationDetails varchar2(500 char),
	context varchar2(50 char)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE CI_Contact ( 
	ID number(12),
	FK_Address number(12),
	FK_OnlineResource number(12),
	hoursOfService varchar2(500 char),
	contactInstructions varchar2(4000 char)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE CI_OnLineFunctionCode ( 
	ID number(12),
	codeListValue varchar2(500 char),
	codeSpace varchar2(500 char)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE CI_OnlineResource ( 
	ID number(12),
	linkage varchar2(500 char),
	protocol varchar2(100 char),
	name varchar2(200 char),
	description varchar2(500 char),
	FK_function number(12)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE CI_PresentationFormCode ( 
	ID number(12),
	codeListValue varchar2(50 char),
	codeSpace number(12)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE CI_RespParty ( 
	ID number(12),
	individualName varchar2(500 char),
	organisationName varchar2(500 char),
	positionName varchar2(200 char),
	FK_Role number(12),
	FK_Contact number(12)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE CI_RoleCode ( 
	ID number(12),
	codeListValue varchar2(500 char),
	codeSpace varchar2(500 char)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE CI_Series ( 
	ID number(12),
	FK_Citation number(12),
	name varchar2(150 char),
	issueIdentification varchar2(500 char),
	page varchar2(250 char)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE CSW_CouplingType ( 
	ID number(12),
	codeListValue varchar2(50 char),
	codeSpace varchar2(250 char)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE CSW_ServiceIdentification ( 
	ID number(12),
	serviceType varchar2(50 char),
	fees varchar2(250 char),
	PLANNEDAVAILDATETIME TIMESTAMP,
	orderingInstructions varchar2(2000 char),
	turnaround varchar2(500 char),
	FK_LegalConst number(12),
	FK_Const number(12),
	FK_CouplingType number(12),
	FK_SecConst number(12)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE DeliveryPoint ( 
	ID number(12),
	deliveryPoint varchar2(500 char)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE DQ_ConformanceResult (
	ID number(12),
	fk_element number(12),
	explanation varchar2(500 char),
	pass CHAR,
	fk_citation number(12)
)
 NOLOGGING  NOMONITORING;
 
CREATE TABLE DQ_DataQuality ( 
	ID number(12),
	ScopeLevelCodeListValue varchar2(150 char),
	ScopeLeveDescription varchar2(4000 char),
	lineageStatement varchar2(4000 char),
	FK_Metadata number(12)
) 
 NOLOGGING  NOMONITORING;
 
CREATE TABLE DQ_Element ( 
	ID number(12),
	nameOfMeasure varchar2(50 char),
	measureIdentCode varchar2(150 char),
	measureIdentCodeSpace varchar2(250 char),
	type varchar2(50 char),
	FK_DataQuality number(12)
)
 NOLOGGING  NOMONITORING;
 
CREATE TABLE DQ_QuantitativeResult (
	ID number(12),
	fk_element number(12),
	identifier varchar2(50 char),
	codeSpace varchar2(50 char)
)
 NOLOGGING  NOMONITORING;
 
CREATE TABLE DS_AssociationTypeCode (
	ID number(12),
	codeListValue varchar2(50 char),
	codeSpace varchar2(500 char)
)
 NOLOGGING  NOMONITORING;
 
CREATE TABLE ElectronicMailAddress ( 
	ID number(12),
	email varchar2(200 char)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE EX_BoundingPolygon ( 
	FK_DataIdent number(12),
	description varchar2(4000 char),
	ID number(12),
	crs varchar2(150 char),
	geom SDO_Geometry
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE EX_GeographicDescription ( 
	ID number(12),
	geographicIdentifierCode varchar2(150 char),
	FK_DataIdent number(12)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE EX_GeogrBBox ( 
	ID number(12),
	FK_Owner number(12),
	description varchar2(4000 char),
	crs varchar2(150 char),
	geom SDO_Geometry
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE EX_TemporalExtent ( 
	FK_DataIdent number(12),
	description varchar2(4000 char),
	begin_ TIMESTAMP,
	end_ TIMESTAMP,
	tmePosition TIMESTAMP,
	ID number(12)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE EX_VerticalExtent ( 
	ID number(12),
	minVal NUMBER(36,8),
	maxVal NUMBER(36,8),
	description varchar2(4000 char),
	FK_DataIdent number(12),
	verticaldatum varchar2(4000 char),
	hrefAttribute varchar2(4000 char)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE Facsimile ( 
	FK_Contact number(12),
	faxnumber varchar2(500 char),
	ID number(12)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE FAILEDREQUESTS ( 
	ID number(12),
	REQUEST varchar2(4000 char),
	CSWADDRESS varchar2(500 char),
	REPEAT number(12)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE FeatureTypes ( 
	localName varchar2(100 char),
	FK_FeatCatDesc number(12),
	ID number(12)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE FileIdentifier ( 
	ID number(12),
	fileidentifier varchar2(250 char)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE HierarchylevelCode ( 
	ID number(12),
	codeListValue varchar2(150 char),
	codeSpace varchar2(500 char)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE HierarchylevelName ( 
	ID number(12),
	Name varchar2(100 char)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE JT_Address_DelivPoint ( 
	FK_Address number(12),
	FK_DelivPoint number(12)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE JT_Address_Email ( 
	FK_Address number(12),
	FK_Email number(12)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE JT_Citation_PresForm ( 
	FK_Citation  number(12),
	FK_PresFormCode number(12)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE JT_Citation_RespParty ( 
	FK_Citation number(12),
	FK_RespParty number(12)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE JT_Citation_Ident ( 
    FK_Citation number(12),
    FK_Identifier number(12)
) 
 NOLOGGING  NOMONITORING;
 
CREATE TABLE JT_DataIdent_CharSet ( 
	FK_DataIdent number(12),
	FK_CharSet number(12)
) 
 NOLOGGING  NOMONITORING;
 
CREATE TABLE JT_DataIdent_SpatialRepType ( 
	FK_DataIdent number(12),
	FK_SpatialRepType number(12)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE JT_DataIdent_TopicCat ( 
	FK_DataIdent number(12),
	FK_TopicCategory number(12)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE JT_DigTransOpt_MediumFormat ( 
	fk_digtransopt number(12),
	fk_mediumformat number(12)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE JT_DigTransOpt_MediumName ( 
	fk_digtransopt number(12),
	fk_mediumname number(12)
) 
 NOLOGGING  NOMONITORING;
 
CREATE TABLE JT_DigTransOpt_OnlineRes (
	fk_digtransopt number(12),
	fk_onlineresource number(12)
)
 NOLOGGING  NOMONITORING;

CREATE TABLE JT_Dist_DistFormat ( 
	fk_distribution number(12),
	FK_Format number(12)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE JT_Dist_Distributor ( 
	fk_distribution number(12),
	fk_distributor number(12)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE JT_Ident_Const ( 
	FK_Constraint number(12),
	FK_Identification number(12)
) 
 NOLOGGING  NOMONITORING;
 
CREATE TABLE JT_Ident_Keywords ( 
	FK_Ident number(12),
	FK_Keywords number(12)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE JT_Ident_LegalConst ( 
	FK_Identification number(12),
	FK_LegalConst number(12)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE JT_Ident_Mainten ( 
	FK_Identification number(12),
	FK_Maintenance number(12)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE JT_Ident_Progress ( 
	FK_Identification number(12),
	FK_Progress number(12)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE JT_Ident_RespParty ( 
	FK_Identification number(12),
	FK_ResponsibleParty number(12)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE JT_Ident_SecConst ( 
	FK_SecConstraint number(12),
	FK_Identification number(12)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE JT_Ident_Usage ( 
	FK_Ident number(12),
	FK_Usage number(12)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE JT_Keywords_Keyword ( 
	FK_Keywords number(12),
	FK_Keyword number(12)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE JT_LegalConst_accessConst ( 
	FK_LegalConst number(12),
	FK_RestrictCode number(12)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE JT_LegalConst_useConst ( 
	FK_LegalConst number(12),
	FK_RestrictCode number(12)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE JT_Procstep_RespParty ( 
	fk_procstep number(12),
	fk_responsibleparty number(12)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE JT_LI_SRC_LI_PROCSTEP ( 
	fk_source number(12),
	fk_procstep number(12)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE JT_Metadata_AppSchemaInf ( 
	fk_metadata number(12),
	fk_AppSchemaInf number(12)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE JT_Metadata_Const ( 
	FK_Metadata number(12),
	FK_Constraints number(12)
) 
 NOLOGGING  NOMONITORING;
 
CREATE TABLE JT_Metadata_FeatCatDesc ( 
	FK_Metadata number(12),
	FK_FeatCatDesc number(12)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE JT_Metadata_LegalConst ( 
	FK_Metadata number(12),
	FK_LegalConstraint number(12)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE JT_Metadata_Locale ( 
	FK_Metadata number(12),
	FK_Locale number(12)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE JT_Metadata_PortCatRef ( 
	FK_Metadata number(12),
	FK_PortCatRef number(12)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE JT_Metadata_RefSys ( 
	FK_Metadata number(12),
	FK_RefSys number(12)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE JT_Metadata_RespParty ( 
	FK_Metadata number(12),
	FK_RespParty number(12)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE JT_Metadata_SecConst ( 
	FK_Metadata number(12),
	FK_SecConstraints number(12)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE JT_Operation_DCP ( 
	FK_Operation number(12),
	FK_DCP number(12)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE JT_Operation_Name ( 
	fk_operation number(12),
	fk_name number(12)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE JT_Operation_OperatesOn ( 
	fk_operation number(12),
	fk_operateson number(12)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE JT_Operation_Parameter(
	FK_Operation number(12),
	FK_Parameter number(12)
)
 NOLOGGING  NOMONITORING;
 
CREATE TABLE JT_OpMeta_OnlineRes ( 
	FK_Operation number(12),
	FK_OnlineResource number(12)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE JT_Quality_Procstep ( 
	fk_quality number(12),
	fk_procstep number(12)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE JT_SecConst_ClassificationCode ( 
	FK_SecConst number(12),
	FK_ClassificationCode number(12)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE JT_Usage_RespParty(
	fk_usage number(12),
	fk_respparty number(12)
)
 NOLOGGING  NOMONITORING;
 
CREATE TABLE Keyword ( 
	ID number(12),
	keyword varchar2(200 char)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE Language (
	ID number(12),
	FK_DataIdent number(12),
	language varchar2(150 char)
)
 NOLOGGING  NOMONITORING;
 
CREATE TABLE LI_ProcessStep ( 
	ID number(12),
	description varchar2(2000 char),
	rationale varchar2(500 char),
	dateTime TIMESTAMP
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE LI_Source ( 
	ID number(12),
	fk_dataquality number(12),
	description varchar2(2000 char),
	scaleDenominator number(12),
	fk_sourceReferenceSystem number(12),
	fk_sourceCitation number(12)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE MD_AggregateInfo (
	ID number(12),
	FK_DataIdent number(12),
	FK_DateSetName number(12),
	FK_TypeCode number(12)
)
 NOLOGGING  NOMONITORING;

CREATE TABLE MD_AppSchemaInformation ( 
	ID number(12),
	fk_citation number(12),
	schemaLanguage varchar2(50 char),
	constraintLanguage varchar2(50 char),
	schemaAscii varchar2(50 char),
	graphicsFile64b varchar2(4000 char),
	softwareDevelFile64b varchar2(4000 char),
	softwareDevelFileFormat varchar2(150 char)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE MD_BrowseGraphic ( 
	FK_Ident number(12),
	fileName varchar2(500 char),
	fileDescription varchar2(4000 char),
	fileType varchar2(12 char),
	ID number(12)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE MD_CharacterSetCode ( 
	ID number(12),
	codeListValue varchar2(50 char),
	codeSpace varchar2(500 char)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE MD_ClassificationCode ( 
	ID number(12),
	codeListValue varchar2(50 char),
	codeSpace varchar2(500 char)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE MD_Constraints ( 
	ID number(12),
	useLimitations varchar2(4000 char)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE MD_DataIdentification ( 
	ID number(12),
	supplementalInformation varchar2(1000 char),
	uuid varchar2(250 char)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE MD_DigTransferOpt ( 
	ID number(12),
	fk_distribution number(12),
	unitsOfDistribution varchar2(150 char),
	transferSize real,
	off_mediumnote varchar2(4000 char)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE MD_Distribution ( 
	ID number(12),
	fk_metadata number(12)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE MD_Distributor ( 
	ID number(12),
	FK_ResponsibleParty number(12)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE MD_FeatCatDesc ( 
	ID number(12),
	FK_Citation number(12),
	language varchar2(200 char),
	includedWithDataset CHAR 
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE MD_Format ( 
	ID number(12),
	name varchar2(200 char),
	version varchar2(50 char),
	specification varchar2(4000 char),
	fileDecompTech varchar2(500 char),
	amendmentNumber varchar2(150 char)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE MD_GeoObjTypeCode ( 
	ID number(12),
	codeListValue varchar2(50 char),
	codeSpace varchar2(500 char)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE MD_Identification ( 
	ID number(12),
	FK_Citation number(12),
	abstract varchar2(4000 char),
	purpose varchar2(1000 char)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE MD_Keywords ( 
	ID number(12),
	FK_Thesaurus number(12),
	FK_Type number(12)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE MD_KeywordTypeCode ( 
	ID number(12),
	codeListValue varchar2(100 char),
	codeSpace varchar2(250 char)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE MD_LegalConstraints ( 
	ID number(12),
	defined char,
	useLimitations varchar2(4000 char)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE MD_MainFreqCode ( 
	ID number(12),
	codeListValue varchar2(500 char),
	codeSpace varchar2(500 char)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE MD_MaintenanceInformation ( 
	ID number(12),
	FK_MainFreq number(12),
	FK_Scope number(12),
	dateOfNextUpdate TIMESTAMP,
	userDefFrequency varchar2(150 char),
	note varchar2(4000 char)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE MD_MediumFormatCode ( 
	ID number(12),
	codeListValue varchar2(50 char),
	codeSpace varchar2(500 char)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE MD_MediumNameCode ( 
	ID number(12),
	codeListValue varchar2(50 char),
	codeSpace varchar2(500 char)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE MD_Metadata ( 
	ID number(12),
	FK_fileIdentifier number(12),
	language varchar2(100 char),
	FK_Characterset number(12),
	parentIdentifier varchar2(150 char),
	dateStamp TIMESTAMP,
	mdStandName varchar2(50 char),
	mdStandVersion varchar2(50 char),
	FK_HLevelCode number(12),
	FK_HLevelName number(12),
	testdata varchar2(255 char)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE MD_PortrayalCatRef ( 
	FK_Citation number(12),
	ID number(12)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE MD_ProgressCode ( 
	ID number(12),
	codeListValue varchar2(500 char),
	codeSpace varchar2(500 char)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE MD_Resolution ( 
	ID number(12),
	FK_DataIdent number(12),
	equivalentScale number(12),
	distanceValue decimal(10,3),
	uomName varchar2(50 char)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE MD_RestrictionCode ( 
	ID number(12),
	codeListValue varchar2(50 char),
	codeSpace varchar2(500 char)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE MD_ScopeCode ( 
	ID number(12),
	codeListValue varchar2(50 char),
	codeSpace varchar2(500 char)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE MD_SecurityConstraints ( 
	ID number(12),
	userNote varchar2(2000 char),
	classificationSystem varchar2(200 char),
	handlingDescription varchar2(200 char),
	useLimitations varchar2(4000 char)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE MD_SpatialRepTypeCode ( 
	ID number(12),
	codeListValue varchar2(50 char),
	codeSpace varchar2(500 char)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE MD_StandOrderProc ( 
	ID number(12),
	fees varchar2(2000 char),
	orderingInstructions varchar2(4000 char),
	turnaround varchar2(500 char),
	FK_Distributor number(12)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE MD_TopicCategoryCode ( 
	ID number(12),
	category varchar2(500 char)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE MD_TopoLevelCode ( 
	ID number(12),
	codeListValue varchar2(50 char),
	codeSpace varchar2(500 char)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE MD_Usage ( 
	ID number(12),
	specificUsage varchar2(500 char)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE MD_VecSpatialRep ( 
	ID number(12),
	FK_TopoLevelCode number(12),
	FK_GeoObjTypeCode number(12),
	FK_Metadata number(12),
	geoObjCount number(12)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE OperatesOn ( 
	ID number(12),
	fk_dataidentification number(12),
	fk_serviceidentification number(12),
	uuidref varchar2(250 char),
	name varchar2(250 char),
	title varchar2(250 char),
	abstract varchar2(4000 char)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE OperationNames ( 
	ID number(12),
	name varchar2(50 char),
	namespace varchar2(250 char)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE OtherConstraints ( 
	FK_LegalConstraints number(12),
	constraint_ varchar2(4000 char),
	ID number(12)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE PT_Locale (
	ID number(12),
	languageCode varchar2(150 char),
	country varchar2(150 char),
	FK_CharacterEncoding number(12)
)
 NOLOGGING  NOMONITORING;

CREATE TABLE PublicationDate (
	ID number(12),
	FK_Citation number(12),
	publicationDate TIMESTAMP
)
 NOLOGGING  NOMONITORING;
 
CREATE TABLE QuantitativeRes_Value (
	ID number(12),
	fk_quantitativeResult number(12),
	value varchar2(50 char)
)
 NOLOGGING  NOMONITORING;
 
CREATE TABLE RS_Identifier ( 
	ID number(12),
	code varchar2(150 char),
	codeSpace varchar2(250 char),
	version varchar2(50 char),
	FK_Authority number(12)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE ServiceVersion ( 
	FK_ServiceIdent number(12),
	version varchar2(10 char),
	ID number(12)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE SV_DCPList ( 
	ID number(12),
	codeListValue varchar2(50 char),
	codeSpace varchar2(150 char)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE SV_OperationMetadata ( 
	ID number(12),
	operationDescription varchar2(4000 char),
	invocationName varchar2(150 char),
	FK_ServiceIdent number(12)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE SV_Parameter ( 	
	ID number(12),
	name varchar2(50 char),
	type varchar2(150 char),
	direction varchar2(6 char),
	description varchar2(4000 char),
	optionality varchar2(20 char),
	repeatability CHAR,
	valuetype varchar2(150 char)
) 
 NOLOGGING  NOMONITORING;

CREATE TABLE Voice ( 
	FK_Contact number(12),
	voicenumber varchar2(500 char),
	ID number(12)
) 
 NOLOGGING  NOMONITORING;
 
 
 
	
-- PRIMARY KEY CONSTRAINTS
ALTER TABLE AlternateTitle ADD CONSTRAINT PK_AlternateTitle  PRIMARY KEY (ID) ;
ALTER TABLE CI_Address ADD CONSTRAINT PK_CI_Address  PRIMARY KEY (ID) ;
ALTER TABLE CI_Citation ADD CONSTRAINT PK_CI_Citation  PRIMARY KEY (ID) ;
ALTER TABLE CI_Contact ADD CONSTRAINT PK_CI_Contact PRIMARY KEY (ID) ;
ALTER TABLE CI_OnLineFunctionCode ADD CONSTRAINT PK_CI_OnLineFunction PRIMARY KEY (ID) ;
ALTER TABLE CI_OnlineResource ADD CONSTRAINT PK_CI_OnlineResource PRIMARY KEY (ID) ;
ALTER TABLE CI_PresentationFormCode ADD CONSTRAINT PK_CI_PresentationFormCode PRIMARY KEY (ID) ;
ALTER TABLE CI_RespParty ADD CONSTRAINT PK_CI_RespParty PRIMARY KEY (ID) ;
ALTER TABLE CI_RoleCode ADD CONSTRAINT PK_CI_RoleCode PRIMARY KEY (ID) ;
ALTER TABLE CI_Series ADD CONSTRAINT PK_CI_Series PRIMARY KEY (ID) ;
ALTER TABLE CSW_CouplingType ADD CONSTRAINT PK_CSW_CouplingType PRIMARY KEY (ID) ;
ALTER TABLE CSW_ServiceIdentification ADD CONSTRAINT PK_ServiceIdentification PRIMARY KEY (ID) ;
ALTER TABLE DeliveryPoint ADD CONSTRAINT PK_DeliveryPoint PRIMARY KEY (ID) ;
ALTER TABLE DQ_ConformanceResult ADD CONSTRAINT PK_DQ_ConformanceResult PRIMARY KEY (ID) ;
ALTER TABLE DQ_DataQuality ADD CONSTRAINT PK_DQ_DataQuality PRIMARY KEY (ID) ;
ALTER TABLE DQ_Element ADD CONSTRAINT PK_DQ_Element PRIMARY KEY (ID) ;
ALTER TABLE DQ_QuantitativeResult ADD CONSTRAINT PK_DQ_QuantitativeResult PRIMARY KEY (ID) ;	
ALTER TABLE DS_AssociationTypeCode ADD CONSTRAINT PK_DS_AssociationTypeCode PRIMARY KEY (ID) ;
ALTER TABLE ElectronicMailAddress ADD CONSTRAINT PK_ElectronicMailAddress PRIMARY KEY (ID) ;
ALTER TABLE EX_BoundingPolygon ADD CONSTRAINT PK_EX_BoundingPolygon PRIMARY KEY (ID) ;
ALTER TABLE EX_GeographicDescription ADD CONSTRAINT PK_EX_GeographicDescription PRIMARY KEY (ID) ;
ALTER TABLE EX_GeogrBBox ADD CONSTRAINT PK_EX_GeogrBBox PRIMARY KEY (ID) ;
ALTER TABLE EX_TemporalExtent ADD CONSTRAINT PK_EX_TemporalExtent PRIMARY KEY (ID) ;
ALTER TABLE EX_VerticalExtent ADD CONSTRAINT PK_EX_VerticalExtent PRIMARY KEY (ID) ;
ALTER TABLE Facsimile ADD CONSTRAINT PK_Facsimile PRIMARY KEY (ID) ;
ALTER TABLE FAILEDREQUESTS ADD CONSTRAINT PK_FAILEDREQUESTS PRIMARY KEY (ID) ;
ALTER TABLE FeatureTypes ADD CONSTRAINT PK_FeatureTypes PRIMARY KEY (ID) ;
ALTER TABLE FileIdentifier ADD CONSTRAINT PK_FileIdentifier PRIMARY KEY (ID) ;
ALTER TABLE HierarchylevelCode ADD CONSTRAINT PK_HierarchylevelCode PRIMARY KEY (ID) ;
ALTER TABLE HierarchylevelName ADD CONSTRAINT PK_HierachylevelName PRIMARY KEY (ID) ;
ALTER TABLE Keyword ADD CONSTRAINT PK_Keyword PRIMARY KEY (ID) ;
ALTER TABLE Language ADD CONSTRAINT PK_Language PRIMARY KEY (ID) ;
ALTER TABLE LI_ProcessStep ADD CONSTRAINT PK_LI_ProcessStep PRIMARY KEY (ID) ;
ALTER TABLE LI_Source ADD CONSTRAINT PK_LI_Source PRIMARY KEY (ID) ;
ALTER TABLE MD_AggregateInfo ADD CONSTRAINT PK_MD_AggregateInfo PRIMARY KEY (ID) ;
ALTER TABLE MD_AppSchemaInformation ADD CONSTRAINT PK_MD_AppSchemaInformation PRIMARY KEY (ID) ;
ALTER TABLE MD_BrowseGraphic ADD CONSTRAINT PK_MD_BrowseGraphic PRIMARY KEY (ID) ;
ALTER TABLE MD_CharacterSetCode ADD CONSTRAINT PK_MD_CharacterSetCode PRIMARY KEY (ID) ;
ALTER TABLE MD_ClassificationCode ADD CONSTRAINT PK_MD_ClassificationCode PRIMARY KEY (ID) ;
ALTER TABLE MD_Constraints ADD CONSTRAINT PK_MD_Constraints PRIMARY KEY (ID) ;
ALTER TABLE MD_DataIdentification ADD CONSTRAINT PK_MD_DataIdent PRIMARY KEY (ID) ;
ALTER TABLE MD_DigTransferOpt ADD CONSTRAINT PK_MD_DigTransferOpt PRIMARY KEY (ID) ;
ALTER TABLE MD_Distribution ADD CONSTRAINT PK_MD_Distribution PRIMARY KEY (ID) ;
ALTER TABLE MD_Distributor ADD CONSTRAINT PK_MD_Distributor PRIMARY KEY (ID) ;
ALTER TABLE MD_FeatCatDesc ADD CONSTRAINT PK_MD_FeatCatDesc PRIMARY KEY (ID) ;
ALTER TABLE MD_Format ADD CONSTRAINT PK_MD_Format PRIMARY KEY (ID) ;
ALTER TABLE MD_GeoObjTypeCode ADD CONSTRAINT PK_MD_GeoObjTypeCode PRIMARY KEY (ID) ;
ALTER TABLE MD_Identification ADD CONSTRAINT PK_MD_Identification PRIMARY KEY (ID) ;
ALTER TABLE MD_Keywords ADD CONSTRAINT PK_MD_Keywords PRIMARY KEY (ID) ;
ALTER TABLE MD_KeywordTypeCode ADD CONSTRAINT PK_MD_KeywordTypeCode PRIMARY KEY (ID) ;
ALTER TABLE MD_LegalConstraints ADD CONSTRAINT PK_MD_LegalConstraints PRIMARY KEY (ID) ;
ALTER TABLE MD_MainFreqCode ADD CONSTRAINT PK_MD_MaintenanceInf PRIMARY KEY (ID) ;
ALTER TABLE MD_MaintenanceInformation ADD CONSTRAINT PK_MD_MaintenanceInformation PRIMARY KEY (ID) ;
ALTER TABLE MD_MediumFormatCode ADD CONSTRAINT PK_MD_MediumFormatCode PRIMARY KEY (ID) ;
ALTER TABLE MD_MediumNameCode ADD CONSTRAINT PK_MD_MediumNameCode PRIMARY KEY (ID) ;
ALTER TABLE MD_Metadata ADD CONSTRAINT PK_MD_Metadata PRIMARY KEY (ID) ;
ALTER TABLE MD_PortrayalCatRef ADD CONSTRAINT PK_MD_PortrayalCatRef PRIMARY KEY (ID) ;
ALTER TABLE MD_ProgressCode ADD CONSTRAINT PK_MD_ProgressCode PRIMARY KEY (ID) ;
ALTER TABLE MD_Resolution ADD CONSTRAINT PK_MD_Resolution PRIMARY KEY (ID) ;
ALTER TABLE MD_RestrictionCode ADD CONSTRAINT PK_MD_RestrictionCode PRIMARY KEY (ID) ;
ALTER TABLE MD_ScopeCode ADD CONSTRAINT PK_MD_ScopeCode PRIMARY KEY (ID) ;
ALTER TABLE MD_SecurityConstraints ADD CONSTRAINT PK_MD_SecurityConstraints PRIMARY KEY (ID) ;
ALTER TABLE MD_SpatialRepTypeCode ADD CONSTRAINT PK_MD_SpatialRepType PRIMARY KEY (ID) ;
ALTER TABLE MD_StandOrderProc ADD CONSTRAINT PK_MD_StandOrderProc PRIMARY KEY (ID) ;
ALTER TABLE MD_TopicCategoryCode ADD CONSTRAINT PK_MD_TopicCategory PRIMARY KEY (ID) ;
ALTER TABLE MD_TopoLevelCode ADD CONSTRAINT PK_MD_TopoLevelCode PRIMARY KEY (ID) ;
ALTER TABLE MD_Usage ADD CONSTRAINT PK_MD_Usage PRIMARY KEY (ID) ;
ALTER TABLE MD_VecSpatialRep ADD CONSTRAINT PK_MD_MD_VecSpatialRep PRIMARY KEY (ID) ;
ALTER TABLE OperatesOn ADD CONSTRAINT PK_OperatesOn PRIMARY KEY (ID) ;
ALTER TABLE OperationNames ADD CONSTRAINT PK_OperationNames PRIMARY KEY (ID) ;
ALTER TABLE OtherConstraints ADD CONSTRAINT PK_OtherConstraints PRIMARY KEY (ID) ;
ALTER TABLE PT_Locale ADD CONSTRAINT PK_PT_Locale PRIMARY KEY (ID) ;
ALTER TABLE PublicationDate ADD CONSTRAINT PK_PublicationDate PRIMARY KEY (ID) ;
ALTER TABLE QuantitativeRes_Value ADD CONSTRAINT PK_QuantitativeResValue PRIMARY KEY (ID) ;
ALTER TABLE RS_Identifier ADD CONSTRAINT PK_MD_ReferenceSystem PRIMARY KEY (ID) ;
ALTER TABLE ServiceVersion ADD CONSTRAINT PK_ServiceVersion PRIMARY KEY (ID) ;
ALTER TABLE SV_DCPList ADD CONSTRAINT PK_SV_DCPList PRIMARY KEY (ID) ;
ALTER TABLE SV_OperationMetadata ADD CONSTRAINT PK_SV_OperationMetadata PRIMARY KEY (ID) ;
ALTER TABLE SV_Parameter ADD CONSTRAINT PK_SV_Parameter PRIMARY KEY (ID) ;
ALTER TABLE Voice ADD CONSTRAINT PK_Voice PRIMARY KEY (ID) ;

-- UNIQUE CONSTRAINTS
ALTER TABLE CI_RoleCode ADD CONSTRAINT UQ_CI_RoleCd_cdListVal UNIQUE (codeListValue) ;
ALTER TABLE CSW_CouplingType ADD CONSTRAINT UQ_CSW_CoupType_cdLstVal UNIQUE (codeListValue) ;
ALTER TABLE DS_AssociationTypeCode ADD CONSTRAINT UQ_DS_AssTypeCd_cdListVal UNIQUE (codeListValue) ;
ALTER TABLE FileIdentifier ADD CONSTRAINT UQ_FileId_fileid UNIQUE (fileidentifier) ;
ALTER TABLE HierarchylevelCode ADD CONSTRAINT UQ_HlevelCd_cdListVal UNIQUE (codeListValue) ;
ALTER TABLE HierarchylevelName ADD CONSTRAINT UQ_HlevelName_Name UNIQUE (Name) ;
ALTER TABLE MD_CharacterSetCode ADD CONSTRAINT UQ_MD_CharSetCd_cdListVal UNIQUE (codeListValue) ;
ALTER TABLE MD_ClassificationCode ADD CONSTRAINT UQ_MD_ClassCd_cdListVal UNIQUE (codeListValue) ;
ALTER TABLE MD_GeoObjTypeCode ADD CONSTRAINT UQ_MD_GeoObjTpCd_cdListVal UNIQUE (codeListValue) ;
ALTER TABLE MD_MainFreqCode ADD CONSTRAINT UQ_MD_MainFreqCd_cdListVal UNIQUE (codeListValue) ;
ALTER TABLE MD_MediumFormatCode ADD CONSTRAINT UQ_MD_MedFrmCd_cdListVal UNIQUE (codeListValue) ;
ALTER TABLE MD_MediumNameCode ADD CONSTRAINT UQ_MD_MedNameCd_cdListVal UNIQUE (codeListValue) ;
ALTER TABLE MD_Metadata ADD CONSTRAINT UQ_MD_Meta_FK_fileId UNIQUE (FK_fileIdentifier) ;
ALTER TABLE MD_ProgressCode ADD CONSTRAINT UQ_MD_ProgrCd_cdListVal UNIQUE (codeListValue) ;
ALTER TABLE MD_RestrictionCode ADD CONSTRAINT UQ_MD_RestrictCd_cdListVal UNIQUE (codeListValue) ;
ALTER TABLE MD_ScopeCode ADD CONSTRAINT UQ_MD_ScopeCode_codeListValue UNIQUE (codeListValue) ;
ALTER TABLE MD_SpatialRepTypeCode ADD CONSTRAINT UQ_MD_SpaRepTypeCd_cdListVal UNIQUE (codeListValue) ;
ALTER TABLE MD_TopicCategoryCode ADD CONSTRAINT UQ_MD_TopicCatCd_cat UNIQUE (category) ;
ALTER TABLE MD_TopoLevelCode ADD CONSTRAINT UQ_MD_TopoLevelCd_cdListVal UNIQUE (codeListValue) ;
ALTER TABLE SV_DCPList ADD CONSTRAINT UQ_SV_DCPList_codeListValue UNIQUE (codeListValue) ;

-- SEQUENCES
CREATE SEQUENCE AlternateTitle_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE CI_Address_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE CI_Citation_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE CI_Contact_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE CI_OnLineFunctionCode_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE CI_OnlineResource_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE CI_PresentationFormCode_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE CI_RespParty_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE CI_RoleCode_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE CI_Series_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE CSW_CouplingType_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE CSW_ServiceIdentificati_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE DeliveryPoint_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE DQ_ConformanceResult_ID_seq increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE DQ_DataQuality_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE DQ_Element_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE DQ_QuantitativeResult_ID_seq increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE DS_AssociationTypeCode_ID_seq increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE ElectronicMailAddress_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE EX_BoundingPolygon_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE EX_GeographicDescriptio_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE EX_GeogrBBox_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE EX_TemporalExtent_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE EX_VerticalExtent_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE Facsimile_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE FAILEDREQUESTS_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE FeatureTypes_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE FileIdentifier_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE HierarchylevelCode_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE HierarchylevelName_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE Keyword_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE Language_ID_seq increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE LI_ProcessStep_ID_SEQ increment by 1 start with 0 NOMAXVALUE minvalue 0 nocycle nocache noorder;
CREATE SEQUENCE LI_Source_ID_SEQ increment by 1 start with 0 NOMAXVALUE minvalue 0 nocycle nocache noorder;
CREATE SEQUENCE MD_AggregateInfo_ID_seq increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE MD_ApplicationSchemaInf_ID_SEQ increment by 1 start with 0 NOMAXVALUE minvalue 0 nocycle nocache noorder;
CREATE SEQUENCE MD_BrowseGraphic_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE MD_CharacterSetCode_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE MD_ClassificationCode_ID_SEQ increment by 1 start with 0 NOMAXVALUE minvalue 0 nocycle nocache noorder;
CREATE SEQUENCE MD_Constraints_ID_SEQ increment by 1 start with 0 NOMAXVALUE minvalue 0 nocycle nocache noorder;
CREATE SEQUENCE MD_DataIdentification_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE MD_DigTransferOpt_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE MD_Distribution_ID_SEQ increment by 1 start with 0 NOMAXVALUE minvalue 0 nocycle nocache noorder;
CREATE SEQUENCE MD_Distributor_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE MD_FeatCatDesc_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE MD_Format_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE MD_GeoObjTypeCode_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE MD_Identification_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE MD_Keywords_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE MD_KeywordTypeCode_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE MD_LegalConstraints_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE MD_MainFreqCode_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE MD_MaintenanceInformati_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE MD_MediumFormatCode_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE MD_MediumNameCode_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE MD_Metadata_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE MD_PortrayalCatRef_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE MD_ProgressCode_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE MD_Resolution_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE MD_RestrictionCode_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE MD_ScopeCode_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE MD_SecurityConstraints_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE MD_SpatialRepTypeCode_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE MD_StandOrderProc_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE MD_TopicCategoryCode_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE MD_TopoLevelCode_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE MD_Usage_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE MD_VectorSpatialReprens_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE OperatesOn_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE OperationNames_ID_SEQ increment by 1 start with 0 NOMAXVALUE minvalue 0 nocycle nocache noorder;
CREATE SEQUENCE OtherConstraints_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE PT_Locale_ID_seq increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE PublicationDate_ID_seq increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE QuantitativeRes_Value_ID_seq increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE RS_Identifier_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE ServiceVersion_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE SV_DCPList_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE SV_OperationMetadata_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE SV_Parameter_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;
CREATE SEQUENCE Voice_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

-- Codelist: CI_OnlineFunctionCode ( ID, B.5.3 )
INSERT INTO CI_OnlineFunctionCode ( ID,codeListValue) VALUES (1, 'download' ) ;
INSERT INTO CI_OnlineFunctionCode ( ID,codeListValue) VALUES (2, 'information' ) ;
INSERT INTO CI_OnlineFunctionCode ( ID,codeListValue) VALUES (3, 'offlineAccess' ) ;
INSERT INTO CI_OnlineFunctionCode ( ID,codeListValue) VALUES (4, 'order' ) ;
INSERT INTO CI_OnlineFunctionCode ( ID,codeListValue) VALUES (5, 'search' ) ;


-- Codelist: CI_PresentationFormCode (  B.5.4 )
INSERT INTO CI_PresentationFormCode ( ID,codeListValue) VALUES ( 1,'documentDigital' ) ;
INSERT INTO CI_PresentationFormCode ( ID,codeListValue) VALUES ( 2,'documentHardcopy' ) ;
INSERT INTO CI_PresentationFormCode ( ID,codeListValue) VALUES ( 3,'imageDigital' ) ;
INSERT INTO CI_PresentationFormCode ( ID,codeListValue) VALUES ( 4,'imageHardcopy' ) ;
INSERT INTO CI_PresentationFormCode ( ID,codeListValue) VALUES ( 5,'mapDigital' ) ;
INSERT INTO CI_PresentationFormCode ( ID,codeListValue) VALUES ( 6,'mapHardcopy' ) ;
INSERT INTO CI_PresentationFormCode ( ID,codeListValue) VALUES ( 7,'modelDigital' ) ;
INSERT INTO CI_PresentationFormCode ( ID,codeListValue) VALUES ( 8,'modelHardcopy' ) ;
INSERT INTO CI_PresentationFormCode ( ID,codeListValue) VALUES ( 9,'profileDigital' ) ;
INSERT INTO CI_PresentationFormCode ( ID,codeListValue) VALUES ( 10,'profileHardcopy' ) ;
INSERT INTO CI_PresentationFormCode ( ID,codeListValue) VALUES ( 11,'tableDigital' ) ;
INSERT INTO CI_PresentationFormCode ( ID,codeListValue) VALUES ( 12,'tableHardcopy' ) ;
INSERT INTO CI_PresentationFormCode ( ID,codeListValue) VALUES ( 13,'videoDigital' ) ;
INSERT INTO CI_PresentationFormCode ( ID,codeListValue) VALUES ( 14,'videoHardcopy' ) ;

-- Codelist: CI_RoleCode (  B.5.5 )
INSERT INTO CI_RoleCode ( ID, codeListValue) VALUES ( 1, 'resourceProvider' ) ;
INSERT INTO CI_RoleCode ( ID, codeListValue) VALUES ( 2,'custodian' ) ;
INSERT INTO CI_RoleCode ( ID, codeListValue) VALUES ( 3,'owner' ) ;
INSERT INTO CI_RoleCode ( ID, codeListValue) VALUES ( 4,'user' ) ;
INSERT INTO CI_RoleCode ( ID, codeListValue) VALUES ( 5,'distributor' ) ;
INSERT INTO CI_RoleCode ( ID, codeListValue) VALUES ( 6,'originator' ) ;
INSERT INTO CI_RoleCode ( ID, codeListValue) VALUES ( 7,'pointOfContact' ) ;
INSERT INTO CI_RoleCode ( ID, codeListValue) VALUES ( 8,'principalInvestigator' ) ;
INSERT INTO CI_RoleCode ( ID, codeListValue) VALUES ( 9,'processor' ) ;
INSERT INTO CI_RoleCode ( ID, codeListValue) VALUES ( 10,'publisher' ) ;
INSERT INTO CI_RoleCode ( ID, codeListValue) VALUES ( 11,'author' ) ;

-- Codelist: MD_CharacterSetCode (  B.5.10 )
INSERT INTO MD_CharacterSetCode ( ID, codeListValue) VALUES ( 1, 'ucs2' ) ;
INSERT INTO MD_CharacterSetCode ( ID, codeListValue) VALUES ( 2,'ucs4' ) ;
INSERT INTO MD_CharacterSetCode ( ID, codeListValue) VALUES ( 3,'utf7' ) ;
INSERT INTO MD_CharacterSetCode ( ID, codeListValue) VALUES ( 4,'utf8' ) ;
INSERT INTO MD_CharacterSetCode ( ID, codeListValue) VALUES ( 5,'utf16' ) ;
INSERT INTO MD_CharacterSetCode ( ID, codeListValue) VALUES ( 6,'8859part1' ) ;
INSERT INTO MD_CharacterSetCode ( ID, codeListValue) VALUES ( 7,'8859part2' ) ;
INSERT INTO MD_CharacterSetCode ( ID, codeListValue) VALUES ( 8,'8859part3' ) ;
INSERT INTO MD_CharacterSetCode ( ID, codeListValue) VALUES ( 9,'8859part4' ) ;
INSERT INTO MD_CharacterSetCode ( ID, codelistvalue) VALUES ( 10,'8859part5' ) ;
INSERT INTO MD_CharacterSetCode ( ID, codeListValue) VALUES ( 11,'8859part6' ) ;
INSERT INTO MD_CharacterSetCode ( ID, codeListValue) VALUES ( 12,'8859part7' ) ;
INSERT INTO MD_CharacterSetCode ( ID, codeListValue) VALUES ( 13,'8859part8' ) ;
INSERT INTO MD_CharacterSetCode ( ID, codeListValue) VALUES ( 14,'8859part9' ) ;
INSERT INTO MD_CharacterSetCode ( ID, codeListValue) VALUES ( 15,'8859part10' ) ;
INSERT INTO MD_CharacterSetCode ( ID, codeListValue) VALUES ( 16,'8859part11' ) ;
INSERT INTO MD_CharacterSetCode ( ID, codeListValue) VALUES ( 17,'(reserved for future use)' ) ;
INSERT INTO MD_CharacterSetCode ( ID, codeListValue) VALUES ( 18,'8859part13' ) ;
INSERT INTO MD_CharacterSetCode ( ID, codeListValue) VALUES ( 19,'8859part14' ) ;
INSERT INTO MD_CharacterSetCode ( ID, codeListValue) VALUES ( 20,'8859part15' ) ;
INSERT INTO MD_CharacterSetCode ( ID, codeListValue) VALUES ( 21,'8859part16' ) ;
INSERT INTO MD_CharacterSetCode ( ID, codeListValue) VALUES ( 22,'jis' ) ;
INSERT INTO MD_CharacterSetCode ( ID, codeListValue) VALUES ( 23,'shiftJIS' ) ;
INSERT INTO MD_CharacterSetCode ( ID, codeListValue) VALUES ( 24,'eucJP' ) ;
INSERT INTO MD_CharacterSetCode ( ID, codeListValue) VALUES ( 25,'usAscii' ) ;
INSERT INTO MD_CharacterSetCode ( ID, codeListValue) VALUES ( 26,'ebcdic' ) ;
INSERT INTO MD_CharacterSetCode ( ID, codeListValue) VALUES ( 27,'eucKR' ) ;
INSERT INTO MD_CharacterSetCode ( ID, codeListValue) VALUES ( 28,'big5' ) ;
INSERT INTO MD_CharacterSetCode ( ID, codeListValue) VALUES ( 29,'GB2312' ) ;

-- codelist: MD_GeoObjTypeCode (  B.5.15 )
INSERT INTO MD_GeoObjTypeCode ( ID, codeListValue) VALUES ( 1, 'complex' ) ;
INSERT INTO MD_GeoObjTypeCode ( ID, codeListValue) VALUES ( 2, 'composite' ) ;
INSERT INTO MD_GeoObjTypeCode ( ID, codeListValue) VALUES ( 3, 'curve' ) ;
INSERT INTO MD_GeoObjTypeCode ( ID, codeListValue) VALUES ( 4, 'point' ) ;
INSERT INTO MD_GeoObjTypeCode ( ID, codeListValue) VALUES ( 5, 'solid' ) ;
INSERT INTO MD_GeoObjTypeCode ( ID, codeListValue) VALUES ( 6, 'surface' ) ;

-- codelist: MD_KeywordTypeCode  (  B.5.17 )
INSERT INTO MD_KeywordTypeCode ( ID, codeListValue) VALUES ( 1, 'discipline' ) ;
INSERT INTO MD_KeywordTypeCode ( ID, codeListValue) VALUES ( 2, 'place' ) ;
INSERT INTO MD_KeywordTypeCode ( ID, codeListValue) VALUES ( 3, 'stratum' ) ;
INSERT INTO MD_KeywordTypeCode ( ID, codeListValue) VALUES ( 4, 'temporal' ) ;
INSERT INTO MD_KeywordTypeCode ( ID, codeListValue) VALUES ( 5, 'theme' ) ;

-- codelist: MD_MainFreqCode (  B.5.18 )
INSERT INTO MD_MainFreqCode ( ID, codeListValue) VALUES ( 1, 'continual' ) ;
INSERT INTO MD_MainFreqCode ( ID, codeListValue) VALUES ( 2, 'daily' ) ;
INSERT INTO MD_MainFreqCode ( ID, codeListValue) VALUES ( 3, 'weekly' ) ;
INSERT INTO MD_MainFreqCode ( ID, codeListValue) VALUES ( 4, 'fortnightly' ) ;
INSERT INTO MD_MainFreqCode ( ID, codeListValue) VALUES ( 5, 'monthly' ) ;
INSERT INTO MD_MainFreqCode ( ID, codeListValue) VALUES ( 6, 'quarterly' ) ;
INSERT INTO MD_MainFreqCode ( ID, codeListValue) VALUES ( 7, 'biannually' ) ;
INSERT INTO MD_MainFreqCode ( ID, codeListValue) VALUES ( 8, 'annually' ) ;
INSERT INTO MD_MainFreqCode ( ID, codeListValue) VALUES ( 9, 'asNeeded' ) ;
INSERT INTO MD_MainFreqCode ( ID, codeListValue) VALUES ( 10, 'irregular' ) ;
INSERT INTO MD_MainFreqCode ( ID, codeListValue) VALUES ( 11, 'notPlanned' ) ;
INSERT INTO MD_MainFreqCode ( ID, codeListValue) VALUES ( 12, 'unknown' ) ;

-- codelist: MD_MediumFormatCode (  B.5.19 )
INSERT INTO MD_MediumFormatCode ( ID, codeListValue) VALUES ( 1, 'cpio' ) ;
INSERT INTO MD_MediumFormatCode ( ID, codeListValue) VALUES ( 2, 'tar' ) ;
INSERT INTO MD_MediumFormatCode ( ID, codeListValue) VALUES ( 3, 'highSierra' ) ;
INSERT INTO MD_MediumFormatCode ( ID, codeListValue) VALUES ( 4, 'iso9660' ) ;
INSERT INTO MD_MediumFormatCode ( ID, codeListValue) VALUES ( 5, 'iso9660RockRidge' ) ;
INSERT INTO MD_MediumFormatCode ( ID, codeListValue) VALUES ( 6, 'iso9660AppleHFS' ) ;

-- codelist: MD_MediumNameCode (  B.5.20 )
INSERT INTO MD_MediumNameCode ( ID, codeListValue) VALUES ( 1, 'cdRom' ) ;
INSERT INTO MD_MediumNameCode ( ID, codeListValue) VALUES ( 2, 'dvd' ) ;
INSERT INTO MD_MediumNameCode ( ID, codeListValue) VALUES ( 3, 'dvdRom' ) ;
INSERT INTO MD_MediumNameCode ( ID, codeListValue) VALUES ( 4, '3halfInchFloppy' ) ;
INSERT INTO MD_MediumNameCode ( ID, codeListValue) VALUES ( 5, '5quarterInchFloppy' ) ;
INSERT INTO MD_MediumNameCode ( ID, codeListValue) VALUES ( 6, '7trackTape' ) ;
INSERT INTO MD_MediumNameCode ( ID, codeListValue) VALUES ( 7, '9trackTape' ) ;
INSERT INTO MD_MediumNameCode ( ID, codeListValue) VALUES ( 8, '3480Cartridge' ) ;
INSERT INTO MD_MediumNameCode ( ID, codeListValue) VALUES ( 9, '3490Cartridge' ) ;
INSERT INTO MD_MediumNameCode ( ID, codeListValue) VALUES ( 10, '3580Cartridge' ) ;

-- Codelist: MD_ProgressCode (  B.5.23 )
INSERT INTO MD_ProgressCode ( ID, codeListValue) VALUES ( 1, 'completed' ) ;
INSERT INTO MD_ProgressCode ( ID, codeListValue) VALUES ( 2, 'historicalArchive' ) ;
INSERT INTO MD_ProgressCode ( ID, codeListValue) VALUES ( 3, 'obsolete' ) ;
INSERT INTO MD_ProgressCode ( ID, codeListValue) VALUES ( 4, 'onGoing' ) ;
INSERT INTO MD_ProgressCode ( ID, codeListValue) VALUES ( 5, 'planned' ) ;
INSERT INTO MD_ProgressCode ( ID, codeListValue) VALUES ( 6, 'required' ) ;
INSERT INTO MD_ProgressCode ( ID, codeListValue) VALUES ( 7, 'underDevelopment' ) ;

-- Codelist: MD_RestrictionCode (  B.5.24 )
INSERT INTO MD_RestrictionCode ( ID, codeListValue) VALUES ( 1, 'copyright' ) ;
INSERT INTO MD_RestrictionCode ( ID, codeListValue) VALUES ( 2, 'patent' ) ;
INSERT INTO MD_RestrictionCode ( ID, codeListValue) VALUES ( 3, 'patentPending' ) ;
INSERT INTO MD_RestrictionCode ( ID, codeListValue) VALUES ( 4, 'trademark' ) ;
INSERT INTO MD_RestrictionCode ( ID, codeListValue) VALUES ( 5, 'license' ) ;
INSERT INTO MD_RestrictionCode ( ID, codeListValue) VALUES ( 6, 'intellectualPropertyRights' ) ;
INSERT INTO MD_RestrictionCode ( ID, codeListValue) VALUES ( 7, 'restricted' ) ;
INSERT INTO MD_RestrictionCode ( ID, codeListValue) VALUES ( 8, 'otherRestrictions' ) ;

-- Codelist: MD_ScopeCode (  B.5.25 )
INSERT INTO MD_ScopeCode ( ID, codeListValue) VALUES ( 1, 'attribute' ) ;
INSERT INTO MD_ScopeCode ( ID, codeListValue) VALUES ( 2, 'attributeType' ) ;
INSERT INTO MD_ScopeCode ( ID, codeListValue) VALUES ( 3, 'collectionHardware' ) ;
INSERT INTO MD_ScopeCode ( ID, codeListValue) VALUES ( 4, 'collectionSession' ) ;
INSERT INTO MD_ScopeCode ( ID, codeListValue) VALUES ( 5, 'dataset' ) ;
INSERT INTO MD_ScopeCode ( ID, codeListValue) VALUES ( 6, 'series' ) ;
INSERT INTO MD_ScopeCode ( ID, codeListValue) VALUES ( 7, 'nonGeographicDataset' ) ;
INSERT INTO MD_ScopeCode ( ID, codeListValue) VALUES ( 8, 'dimensionGroup' ) ;
INSERT INTO MD_ScopeCode ( ID, codeListValue) VALUES ( 9, 'feature' ) ;
INSERT INTO MD_ScopeCode ( ID, codeListValue) VALUES ( 10,'featureType' ) ;
INSERT INTO MD_ScopeCode ( ID, codeListValue) VALUES ( 11,'propertyType' ) ;
INSERT INTO MD_ScopeCode ( ID, codeListValue) VALUES ( 12,'fieldSession' ) ;
INSERT INTO MD_ScopeCode ( ID, codeListValue) VALUES ( 13,'software' ) ;
INSERT INTO MD_ScopeCode ( ID, codeListValue) VALUES ( 14,'service' ) ;
INSERT INTO MD_ScopeCode ( ID, codeListValue) VALUES ( 15,'model' ) ;
INSERT INTO MD_ScopeCode ( ID, codeListValue) VALUES ( 16,'tile' ) ;

-- Codelist: MD_SpatialRepTypeCode  (  B.5.26 )
INSERT INTO MD_SpatialRepTypeCode ( ID,codeListValue) VALUES ( 1, 'vector' ) ;
INSERT INTO MD_SpatialRepTypeCode ( ID,codeListValue) VALUES ( 2, 'grid' ) ;
INSERT INTO MD_SpatialRepTypeCode ( ID,codeListValue) VALUES ( 3, 'textTable' ) ;
INSERT INTO MD_SpatialRepTypeCode ( ID,codeListValue) VALUES ( 4, 'tin' ) ;
INSERT INTO MD_SpatialRepTypeCode ( ID,codeListValue) VALUES ( 5, 'stereoModel' ) ;
INSERT INTO MD_SpatialRepTypeCode ( ID,codeListValue) VALUES ( 6, 'video' ) ;

-- Enumeration: MD_TopicCategoryCode (  B.5.27 )
INSERT INTO MD_TopicCategoryCode ( ID, category) VALUES ( 1, 'farming' ) ;
INSERT INTO MD_TopicCategoryCode ( ID, category) VALUES ( 2, 'biota' ) ;
INSERT INTO MD_TopicCategoryCode ( ID, category) VALUES ( 3, 'boundaries' ) ;
INSERT INTO MD_TopicCategoryCode ( ID, category) VALUES ( 4,'climatologyMeteorologyAtmosphere' ) ;
INSERT INTO MD_TopicCategoryCode ( ID, category) VALUES ( 5,'economy' ) ;
INSERT INTO MD_TopicCategoryCode ( ID, category) VALUES ( 6,'elevation' ) ;
INSERT INTO MD_TopicCategoryCode ( ID, category) VALUES ( 7,'environment' ) ;
INSERT INTO MD_TopicCategoryCode ( ID, category) VALUES ( 8,'geoscientificInformation' ) ;
INSERT INTO MD_TopicCategoryCode ( ID, category) VALUES ( 9,'health' ) ;
INSERT INTO MD_TopicCategoryCode ( ID, category) VALUES ( 10,'imageryBaseMapsEarthCover' ) ;
INSERT INTO MD_TopicCategoryCode ( ID, category) VALUES ( 11,'intelligenceMilitary' ) ;
INSERT INTO MD_TopicCategoryCode ( ID, category) VALUES ( 12,'inlandWaters' ) ;
INSERT INTO MD_TopicCategoryCode ( ID, category) VALUES ( 13,'location' ) ;
INSERT INTO MD_TopicCategoryCode ( ID, category) VALUES ( 14,'oceans' ) ;
INSERT INTO MD_TopicCategoryCode ( ID, category) VALUES ( 15,'planningCadastre' ) ;
INSERT INTO MD_TopicCategoryCode ( ID, category) VALUES ( 16,'society' ) ;
INSERT INTO MD_TopicCategoryCode ( ID, category) VALUES ( 17,'structure' ) ;
INSERT INTO MD_TopicCategoryCode ( ID, category) VALUES ( 18,'transportation' ) ;
INSERT INTO MD_TopicCategoryCode ( ID, category) VALUES ( 19,'utilitiesCommunication' ) ;

-- codelist: MD_TopoLevelCode (  B.5.28 )
INSERT INTO MD_TopoLevelCode ( ID, codeListValue) VALUES ( 1,'geometryOnly' ) ;
INSERT INTO MD_TopoLevelCode ( ID, codeListValue) VALUES ( 2,'topology1D' ) ;
INSERT INTO MD_TopoLevelCode ( ID, codeListValue) VALUES ( 3,'planarGraph' ) ;
INSERT INTO MD_TopoLevelCode ( ID, codeListValue) VALUES ( 4,'fullPlanarGraph' ) ;
INSERT INTO MD_TopoLevelCode ( ID, codeListValue) VALUES ( 5,'surfaceGraph' ) ;
INSERT INTO MD_TopoLevelCode ( ID, codeListValue) VALUES ( 6,'fullSurfaceGraph' ) ;
INSERT INTO MD_TopoLevelCode ( ID, codeListValue) VALUES ( 7,'topology3D' ) ;
INSERT INTO MD_TopoLevelCode ( ID, codeListValue) VALUES ( 8,'fullTopology3D' ) ;
INSERT INTO MD_TopoLevelCode ( ID, codeListValue) VALUES ( 9,'abstract' ) ;


-- Codelist: hierarchylevelcode (  B.---- )
INSERT INTO hierarchylevelcode ( ID, codeListValue) VALUES ( 1, 'attribute' ) ;
INSERT INTO hierarchylevelcode ( ID, codeListValue) VALUES ( 2,'attributeType' ) ;
INSERT INTO hierarchylevelcode ( ID, codeListValue) VALUES ( 3,'collectionHardware' ) ;
INSERT INTO hierarchylevelcode ( ID, codeListValue) VALUES ( 4,'collectionSession' ) ;
INSERT INTO hierarchylevelcode ( ID, codeListValue) VALUES ( 5,'dataset' ) ;
INSERT INTO hierarchylevelcode ( ID, codeListValue) VALUES ( 6,'series' ) ;
INSERT INTO hierarchylevelcode ( ID, codeListValue) VALUES ( 7,'nonGeographicDataset' ) ;
INSERT INTO hierarchylevelcode ( ID, codeListValue) VALUES ( 8,'dimensionGroup' ) ;
INSERT INTO hierarchylevelcode ( ID, codeListValue) VALUES ( 9,'feature' ) ;
INSERT INTO hierarchylevelcode ( ID, codeListValue) VALUES ( 10,'featureType' ) ;
INSERT INTO hierarchylevelcode ( ID, codeListValue) VALUES ( 11,'propertyType' ) ;
INSERT INTO hierarchylevelcode ( ID, codeListValue) VALUES ( 12,'fieldSession' ) ;
INSERT INTO hierarchylevelcode ( ID, codeListValue) VALUES ( 13,'software' ) ;
INSERT INTO hierarchylevelcode ( ID, codeListValue) VALUES ( 14,'service' ) ;
INSERT INTO hierarchylevelcode ( ID, codeListValue) VALUES ( 15,'model' ) ;
INSERT INTO hierarchylevelcode ( ID, codeListValue) VALUES ( 16,'tile' ) ;
INSERT INTO hierarchylevelcode ( ID, codeListValue) VALUES ( 17,'application' ) ;

-- Codelist CSW coupling type
INSERT INTO CSW_CouplingType ( ID, codeListValue) VALUES ( 1, 'tight' ) ;
INSERT INTO CSW_CouplingType ( ID, codeListValue) VALUES ( 2, 'loose' ) ;
INSERT INTO CSW_CouplingType ( ID, codeListValue) VALUES ( 3, 'mixed' ) ;

-- Codelist DCP Type
insert into sv_dcplist ( ID, codeListValue ) values ( 1, 'WebServices' );
insert into sv_dcplist ( ID, codeListValue ) values ( 2, 'HTTPGet' );
insert into sv_dcplist ( ID, codeListValue ) values ( 3,'HTTPPost' );
insert into sv_dcplist ( ID, codeListValue ) values ( 4,'HTTPSoap' );
insert into sv_dcplist ( ID, codeListValue ) values ( 5,'COM' );
insert into sv_dcplist ( ID, codeListValue ) values ( 6,'XML' );
insert into sv_dcplist ( ID, codeListValue ) values ( 7,'SQL' );
insert into sv_dcplist ( ID, codeListValue ) values ( 8,'Corba' );
insert into sv_dcplist ( ID, codeListValue ) values ( 9,'Java' );

-- Codelist MD ClassificationCode (B5.11)
insert into md_classificationcode ( ID, codeListValue ) values ( 1,'unclassified' );
insert into md_classificationcode ( ID, codeListValue ) values ( 2,'restricted' );
insert into md_classificationcode ( ID, codeListValue ) values ( 3,'confidential' );
insert into md_classificationcode ( ID, codeListValue ) values ( 4,'secret' );
insert into md_classificationcode ( ID, codeListValue ) values ( 5,'topSecret' );


-- DS_AssociationTypeCode (B5.7)
insert into ds_associationtypecode ( ID, codeListValue ) values ( 1, 'crossReference' );
insert into ds_associationtypecode ( ID, codeListValue ) values ( 2, 'largerWorkCitation' );
insert into ds_associationtypecode ( ID, codeListValue ) values ( 3, 'partOfSeamlessDatabase' );
insert into ds_associationtypecode ( ID, codeListValue ) values ( 4, 'source' );
insert into ds_associationtypecode ( ID, codeListValue ) values ( 5, 'stereoMate' );

commit;
