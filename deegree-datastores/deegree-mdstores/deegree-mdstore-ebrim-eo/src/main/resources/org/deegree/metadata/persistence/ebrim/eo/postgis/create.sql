CREATE SEQUENCE globalSeq MINVALUE 0;

CREATE TABLE IDXTB_RegistryPackage( 
	internalId integer DEFAULT nextval('globalSeq'::text) PRIMARY KEY,
	id varchar(150) UNIQUE NOT NULL,
	externalId varchar(150),
	name varchar(250),
	description text,
	data bytea NOT NULL
);

CREATE TABLE IDXTB_ExtrinsicObject ( 
	internalId integer DEFAULT nextval('globalSeq'::text) PRIMARY KEY,
	id varchar(150) UNIQUE NOT NULL,
	objectType varchar(150) NOT NULL,
	home varchar(250),
	lid varchar(150),
	status varchar(150),
	externalId varchar(150),
	name varchar(250),
	description text,
	versionInfo varchar(250),
	data bytea NOT NULL,
	resource bytea,
	isOpaque boolean,
	
	-- Slots for EOAcquisitionPlatform
	ap_instShortName varchar(150),
	ap_platformOrbitType varchar(150),
	ap_platformSerialId varchar(150),
	ap_sensorResolution decimal(10,2),
	ap_sensorOpMode varchar(150),
	ap_sensorType varchar(150),
	ap_shortName varchar(150),
	ap_swathId varchar(150),
	
	-- Slots for EOArchivingInformation   
	ai_archivingDate timestamp,
	ai_archivingIdentifier varchar(150),
	-- rim:Name for EOArchivingInformation
	ai_archivingCenter varchar(250),
	
	-- Slots for EOBrowseInformation
	bi_subType varchar(150),
	-- rim:Name for EOBrowseInformation
	bi_type varchar(150),
	
	-- Slots for EODataLayer
	dl_highestLocation decimal(10,2),
	dl_lowestLocation decimal(10,2),
	-- rim:Name for EODataLayer
	dl_specy varchar(500),

	-- Slots for EOProduct
	ep_acquisitionDate timestamp,
	ep_acquisitionStation varchar(150),
	ep_acquisitionSubType varchar(150),
	ep_acquisitionType varchar(150),
	ep_acrossTrackIncAngle decimal(10,2),
	ep_alongTrackIncAngle decimal(10,2),
	ep_ascendingNodeDate timestamp,
	ep_ascNdLong decimal(10,2),
	ep_beginPosition timestamp,
	ep_cloudCoverPerc decimal(10,2),
	ep_compTimeAscNd decimal(10,2),
	ep_doi varchar(250),
	ep_endPosition timestamp,
	ep_illumAzimuthAngle decimal(10,2),
	ep_illumElevationAngle decimal(10,2),
	ep_imgQualityDeg decimal(10,2),
	ep_imgQualityDegQuotMd varchar(250),
	ep_incidenceAngle decimal(10,2),
	ep_lastOrbitNumber integer,
	ep_orbitDirection varchar(50),
	ep_orbitDuration decimal(10,2),
	ep_orbitNumber integer,
	ep_parentIdentifier varchar(150),
	ep_pitch decimal(10,2),
	ep_productType varchar(150),
	ep_roll decimal(10,2),
	ep_snowCoverPerc decimal(10,2),
	ep_startTimeAscNd decimal(10,2),
	ep_status varchar(150),
	ep_vendorSpecAttr text,
	ep_vendorSpecVal text,
	ep_wrsLatitudeGrid varchar(250),
	ep_wrsLongitudeGrid varchar(250),
	ep_yaw decimal(10,2),	

	-- Slots for EOMaskInformation
	mi_format varchar(150),
	mi_type varchar(150),
	
	-- Slots for EOProductInformation
	pi_size integer,
	
	fk_registrypackage integer NOT NULL  REFERENCES IDXTB_RegistryPackage(internalId) ON DELETE CASCADE 
);

