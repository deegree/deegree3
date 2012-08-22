SELECT DropGeometryColumn('', 'ex_boundingpolygon','geom');
SELECT DropGeometryColumn('', 'ex_geogrbbox','geom');

DROP SEQUENCE AlternateTitle_ID_seq ;
DROP SEQUENCE CI_Address_ID_seq ;
DROP SEQUENCE CI_Citation_ID_seq ;
DROP SEQUENCE CI_Contact_ID_seq ;
DROP SEQUENCE CI_OnLineFunctionCode_ID_seq ;
DROP SEQUENCE CI_OnlineResource_ID_seq ;
DROP SEQUENCE CI_PresentationFormCode_ID_seq ;
DROP SEQUENCE CI_RespParty_ID_seq ;
DROP SEQUENCE CI_RoleCode_ID_seq ;
DROP SEQUENCE CI_Series_ID_seq ;
DROP SEQUENCE CSW_CouplingType_ID_seq ;
DROP SEQUENCE CSW_ServiceIdentification_ID_seq ;
DROP SEQUENCE DeliveryPoint_ID_seq ;
DROP SEQUENCE DQ_DataQuality_ID_seq ;
DROP SEQUENCE DQ_Element_ID_seq ;
DROP SEQUENCE ElectronicMailAddress_ID_seq ;
DROP SEQUENCE EX_BoundingPolygon_ID_seq ;
DROP SEQUENCE EX_GeographicDescription_ID_seq ;
DROP SEQUENCE EX_GeogrBBox_ID_seq ;
DROP SEQUENCE EX_TemporalExtent_ID_seq ;
DROP SEQUENCE EX_VerticalExtent_ID_seq ;
DROP SEQUENCE Facsimile_ID_seq ;
DROP SEQUENCE FAILEDREQUESTS_ID_seq ;
DROP SEQUENCE FeatureTypes_ID_seq ;
DROP SEQUENCE FileIdentifier_ID_seq ;
DROP SEQUENCE HierarchylevelCode_ID_seq ;
DROP SEQUENCE HierarchylevelName_ID_seq ;
DROP SEQUENCE Keyword_ID_seq ;
DROP SEQUENCE LI_ProcessStep_ID_seq ;
DROP SEQUENCE LI_Source_ID_seq ;
DROP SEQUENCE MD_ApplicationSchemaInformation_ID_seq ;
DROP SEQUENCE MD_BrowseGraphic_ID_seq ;
DROP SEQUENCE MD_CharacterSetCode_ID_seq ;
DROP SEQUENCE MD_ClassificationCode_ID_seq ;
DROP SEQUENCE MD_DataIdentification_ID_seq ;
DROP SEQUENCE MD_DigTransferOpt_ID_seq ;
DROP SEQUENCE MD_Distribution_ID_seq ;
DROP SEQUENCE MD_Distributor_ID_seq ;
DROP SEQUENCE MD_FeatCatDesc_ID_seq ;
DROP SEQUENCE MD_Format_ID_seq ;
DROP SEQUENCE MD_GeoObjTypeCode_ID_seq ;
DROP SEQUENCE MD_Identification_ID_seq ;
DROP SEQUENCE MD_Keywords_ID_seq ;
DROP SEQUENCE MD_KeywordTypeCode_ID_seq ;
DROP SEQUENCE MD_LegalConstraints_ID_seq ;
DROP SEQUENCE MD_MainFreqCode_ID_seq ;
DROP SEQUENCE MD_MaintenanceInformation_ID_seq ;
DROP SEQUENCE MD_MediumFormatCode_ID_seq ;
DROP SEQUENCE MD_MediumNameCode_ID_seq ;
DROP SEQUENCE MD_Metadata_ID_seq ;
DROP SEQUENCE MD_PortrayalCatRef_ID_seq ;
DROP SEQUENCE MD_ProgressCode_ID_seq ;
DROP SEQUENCE MD_Resolution_ID_seq ;
DROP SEQUENCE MD_RestrictionCode_ID_seq ;
DROP SEQUENCE MD_ScopeCode_ID_seq ;
DROP SEQUENCE MD_SecurityConstraints_ID_seq;
DROP SEQUENCE MD_SpatialRepTypeCode_ID_seq ;
DROP SEQUENCE MD_StandOrderProc_ID_seq ;
DROP SEQUENCE MD_TopicCategoryCode_ID_seq ;
DROP SEQUENCE MD_TopoLevelCode_ID_seq ;
DROP SEQUENCE MD_Usage_ID_seq ;
DROP SEQUENCE MD_VectorSpatialReprenstation_ID_seq ;
DROP SEQUENCE OperatesOn_ID_seq ;
DROP SEQUENCE OperationNames_ID_seq ;
DROP SEQUENCE OtherConstraints_ID_seq ;
DROP SEQUENCE RS_Identifier_ID_seq ;
DROP SEQUENCE ServiceVersion_ID_seq ;
DROP SEQUENCE SV_DCPList_ID_seq ;
DROP SEQUENCE SV_OperationMetadata_ID_seq ;
DROP SEQUENCE SV_Parameter_ID_seq ;
DROP SEQUENCE Voice_ID_seq ;

DROP TABLE AlternateTitle ;
DROP TABLE CI_Address ;
DROP TABLE CI_Citation ;
DROP TABLE CI_Contact ;
DROP TABLE CI_OnLineFunctionCode ;
DROP TABLE CI_OnlineResource ;
DROP TABLE CI_PresentationFormCode ;
DROP TABLE CI_RespParty ;
DROP TABLE CI_RoleCode ;
DROP TABLE CI_Series ;
DROP TABLE CSW_CouplingType ;
DROP TABLE CSW_ServiceIdentification ;
DROP TABLE DeliveryPoint ;
DROP TABLE DQ_DataQuality ;
DROP TABLE DQ_Element ;
DROP TABLE ElectronicMailAddress ;
DROP TABLE EX_BoundingPolygon ;
DROP TABLE EX_GeographicDescription ;
DROP TABLE EX_GeogrBBox ;
DROP TABLE EX_TemporalExtent ;
DROP TABLE EX_VerticalExtent ;
DROP TABLE Facsimile ;
DROP TABLE FAILEDREQUESTS ;
DROP TABLE FeatureTypes ;
DROP TABLE FileIdentifier ;
DROP TABLE HierarchylevelCode ;
DROP TABLE HierarchylevelName ;
DROP TABLE JT_Citation_RespParty ;
DROP TABLE JT_DataIdent_SpatialRepType ;
DROP TABLE JT_DataIdent_TopicCat ;
DROP TABLE JT_DigTransOpt_MediumFormat ;
DROP TABLE JT_DigTransOpt_MediumName ;
DROP TABLE JT_Dist_DistFormat ;
DROP TABLE JT_Dist_Distributor ;
DROP TABLE JT_Ident_Keywords ;
DROP TABLE JT_Ident_LegalConst ;
DROP TABLE JT_Ident_Mainten ;
DROP TABLE JT_Ident_RespParty ;
DROP TABLE JT_Ident_SecConst ;
DROP TABLE JT_Ident_Usage ;
DROP TABLE JT_Keywords_Keyword ;
DROP TABLE JT_LegalConst_accessConst ;
DROP TABLE JT_LegalConst_useConst ;
DROP TABLE JT_LI_SRC_LI_PROCSTEP ;
DROP TABLE JT_Metadata_AppSchemaInf ;
DROP TABLE JT_Metadata_FeatCatDesc ;
DROP TABLE JT_Metadata_LegalConst ;
DROP TABLE JT_Metadata_PortCatRef ;
DROP TABLE JT_Metadata_RefSys ;
DROP TABLE JT_Metadata_RespParty ;
DROP TABLE JT_Metadata_SecConst ;
DROP TABLE JT_Operation_DCP ;
DROP TABLE JT_Operation_Name ;
DROP TABLE JT_Operation_OperatesOn ;
DROP TABLE JT_OpMeta_OnlineRes ;
DROP TABLE JT_Quality_Procstep ;
DROP TABLE JT_SecConst_ClassificationCode ;
DROP TABLE Keyword ;
DROP TABLE LI_ProcessStep ;
DROP TABLE LI_Source ;
DROP TABLE MD_ApplicationSchemaInformation ;
DROP TABLE MD_BrowseGraphic ;
DROP TABLE MD_CharacterSetCode ;
DROP TABLE MD_ClassificationCode ;
DROP TABLE MD_Constraints ;
DROP TABLE MD_DataIdentification ;
DROP TABLE MD_DigTransferOpt ;
DROP TABLE MD_Distribution ;
DROP TABLE MD_Distributor ;
DROP TABLE MD_FeatCatDesc ;
DROP TABLE MD_Format ;
DROP TABLE MD_GeoObjTypeCode ;
DROP TABLE MD_Identification ;
DROP TABLE MD_Keywords ;
DROP TABLE MD_KeywordTypeCode ;
DROP TABLE MD_LegalConstraints ;
DROP TABLE MD_MainFreqCode ;
DROP TABLE MD_MaintenanceInformation ;
DROP TABLE MD_MediumFormatCode ;
DROP TABLE MD_MediumNameCode ;
DROP TABLE MD_Metadata ;
DROP TABLE MD_PortrayalCatRef ;
DROP TABLE MD_ProgressCode ;
DROP TABLE MD_Resolution ;
DROP TABLE MD_RestrictionCode ;
DROP TABLE MD_ScopeCode ;
DROP TABLE MD_SecurityConstraints ;
DROP TABLE MD_SpatialRepTypeCode ;
DROP TABLE MD_StandOrderProc ;
DROP TABLE MD_TopicCategoryCode ;
DROP TABLE MD_TopoLevelCode ;
DROP TABLE MD_Usage ;
DROP TABLE MD_VectorSpatialReprenstation ;
DROP TABLE OperatesOn ;
DROP TABLE OperationNames ;
DROP TABLE OtherConstraints ;
DROP TABLE RS_Identifier ;
DROP TABLE ServiceVersion ;
DROP TABLE SV_DCPList ;
DROP TABLE SV_OperationMetadata ;
DROP TABLE SV_Parameter ;
DROP TABLE Voice ;

