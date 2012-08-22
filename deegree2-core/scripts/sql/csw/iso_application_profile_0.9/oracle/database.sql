CREATE TABLE AlternateTitle ( 
	FK_Citation NUMBER(38) NOT NULL,
	alternateTitle VARCHAR2(250) NOT NULL,
	ID NUMBER(38) NOT NULL
) ;
CREATE SEQUENCE AlternateTitle_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;


CREATE TABLE CI_Address ( 
	ID NUMBER(38) NOT NULL,
	FK_DeliveryPoint NUMBER(38),
	city VARCHAR2(200),
	administrativeArea VARCHAR2(200),
	postalCode VARCHAR2(50),
	country VARCHAR2(50),
	FK_Email NUMBER(38)
) ;
CREATE SEQUENCE CI_Address_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;


CREATE TABLE CI_Citation ( 
	ID NUMBER(38) NOT NULL,
	FK_PresFromCode NUMBER(38),
	title VARCHAR2(200) NOT NULL,
	edition VARCHAR2(2000),
	editionDate TIMESTAMP,
	identifier VARCHAR2(250),
	ISBN VARCHAR2(200),
	ISSN VARCHAR2(200),
	revisionDate TIMESTAMP,
	creationDate TIMESTAMP,
	publicationDate TIMESTAMP,
	fk_fileidentifier NUMBER(38),
	context VARCHAR2(50)
) ;
CREATE SEQUENCE CI_Citation_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE TABLE CI_Contact ( 
	ID NUMBER(38) NOT NULL,
	FK_Address NUMBER(38),
	FK_OnlineResource NUMBER(38),
	hoursOfService VARCHAR2(500),
	contactInstructions VARCHAR2(4000)
) ;
CREATE SEQUENCE CI_Contact_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;


CREATE TABLE CI_OnlineFunctionCode ( 
	ID NUMBER(38) NOT NULL,
	codeListValue VARCHAR2(500) NOT NULL,
	codeSpace VARCHAR2(500)
) ;
CREATE SEQUENCE CI_OnLineFunctionCode_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE TABLE CI_OnlineResource ( 
	ID NUMBER(38) NOT NULL,
	linkage VARCHAR2(500) NOT NULL,
	FK_function NUMBER(38)
) ;
CREATE SEQUENCE CI_OnlineResource_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE TABLE CI_PresentationFormCode ( 
	ID NUMBER(38) NOT NULL,
	codeListValue VARCHAR2(50) NOT NULL,
	codeSpace NUMBER(38)
) ;
CREATE SEQUENCE CI_PresentationFormCode_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE TABLE CI_RespParty ( 
	ID NUMBER(38) NOT NULL,
	individualName VARCHAR2(500),
	organisationName VARCHAR2(500),
	positionName VARCHAR2(200),
	FK_Role NUMBER(38) NOT NULL,
	FK_Contact NUMBER(38)
) ;
CREATE SEQUENCE CI_RespParty_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE TABLE CI_RoleCode ( 
	ID NUMBER(38) NOT NULL,
	codeListValue VARCHAR2(500) NOT NULL,
	codeSpace VARCHAR2(500)
) ;
CREATE SEQUENCE CI_RoleCode_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE TABLE CI_Series ( 
	ID NUMBER(38) NOT NULL,
	FK_Citation NUMBER(38) NOT NULL,
	name VARCHAR2(150),
	issueIdentification VARCHAR2(500),
	page VARCHAR2(250)
) ;
CREATE SEQUENCE CI_Series_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE TABLE CSW_CouplingType ( 
	ID NUMBER(38) NOT NULL,
	codeListValue VARCHAR2(50) NOT NULL,
	codeSpace VARCHAR2(250)
) ;
CREATE SEQUENCE CSW_CouplingType_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE TABLE CSW_ServiceIdentification ( 
	ID NUMBER(38) NOT NULL,
	serviceType VARCHAR2(50) NOT NULL,
	fees VARCHAR2(250),
	plannedAvailDateTime TIMESTAMP,
	orderingInstructions VARCHAR2(2000),
	turnaround VARCHAR2(500),
	FK_LegalConst NUMBER(38),
	FK_CouplingType NUMBER(38) NOT NULL,
	FK_SecConst NUMBER(38)
) ;
CREATE SEQUENCE CSW_ServiceIdentificati_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE TABLE DeliveryPoint ( 
	ID NUMBER(38) NOT NULL,
	deliveryPoint VARCHAR2(500) NOT NULL
) ;
CREATE SEQUENCE DeliveryPoint_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE TABLE DQ_DataQuality ( 
	ID NUMBER(38) NOT NULL,
	ScopeLevelCodeListValue VARCHAR2(150) NOT NULL,
	ScopeLeveDescription VARCHAR2(4000),
	lineageStatement VARCHAR2(4000),
	FK_Metadata NUMBER(38) NOT NULL
) ;
CREATE SEQUENCE DQ_DataQuality_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE TABLE DQ_Element ( 
	ID NUMBER(38) NOT NULL,
	nameOfMeasure VARCHAR2(50),
	uomName1 VARCHAR2(50) NOT NULL,
	convToISOStdUnit1 NUMBER(15,4) NOT NULL,
	value1 NUMBER(15,4) NOT NULL,
	uomName2 VARCHAR2(50),
	convToISOStdUnit2 NUMBER(15,4),
	value2 NUMBER(15,4),
	type VARCHAR2(50) NOT NULL,
	FK_DataQuality NUMBER(38) NOT NULL
) ;
CREATE SEQUENCE DQ_Element_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE TABLE ElectronicMailAddress ( 
	ID NUMBER(38) NOT NULL,
	email VARCHAR2(200) NOT NULL
) ;
CREATE SEQUENCE ElectronicMailAddress_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE TABLE EX_BoundingPolygon ( 
	FK_DataIdent NUMBER(38) NOT NULL,
	description VARCHAR2(4000),
	ID NUMBER(38) NOT NULL,
	crs VARCHAR2(150) NOT NULL,
	geom SDO_GEOMETRY
) ;
CREATE SEQUENCE EX_BoundingPolygon_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE TABLE EX_GeographicDescription ( 
	ID NUMBER(38) NOT NULL,
	geographicIdentifierCode VARCHAR2(150) NOT NULL,
	FK_DataIdent NUMBER(38)
) ;
CREATE SEQUENCE EX_GeographicDescriptio_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE TABLE EX_GeogrBBox ( 
	ID NUMBER(38) NOT NULL,
	FK_Owner NUMBER(38) NOT NULL,
	description VARCHAR2(4000),
	crs VARCHAR2(150) NOT NULL,
	geom SDO_GEOMETRY NOT NULL
) ;
CREATE SEQUENCE EX_GeogrBBox_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE TABLE EX_TemporalExtent ( 
	FK_DataIdent NUMBER(38) NOT NULL,
	description VARCHAR2(4000),
	begin_ TIMESTAMP,
	end_ TIMESTAMP,
	tmePosition TIMESTAMP,
	ID NUMBER(38) NOT NULL
) ;
CREATE SEQUENCE EX_TemporalExtent_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE TABLE EX_VerticalExtent ( 
	ID NUMBER(38) NOT NULL,
	minVal NUMBER(15,4) NOT NULL,
	maxVal NUMBER(15,4) NOT NULL,
	uomName VARCHAR2(150) NOT NULL,
	convToISOStdUnit NUMBER(15,4) NOT NULL,
	description VARCHAR2(4000),
	FK_DataIdent NUMBER(38) NOT NULL,
	FK_VerticalDatum NUMBER(38) NOT NULL
) ;
CREATE SEQUENCE EX_VerticalExtent_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE TABLE Facsimile ( 
	FK_Contact NUMBER(38) NOT NULL,
	faxnumber VARCHAR2(500) NOT NULL,
	ID NUMBER(38) NOT NULL
) ;
CREATE SEQUENCE Facsimile_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE TABLE FeatureTypes ( 
	localName VARCHAR2(100) NOT NULL,
	FK_FeatCatDesc NUMBER(38) NOT NULL,
	ID NUMBER(38) NOT NULL
) ;
CREATE SEQUENCE FeatureTypes_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE TABLE FileIdentifier ( 
	ID NUMBER(38) NOT NULL,
	fileidentifier VARCHAR2(250) NOT NULL
) ;
CREATE SEQUENCE FileIdentifier_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;