--Geospatial columns in idxtb_extrinsicobject
SELECT AddGeometryColumn('public','idxtb_extrinsicobject','ep_multiextentof','-1','MULTIPOLYGON','2');
SELECT AddGeometryColumn('public','idxtb_extrinsicobject','ep_centerof','-1','POINT','2');


CREATE TABLE idxtb_association(
	internalId integer DEFAULT nextval('globalSeq'::text) PRIMARY KEY,
	id varchar(150) UNIQUE NOT NULL,
	objectType varchar(150),
	home varchar(250),
	lid varchar(150),
	status varchar(150),
	externalId varchar(150),
	name varchar(250),
	description text,
	versionInfo varchar(250),
	sourceObject  varchar(150) NOT NULL,
	targetObject  varchar(150) NOT NULL,
	associationType varchar(150) NOT NULL,
	data bytea NOT NULL,
	fk_registrypackage integer NOT NULL  REFERENCES IDXTB_RegistryPackage(internalId) ON DELETE CASCADE 
);


CREATE TABLE idxtb_classification(
	internalId integer DEFAULT nextval('globalSeq'::text) PRIMARY KEY,
	id varchar(150) UNIQUE NOT NULL,
	objectType varchar(150),
	home varchar(250),
	lid varchar(150),
	status varchar(150),
	externalId varchar(150),
	name varchar(250),
	description text,
	versionInfo varchar(250),
	classificationScheme varchar(150) NOT NULL,
	classificationNode varchar(150) NOT NULL,
	classifiedObject  varchar(150) NOT NULL,
	data bytea NOT NULL,
	fk_registrypackage integer NOT NULL  REFERENCES IDXTB_RegistryPackage(internalId) ON DELETE CASCADE 
);

CREATE TABLE idxtb_classificationNode(
	internalId integer DEFAULT nextval('globalSeq'::text) PRIMARY KEY,
	id varchar(150) UNIQUE NOT NULL,
	objectType varchar(150),
	home varchar(250),
	lid varchar(150),
	status varchar(150),
	externalId varchar(150),
	name varchar(250),
	description text,
	versionInfo varchar(250),
	parent varchar(150),
	code varchar(256),
	path varchar(500),
	data bytea NOT NULL,
	fk_registrypackage integer NOT NULL  REFERENCES IDXTB_RegistryPackage(internalId) ON DELETE CASCADE 
);

CREATE TABLE management(
	key varchar (150),
	value varchar (150)
);

-- index IDXTB_RegistryPackage
CREATE INDEX description_RegistryPackage_idx on IDXTB_RegistryPackage ( description );
CREATE INDEX externalid_RegistryPackage_idx on IDXTB_RegistryPackage ( externalId );
CREATE INDEX id_RegistryPackage_idx on IDXTB_RegistryPackage ( id );
CREATE INDEX name_RegistryPackage_idx on IDXTB_RegistryPackage ( name );

-- index IDXTB_ExtrinsicObject
CREATE INDEX isopaque_ExtrinsicObject_idx on IDXTB_ExtrinsicObject ( isopaque );
CREATE INDEX id_ExtrinsicObject_idx on IDXTB_ExtrinsicObject ( id );
CREATE INDEX objectType_ExtrinsicObject_idx on IDXTB_ExtrinsicObject ( objectType );
CREATE INDEX home_ExtrinsicObject_idx on IDXTB_ExtrinsicObject ( home );
CREATE INDEX lid_ExtrinsicObject_idx on IDXTB_ExtrinsicObject ( lid );
CREATE INDEX status_ExtrinsicObject_idx on IDXTB_ExtrinsicObject ( status );
CREATE INDEX externalId_ExtrinsicObject_idx on IDXTB_ExtrinsicObject ( externalId );
CREATE INDEX name_ExtrinsicObject_idx on IDXTB_ExtrinsicObject ( name );
CREATE INDEX description_ExtrinsicObject_idx on IDXTB_ExtrinsicObject ( description );
CREATE INDEX versionInfo_ExtrinsicObject_idx on IDXTB_ExtrinsicObject ( versionInfo );
CREATE INDEX fk_registryPackage_ExtrinsicObject_idx on IDXTB_ExtrinsicObject ( fk_registryPackage );