CREATE TABLE AlternateTitle ( 
	FK_Citation integer,
	alternateTitle varchar(250),
	ID integer DEFAULT NEXTVAL('AlternateTitle_ID_seq'::TEXT)
) 
 ;

CREATE TABLE CI_Address ( 
	ID integer DEFAULT NEXTVAL('CI_Address_ID_seq'::TEXT),
	FK_DeliveryPoint integer,
	city varchar(200),
	administrativeArea varchar(200),
	postalCode varchar(50),
	country varchar(50),
	FK_Email integer
) 
 ;

CREATE TABLE CI_Citation ( 
	ID integer DEFAULT NEXTVAL('CI_Citation_ID_seq'::TEXT),
	FK_PresFromCode integer,
	title varchar(200),
	edition varchar(2000),
	editionDate timestamp,
	identifier varchar(250),
	ISBN varchar(200),
	ISSN varchar(200),
	revisionDate timestamp,
	creationDate timestamp,
	publicationDate timestamp,
	fk_fileidentifier integer,
	context varchar(50)
) 
 ;

CREATE TABLE CI_Contact ( 
	ID integer DEFAULT NEXTVAL('CI_Contact_ID_seq'::TEXT),
	FK_Address integer,
	FK_OnlineResource integer,
	hoursOfService varchar(500),
	contactInstructions text
) 
 ;

CREATE TABLE CI_OnLineFunctionCode ( 
	ID integer DEFAULT NEXTVAL('CI_OnLineFunctionCode_ID_seq'::TEXT),
	codeListValue varchar(500),
	codeSpace varchar(500)
) 
 ;

CREATE TABLE CI_OnlineResource ( 
	ID integer DEFAULT NEXTVAL('CI_OnlineResource_ID_seq'::TEXT),
	linkage varchar(500),
	FK_function integer
) 
 ;

CREATE TABLE CI_PresentationFormCode ( 
	ID integer DEFAULT NEXTVAL('CI_PresentationFormCode_ID_seq'::TEXT),
	codeListValue varchar(50),
	codeSpace integer
) 
 ;

CREATE TABLE CI_RespParty ( 
	ID integer DEFAULT NEXTVAL('CI_RespParty_ID_seq'::TEXT),
	individualName varchar(500),
	organisationName varchar(500),
	positionName varchar(200),
	FK_Role integer,
	FK_Contact integer
) 
 ;

CREATE TABLE CI_RoleCode ( 
	ID integer DEFAULT NEXTVAL('CI_RoleCode_ID_seq'::TEXT),
	codeListValue varchar(500),
	codeSpace varchar(500)
) 
 ;

CREATE TABLE CI_Series ( 
	ID integer DEFAULT NEXTVAL('CI_Series_ID_seq'::TEXT),
	FK_Citation integer,
	name varchar(150),
	issueIdentification varchar(500),
	page varchar(250)
) 
 ;

CREATE TABLE CSW_CouplingType ( 
	ID integer DEFAULT NEXTVAL('CSW_CouplingType_ID_seq'::TEXT),
	codeListValue varchar(50),
	codeSpace varchar(250)
) 
 ;

CREATE TABLE CSW_ServiceIdentification ( 
	ID integer DEFAULT NEXTVAL('CSW_ServiceIdentification_ID_seq'::TEXT),
	serviceType varchar(50),
	fees varchar(250),
	plannedAvailDateTime timestamp,
	orderingInstructions varchar(2000),
	turnaround varchar(500),
	FK_LegalConst integer,
	FK_CouplingType integer,
	FK_SecConst integer
) 
 ;

CREATE TABLE DeliveryPoint ( 
	ID integer DEFAULT NEXTVAL('DeliveryPoint_ID_seq'::TEXT),
	deliveryPoint varchar(500)
) 
 ;

CREATE TABLE DQ_DataQuality ( 
	ID integer DEFAULT NEXTVAL('DQ_DataQuality_ID_seq'::TEXT),
	ScopeLevelCodeListValue varchar(150),
	ScopeLeveDescription text,
	lineageStatement text,
	FK_Metadata integer
) 
 ;

CREATE TABLE DQ_Element ( 
	ID integer DEFAULT NEXTVAL('DQ_Element_ID_seq'::TEXT),
	nameOfMeasure varchar(50),
	uomName1 varchar(50),
	convToISOStdUnit1 real,
	value1 real,
	uomName2 varchar(50),
	convToISOStdUnit2 real,
	value2 real,
	type varchar(50),
	FK_DataQuality integer
) 
 ;

CREATE TABLE ElectronicMailAddress ( 
	ID integer DEFAULT NEXTVAL('ElectronicMailAddress_ID_seq'::TEXT),
	email varchar(200)
) 
 ;

CREATE TABLE EX_BoundingPolygon ( 
	FK_DataIdent integer,
	description text,
	ID integer DEFAULT NEXTVAL('EX_BoundingPolygon_ID_seq'::TEXT),
	crs varchar(150)
) 
 ;

CREATE TABLE EX_GeographicDescription ( 
	ID integer DEFAULT NEXTVAL('EX_GeographicDescription_ID_seq'::TEXT),
	geographicIdentifierCode varchar(150),
	FK_DataIdent integer
) 
 ;

CREATE TABLE EX_GeogrBBox ( 
	ID integer DEFAULT NEXTVAL('EX_GeogrBBox_ID_seq'::TEXT),
	FK_Owner integer,
	description text,
	crs varchar(150)
) 
 ;

CREATE TABLE EX_TemporalExtent ( 
	FK_DataIdent integer,
	description text,
	begin_ timestamp,
	end_ timestamp,
	tmePosition timestamp,
	ID integer DEFAULT NEXTVAL('EX_TemporalExtent_ID_seq'::TEXT)
) 
 ;

CREATE TABLE EX_VerticalExtent ( 
	ID integer DEFAULT NEXTVAL('EX_VerticalExtent_ID_seq'::TEXT),
	minVal real,
	maxVal real,
	uomName varchar(150),
	convToISOStdUnit real,
	description text,
	FK_DataIdent integer,
	FK_VerticalDatum integer
) 
 ;

CREATE TABLE Facsimile ( 
	FK_Contact integer,
	number varchar(500),
	ID integer DEFAULT NEXTVAL('Facsimile_ID_seq'::TEXT)
) 
 ;

CREATE TABLE FAILEDREQUESTS ( 
	ID integer DEFAULT NEXTVAL('FAILEDREQUESTS_ID_seq'::TEXT),
	REQUEST text,
	CSWADDRESS varchar(500),
	REPEAT integer
) 
 ;

CREATE TABLE FeatureTypes ( 
	localName varchar(100),
	FK_FeatCatDesc integer,
	ID integer DEFAULT NEXTVAL('FeatureTypes_ID_seq'::TEXT)
) 
 ;

CREATE TABLE FileIdentifier ( 
	ID integer DEFAULT NEXTVAL('FileIdentifier_ID_seq'::TEXT),
	fileidentifier varchar(250)
) 
 ;

CREATE TABLE HierarchylevelCode ( 
	ID integer DEFAULT NEXTVAL('HierarchylevelCode_ID_seq'::TEXT),
	codeListValue varchar(150),
	codeSpace varchar(500)
) 
 ;

CREATE TABLE HierarchylevelName ( 
	ID integer DEFAULT NEXTVAL('HierarchylevelName_ID_seq'::TEXT),
	Name varchar(100)
) 
 ;

CREATE TABLE JT_Citation_RespParty ( 
	FK_Citation integer,
	FK_RespParty integer
) 
 ;

CREATE TABLE JT_DataIdent_SpatialRepType ( 
	FK_DataIdent integer,
	FK_SpatialRepType integer
) 
 ;

CREATE TABLE JT_DataIdent_TopicCat ( 
	FK_DataIdent integer,
	FK_TopicCategory integer
) 
 ;

CREATE TABLE JT_DigTransOpt_MediumFormat ( 
	fk_digtransopt integer,
	fk_mediumformat integer
) 
 ;

CREATE TABLE JT_DigTransOpt_MediumName ( 
	fk_digtransopt integer,
	fk_mediumname integer
) 
 ;

CREATE TABLE JT_Dist_DistFormat ( 
	fk_distribution integer,
	FK_Format integer
) 
 ;