CREATE TABLE HierarchylevelCode ( 
	ID NUMBER(38) NOT NULL,
	codeListValue VARCHAR2(150) NOT NULL,
	codeSpace VARCHAR2(500)
) ;
CREATE SEQUENCE HierarchylevelCode_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE TABLE HierarchylevelName ( 
	ID NUMBER(38) NOT NULL,
	Name VARCHAR2(100) NOT NULL
) ;
CREATE SEQUENCE HierarchylevelName_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE TABLE JT_Citation_RespParty ( 
	FK_Citation NUMBER(38) NOT NULL,
	FK_RespParty NUMBER(38) NOT NULL
) ;

CREATE TABLE JT_DataIdent_SpatialRepType ( 
	FK_DataIdent NUMBER(38) NOT NULL,
	FK_SpatialRepType NUMBER(38) NOT NULL
) ;

CREATE TABLE JT_DataIdent_TopicCat ( 
	FK_DataIdent NUMBER(38) NOT NULL,
	FK_TopicCategory NUMBER(38) NOT NULL
) ;

CREATE TABLE JT_DigTransOpt_MediumFormat ( 
	fk_digtransopt NUMBER(38) NOT NULL,
	fk_mediumformat NUMBER(38) NOT NULL
) ;

CREATE TABLE JT_DigTransOpt_MediumName ( 
	fk_digtransopt NUMBER(38) NOT NULL,
	fk_mediumname NUMBER(38) NOT NULL
) ;

CREATE TABLE JT_Dist_DistFormat ( 
	fk_distribution NUMBER(38) NOT NULL,
	FK_Format NUMBER(38) NOT NULL
) ;

CREATE TABLE JT_Dist_Distributor ( 
	fk_distribution NUMBER(38) NOT NULL,
	fk_distributor NUMBER(38) NOT NULL
) ;

CREATE TABLE JT_Ident_Keywords ( 
	FK_Ident NUMBER(38) NOT NULL,
	FK_Keywords NUMBER(38) NOT NULL
) ;

CREATE TABLE JT_Ident_LegalConst ( 
	FK_Identification NUMBER(38) NOT NULL,
	FK_LegalConst NUMBER(38) NOT NULL
) ;

CREATE TABLE JT_Ident_Mainten ( 
	FK_Identification NUMBER(38) NOT NULL,
	FK_Maintenance NUMBER(38) NOT NULL
) ;

CREATE TABLE JT_Ident_RespParty ( 
	FK_Identification NUMBER(38) NOT NULL,
	FK_ResponsibleParty NUMBER(38) NOT NULL
) ;

CREATE TABLE JT_Ident_SecConst ( 
	FK_SecConstraint NUMBER(38) NOT NULL,
	FK_Identification NUMBER(38) NOT NULL
) ;

CREATE TABLE JT_Ident_Usage ( 
	FK_Ident NUMBER(38) NOT NULL,
	FK_Usage NUMBER(38) NOT NULL
) ;

CREATE TABLE JT_Keywords_Keyword ( 
	FK_Keywords NUMBER(38) NOT NULL,
	FK_Keyword NUMBER(38) NOT NULL
) ;