-- index EOProduct
CREATE INDEX ep_acquisitiondate_ExtrinsicObject_idx on IDXTB_ExtrinsicObject ( ep_acquisitiondate );
CREATE INDEX ep_acquisitionstation_ExtrinsicObject_idx on IDXTB_ExtrinsicObject ( ep_acquisitionstation );
CREATE INDEX ep_acquisitionsubtype_ExtrinsicObject_idx on IDXTB_ExtrinsicObject ( ep_acquisitionsubtype );
CREATE INDEX ep_acquisitiontype_ExtrinsicObject_idx on IDXTB_ExtrinsicObject ( ep_acquisitiontype );
CREATE INDEX ep_acrosstrackincangle_ExtrinsicObject_idx on IDXTB_ExtrinsicObject ( ep_acrosstrackincangle );
CREATE INDEX ep_alongtrackincangle_ExtrinsicObject_idx on IDXTB_ExtrinsicObject ( ep_alongtrackincangle );
CREATE INDEX ep_ascendingnodedate_ExtrinsicObject_idx on IDXTB_ExtrinsicObject ( ep_ascendingnodedate );
CREATE INDEX ep_ascndlong_ExtrinsicObject_idx on IDXTB_ExtrinsicObject ( ep_ascndlong );
CREATE INDEX ep_beginposition_ExtrinsicObject_idx on IDXTB_ExtrinsicObject ( ep_beginposition );
CREATE INDEX ep_cloudcoverperc_ExtrinsicObject_idx on IDXTB_ExtrinsicObject ( ep_cloudCoverPerc );
CREATE INDEX ep_comptimeascnd_ExtrinsicObject_idx on IDXTB_ExtrinsicObject ( ep_comptimeascnd );
CREATE INDEX ep_doi_ExtrinsicObject_idx on IDXTB_ExtrinsicObject ( ep_doi );
CREATE INDEX ep_endposition_ExtrinsicObject_idx on IDXTB_ExtrinsicObject ( ep_endposition );
CREATE INDEX ep_imgqualitydeg_ExtrinsicObject_idx on IDXTB_ExtrinsicObject ( ep_imgqualitydeg );
CREATE INDEX ep_imgqualitydegquotmd_ExtrinsicObject_idx on IDXTB_ExtrinsicObject ( ep_imgqualitydegquotmd );
CREATE INDEX ep_incidenceangle_ExtrinsicObject_idx on IDXTB_ExtrinsicObject ( ep_incidenceangle );
CREATE INDEX ep_lastorbitnumber_ExtrinsicObject_idx on IDXTB_ExtrinsicObject ( ep_lastorbitnumber );
CREATE INDEX ep_orbitdirection_ExtrinsicObject_idx on IDXTB_ExtrinsicObject ( ep_orbitdirection );
CREATE INDEX ep_orbitduration_ExtrinsicObject_idx on IDXTB_ExtrinsicObject ( ep_orbitduration );
CREATE INDEX ep_orbitnumber_ExtrinsicObject_idx on IDXTB_ExtrinsicObject ( ep_orbitnumber );
CREATE INDEX ep_parentidentifier_ExtrinsicObject_idx on IDXTB_ExtrinsicObject ( ep_parentidentifier );
CREATE INDEX ep_pitch_ExtrinsicObject_idx on IDXTB_ExtrinsicObject ( ep_pitch );
CREATE INDEX ep_producttype_ExtrinsicObject_idx on IDXTB_ExtrinsicObject ( ep_producttype );
CREATE INDEX ep_roll_ExtrinsicObject_idx on IDXTB_ExtrinsicObject ( ep_roll );
CREATE INDEX ep_snowcoverperc_ExtrinsicObject_idx on IDXTB_ExtrinsicObject ( ep_snowCoverPerc );
CREATE INDEX ep_starttimeascnd_ExtrinsicObject_idx on IDXTB_ExtrinsicObject ( ep_starttimeascnd );
CREATE INDEX ep_status_ExtrinsicObject_idx on IDXTB_ExtrinsicObject ( ep_status );
CREATE INDEX ep_vendorspecattr_ExtrinsicObject_idx on  IDXTB_ExtrinsicObject ( ep_vendorspecattr );
CREATE INDEX ep_vendorspecval_ExtrinsicObject_idx on IDXTB_ExtrinsicObject ( ep_vendorspecval );
CREATE INDEX ep_wrslatitudeGrid_ExtrinsicObject_idx on IDXTB_ExtrinsicObject ( ep_wrslatitudeGrid );
CREATE INDEX ep_wrslongitudeGrid_ExtrinsicObject_idx on IDXTB_ExtrinsicObject ( ep_wrslongitudeGrid );
CREATE INDEX ep_yaw_ExtrinsicObject_idx on IDXTB_ExtrinsicObject ( ep_yaw );