CREATE TABLE JT_Dist_Distributor ( 
	fk_distribution integer,
	fk_distributor integer
) 
 ;

CREATE TABLE JT_Ident_Keywords ( 
	FK_Ident integer,
	FK_Keywords integer
) 
 ;

CREATE TABLE JT_Ident_LegalConst ( 
	FK_Identification integer,
	FK_LegalConst integer
) 
 ;

CREATE TABLE JT_Ident_Mainten ( 
	FK_Identification integer,
	FK_Maintenance integer
) 
 ;

CREATE TABLE JT_Ident_RespParty ( 
	FK_Identification integer,
	FK_ResponsibleParty integer
) 
 ;

CREATE TABLE JT_Ident_SecConst ( 
	FK_SecConstraint integer,
	FK_Identification integer
) 
 ;

CREATE TABLE JT_Ident_Usage ( 
	FK_Ident integer,
	FK_Usage integer
) 
 ;

CREATE TABLE JT_Keywords_Keyword ( 
	FK_Keywords integer,
	FK_Keyword integer
) 
 ;

CREATE TABLE JT_LegalConst_accessConst ( 
	FK_LegalConst integer,
	FK_RestrictCode integer
) 
 ;

CREATE TABLE JT_LegalConst_useConst ( 
	FK_LegalConst integer,
	FK_RestrictCode integer
) 
 ;

CREATE TABLE JT_LI_SRC_LI_PROCSTEP ( 
	fk_source integer,
	fk_procstep integer
) 
 ;

CREATE TABLE JT_Metadata_AppSchemaInf ( 
	fk_metadata integer,
	fk_AppSchemaInf integer
) 
 ;

CREATE TABLE JT_Metadata_FeatCatDesc ( 
	FK_Metadata integer,
	FK_FeatCatDesc integer
) 
 ;

CREATE TABLE JT_Metadata_LegalConst ( 
	FK_Metadata integer,
	FK_LegalConstraint integer
) 
 ;

CREATE TABLE JT_Metadata_PortCatRef ( 
	FK_Metadata integer,
	FK_PortCatRef integer
) 
 ;

CREATE TABLE JT_Metadata_RefSys ( 
	FK_Metadata integer,
	FK_RefSys integer
) 
 ;

CREATE TABLE JT_Metadata_RespParty ( 
	FK_Metadata integer,
	FK_RespParty integer
) 
 ;

CREATE TABLE JT_Metadata_SecConst ( 
	FK_Metadata integer,
	FK_SecConstraints integer
) 
 ;

CREATE TABLE JT_Operation_DCP ( 
	FK_Operation integer,
	FK_DCP integer
) 
 ;

CREATE TABLE JT_Operation_Name ( 
	fk_operation integer,
	fk_name integer
) 
 ;

CREATE TABLE JT_Operation_OperatesOn ( 
	fk_operation integer,
	fk_operateson integer
) 
 ;

CREATE TABLE JT_OpMeta_OnlineRes ( 
	FK_Operation integer,
	FK_OnlineResource integer
) 
 ;

CREATE TABLE JT_Quality_Procstep ( 
	fk_quality integer,
	fk_procstep integer
) 
 ;

CREATE TABLE JT_SecConst_ClassificationCode ( 
	FK_SecConst integer,
	FK_ClassificationCode integer
) 
 ;

CREATE TABLE Keyword ( 
	ID integer DEFAULT NEXTVAL('Keyword_ID_seq'::TEXT),
	keyword varchar(200)
) 
 ;

CREATE TABLE LI_ProcessStep ( 
	ID integer DEFAULT NEXTVAL('LI_ProcessStep_ID_seq'::TEXT),
	description varchar(2000),
	rationale varchar(500),
	dateTime timestamp,
	fk_processor integer
) 
 ;

CREATE TABLE LI_Source ( 
	ID integer DEFAULT NEXTVAL('LI_Source_ID_seq'::TEXT),
	fk_dataquality integer,
	description varchar(2000),
	scaleDenominator integer,
	fk_sourceReferenceSystem integer,
	fk_sourceCitation integer
) 
 ;

CREATE TABLE MD_ApplicationSchemaInformation ( 
	ID integer DEFAULT NEXTVAL('MD_ApplicationSchemaInformation_ID_seq'::TEXT),
	fk_citation integer,
	schemaLanguage varchar(50),
	constraintLanguage varchar(50),
	schemaAscii varchar(50),
	graphicsFile64b text,
	graphicsFileHex text,
	softwareDevelFile64b text,
	softwareDevelFileHex text,
	softwareDevelFileFormat varchar(150)
) 
 ;

CREATE TABLE MD_BrowseGraphic ( 
	FK_Ident integer,
	fileName varchar(500),
	fileDescription text,
	fileType varchar(12),
	ID integer DEFAULT NEXTVAL('MD_BrowseGraphic_ID_seq'::TEXT)
) 
 ;

CREATE TABLE MD_CharacterSetCode ( 
	ID integer DEFAULT NEXTVAL('MD_CharacterSetCode_ID_seq'::TEXT),
	codeListValue varchar(50),
	codeSpace varchar(500)
) 
 ;

CREATE TABLE MD_ClassificationCode ( 
	ID integer DEFAULT NEXTVAL('MD_ClassificationCode_ID_seq'::TEXT),
	codeListValue varchar(50),
	codeSpace varchar(500)
) 
 ;

CREATE TABLE MD_Constraints ( 
	ID integer,
	useLimitation text
) 
 ;

CREATE TABLE MD_DataIdentification ( 
	ID integer DEFAULT NEXTVAL('MD_DataIdentification_ID_seq'::TEXT),
	language varchar(150),
	supplementalInformation varchar(500),
	FK_Characterset integer
) 
 ;

CREATE TABLE MD_DigTransferOpt ( 
	ID integer DEFAULT NEXTVAL('MD_DigTransferOpt_ID_seq'::TEXT),
	fk_distribution integer,
	unitsOfDistribution varchar(150),
	fk_OnlineResource integer,
	transferSize real,
	off_mediumnote text
) 
 ;

CREATE TABLE MD_Distribution ( 
	ID integer DEFAULT NEXTVAL('MD_Distribution_ID_seq'::TEXT),
	fk_metadata integer
) 
 ;

CREATE TABLE MD_Distributor ( 
	ID integer DEFAULT NEXTVAL('MD_Distributor_ID_seq'::TEXT),
	FK_ResponsibleParty integer
) 
 ;

CREATE TABLE MD_FeatCatDesc ( 
	ID integer DEFAULT NEXTVAL('MD_FeatCatDesc_ID_seq'::TEXT),
	FK_Citation integer,
	language varchar(200),
	includedWithDataset boolean
) 
 ;

CREATE TABLE MD_Format ( 
	ID integer DEFAULT NEXTVAL('MD_Format_ID_seq'::TEXT),
	name varchar(200),
	version varchar(50),
	specification text,
	fileDecompTech varchar(500),
	amendmentNumber varchar(150)
) 
 ;

CREATE TABLE MD_GeoObjTypeCode ( 
	ID integer DEFAULT NEXTVAL('MD_GeoObjTypeCode_ID_seq'::TEXT),
	codeListValue varchar(50),
	codeSpace varchar(500)
) 
 ;

CREATE TABLE MD_Identification ( 
	ID integer DEFAULT NEXTVAL('MD_Identification_ID_seq'::TEXT),
	FK_Progress integer,
	FK_Citation integer,
	abstract text,
	purpose varchar(1000)
) 
 ;

CREATE TABLE MD_Keywords ( 
	ID integer DEFAULT NEXTVAL('MD_Keywords_ID_seq'::TEXT),
	FK_Thesaurus integer,
	FK_Type integer
) 
 ;

CREATE TABLE MD_KeywordTypeCode ( 
	ID integer DEFAULT NEXTVAL('MD_KeywordTypeCode_ID_seq'::TEXT),
	codeListValue varchar(100),
	codeSpace varchar(250)
) 
 ;

CREATE TABLE MD_LegalConstraints ( 
	ID integer DEFAULT NEXTVAL('MD_LegalConstraints_ID_seq'::TEXT),
	defined boolean,
	useLimitations text
) 
 ;

CREATE TABLE MD_MainFreqCode ( 
	ID integer DEFAULT NEXTVAL('MD_MainFreqCode_ID_seq'::TEXT),
	codeListValue varchar(500),
	codeSpace varchar(500)
) 
 ;

CREATE TABLE MD_MaintenanceInformation ( 
	ID integer DEFAULT NEXTVAL('MD_MaintenanceInformation_ID_seq'::TEXT),
	FK_MainFreq integer,
	FK_Scope integer,
	dateOfNextUpdate timestamp,
	userDefFrequency varchar(150),
	note text
) 
 ;

CREATE TABLE MD_MediumFormatCode ( 
	ID integer DEFAULT NEXTVAL('MD_MediumFormatCode_ID_seq'::TEXT),
	codeListValue varchar(50),
	codeSpace varchar(500)
) 
 ;

CREATE TABLE MD_MediumNameCode ( 
	ID integer DEFAULT NEXTVAL('MD_MediumNameCode_ID_seq'::TEXT),
	codeListValue varchar(50),
	codeSpace varchar(500)
) 
 ;