CREATE TABLE JT_LegalConst_accessConst ( 
	FK_LegalConst NUMBER(38) NOT NULL,
	FK_RestrictCode NUMBER(38) NOT NULL
) ;

CREATE TABLE JT_LegalConst_useConst ( 
	FK_LegalConst NUMBER(38) NOT NULL,
	FK_RestrictCode NUMBER(38) NOT NULL
) ;

CREATE TABLE JT_LI_SRC_LI_PROCSTEP ( 
	fk_source NUMBER(38) NOT NULL,
	fk_procstep NUMBER(38) NOT NULL
) ;

CREATE TABLE JT_Metadata_AppSchemaInf ( 
	fk_metadata NUMBER(38) NOT NULL,
	fk_AppSchemaInf NUMBER(38) NOT NULL
) ;

CREATE TABLE JT_Metadata_FeatCatDesc ( 
	FK_Metadata NUMBER(38) NOT NULL,
	FK_FeatCatDesc NUMBER(38) NOT NULL
) ;

CREATE TABLE JT_Metadata_LegalConst ( 
	FK_Metadata NUMBER(38) NOT NULL,
	FK_LegalConstraint NUMBER(38) NOT NULL
) ;

CREATE TABLE JT_Metadata_PortCatRef ( 
	FK_Metadata NUMBER(38) NOT NULL,
	FK_PortCatRef NUMBER(38)
) ;

CREATE TABLE JT_Metadata_RefSys ( 
	FK_Metadata NUMBER(38) NOT NULL,
	FK_RefSys NUMBER(38) NOT NULL
) ;

CREATE TABLE JT_Metadata_RespParty ( 
	FK_Metadata NUMBER(38) NOT NULL,
	FK_RespParty NUMBER(38) NOT NULL
) ;

CREATE TABLE JT_Metadata_SecConst ( 
	FK_Metadata NUMBER(38) NOT NULL,
	FK_SecConstraints NUMBER(38) NOT NULL
) ;

CREATE TABLE JT_Operation_DCP ( 
	FK_Operation NUMBER(38) NOT NULL,
	FK_DCP NUMBER(38) NOT NULL
) ;

CREATE TABLE JT_Operation_Name ( 
	fk_operation NUMBER(38) NOT NULL,
	fk_name NUMBER(38) NOT NULL
) ;

CREATE TABLE JT_Operation_OperatesOn ( 
	fk_operation NUMBER(38) NOT NULL,
	fk_operateson NUMBER(38) NOT NULL
) ;

CREATE TABLE JT_OpMeta_OnlineRes ( 
	FK_Operation NUMBER(38) NOT NULL,
	FK_OnlineResource NUMBER(38) NOT NULL
) ;

CREATE TABLE JT_Quality_Procstep ( 
	fk_quality NUMBER(38) NOT NULL,
	fk_procstep NUMBER(38) NOT NULL
) ;

CREATE TABLE JT_SecConst_ClassificationCode ( 
	FK_SecConst NUMBER(38) NOT NULL,
	FK_ClassificationCode NUMBER(38) NOT NULL
) ;

CREATE TABLE Keyword ( 
	ID NUMBER(38) NOT NULL,
	keyword VARCHAR2(200) NOT NULL
) ;
CREATE SEQUENCE Keyword_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE TABLE LI_ProcessStep ( 
	ID NUMBER(38) NOT NULL,
	description VARCHAR2(2000) NOT NULL,
	rationale VARCHAR2(500),
	dateTime TIMESTAMP,
	fk_processor NUMBER(38)
) ;
CREATE SEQUENCE LI_ProcessStep_ID_SEQ increment by 1 start with 0 NOMAXVALUE minvalue 0 nocycle nocache noorder;

CREATE TABLE LI_Source ( 
	ID NUMBER(38) NOT NULL,
	fk_dataquality NUMBER(38) NOT NULL,
	description VARCHAR2(2000),
	scaleDenominator NUMBER(38),
	fk_sourceReferenceSystem NUMBER(38),
	fk_sourceCitation NUMBER(38)
) ;
CREATE SEQUENCE LI_Source_ID_SEQ increment by 1 start with 0 NOMAXVALUE minvalue 0 nocycle nocache noorder;

CREATE TABLE MD_AppSchemaInformation ( 
	ID NUMBER(38) NOT NULL,
	fk_citation NUMBER(38) NOT NULL,
	schemaLanguage VARCHAR2(50) NOT NULL,
	constraintLanguage VARCHAR2(50) NOT NULL,
	schemaAscii VARCHAR2(50),
	graphicsFile64b CLOB,
	graphicsFileHex CLOB,
	softwareDevelFile64b CLOB,
	softwareDevelFileHex CLOB,
	softwareDevelFileFormat VARCHAR2(150)
) ;
CREATE SEQUENCE MD_ApplicationSchemaInf_ID_SEQ increment by 1 start with 0 NOMAXVALUE minvalue 0 nocycle nocache noorder;

CREATE TABLE MD_BrowseGraphic ( 
	FK_Ident NUMBER(38) NOT NULL,
	fileName VARCHAR2(500) NOT NULL,
	fileDescription VARCHAR2(4000),
	fileType VARCHAR2(12),
	ID NUMBER(38) NOT NULL
) ;
CREATE SEQUENCE MD_BrowseGraphic_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE TABLE MD_CharacterSetCode ( 
	ID NUMBER(38) NOT NULL,
	codeListValue VARCHAR2(50) NOT NULL,
	codeSpace VARCHAR2(500)
) ;
CREATE SEQUENCE MD_CharacterSetCode_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE TABLE MD_ClassificationCode ( 
	ID NUMBER(38) NOT NULL,
	codeListValue VARCHAR2(50) NOT NULL,
	codeSpace VARCHAR2(500)
) ;
CREATE SEQUENCE MD_ClassificationCode_ID_SEQ increment by 1 start with 0 NOMAXVALUE minvalue 0 nocycle nocache noorder;