-- index AcqPlatform	
CREATE INDEX ap_instshortname_ExtrinsicObject_idx on IDXTB_ExtrinsicObject( ap_instshortname );
CREATE INDEX ap_platformorbittype_ExtrinsicObject_idx on IDXTB_ExtrinsicObject( ap_platformorbittype );
CREATE INDEX ap_platformserialid_ExtrinsicObject_idx on IDXTB_ExtrinsicObject( ap_platformserialid );
CREATE INDEX ap_sensoropmode_ExtrinsicObject_idx on IDXTB_ExtrinsicObject( ap_sensoropmode );
CREATE INDEX ap_sensorresolution_ExtrinsicObject_idx on IDXTB_ExtrinsicObject( ap_sensorresolution );
CREATE INDEX ap_sensortype_ExtrinsicObject_idx on IDXTB_ExtrinsicObject( ap_sensortype );
CREATE INDEX ap_shortname_ExtrinsicObject_idx on IDXTB_ExtrinsicObject( ap_shortname );
CREATE INDEX ap_swathid_ExtrinsicObject_idx on IDXTB_ExtrinsicObject( ap_swathid );

-- index ArchivingInfo
CREATE INDEX ai_archivingcenter_ExtrinsicObject_idx on IDXTB_ExtrinsicObject( ai_archivingcenter );
CREATE INDEX ai_archivingdate_ExtrinsicObject_idx on IDXTB_ExtrinsicObject( ai_archivingdate );
CREATE INDEX ai_archivingidentifier_ExtrinsicObject_idx on IDXTB_ExtrinsicObject( ai_archivingidentifier );

-- index BrowseInfo
CREATE INDEX bi_subtype_ExtrinsicObject_idx on IDXTB_ExtrinsicObject ( bi_subtype );
CREATE INDEX bi_type_ExtrinsicObject_idx on IDXTB_ExtrinsicObject ( bi_type );

-- index DataLayer
CREATE INDEX dl_highestlocation_ExtrinsicObject_idx on IDXTB_ExtrinsicObject ( dl_highestlocation );
CREATE INDEX dl_lowestlocation_ExtrinsicObject_idx on IDXTB_ExtrinsicObject ( dl_lowestlocation );
CREATE INDEX dl_specy_ExtrinsicObject_idx on IDXTB_ExtrinsicObject ( dl_specy );

-- index IDXTB_MaskInfo
CREATE INDEX mi_format_ExtrinsicObject_idx on IDXTB_ExtrinsicObject ( mi_format );
CREATE INDEX mi_type_ExtrinsicObject_idx on IDXTB_ExtrinsicObject ( mi_type );