CREATE TABLE MD_Metadata ( 
	ID integer DEFAULT NEXTVAL('MD_Metadata_ID_seq'::TEXT),
	FK_fileIdentifier integer,
	language varchar(100),
	FK_Characterset integer,
	parentIdentifier varchar(150),
	dateStamp timestamp,
	mdStandName varchar(50),
	mdStandVersion varchar(50),
	FK_HLevelCode integer,
	FK_HLevelName integer,
	testdata varchar(255)
) 
 ;

CREATE TABLE MD_PortrayalCatRef ( 
	FK_Citation integer,
	ID integer DEFAULT NEXTVAL('MD_PortrayalCatRef_ID_seq'::TEXT)
) 
 ;

CREATE TABLE MD_ProgressCode ( 
	ID integer DEFAULT NEXTVAL('MD_ProgressCode_ID_seq'::TEXT),
	codeListValue varchar(500),
	codeSpace varchar(500)
) 
 ;

CREATE TABLE MD_Resolution ( 
	ID integer DEFAULT NEXTVAL('MD_Resolution_ID_seq'::TEXT),
	FK_DataIdent integer,
	equivalentScale integer,
	distanceValue decimal(10,3),
	uomName varchar(50),
	convToISOStdUnit real
) 
 ;

CREATE TABLE MD_RestrictionCode ( 
	ID integer DEFAULT NEXTVAL('MD_RestrictionCode_ID_seq'::TEXT),
	codeListValue varchar(50),
	codeSpace varchar(500)
) 
 ;

CREATE TABLE MD_ScopeCode ( 
	ID integer DEFAULT NEXTVAL('MD_ScopeCode_ID_seq'::TEXT),
	codeListValue varchar(50),
	codeSpace varchar(500)
) 
 ;

CREATE TABLE MD_SecurityConstraints ( 
	ID integer,
	userNote varchar(2000),
	classificationSystem varchar(200),
	handlingDescription varchar(200),
	useLimitations text
) 
 ;

CREATE TABLE MD_SpatialRepTypeCode ( 
	ID integer DEFAULT NEXTVAL('MD_SpatialRepTypeCode_ID_seq'::TEXT),
	codeListValue varchar(50),
	codeSpace varchar(500)
) 
 ;

CREATE TABLE MD_StandOrderProc ( 
	ID integer DEFAULT NEXTVAL('MD_StandOrderProc_ID_seq'::TEXT),
	fees varchar(2000),
	orderingInstructions text,
	turnaround varchar(500),
	FK_Distributor integer
) 
 ;

CREATE TABLE MD_TopicCategoryCode ( 
	ID integer DEFAULT NEXTVAL('MD_TopicCategoryCode_ID_seq'::TEXT),
	category varchar(500)
) 
 ;

CREATE TABLE MD_TopoLevelCode ( 
	ID integer DEFAULT NEXTVAL('MD_TopoLevelCode_ID_seq'::TEXT),
	codeListValue varchar(50),
	codeSpace varchar(500)
) 
 ;

CREATE TABLE MD_Usage ( 
	ID integer DEFAULT NEXTVAL('MD_Usage_ID_seq'::TEXT),
	specificUsage varchar(500),
	fk_usercontactinfo integer
) 
 ;

CREATE TABLE MD_VectorSpatialReprenstation ( 
	ID integer DEFAULT NEXTVAL('MD_VectorSpatialReprenstation_ID_seq'::TEXT),
	FK_TopoLevelCode integer,
	FK_GeoObjTypeCode integer,
	FK_Metadata integer,
	geoObjCount integer
) 
 ;

CREATE TABLE OperatesOn ( 
	ID integer DEFAULT NEXTVAL('OperatesOn_ID_seq'::TEXT),
	fk_dataidentification integer,
	fk_serviceidentification integer,
	name varchar(250),
	title varchar(250),
	abstract text
) 
 ;

CREATE TABLE OperationNames ( 
	ID integer DEFAULT NEXTVAL('OperationNames_ID_seq'::TEXT),
	name varchar(50),
	namespace varchar(250)
) 
 ;

CREATE TABLE OtherConstraints ( 
	FK_LegalConstraints integer,
	constraint_ text,
	ID integer DEFAULT NEXTVAL('OtherConstraints_ID_seq'::TEXT)
) 
 ;

CREATE TABLE RS_Identifier ( 
	ID integer DEFAULT NEXTVAL('RS_Identifier_ID_seq'::TEXT),
	code varchar(150),
	codeSpace varchar(250),
	version varchar(50),
	FK_Authority integer
) 
 ;

CREATE TABLE ServiceVersion ( 
	FK_ServiceIdent integer,
	version varchar(10),
	ID integer DEFAULT NEXTVAL('ServiceVersion_ID_seq'::TEXT)
) 
 ;

CREATE TABLE SV_DCPList ( 
	ID integer DEFAULT NEXTVAL('SV_DCPList_ID_seq'::TEXT),
	codeListValue varchar(50),
	codeSpace varchar(150)
) 
 ;

CREATE TABLE SV_OperationMetadata ( 
	ID integer DEFAULT NEXTVAL('SV_OperationMetadata_ID_seq'::TEXT),
	operationDescription text,
	invocationName varchar(150),
	FK_ServiceIdent integer
) 
 ;

CREATE TABLE SV_Parameter ( 
	FK_Operation integer,
	name varchar(50),
	type varchar(150),
	direction varchar(6),
	description text,
	optionality varchar(20),
	repeatability boolean,
	ID integer DEFAULT NEXTVAL('SV_Parameter_ID_seq'::TEXT)
) 
 ;

CREATE TABLE Voice ( 
	FK_Contact integer,
	number varchar(500),
	ID integer DEFAULT NEXTVAL('Voice_ID_seq'::TEXT)
) 
 ;
 
SELECT AddGeometryColumn('', 'ex_boundingpolygon','geom',-1,'POLYGON',2);
SELECT AddGeometryColumn('', 'ex_geogrbbox','geom',-1,'POLYGON',2);


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


ALTER TABLE FAILEDREQUESTS ADD CONSTRAINT PK_FAILEDREQUESTS 
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


ALTER TABLE MD_ApplicationSchemaInformation ADD CONSTRAINT PK_MD_ApplicationSchemaInformation 
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


ALTER TABLE MD_VectorSpatialReprenstation ADD CONSTRAINT PK_MD_VectorSpatialReprenstation 
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


ALTER TABLE Voice ADD CONSTRAINT PK_Voice 	PRIMARY KEY (ID) ;