CREATE TABLE MD_Constraints ( 
	ID NUMBER(38) NOT NULL,
	useLimitation VARCHAR2(4000)
) ;

CREATE TABLE MD_DataIdentification ( 
	ID NUMBER(38) NOT NULL,
	language VARCHAR2(150) NOT NULL,
	supplementalInformation VARCHAR2(500),
	FK_Characterset NUMBER(38)
) ;
CREATE SEQUENCE MD_DataIdentification_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE TABLE MD_DigTransferOpt ( 
	ID NUMBER(38) NOT NULL,
	fk_distribution NUMBER(38) NOT NULL,
	unitsOfDistribution VARCHAR2(150),
	fk_OnlineResource NUMBER(38),
	transferSize NUMBER(15,4),
	off_mediumnote VARCHAR2(4000)
) ;
CREATE SEQUENCE MD_DigTransferOpt_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE TABLE MD_Distribution ( 
	ID NUMBER(38) NOT NULL,
	fk_metadata NUMBER(38) NOT NULL
) ;
CREATE SEQUENCE MD_Distribution_ID_SEQ increment by 1 start with 0 NOMAXVALUE minvalue 0 nocycle nocache noorder;


CREATE TABLE MD_Distributor ( 
	ID NUMBER(38) NOT NULL,
	FK_ResponsibleParty NUMBER(38) NOT NULL
) ;
CREATE SEQUENCE MD_Distributor_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE TABLE MD_FeatCatDesc ( 
	ID NUMBER(38) NOT NULL,
	FK_Citation NUMBER(38) NOT NULL,
	language VARCHAR2(200),
	includedWithDataset CHAR NOT NULL
) ;
CREATE SEQUENCE MD_FeatCatDesc_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE TABLE MD_Format ( 
	ID NUMBER(38) NOT NULL,
	name VARCHAR2(200) NOT NULL,
	version VARCHAR2(50) NOT NULL,
	specification VARCHAR2(4000),
	fileDecompTech VARCHAR2(500),
	amendmentNumber VARCHAR2(150)
) ;
CREATE SEQUENCE MD_Format_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE TABLE MD_GeoObjTypeCode ( 
	ID NUMBER(38) NOT NULL,
	codeListValue VARCHAR2(50) NOT NULL,
	codeSpace VARCHAR2(500)
) ;
CREATE SEQUENCE MD_GeoObjTypeCode_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE TABLE MD_Identification ( 
	ID NUMBER(38) NOT NULL,
	FK_Progress NUMBER(38),
	FK_Citation NUMBER(38) NOT NULL,
	abstract VARCHAR2(4000) NOT NULL,
	purpose VARCHAR2(1000)
) ;
CREATE SEQUENCE MD_Identification_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE TABLE MD_Keywords ( 
	ID NUMBER(38) NOT NULL,
	FK_Thesaurus NUMBER(38),
	FK_Type NUMBER(38)
) ;
CREATE SEQUENCE MD_Keywords_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE TABLE MD_KeywordTypeCode ( 
	ID NUMBER(38) NOT NULL,
	codeListValue VARCHAR2(100) NOT NULL,
	codeSpace VARCHAR2(250)
) ;
CREATE SEQUENCE MD_KeywordTypeCode_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE TABLE MD_LegalConstraints ( 
	ID NUMBER(38) NOT NULL,
	defined CHAR NOT NULL,
	useLimitations VARCHAR2(4000)
) ;
CREATE SEQUENCE MD_LegalConstraints_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE TABLE MD_MainFreqCode ( 
	ID NUMBER(38) NOT NULL,
	codeListValue VARCHAR2(500) NOT NULL,
	codeSpace VARCHAR2(500)
) ;
CREATE SEQUENCE MD_MainFreqCode_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE TABLE MD_MaintenanceInformation ( 
	ID NUMBER(38) NOT NULL,
	FK_MainFreq NUMBER(38) NOT NULL,
	FK_Scope NUMBER(38),
	dateOfNextUpdate TIMESTAMP,
	userDefFrequency VARCHAR2(150),
	note VARCHAR2(4000)
) ;
CREATE SEQUENCE MD_MaintenanceInformati_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE TABLE MD_MediumFormatCode ( 
	ID NUMBER(38) NOT NULL,
	codeListValue VARCHAR2(50) NOT NULL,
	codeSpace VARCHAR2(500)
) ;
CREATE SEQUENCE MD_MediumFormatCode_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE TABLE MD_MediumNameCode ( 
	ID NUMBER(38) NOT NULL,
	codeListValue VARCHAR2(50) NOT NULL,
	codeSpace VARCHAR2(500)
) ;
CREATE SEQUENCE MD_MediumNameCode_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE TABLE MD_Metadata ( 
	ID NUMBER(38) NOT NULL,
	FK_fileIdentifier NUMBER(38) NOT NULL,
	language VARCHAR2(100),
	FK_Characterset NUMBER(38),
	parentIdentifier VARCHAR2(150),
	dateStamp TIMESTAMP NOT NULL,
	mdStandName VARCHAR2(50),
	mdStandVersion VARCHAR2(50),
	FK_HLevelCode NUMBER(38) NOT NULL,
	FK_HLevelName NUMBER(38) NOT NULL,
	testdata VARCHAR2(255)
) ;
CREATE SEQUENCE MD_Metadata_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE TABLE MD_PortrayalCatRef ( 
	FK_Citation NUMBER(38) NOT NULL,
	ID NUMBER(38) NOT NULL
) ;
CREATE SEQUENCE MD_PortrayalCatRef_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE TABLE MD_ProgressCode ( 
	ID NUMBER(38) NOT NULL,
	codeListValue VARCHAR2(500) NOT NULL,
	codeSpace VARCHAR2(500)
) ;
CREATE SEQUENCE MD_ProgressCode_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE TABLE MD_Resolution ( 
	ID NUMBER(38) NOT NULL,
	FK_DataIdent NUMBER(38) NOT NULL,
	equivalentScale NUMBER(38),
	distanceValue NUMBER(10,3),
	uomName VARCHAR2(50),
	convToISOStdUnit NUMBER(15,4)
) ;
CREATE SEQUENCE MD_Resolution_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE TABLE MD_RestrictionCode ( 
	ID NUMBER(38) NOT NULL,
	codeListValue VARCHAR2(50) NOT NULL,
	codeSpace VARCHAR2(500)
) ;
CREATE SEQUENCE MD_RestrictionCode_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE TABLE MD_ScopeCode ( 
	ID NUMBER(38) NOT NULL,
	codeListValue VARCHAR2(50) NOT NULL,
	codeSpace VARCHAR2(500)
) ;
CREATE SEQUENCE MD_ScopeCode_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE TABLE MD_SecurityConstraints ( 
	ID NUMBER(38) NOT NULL,
	userNote VARCHAR2(2000),
	classificationSystem VARCHAR2(200),
	handlingDescription VARCHAR2(200),
	useLimitations VARCHAR2(4000)
) ;
CREATE SEQUENCE MD_SecurityConstraints_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE TABLE MD_SpatialRepTypeCode ( 
	ID NUMBER(38) NOT NULL,
	codeListValue VARCHAR2(50) NOT NULL,
	codeSpace VARCHAR2(500)
) ;
CREATE SEQUENCE MD_SpatialRepTypeCode_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE TABLE MD_StandOrderProc ( 
	ID NUMBER(38) NOT NULL,
	fees VARCHAR2(2000),
	orderingInstructions VARCHAR2(4000),
	turnaround VARCHAR2(500),
	FK_Distributor NUMBER(38) NOT NULL
) ;
CREATE SEQUENCE MD_StandOrderProc_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE TABLE MD_TopicCategoryCode ( 
	ID NUMBER(38) NOT NULL,
	category VARCHAR2(500) NOT NULL
) ;
CREATE SEQUENCE MD_TopicCategoryCode_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE TABLE MD_TopoLevelCode ( 
	ID NUMBER(38) NOT NULL,
	codeListValue VARCHAR2(50) NOT NULL,
	codeSpace VARCHAR2(500)
) ;
CREATE SEQUENCE MD_TopoLevelCode_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE TABLE MD_Usage ( 
	ID NUMBER(38) NOT NULL,
	specificUsage VARCHAR2(500) NOT NULL,
	fk_usercontactinfo NUMBER(38) NOT NULL
) ;
CREATE SEQUENCE MD_Usage_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE TABLE MD_VecSpatialRep ( 
	ID NUMBER(38) NOT NULL,
	FK_TopoLevelCode NUMBER(38),
	FK_GeoObjTypeCode NUMBER(38),
	FK_Metadata NUMBER(38) NOT NULL,
	geoObjCount NUMBER(38)
) ;
CREATE SEQUENCE MD_VectorSpatialReprens_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE TABLE OperatesOn ( 
	ID NUMBER(38) NOT NULL,
	fk_dataidentification NUMBER(38),
	fk_serviceidentification NUMBER(38) NOT NULL,
	name VARCHAR2(250) NOT NULL,
	title VARCHAR2(250),
	abstract VARCHAR2(4000)
) ;
CREATE SEQUENCE OperatesOn_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE TABLE OperationNames ( 
	ID NUMBER(38) NOT NULL,
	name VARCHAR2(50) NOT NULL,
	namespace VARCHAR2(250)
) ;
CREATE SEQUENCE OperationNames_ID_SEQ increment by 1 start with 0 NOMAXVALUE minvalue 0 nocycle nocache noorder;