-- index IDXTB_ProductInfo
CREATE INDEX pi_size_ExtrinsicObject_idx on IDXTB_ExtrinsicObject ( pi_size );

--idxtb_association
CREATE INDEX id_association_idx on idxtb_association ( id );
CREATE INDEX objectType_association_idx on idxtb_association ( objectType );
CREATE INDEX home_association_idx on idxtb_association ( home );
CREATE INDEX lid_association_idx on idxtb_association ( lid );
CREATE INDEX status_association_idx on idxtb_association ( status );
CREATE INDEX externalId_association_idx on idxtb_association ( externalId );
CREATE INDEX name_association_idx on idxtb_association ( name );
CREATE INDEX description_association_idx on idxtb_association ( description );
CREATE INDEX versionInfo_association_idx on idxtb_association ( versionInfo );
CREATE INDEX sourceObject_association_idx on idxtb_association ( sourceObject );
CREATE INDEX targetObject_association_idx on idxtb_association ( targetObject );
CREATE INDEX associationType_association_idx on idxtb_association ( associationType );
CREATE INDEX fk_registryPackage_association_idx on idxtb_association ( fk_registryPackage );

--idxtb_classification
CREATE INDEX id_classification_idx on idxtb_classification ( id );
CREATE INDEX objectType_classification_idx on idxtb_classification ( objectType );
CREATE INDEX home_classification_idx on idxtb_classification ( home );
CREATE INDEX lid_classification_idx on idxtb_classification ( lid );
CREATE INDEX status_classification_idx on idxtb_classification ( status );
CREATE INDEX externalId_classification_idx on idxtb_classification ( externalId );
CREATE INDEX name_classification_idx on idxtb_classification ( name );
CREATE INDEX description_classification_idx on idxtb_classification ( description );
CREATE INDEX versionInfo_classification_idx on idxtb_classification ( versionInfo );
CREATE INDEX classificationScheme_classification_idx on idxtb_classification ( classificationScheme );
CREATE INDEX classificationNode_classification_idx on idxtb_classification ( classificationNode );
CREATE INDEX classifiedObject_classification_idx on idxtb_classification ( classifiedObject );
CREATE INDEX fk_registryPackage_classification_idx on idxtb_classification ( fk_registryPackage );

-- idxtb_classificationNode
CREATE INDEX id_classificationNode_idx on idxtb_classificationNode ( id );
CREATE INDEX objectType_classificationNode_idx on idxtb_classificationNode ( objectType );
CREATE INDEX home_classificationNode_idx on idxtb_classificationNode ( home );
CREATE INDEX lid_classificationNode_idx on idxtb_classificationNode ( lid );
CREATE INDEX status_classificationNode_idx on idxtb_classificationNode ( status );
CREATE INDEX externalId_classificationNode_idx on idxtb_classificationNode ( externalId );
CREATE INDEX name_classificationNode_idx on idxtb_classificationNode ( name );
CREATE INDEX description_classificationNode_idx on idxtb_classificationNode ( description );
CREATE INDEX versionInfo_classificationNode_idx on idxtb_classificationNode ( versionInfo );
CREATE INDEX parent_classificationNode_idx on idxtb_classificationNode ( parent );
CREATE INDEX code_classificationNode_idx on idxtb_classificationNode ( code );
CREATE INDEX path_classificationNode_idx on idxtb_classificationNode ( path );
CREATE INDEX fk_registryPackage_classificationNode_idx on idxtb_classificationNode ( fk_registryPackage );

-- set spatial index 
CREATE INDEX IDXTB_ExtrinsicObject_extent_spx ON IDXTB_ExtrinsicObject USING GIST ( ep_multiExtentOf );
CREATE INDEX IDXTB_ExtrinsicObject_center_spx ON IDXTB_ExtrinsicObject USING GIST ( ep_centerOf );