ALTER TABLE AlternateTitle ADD CONSTRAINT UQ_AlternateTitle_ID UNIQUE (ID) ;
ALTER TABLE CI_Address ADD CONSTRAINT UQ_CI_Address_ID UNIQUE (ID) ;
ALTER TABLE CI_Citation ADD CONSTRAINT UQ_CI_Citation_fk_fileidentifier UNIQUE (fk_fileidentifier) ;
ALTER TABLE CI_Citation ADD CONSTRAINT UQ_CI_Citation_ID UNIQUE (ID) ;
ALTER TABLE CI_Contact ADD CONSTRAINT UQ_CI_Contact_ID UNIQUE (ID) ;
ALTER TABLE CI_OnLineFunctionCode ADD CONSTRAINT UQ_CI_OnLineFunctionCode_codeListValue UNIQUE (codeListValue) ;
ALTER TABLE CI_OnLineFunctionCode ADD CONSTRAINT UQ_CI_OnLineFunctionCode_ID UNIQUE (ID) ;
ALTER TABLE CI_OnlineResource ADD CONSTRAINT UQ_CI_OnlineResource_ID UNIQUE (ID) ;
ALTER TABLE CI_PresentationFormCode ADD CONSTRAINT UQ_CI_PresentationFormCode_codeListValue UNIQUE (codeListValue) ;
ALTER TABLE CI_PresentationFormCode ADD CONSTRAINT UQ_CI_PresentationFormCode_ID UNIQUE (ID) ;
ALTER TABLE CI_RespParty ADD CONSTRAINT UQ_CI_RespParty_ID UNIQUE (ID) ;
ALTER TABLE CI_RoleCode ADD CONSTRAINT UQ_CI_RoleCode_codeListValue UNIQUE (codeListValue) ;
ALTER TABLE CI_RoleCode ADD CONSTRAINT UQ_CI_RoleCode_ID UNIQUE (ID) ;
ALTER TABLE CI_Series ADD CONSTRAINT UQ_CI_Series_ID UNIQUE (ID) ;
ALTER TABLE CSW_CouplingType ADD CONSTRAINT UQ_CSW_CouplingType_codeListValue UNIQUE (codeListValue) ;
ALTER TABLE CSW_CouplingType ADD CONSTRAINT UQ_CSW_CouplingType_ID UNIQUE (ID) ;
ALTER TABLE CSW_ServiceIdentification ADD CONSTRAINT UQ_ServiceIdentification_ID UNIQUE (ID) ;
ALTER TABLE DeliveryPoint ADD CONSTRAINT UQ_DeliveryPoint_ID UNIQUE (ID) ;
ALTER TABLE DQ_Element ADD CONSTRAINT UQ_DQ_Element_ID UNIQUE (ID) ;
ALTER TABLE ElectronicMailAddress ADD CONSTRAINT UQ_ElectronicMailAddress_ID UNIQUE (ID) ;
ALTER TABLE EX_BoundingPolygon ADD CONSTRAINT UQ_EX_BoundingPolygon_ID UNIQUE (ID) ;
ALTER TABLE EX_GeographicDescription ADD CONSTRAINT UQ_EX_GeographicDescription_ID UNIQUE (ID) ;
ALTER TABLE EX_GeogrBBox ADD CONSTRAINT UQ_EX_GeogrBBox_ID UNIQUE (ID) ;
ALTER TABLE EX_TemporalExtent ADD CONSTRAINT UQ_EX_TemporalExtent_ID UNIQUE (ID) ;
ALTER TABLE EX_VerticalExtent ADD CONSTRAINT UQ_EX_VerticalExtent_ID UNIQUE (ID) ;
ALTER TABLE Facsimile ADD CONSTRAINT UQ_Facsimile_ID UNIQUE (ID) ;
ALTER TABLE FeatureTypes ADD CONSTRAINT UQ_FeatureTypes_ID UNIQUE (ID) ;
ALTER TABLE FileIdentifier ADD CONSTRAINT UQ_FileIdentifier_fileidentifier UNIQUE (fileidentifier) ;
ALTER TABLE FileIdentifier ADD CONSTRAINT UQ_FileIdentifier_ID UNIQUE (ID) ;
ALTER TABLE HierarchylevelCode ADD CONSTRAINT UQ_HierarchylevelCode_codeListValue UNIQUE (codeListValue) ;
ALTER TABLE HierarchylevelCode ADD CONSTRAINT UQ_HierarchylevelCode_ID UNIQUE (ID) ;
ALTER TABLE HierarchylevelName ADD CONSTRAINT UQ_HierachylevelName_ID UNIQUE (ID) ;
ALTER TABLE HierarchylevelName ADD CONSTRAINT UQ_HierachylevelName_Name UNIQUE (Name) ;
ALTER TABLE Keyword ADD CONSTRAINT UQ_Keyword_ID UNIQUE (ID) ;
ALTER TABLE MD_BrowseGraphic ADD CONSTRAINT UQ_MD_BrowseGraphic_ID UNIQUE (ID) ;
ALTER TABLE MD_CharacterSetCode ADD CONSTRAINT UQ_MD_CharacterSetCode_codeListValue UNIQUE (codeListValue) ;
ALTER TABLE MD_CharacterSetCode ADD CONSTRAINT UQ_MD_CharacterSetCode_ID UNIQUE (ID) ;
ALTER TABLE MD_ClassificationCode ADD CONSTRAINT UQ_MD_ClassificationCode_codeListValue UNIQUE (codeListValue) ;
ALTER TABLE MD_ClassificationCode ADD CONSTRAINT UQ_MD_ClassificationCode_ID UNIQUE (ID) ;
ALTER TABLE MD_Constraints ADD CONSTRAINT UQ_MD_Constraints_ID UNIQUE (ID) ;
ALTER TABLE MD_DataIdentification ADD CONSTRAINT UQ_MD_DataIdentification_ID UNIQUE (ID) ;
ALTER TABLE MD_DigTransferOpt ADD CONSTRAINT UQ_MD_DigTransferOpt_ID UNIQUE (ID) ;
ALTER TABLE MD_Distributor ADD CONSTRAINT UQ_MD_Distributor_ID UNIQUE (ID) ;
ALTER TABLE MD_FeatCatDesc ADD CONSTRAINT UQ_MD_FeatCatDesc_ID UNIQUE (ID) ;
ALTER TABLE MD_Format ADD CONSTRAINT UQ_MD_Format_ID UNIQUE (ID) ;
ALTER TABLE MD_GeoObjTypeCode ADD CONSTRAINT UQ_MD_GeoObjTypeCode_codeListValue UNIQUE (codeListValue) ;
ALTER TABLE MD_GeoObjTypeCode ADD CONSTRAINT UQ_MD_GeoObjTypeCode_ID UNIQUE (ID) ;
ALTER TABLE MD_Identification ADD CONSTRAINT UQ_MD_Identification_ID UNIQUE (ID) ;
ALTER TABLE MD_Keywords ADD CONSTRAINT UQ_MD_Keywords_ID UNIQUE (ID) ;
ALTER TABLE MD_KeywordTypeCode ADD CONSTRAINT UQ_MD_KeywordTypeCode_ID UNIQUE (ID) ;
ALTER TABLE MD_LegalConstraints ADD CONSTRAINT UQ_MD_LegalConstraints_ID UNIQUE (ID) ;
ALTER TABLE MD_MainFreqCode ADD CONSTRAINT UQ_MD_MainFreqCode_codeListValue UNIQUE (codeListValue) ;
ALTER TABLE MD_MainFreqCode ADD CONSTRAINT UQ_MD_MainFreqCode_ID UNIQUE (ID) ;
ALTER TABLE MD_MaintenanceInformation ADD CONSTRAINT UQ_MD_MaintenanceInformation_ID UNIQUE (ID) ;
ALTER TABLE MD_MediumFormatCode ADD CONSTRAINT UQ_MD_MediumFormatCode_codeListValue UNIQUE (codeListValue) ;
ALTER TABLE MD_MediumFormatCode ADD CONSTRAINT UQ_MD_MediumFormatCode_codeValue UNIQUE (codeListValue) ;
ALTER TABLE MD_MediumFormatCode ADD CONSTRAINT UQ_MD_MediumFormatCode_ID UNIQUE (ID) ;
ALTER TABLE MD_MediumNameCode ADD CONSTRAINT UQ_MD_MediumNameCode_codeListValue UNIQUE (codeListValue) ;
ALTER TABLE MD_MediumNameCode ADD CONSTRAINT UQ_MD_MediumNameCode_ID UNIQUE (ID) ;
ALTER TABLE MD_Metadata ADD CONSTRAINT UQ_MD_Metadata_FK_fileIdentifier UNIQUE (FK_fileIdentifier) ;
ALTER TABLE MD_Metadata ADD CONSTRAINT UQ_MD_Metadata_ID UNIQUE (ID) ;
ALTER TABLE MD_PortrayalCatRef ADD CONSTRAINT UQ_MD_PortrayalCatRef_ID UNIQUE (ID) ;
ALTER TABLE MD_ProgressCode ADD CONSTRAINT UQ_MD_ProgressCode_codeListValue UNIQUE (codeListValue) ;
ALTER TABLE MD_ProgressCode ADD CONSTRAINT UQ_MD_ProgressCode_ID UNIQUE (ID) ;
ALTER TABLE MD_Resolution ADD CONSTRAINT UQ_MD_Resolution_ID UNIQUE (ID) ;
ALTER TABLE MD_RestrictionCode ADD CONSTRAINT UQ_MD_RestrictionCode_codeListValue UNIQUE (codeListValue) ;
ALTER TABLE MD_RestrictionCode ADD CONSTRAINT UQ_MD_RestrictionCode_ID UNIQUE (ID) ;
ALTER TABLE MD_ScopeCode ADD CONSTRAINT UQ_MD_ScopeCode_codeListValue UNIQUE (codeListValue) ;
ALTER TABLE MD_ScopeCode ADD CONSTRAINT UQ_MD_ScopeCode_ID UNIQUE (ID) ;
ALTER TABLE MD_SecurityConstraints ADD CONSTRAINT UQ_MD_SecurityConstraints_ID UNIQUE (ID) ;
ALTER TABLE MD_SpatialRepTypeCode ADD CONSTRAINT UQ_MD_SpatialRepTypeCode_codeListValue UNIQUE (codeListValue) ;
ALTER TABLE MD_SpatialRepTypeCode ADD CONSTRAINT UQ_MD_SpatialRepTypeCode_ID UNIQUE (ID) ;
ALTER TABLE MD_StandOrderProc ADD CONSTRAINT UQ_MD_StandOrderProc_ID UNIQUE (ID) ;
ALTER TABLE MD_TopicCategoryCode ADD CONSTRAINT UQ_MD_TopicCategoryCode_ID UNIQUE (ID) ;
ALTER TABLE MD_TopicCategoryCode ADD CONSTRAINT UQ_MD_TopicCategoryCode_category UNIQUE (category) ;
ALTER TABLE MD_TopoLevelCode ADD CONSTRAINT UQ_MD_TopoLevelCode_codeListValue UNIQUE (codeListValue) ;
ALTER TABLE MD_TopoLevelCode ADD CONSTRAINT UQ_MD_TopoLevelCode_ID UNIQUE (ID) ;
ALTER TABLE MD_Usage ADD CONSTRAINT UQ_MD_Usage_ID UNIQUE (ID) ;
ALTER TABLE MD_VectorSpatialReprenstation ADD CONSTRAINT UQ_MD_VectorSpatialReprenstation_ID UNIQUE (ID) ;
ALTER TABLE OperatesOn ADD CONSTRAINT UQ_OperatesOn_ID UNIQUE (ID) ;
ALTER TABLE OtherConstraints ADD CONSTRAINT UQ_OtherConstraints_ID UNIQUE (ID) ;
ALTER TABLE ServiceVersion ADD CONSTRAINT UQ_ServiceVersion_ID UNIQUE (ID) ;
ALTER TABLE SV_DCPList ADD CONSTRAINT UQ_SV_DCPList_codeListValue UNIQUE (codeListValue) ;
ALTER TABLE SV_DCPList ADD CONSTRAINT UQ_SV_DCPList_ID UNIQUE (ID) ;
ALTER TABLE SV_OperationMetadata ADD CONSTRAINT UQ_SV_OperationMetadata_ID UNIQUE (ID) ;
ALTER TABLE SV_Parameter ADD CONSTRAINT UQ_SV_Parameter_ID UNIQUE (ID) ;
ALTER TABLE Voice ADD CONSTRAINT UQ_Voice_ID UNIQUE (ID) ;