CREATE TABLE OtherConstraints ( 
	ID NUMBER(38) NOT NULL,
	FK_LegalConstraints NUMBER(38) NOT NULL,
	constraint_ VARCHAR2(4000) NOT NULL
) ;
CREATE SEQUENCE OtherConstraints_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE TABLE RS_Identifier ( 
	ID NUMBER(38) NOT NULL,
	code VARCHAR2(150) NOT NULL,
	codeSpace VARCHAR2(250),
	version VARCHAR2(50),
	FK_Authority NUMBER(38)
) ;
CREATE SEQUENCE RS_Identifier_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE TABLE ServiceVersion ( 
	FK_ServiceIdent NUMBER(38) NOT NULL,
	version VARCHAR2(10) NOT NULL,
	ID NUMBER(38) NOT NULL
) ;
CREATE SEQUENCE ServiceVersion_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE TABLE SV_DCPList ( 
	ID NUMBER(38) NOT NULL,
	codeListValue VARCHAR2(50) NOT NULL,
	codeSpace VARCHAR2(150)
) ;
CREATE SEQUENCE SV_DCPList_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE TABLE SV_OperationMetadata ( 
	ID NUMBER(38) NOT NULL,
	operationDescription VARCHAR2(4000),
	invocationName VARCHAR2(150),
	FK_ServiceIdent NUMBER(38) NOT NULL
) ;
CREATE SEQUENCE SV_OperationMetadata_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE TABLE SV_Parameter ( 
	FK_Operation NUMBER(38) NOT NULL,
	name VARCHAR2(50) NOT NULL,
	type VARCHAR2(150) NOT NULL,
	direction VARCHAR2(6) NOT NULL,
	description VARCHAR2(4000) NOT NULL,
	optionality VARCHAR2(20) NOT NULL,
	repeatability CHAR NOT NULL,
	ID NUMBER(38) NOT NULL
) ;
CREATE SEQUENCE SV_Parameter_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

CREATE TABLE Voice ( 
	FK_Contact NUMBER(38) NOT NULL,
	voicenumber VARCHAR2(500) NOT NULL,
	ID NUMBER(38) NOT NULL
) ;
CREATE SEQUENCE Voice_ID_SEQ increment by 1 start with 1 NOMAXVALUE minvalue 1 nocycle nocache noorder;

ALTER TABLE AlternateTitle ADD CONSTRAINT PK_AlternateTitle 
	PRIMARY KEY (ID) ;

ALTER TABLE CI_Address ADD CONSTRAINT PK_CI_Address 
	PRIMARY KEY (ID) ;

ALTER TABLE CI_Citation ADD CONSTRAINT PK_CI_Citation 
	PRIMARY KEY (ID) ;

ALTER TABLE CI_Contact ADD CONSTRAINT PK_CI_Contact 
	PRIMARY KEY (ID) ;

ALTER TABLE CI_OnLineFunctionCode ADD CONSTRAINT PK_CI_OnLineFunction 
	PRIMARY KEY (ID) ;

ALTER TABLE CI_OnlineResource ADD CONSTRAINT PK_CI_OnlineResource 
	PRIMARY KEY (ID) ;

ALTER TABLE CI_PresentationFormCode ADD CONSTRAINT PK_CI_PresentationFormCode 
	PRIMARY KEY (ID) ;

ALTER TABLE CI_RespParty ADD CONSTRAINT PK_CI_RespParty 
	PRIMARY KEY (ID) ;

ALTER TABLE CI_RoleCode ADD CONSTRAINT PK_CI_RoleCode 
	PRIMARY KEY (ID) ;

ALTER TABLE CI_Series ADD CONSTRAINT PK_CI_Series 
	PRIMARY KEY (ID) ;

ALTER TABLE CSW_CouplingType ADD CONSTRAINT PK_CSW_CouplingType 
	PRIMARY KEY (ID) ;

ALTER TABLE CSW_ServiceIdentification ADD CONSTRAINT PK_ServiceIdentification 
	PRIMARY KEY (ID) ;

ALTER TABLE DeliveryPoint ADD CONSTRAINT PK_DeliveryPoint 
	PRIMARY KEY (ID) ;

ALTER TABLE DQ_DataQuality ADD CONSTRAINT PK_DQ_DataQuality 
	PRIMARY KEY (ID) ;

ALTER TABLE DQ_Element ADD CONSTRAINT PK_DQ_Element 
	PRIMARY KEY (ID) ;

ALTER TABLE ElectronicMailAddress ADD CONSTRAINT PK_ElectronicMailAddress 
	PRIMARY KEY (ID) ;

ALTER TABLE EX_BoundingPolygon ADD CONSTRAINT PK_EX_BoundingPolygon 
	PRIMARY KEY (ID) ;

ALTER TABLE EX_GeographicDescription ADD CONSTRAINT PK_EX_GeographicDescription 
	PRIMARY KEY (ID) ;

ALTER TABLE EX_GeogrBBox ADD CONSTRAINT PK_EX_GeogrBBox 
	PRIMARY KEY (ID) ;

ALTER TABLE EX_TemporalExtent ADD CONSTRAINT PK_EX_TemporalExtent 
	PRIMARY KEY (ID) ;

ALTER TABLE EX_VerticalExtent ADD CONSTRAINT PK_EX_VerticalExtent 
	PRIMARY KEY (ID) ;

ALTER TABLE Facsimile ADD CONSTRAINT PK_Facsimile 
	PRIMARY KEY (ID) ;

ALTER TABLE FeatureTypes ADD CONSTRAINT PK_FeatureTypes 
	PRIMARY KEY (ID) ;

ALTER TABLE FileIdentifier ADD CONSTRAINT PK_FileIdentifier 
	PRIMARY KEY (ID) ;

ALTER TABLE HierarchylevelCode ADD CONSTRAINT PK_HierarchylevelCode 
	PRIMARY KEY (ID) ;

ALTER TABLE HierarchylevelName ADD CONSTRAINT PK_HierachylevelName 
	PRIMARY KEY (ID) ;

ALTER TABLE Keyword ADD CONSTRAINT PK_Keyword 
	PRIMARY KEY (ID) ;

ALTER TABLE LI_ProcessStep ADD CONSTRAINT PK_LI_ProcessStep 
	PRIMARY KEY (ID) ;

ALTER TABLE LI_Source ADD CONSTRAINT PK_LI_Source 
	PRIMARY KEY (ID) ;

ALTER TABLE MD_AppSchemaInformation ADD CONSTRAINT PK_MD_AppSchemaInformation 
	PRIMARY KEY (ID) ;

ALTER TABLE MD_BrowseGraphic ADD CONSTRAINT PK_MD_BrowseGraphic 
	PRIMARY KEY (ID) ;

ALTER TABLE MD_CharacterSetCode ADD CONSTRAINT PK_MD_CharacterSetCode 
	PRIMARY KEY (ID) ;

ALTER TABLE MD_ClassificationCode ADD CONSTRAINT PK_MD_ClassificationCode 
	PRIMARY KEY (ID) ;

ALTER TABLE MD_Constraints ADD CONSTRAINT PK_MD_Constraints 
	PRIMARY KEY (ID) ;

ALTER TABLE MD_DataIdentification ADD CONSTRAINT PK_MD_DataIdent 
	PRIMARY KEY (ID) ;

ALTER TABLE MD_DigTransferOpt ADD CONSTRAINT PK_MD_DigTransferOpt 
	PRIMARY KEY (ID) ;