CREATE SEQUENCE AlternateTitle_ID_seq ;

CREATE SEQUENCE CI_Address_ID_seq ;

CREATE SEQUENCE CI_Citation_ID_seq ;

CREATE SEQUENCE CI_Contact_ID_seq ;

CREATE SEQUENCE CI_OnLineFunctionCode_ID_seq ;

CREATE SEQUENCE CI_OnlineResource_ID_seq ;

CREATE SEQUENCE CI_PresentationFormCode_ID_seq ;

CREATE SEQUENCE CI_RespParty_ID_seq ;

CREATE SEQUENCE CI_RoleCode_ID_seq ;

CREATE SEQUENCE CI_Series_ID_seq ;

CREATE SEQUENCE CSW_CouplingType_ID_seq ;

CREATE SEQUENCE CSW_ServiceIdentification_ID_seq ;

CREATE SEQUENCE DeliveryPoint_ID_seq ;

CREATE SEQUENCE DQ_DataQuality_ID_seq ;

CREATE SEQUENCE DQ_Element_ID_seq ;

CREATE SEQUENCE ElectronicMailAddress_ID_seq ;

CREATE SEQUENCE EX_BoundingPolygon_ID_seq ;

CREATE SEQUENCE EX_GeographicDescription_ID_seq ;

CREATE SEQUENCE EX_GeogrBBox_ID_seq ;

CREATE SEQUENCE EX_TemporalExtent_ID_seq ;

CREATE SEQUENCE EX_VerticalExtent_ID_seq ;

CREATE SEQUENCE Facsimile_ID_seq ;

CREATE SEQUENCE FAILEDREQUESTS_ID_seq ;

CREATE SEQUENCE FeatureTypes_ID_seq ;

CREATE SEQUENCE FileIdentifier_ID_seq ;

CREATE SEQUENCE HierarchylevelCode_ID_seq ;

CREATE SEQUENCE HierarchylevelName_ID_seq ;

CREATE SEQUENCE Keyword_ID_seq ;

CREATE SEQUENCE LI_ProcessStep_ID_seq ;

CREATE SEQUENCE LI_Source_ID_seq ;

CREATE SEQUENCE MD_ApplicationSchemaInformation_ID_seq ;

CREATE SEQUENCE MD_BrowseGraphic_ID_seq ;

CREATE SEQUENCE MD_CharacterSetCode_ID_seq ;

CREATE SEQUENCE MD_ClassificationCode_ID_seq ;

CREATE SEQUENCE MD_DataIdentification_ID_seq ;

CREATE SEQUENCE MD_DigTransferOpt_ID_seq ;

CREATE SEQUENCE MD_Distribution_ID_seq ;

CREATE SEQUENCE MD_Distributor_ID_seq ;

CREATE SEQUENCE MD_FeatCatDesc_ID_seq ;

CREATE SEQUENCE MD_Format_ID_seq ;

CREATE SEQUENCE MD_GeoObjTypeCode_ID_seq ;

CREATE SEQUENCE MD_Identification_ID_seq ;

CREATE SEQUENCE MD_Keywords_ID_seq ;

CREATE SEQUENCE MD_KeywordTypeCode_ID_seq ;

CREATE SEQUENCE MD_LegalConstraints_ID_seq ;

CREATE SEQUENCE MD_MainFreqCode_ID_seq ;

CREATE SEQUENCE MD_MaintenanceInformation_ID_seq ;

CREATE SEQUENCE MD_MediumFormatCode_ID_seq ;

CREATE SEQUENCE MD_MediumNameCode_ID_seq ;

CREATE SEQUENCE MD_Metadata_ID_seq ;

CREATE SEQUENCE MD_PortrayalCatRef_ID_seq ;

CREATE SEQUENCE MD_ProgressCode_ID_seq ;

CREATE SEQUENCE MD_Resolution_ID_seq ;

CREATE SEQUENCE MD_RestrictionCode_ID_seq ;

CREATE SEQUENCE MD_ScopeCode_ID_seq ;

CREATE SEQUENCE MD_SecurityConstraints_ID_seq;

CREATE SEQUENCE MD_SpatialRepTypeCode_ID_seq ;

CREATE SEQUENCE MD_StandOrderProc_ID_seq ;

CREATE SEQUENCE MD_TopicCategoryCode_ID_seq ;

CREATE SEQUENCE MD_TopoLevelCode_ID_seq ;

CREATE SEQUENCE MD_Usage_ID_seq ;

CREATE SEQUENCE MD_VectorSpatialReprenstation_ID_seq ;

CREATE SEQUENCE OperatesOn_ID_seq ;

CREATE SEQUENCE OperationNames_ID_seq ;

CREATE SEQUENCE OtherConstraints_ID_seq ;

CREATE SEQUENCE RS_Identifier_ID_seq ;

CREATE SEQUENCE ServiceVersion_ID_seq ;

CREATE SEQUENCE SV_DCPList_ID_seq ;

CREATE SEQUENCE SV_OperationMetadata_ID_seq ;

CREATE SEQUENCE SV_Parameter_ID_seq ;

CREATE SEQUENCE Voice_ID_seq ;


-- Codelist: CI_OnlineFunctionCode (  B.5.3 )
INSERT INTO CI_OnlineFunctionCode ( codeListValue) VALUES ( 'download' ) ;
INSERT INTO CI_OnlineFunctionCode ( codeListValue) VALUES ( 'information' ) ;
INSERT INTO CI_OnlineFunctionCode ( codeListValue) VALUES ( 'offlineAccess' ) ;
INSERT INTO CI_OnlineFunctionCode ( codeListValue) VALUES ( 'order' ) ;
INSERT INTO CI_OnlineFunctionCode ( codeListValue) VALUES ( 'search' ) ;

-- Codelist: CI_PresentationFormCode (  B.5.4 )
INSERT INTO CI_PresentationFormCode ( codeListValue) VALUES ( 'documentDigital' ) ;
INSERT INTO CI_PresentationFormCode ( codeListValue) VALUES ( 'documentHardcopy' ) ;
INSERT INTO CI_PresentationFormCode ( codeListValue) VALUES ( 'imageDigital' ) ;
INSERT INTO CI_PresentationFormCode ( codeListValue) VALUES ( 'imageHardcopy' ) ;
INSERT INTO CI_PresentationFormCode ( codeListValue) VALUES ( 'mapDigital' ) ;
INSERT INTO CI_PresentationFormCode ( codeListValue) VALUES ( 'mapHardcopy' ) ;
INSERT INTO CI_PresentationFormCode ( codeListValue) VALUES ( 'modelDigital' ) ;
INSERT INTO CI_PresentationFormCode ( codeListValue) VALUES ( 'modelHardcopy' ) ;
INSERT INTO CI_PresentationFormCode ( codeListValue) VALUES ( 'profileDigital' ) ;
INSERT INTO CI_PresentationFormCode ( codeListValue) VALUES ( 'profileHardcopy' ) ;
INSERT INTO CI_PresentationFormCode ( codeListValue) VALUES ( 'tableDigital' ) ;
INSERT INTO CI_PresentationFormCode ( codeListValue) VALUES ( 'tableHardcopy' ) ;
INSERT INTO CI_PresentationFormCode ( codeListValue) VALUES ( 'videoDigital' ) ;
INSERT INTO CI_PresentationFormCode ( codeListValue) VALUES ( 'videoHardcopy' ) ;

-- Codelist: CI_RoleCode (  B.5.5 )
INSERT INTO CI_RoleCode ( codeListValue) VALUES ( 'resourceProvider' ) ;
INSERT INTO CI_RoleCode ( codeListValue) VALUES ( 'custodian' ) ;
INSERT INTO CI_RoleCode ( codeListValue) VALUES ( 'owner' ) ;
INSERT INTO CI_RoleCode ( codeListValue) VALUES ( 'user' ) ;
INSERT INTO CI_RoleCode ( codeListValue) VALUES ( 'distributor' ) ;
INSERT INTO CI_RoleCode ( codeListValue) VALUES ( 'originator' ) ;
INSERT INTO CI_RoleCode ( codeListValue) VALUES ( 'pointOfContact' ) ;
INSERT INTO CI_RoleCode ( codeListValue) VALUES ( 'principalInvestigator' ) ;
INSERT INTO CI_RoleCode ( codeListValue) VALUES ( 'processor' ) ;
INSERT INTO CI_RoleCode ( codeListValue) VALUES ( 'publisher' ) ;
INSERT INTO CI_RoleCode ( codeListValue) VALUES ( 'author' ) ;