ALTER TABLE MD_Distribution ADD CONSTRAINT PK_MD_Distribution 
	PRIMARY KEY (ID) ;

ALTER TABLE MD_Distributor ADD CONSTRAINT PK_MD_Distributor 
	PRIMARY KEY (ID) ;

ALTER TABLE MD_FeatCatDesc ADD CONSTRAINT PK_MD_FeatCatDesc 
	PRIMARY KEY (ID) ;

ALTER TABLE MD_Format ADD CONSTRAINT PK_MD_Format 
	PRIMARY KEY (ID) ;

ALTER TABLE MD_GeoObjTypeCode ADD CONSTRAINT PK_MD_GeoObjTypeCode 
	PRIMARY KEY (ID) ;

ALTER TABLE MD_Identification ADD CONSTRAINT PK_MD_Identification 
	PRIMARY KEY (ID) ;

ALTER TABLE MD_Keywords ADD CONSTRAINT PK_MD_Keywords 
	PRIMARY KEY (ID) ;

ALTER TABLE MD_KeywordTypeCode ADD CONSTRAINT PK_MD_KeywordTypeCode 
	PRIMARY KEY (ID) ;

ALTER TABLE MD_LegalConstraints ADD CONSTRAINT PK_MD_LegalConstraints 
	PRIMARY KEY (ID) ;

ALTER TABLE MD_MainFreqCode ADD CONSTRAINT PK_MD_MaintenanceInf 
	PRIMARY KEY (ID) ;

ALTER TABLE MD_MaintenanceInformation ADD CONSTRAINT PK_MD_MaintenanceInformation 
	PRIMARY KEY (ID) ;

ALTER TABLE MD_MediumFormatCode ADD CONSTRAINT PK_MD_MediumFormatCode 
	PRIMARY KEY (ID) ;

ALTER TABLE MD_MediumNameCode ADD CONSTRAINT PK_MD_MediumNameCode 
	PRIMARY KEY (ID) ;

ALTER TABLE MD_Metadata ADD CONSTRAINT PK_MD_Metadata 
	PRIMARY KEY (ID) ;

ALTER TABLE MD_PortrayalCatRef ADD CONSTRAINT PK_MD_PortrayalCatRef 
	PRIMARY KEY (ID) ;

ALTER TABLE MD_ProgressCode ADD CONSTRAINT PK_MD_ProgressCode 
	PRIMARY KEY (ID) ;

ALTER TABLE MD_Resolution ADD CONSTRAINT PK_MD_Resolution 
	PRIMARY KEY (ID) ;

ALTER TABLE MD_RestrictionCode ADD CONSTRAINT PK_MD_RestrictionCode 
	PRIMARY KEY (ID) ;

ALTER TABLE MD_ScopeCode ADD CONSTRAINT PK_MD_ScopeCode 
	PRIMARY KEY (ID) ;

ALTER TABLE MD_SecurityConstraints ADD CONSTRAINT PK_MD_SecurityConstraints 
	PRIMARY KEY (ID) ;

ALTER TABLE MD_SpatialRepTypeCode ADD CONSTRAINT PK_MD_SpatialRepType 
	PRIMARY KEY (ID) ;

ALTER TABLE MD_StandOrderProc ADD CONSTRAINT PK_MD_StandOrderProc 
	PRIMARY KEY (ID) ;

ALTER TABLE MD_TopicCategoryCode ADD CONSTRAINT PK_MD_TopicCategory 
	PRIMARY KEY (ID) ;

ALTER TABLE MD_TopoLevelCode ADD CONSTRAINT PK_MD_TopoLevelCode 
	PRIMARY KEY (ID) ;

ALTER TABLE MD_Usage ADD CONSTRAINT PK_MD_Usage 
	PRIMARY KEY (ID) ;

ALTER TABLE MD_VecSpatialRep ADD CONSTRAINT PK_MD_VecSpatialRep 
	PRIMARY KEY (ID) ;

ALTER TABLE OperatesOn ADD CONSTRAINT PK_OperatesOn 
	PRIMARY KEY (ID) ;

ALTER TABLE OperationNames ADD CONSTRAINT PK_OperationNames 
	PRIMARY KEY (ID) ;

ALTER TABLE OtherConstraints ADD CONSTRAINT PK_OtherConstraints 
	PRIMARY KEY (ID) ;

ALTER TABLE RS_Identifier ADD CONSTRAINT PK_MD_ReferenceSystem 
	PRIMARY KEY (ID) ;

ALTER TABLE ServiceVersion ADD CONSTRAINT PK_ServiceVersion 
	PRIMARY KEY (ID) ;

ALTER TABLE SV_DCPList ADD CONSTRAINT PK_SV_DCPList 
	PRIMARY KEY (ID) ;

ALTER TABLE SV_OperationMetadata ADD CONSTRAINT PK_SV_OperationMetadata 
	PRIMARY KEY (ID) ;

ALTER TABLE SV_Parameter ADD CONSTRAINT PK_SV_Parameter 
	PRIMARY KEY (ID) ;

ALTER TABLE Voice ADD CONSTRAINT PK_Voice 
	PRIMARY KEY (ID) ;



ALTER TABLE CI_OnLineFunctionCode
	ADD CONSTRAINT UQ_CI_OnLineFuncCode_clValue UNIQUE (codeListValue) ;

ALTER TABLE CI_PresentationFormCode
	ADD CONSTRAINT UQ_CI_PreseFormCode_clValue UNIQUE (codeListValue) ;

ALTER TABLE CI_RoleCode
	ADD CONSTRAINT UQ_CI_RoleCode_codeListValue UNIQUE (codeListValue) ;

ALTER TABLE CSW_CouplingType
	ADD CONSTRAINT UQ_CouplingType_clValue UNIQUE (codeListValue) ;