-- Codelist: MD_CharacterSetCode (  B.5.10 )
INSERT INTO MD_CharacterSetCode ( codeListValue) VALUES ( 'ucs2' ) ;
INSERT INTO MD_CharacterSetCode ( codeListValue) VALUES ( 'ucs4' ) ;
INSERT INTO MD_CharacterSetCode ( codeListValue) VALUES ( 'utf7' ) ;
INSERT INTO MD_CharacterSetCode ( codeListValue) VALUES ( 'utf8' ) ;
INSERT INTO MD_CharacterSetCode ( codeListValue) VALUES ( 'utf16' ) ;
INSERT INTO MD_CharacterSetCode ( codeListValue) VALUES ( '8859part1' ) ;
INSERT INTO MD_CharacterSetCode ( codeListValue) VALUES ( '8859part2' ) ;
INSERT INTO MD_CharacterSetCode ( codeListValue) VALUES ( '8859part3' ) ;
INSERT INTO MD_CharacterSetCode ( codeListValue) VALUES ( '8859part4' ) ;
INSERT INTO MD_CharacterSetCode ( codelistvalue) VALUES ( '8859part5' ) ;
INSERT INTO MD_CharacterSetCode ( codeListValue) VALUES ( '8859part6' ) ;
INSERT INTO MD_CharacterSetCode ( codeListValue) VALUES ( '8859part7' ) ;
INSERT INTO MD_CharacterSetCode ( codeListValue) VALUES ( '8859part8' ) ;
INSERT INTO MD_CharacterSetCode ( codeListValue) VALUES ( '8859part9' ) ;
INSERT INTO MD_CharacterSetCode ( codeListValue) VALUES ( '8859part10' ) ;
INSERT INTO MD_CharacterSetCode ( codeListValue) VALUES ( '8859part11' ) ;
INSERT INTO MD_CharacterSetCode ( codeListValue) VALUES ( '(reserved for future use)' ) ;
INSERT INTO MD_CharacterSetCode ( codeListValue) VALUES ( '8859part13' ) ;
INSERT INTO MD_CharacterSetCode ( codeListValue) VALUES ( '8859part14' ) ;
INSERT INTO MD_CharacterSetCode ( codeListValue) VALUES ( '8859part15' ) ;
INSERT INTO MD_CharacterSetCode ( codeListValue) VALUES ( '8859part16' ) ;
INSERT INTO MD_CharacterSetCode ( codeListValue) VALUES ( 'jis' ) ;
INSERT INTO MD_CharacterSetCode ( codeListValue) VALUES ( 'shiftJIS' ) ;
INSERT INTO MD_CharacterSetCode ( codeListValue) VALUES ( 'eucJP' ) ;
INSERT INTO MD_CharacterSetCode ( codeListValue) VALUES ( 'usAscii' ) ;
INSERT INTO MD_CharacterSetCode ( codeListValue) VALUES ( 'ebcdic' ) ;
INSERT INTO MD_CharacterSetCode ( codeListValue) VALUES ( 'eucKR' ) ;
INSERT INTO MD_CharacterSetCode ( codeListValue) VALUES ( 'big5' ) ;
INSERT INTO MD_CharacterSetCode ( codeListValue) VALUES ( 'GB2312' ) ;

-- codelist: MD_GeoObjTypeCode (  B.5.15 )
INSERT INTO MD_GeoObjTypeCode ( codeListValue) VALUES ( 'complex' ) ;
INSERT INTO MD_GeoObjTypeCode ( codeListValue) VALUES ( 'composite' ) ;
INSERT INTO MD_GeoObjTypeCode ( codeListValue) VALUES ( 'curve' ) ;
INSERT INTO MD_GeoObjTypeCode ( codeListValue) VALUES ( 'point' ) ;
INSERT INTO MD_GeoObjTypeCode ( codeListValue) VALUES ( 'solid' ) ;
INSERT INTO MD_GeoObjTypeCode ( codeListValue) VALUES ( 'surface' ) ;

-- codelist: MD_KeywordTypeCode  (  B.5.17 )
INSERT INTO MD_KeywordTypeCode ( codeListValue) VALUES ( 'discipline' ) ;
INSERT INTO MD_KeywordTypeCode ( codeListValue) VALUES ( 'place' ) ;
INSERT INTO MD_KeywordTypeCode ( codeListValue) VALUES ( 'stratum' ) ;
INSERT INTO MD_KeywordTypeCode ( codeListValue) VALUES ( 'temporal' ) ;
INSERT INTO MD_KeywordTypeCode ( codeListValue) VALUES ( 'theme' ) ;

-- codelist: MD_MainFreqCode (  B.5.18 )
INSERT INTO MD_MainFreqCode ( codeListValue) VALUES ( 'continual' ) ;
INSERT INTO MD_MainFreqCode ( codeListValue) VALUES ( 'daily' ) ;
INSERT INTO MD_MainFreqCode ( codeListValue) VALUES ( 'weekly' ) ;
INSERT INTO MD_MainFreqCode ( codeListValue) VALUES ( 'fortnightly' ) ;
INSERT INTO MD_MainFreqCode ( codeListValue) VALUES ( 'monthly' ) ;
INSERT INTO MD_MainFreqCode ( codeListValue) VALUES ( 'quarterly' ) ;
INSERT INTO MD_MainFreqCode ( codeListValue) VALUES ( 'biannually' ) ;
INSERT INTO MD_MainFreqCode ( codeListValue) VALUES ( 'annually' ) ;
INSERT INTO MD_MainFreqCode ( codeListValue) VALUES ( 'asNeeded' ) ;
INSERT INTO MD_MainFreqCode ( codeListValue) VALUES ( 'irregular' ) ;
INSERT INTO MD_MainFreqCode ( codeListValue) VALUES ( 'notPlanned' ) ;
INSERT INTO MD_MainFreqCode ( codeListValue) VALUES ( 'unknown' ) ;

-- codelist: MD_MediumFormatCode (  B.5.19 )
INSERT INTO MD_MediumFormatCode ( codeListValue) VALUES ( 'cpio' ) ;
INSERT INTO MD_MediumFormatCode ( codeListValue) VALUES ( 'tar' ) ;
INSERT INTO MD_MediumFormatCode ( codeListValue) VALUES ( 'highSierra' ) ;
INSERT INTO MD_MediumFormatCode ( codeListValue) VALUES ( 'iso9660' ) ;
INSERT INTO MD_MediumFormatCode ( codeListValue) VALUES ( 'iso9660RockRidge' ) ;
INSERT INTO MD_MediumFormatCode ( codeListValue) VALUES ( 'iso9660AppleHFS' ) ;

-- codelist: MD_MediumNameCode (  B.5.20 )
INSERT INTO MD_MediumNameCode ( codeListValue) VALUES ( 'cdRom' ) ;
INSERT INTO MD_MediumNameCode ( codeListValue) VALUES ( 'dvd' ) ;
INSERT INTO MD_MediumNameCode ( codeListValue) VALUES ( 'dvdRom' ) ;
INSERT INTO MD_MediumNameCode ( codeListValue) VALUES ( '3halfInchFloppy' ) ;
INSERT INTO MD_MediumNameCode ( codeListValue) VALUES ( '5quarterInchFloppy' ) ;
INSERT INTO MD_MediumNameCode ( codeListValue) VALUES ( '7trackTape' ) ;
INSERT INTO MD_MediumNameCode ( codeListValue) VALUES ( '9trackTape' ) ;
INSERT INTO MD_MediumNameCode ( codeListValue) VALUES ( '3480Cartridge' ) ;
INSERT INTO MD_MediumNameCode ( codeListValue) VALUES ( '3490Cartridge' ) ;
INSERT INTO MD_MediumNameCode ( codeListValue) VALUES ( '3580Cartridge' ) ;

-- Codelist: MD_ProgressCode (  B.5.23 )
INSERT INTO MD_ProgressCode ( codeListValue) VALUES ( 'completed' ) ;
INSERT INTO MD_ProgressCode ( codeListValue) VALUES ( 'historicalArchive' ) ;
INSERT INTO MD_ProgressCode ( codeListValue) VALUES ( 'obsolete' ) ;
INSERT INTO MD_ProgressCode ( codeListValue) VALUES ( 'onGoing' ) ;
INSERT INTO MD_ProgressCode ( codeListValue) VALUES ( 'planned' ) ;
INSERT INTO MD_ProgressCode ( codeListValue) VALUES ( 'required' ) ;
INSERT INTO MD_ProgressCode ( codeListValue) VALUES ( 'underDevelopment' ) ;

-- Codelist: MD_RestrictionCode (  B.5.24 )
INSERT INTO MD_RestrictionCode ( codeListValue) VALUES ( 'copyright' ) ;
INSERT INTO MD_RestrictionCode ( codeListValue) VALUES ( 'patent' ) ;
INSERT INTO MD_RestrictionCode ( codeListValue) VALUES ( 'patentPending' ) ;
INSERT INTO MD_RestrictionCode ( codeListValue) VALUES ( 'trademark' ) ;
INSERT INTO MD_RestrictionCode ( codeListValue) VALUES ( 'license' ) ;
INSERT INTO MD_RestrictionCode ( codeListValue) VALUES ( 'intellectualPropertyRights' ) ;
INSERT INTO MD_RestrictionCode ( codeListValue) VALUES ( 'restricted' ) ;
INSERT INTO MD_RestrictionCode ( codeListValue) VALUES ( 'otherRestrictions' ) ;

-- Codelist: MD_ScopeCode (  B.5.25 )
INSERT INTO MD_ScopeCode ( codeListValue) VALUES ( 'attribute' ) ;
INSERT INTO MD_ScopeCode ( codeListValue) VALUES ( 'attributeType' ) ;
INSERT INTO MD_ScopeCode ( codeListValue) VALUES ( 'collectionHardware' ) ;
INSERT INTO MD_ScopeCode ( codeListValue) VALUES ( 'collectionSession' ) ;
INSERT INTO MD_ScopeCode ( codeListValue) VALUES ( 'dataset' ) ;
INSERT INTO MD_ScopeCode ( codeListValue) VALUES ( 'series' ) ;
INSERT INTO MD_ScopeCode ( codeListValue) VALUES ( 'nonGeographicDataset' ) ;
INSERT INTO MD_ScopeCode ( codeListValue) VALUES ( 'dimensionGroup' ) ;
INSERT INTO MD_ScopeCode ( codeListValue) VALUES ( 'feature' ) ;
INSERT INTO MD_ScopeCode ( codeListValue) VALUES ( 'featureType' ) ;
INSERT INTO MD_ScopeCode ( codeListValue) VALUES ( 'propertyType' ) ;
INSERT INTO MD_ScopeCode ( codeListValue) VALUES ( 'fieldSession' ) ;
INSERT INTO MD_ScopeCode ( codeListValue) VALUES ( 'software' ) ;
INSERT INTO MD_ScopeCode ( codeListValue) VALUES ( 'service' ) ;
INSERT INTO MD_ScopeCode ( codeListValue) VALUES ( 'model' ) ;
INSERT INTO MD_ScopeCode ( codeListValue) VALUES ( 'tile' ) ;

-- Codelist: MD_SpatialRepTypeCode  (  B.5.26 )
INSERT INTO MD_SpatialRepTypeCode ( codeListValue) VALUES ( 'vector' ) ;
INSERT INTO MD_SpatialRepTypeCode ( codeListValue) VALUES ( 'grid' ) ;
INSERT INTO MD_SpatialRepTypeCode ( codeListValue) VALUES ( 'textTable' ) ;
INSERT INTO MD_SpatialRepTypeCode ( codeListValue) VALUES ( 'tin' ) ;
INSERT INTO MD_SpatialRepTypeCode ( codeListValue) VALUES ( 'stereoModel' ) ;
INSERT INTO MD_SpatialRepTypeCode ( codeListValue) VALUES ( 'video' ) ;

-- Enumeration: MD_TopicCategoryCode (  B.5.27 )
INSERT INTO MD_TopicCategoryCode (category) VALUES ( 'farming' ) ;
INSERT INTO MD_TopicCategoryCode (category) VALUES ( 'biota' ) ;
INSERT INTO MD_TopicCategoryCode (category) VALUES ( 'boundaries' ) ;
INSERT INTO MD_TopicCategoryCode (category) VALUES ( 'climatologyMeteorologyAtmosphere' ) ;
INSERT INTO MD_TopicCategoryCode (category) VALUES ( 'economy' ) ;
INSERT INTO MD_TopicCategoryCode (category) VALUES ( 'elevation' ) ;
INSERT INTO MD_TopicCategoryCode (category) VALUES ( 'environment' ) ;
INSERT INTO MD_TopicCategoryCode (category) VALUES ( 'geoscientificInformation' ) ;
INSERT INTO MD_TopicCategoryCode (category) VALUES ( 'health' ) ;
INSERT INTO MD_TopicCategoryCode (category) VALUES ( 'imageryBaseMapsEarthCover' ) ;
INSERT INTO MD_TopicCategoryCode (category) VALUES ( 'intelligenceMilitary' ) ;
INSERT INTO MD_TopicCategoryCode (category) VALUES ( 'inlandWaters' ) ;
INSERT INTO MD_TopicCategoryCode (category) VALUES ( 'location' ) ;
INSERT INTO MD_TopicCategoryCode (category) VALUES ( 'oceans' ) ;
INSERT INTO MD_TopicCategoryCode (category) VALUES ( 'planningCadastre' ) ;
INSERT INTO MD_TopicCategoryCode (category) VALUES ( 'society' ) ;
INSERT INTO MD_TopicCategoryCode (category) VALUES ( 'structure' ) ;
INSERT INTO MD_TopicCategoryCode (category) VALUES ( 'transportation' ) ;
INSERT INTO MD_TopicCategoryCode (category) VALUES ( 'utilitiesCommunication' ) ;

-- codelist: MD_TopoLevelCode (  B.5.28 )
INSERT INTO MD_TopoLevelCode ( codeListValue) VALUES ( 'geometryOnly' ) ;
INSERT INTO MD_TopoLevelCode ( codeListValue) VALUES ( 'topology1D' ) ;
INSERT INTO MD_TopoLevelCode ( codeListValue) VALUES ( 'planarGraph' ) ;
INSERT INTO MD_TopoLevelCode ( codeListValue) VALUES ( 'fullPlanarGraph' ) ;
INSERT INTO MD_TopoLevelCode ( codeListValue) VALUES ( 'surfaceGraph' ) ;
INSERT INTO MD_TopoLevelCode ( codeListValue) VALUES ( 'fullSurfaceGraph' ) ;
INSERT INTO MD_TopoLevelCode ( codeListValue) VALUES ( 'topology3D' ) ;
INSERT INTO MD_TopoLevelCode ( codeListValue) VALUES ( 'fullTopology3D' ) ;
INSERT INTO MD_TopoLevelCode ( codeListValue) VALUES ( 'abstract' ) ;


-- Codelist: hierarchylevelcode (  B.---- )
INSERT INTO hierarchylevelcode ( codeListValue) VALUES ( 'attribute' ) ;
INSERT INTO hierarchylevelcode ( codeListValue) VALUES ( 'attributeType' ) ;
INSERT INTO hierarchylevelcode ( codeListValue) VALUES ( 'collectionHardware' ) ;
INSERT INTO hierarchylevelcode ( codeListValue) VALUES ( 'collectionSession' ) ;
INSERT INTO hierarchylevelcode ( codeListValue) VALUES ( 'dataset' ) ;
INSERT INTO hierarchylevelcode ( codeListValue) VALUES ( 'series' ) ;
INSERT INTO hierarchylevelcode ( codeListValue) VALUES ( 'nonGeographicDataset' ) ;
INSERT INTO hierarchylevelcode ( codeListValue) VALUES ( 'dimensionGroup' ) ;
INSERT INTO hierarchylevelcode ( codeListValue) VALUES ( 'feature' ) ;
INSERT INTO hierarchylevelcode ( codeListValue) VALUES ( 'featureType' ) ;
INSERT INTO hierarchylevelcode ( codeListValue) VALUES ( 'propertyType' ) ;
INSERT INTO hierarchylevelcode ( codeListValue) VALUES ( 'fieldSession' ) ;
INSERT INTO hierarchylevelcode ( codeListValue) VALUES ( 'software' ) ;
INSERT INTO hierarchylevelcode ( codeListValue) VALUES ( 'service' ) ;
INSERT INTO hierarchylevelcode ( codeListValue) VALUES ( 'model' ) ;
INSERT INTO hierarchylevelcode ( codeListValue) VALUES ( 'tile' ) ;
INSERT INTO hierarchylevelcode ( codeListValue) VALUES ( 'application' ) ;

-- Codelist CSW coupling type
INSERT INTO CSW_CouplingType ( codeListValue) VALUES ( 'tight' ) ;
INSERT INTO CSW_CouplingType ( codeListValue) VALUES ( 'loose' ) ;
INSERT INTO CSW_CouplingType ( codeListValue) VALUES ( 'mixed' ) ;

-- Codelist DCP Type
insert into sv_dcplist ( codeListValue ) values ( 'HTTPGet' );
insert into sv_dcplist ( codeListValue ) values ( 'HTTPPost' );
insert into sv_dcplist ( codeListValue ) values ( 'HTTPSoap' );
insert into sv_dcplist ( codeListValue ) values ( 'COM' );
insert into sv_dcplist ( codeListValue ) values ( 'XML' );
insert into sv_dcplist ( codeListValue ) values ( 'SQL' );
insert into sv_dcplist ( codeListValue ) values ( 'Corba' );
insert into sv_dcplist ( codeListValue ) values ( 'Java' );

-- Codelist MD ClassificationCode (B5.11)
insert into md_classificationcode ( codeListValue ) values ( 'unclassified' );
insert into md_classificationcode ( codeListValue ) values ( 'restricted' );
insert into md_classificationcode ( codeListValue ) values ( 'confidential' );
insert into md_classificationcode ( codeListValue ) values ( 'secret' );
insert into md_classificationcode ( codeListValue ) values ( 'topSecret' );

commit;