ALTER TABLE FileIdentifier
	ADD CONSTRAINT UQ_FileIdentifier_fid UNIQUE (fileidentifier) ;

ALTER TABLE HierarchylevelCode
	ADD CONSTRAINT UQ_HierarchylevelCode_clValue UNIQUE (codeListValue) ;

ALTER TABLE HierarchylevelName
	ADD CONSTRAINT UQ_HierachylevelName_Name UNIQUE (Name) ;

ALTER TABLE MD_CharacterSetCode
	ADD CONSTRAINT UQ_MD_CharacterSetCode_clValue UNIQUE (codeListValue) ;

ALTER TABLE MD_ClassificationCode
	ADD CONSTRAINT UQ_MD_ClassCode_clValue UNIQUE (codeListValue) ;

ALTER TABLE MD_GeoObjTypeCode
	ADD CONSTRAINT UQ_MD_GeoObjTypeCode_clValue UNIQUE (codeListValue) ;

ALTER TABLE MD_MainFreqCode
	ADD CONSTRAINT UQ_MD_MainFreqCode_clValue UNIQUE (codeListValue) ;

ALTER TABLE MD_MediumFormatCode
	ADD CONSTRAINT UQ_MD_MediumFormatCode_clValue UNIQUE (codeListValue) ;

ALTER TABLE MD_MediumNameCode
	ADD CONSTRAINT UQ_MD_MediumNameCode_clValue UNIQUE (codeListValue) ;

ALTER TABLE MD_Metadata
	ADD CONSTRAINT UQ_MD_Metadata_FK_fid UNIQUE (FK_fileIdentifier) ;

ALTER TABLE MD_ProgressCode
	ADD CONSTRAINT UQ_MD_ProgressCode_clValue UNIQUE (codeListValue) ;

ALTER TABLE MD_RestrictionCode
	ADD CONSTRAINT UQ_MD_RestrictionCode_clValue UNIQUE (codeListValue) ;

ALTER TABLE MD_ScopeCode
	ADD CONSTRAINT UQ_MD_ScopeCode_clValue UNIQUE (codeListValue) ;

ALTER TABLE MD_SpatialRepTypeCode
	ADD CONSTRAINT UQ_MD_SpatRepTypeCd_clValue UNIQUE (codeListValue) ;

ALTER TABLE MD_TopicCategoryCode
	ADD CONSTRAINT UQ_MD_TopicCatCode_cat UNIQUE (category) ;

ALTER TABLE MD_TopoLevelCode
	ADD CONSTRAINT UQ_MD_TopoLevelCode_clValue UNIQUE (codeListValue) ;

ALTER TABLE SV_DCPList
	ADD CONSTRAINT UQ_SV_DCPList_clValue UNIQUE (codeListValue) ;
   
-- set id before inserting a new row
CREATE OR REPLACE TRIGGER SET_otherconstraints_id
BEFORE INSERT
ON otherconstraints
FOR EACH ROW
BEGIN
  SELECT otherconstraints_id_SEQ.NEXTVAL
  INTO :NEW.id
  FROM DUAL;
END;
/
CREATE OR REPLACE TRIGGER SET_voice_id
BEFORE INSERT
ON voice
FOR EACH ROW
BEGIN
  SELECT voice_id_SEQ.NEXTVAL
  INTO :NEW.id
  FROM DUAL;
END;
/
CREATE OR REPLACE TRIGGER SET_facsimile_id
BEFORE INSERT
ON facsimile
FOR EACH ROW
BEGIN
  SELECT facsimile_id_SEQ.NEXTVAL
  INTO :NEW.id
  FROM DUAL;
END;
/
CREATE OR REPLACE TRIGGER SET_alternatetitle_id
BEFORE INSERT
ON alternatetitle
FOR EACH ROW
BEGIN
  SELECT alternatetitle_id_SEQ.NEXTVAL
  INTO :NEW.id
  FROM DUAL;
END;
/
CREATE OR REPLACE TRIGGER SET_ServiceVersion_id
BEFORE INSERT
ON ServiceVersion
FOR EACH ROW
BEGIN
  SELECT ServiceVersion_id_SEQ.NEXTVAL
  INTO :NEW.id
  FROM DUAL;
END;
/ 
CREATE OR REPLACE TRIGGER SET_EX_GeoDesc_id
BEFORE INSERT
ON EX_GeographicDescription
FOR EACH ROW
BEGIN
  SELECT EX_GeographicDescriptio_id_SEQ.NEXTVAL
  INTO :NEW.id
  FROM DUAL;
END;
/   
    
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
insert into sv_dcplist ( ID, codeListValue ) values ( 1, 'HTTPGet' );
insert into sv_dcplist ( ID, codeListValue ) values ( 2,'HTTPPost' );
insert into sv_dcplist ( ID, codeListValue ) values ( 3,'HTTPSoap' );
insert into sv_dcplist ( ID, codeListValue ) values ( 4,'COM' );
insert into sv_dcplist ( ID, codeListValue ) values ( 5,'XML' );
insert into sv_dcplist ( ID, codeListValue ) values ( 6,'SQL' );
insert into sv_dcplist ( ID, codeListValue ) values ( 7,'Corba' );
insert into sv_dcplist ( ID, codeListValue ) values ( 8,'Java' );

-- Codelist MD ClassificationCode (B5.11)
insert into md_classificationcode ( ID, codeListValue ) values ( 1,'unclassified' );
insert into md_classificationcode ( ID, codeListValue ) values ( 2,'restricted' );
insert into md_classificationcode ( ID, codeListValue ) values ( 3,'confidential' );
insert into md_classificationcode ( ID, codeListValue ) values ( 4,'secret' );
insert into md_classificationcode ( ID, codeListValue ) values ( 5,'topSecret' );

commit;
