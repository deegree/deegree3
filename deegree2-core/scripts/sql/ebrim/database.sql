SELECT DropGeometryColumn('', 'slotvalues','geometry');

DROP TABLE Association;
DROP TABLE AuditableEvent;
DROP TABLE Classification;
DROP TABLE ClassificationNode;
DROP TABLE ClassificationScheme;
DROP TABLE Description;
DROP TABLE ExternalIdentifier;
DROP TABLE ExtrinsicObject;
DROP TABLE JT_SLOT_ObjectRef;
DROP TABLE JT_SLOT_RegistryObj;
DROP TABLE LINK_RegObj_RegObj;
DROP TABLE LocalizedString;
DROP TABLE Name;
DROP TABLE ObjectRef;
DROP TABLE RegistryObject;
DROP TABLE RegistryPackage;
DROP TABLE Slot;
DROP TABLE SlotValues;
DROP TABLE VersionInfo;

DROP SEQUENCE Association_ID_seq;
DROP SEQUENCE AuditableEvent_ID_seq;
DROP SEQUENCE Classification_ID_seq;
DROP SEQUENCE ClassificationNode_ID_seq;
DROP SEQUENCE ClassificationScheme_ID_seq;
DROP SEQUENCE Description_ID_seq;
DROP SEQUENCE ExternalIdentifier_ID_seq;
DROP SEQUENCE ExtrinsicObject_ID_seq;
DROP SEQUENCE LINK_RegObj_RegObj_ID_seq;
DROP SEQUENCE LocalizedString_ID_seq;
DROP SEQUENCE Name_ID_seq;
DROP SEQUENCE ObjectRef_ID_seq;
DROP SEQUENCE RegistryObject_ID_seq;
DROP SEQUENCE RegistryPackage_ID_seq;
DROP SEQUENCE Slot_ID_seq;
DROP SEQUENCE SlotValues_ID_seq;
DROP SEQUENCE VersionInfo_ID_seq;

CREATE TABLE Association ( 
	ID integer DEFAULT NEXTVAL('Association_ID_seq'::TEXT) NOT NULL,
	fk_registryobject integer NOT NULL,
	associationType varchar(500) NOT NULL,
	sourceObject varchar(500) NOT NULL,
	targetObject varchar(500) NOT NULL
);

CREATE TABLE AuditableEvent ( 
	ID integer DEFAULT NEXTVAL('AuditableEvent_ID_seq'::TEXT) NOT NULL,
	fk_registryobject integer NOT NULL,
	eventType varchar(500) NOT NULL,
	timestamp timestamp NOT NULL,
	userName varchar(500) NOT NULL,
	requestID varchar(500) NOT NULL
);



CREATE TABLE Classification ( 
	ID integer DEFAULT NEXTVAL('Classification_ID_seq'::TEXT) NOT NULL,
	fk_registryobject integer NOT NULL,
	classificationScheme varchar(500),
	classificationObject varchar(500) NOT NULL,
	classificationNode varchar(500),
	nodeRepresentation varchar(500)
);



CREATE TABLE ClassificationNode ( 
	ID integer DEFAULT NEXTVAL('ClassificationNode_ID_seq'::TEXT) NOT NULL,
	fk_registryobject integer NOT NULL,
	parent varchar(500),
	code varchar(256),
	path varchar(500)
);




CREATE TABLE ClassificationScheme ( 
	ID integer DEFAULT NEXTVAL('ClassificationScheme_ID_seq'::TEXT) NOT NULL,
	fk_registryobject integer NOT NULL,
	isInternal boolean NOT NULL,
	nodeType varchar(500) NOT NULL
);




CREATE TABLE Description ( 
	ID integer DEFAULT NEXTVAL('Description_ID_seq'::TEXT) NOT NULL,
	fk_registryobject integer NOT NULL,
	fk_locstring integer NOT NULL
);




CREATE TABLE ExternalIdentifier ( 
	ID integer DEFAULT NEXTVAL('ExternalIdentifier_ID_seq'::TEXT) NOT NULL,
	fk_registryobject integer NOT NULL,
	registryObject varchar(500) NOT NULL,
	identificationScheme varchar(500) NOT NULL,
	value varchar(256) NOT NULL
);


CREATE TABLE ExtrinsicObject ( 
	ID integer DEFAULT NEXTVAL('ExtrinsicObject_ID_seq'::TEXT) NOT NULL,
	fk_registryobject integer NOT NULL,
	mimetype varchar(256) DEFAULT 'application/octet-stream' NOT NULL,
	isOpaque boolean DEFAULT false NOT NULL,
	cntinfoversionname varchar(16),
	cntinfoversioncomment varchar(500),
	object text
);


CREATE TABLE JT_SLOT_ObjectRef ( 
	fk_objectref integer NOT NULL,
	fk_slot integer NOT NULL
);


CREATE TABLE JT_SLOT_RegistryObj ( 
	fk_registryobj integer NOT NULL,
	fk_slot integer NOT NULL
);




CREATE TABLE LINK_RegObj_RegObj ( 
	ID integer DEFAULT NEXTVAL('LINK_RegObj_RegObj_ID_seq'::TEXT) NOT NULL,
	fk_source integer NOT NULL,
	fk_target integer NOT NULL,
	type varchar(50) NOT NULL
);




CREATE TABLE LocalizedString ( 
	ID integer DEFAULT NEXTVAL('LocalizedString_ID_seq'::TEXT) NOT NULL,
	charset varchar(16) DEFAULT 'UTF-8' NOT NULL,
	value varchar(2000) NOT NULL,
	lang varchar(10)
);




CREATE TABLE Name ( 
	ID integer DEFAULT NEXTVAL('Name_ID_seq'::TEXT) NOT NULL,
	fk_registryobject integer NOT NULL,
	fk_locstring integer NOT NULL
);




CREATE TABLE ObjectRef ( 
	ID integer DEFAULT NEXTVAL('ObjectRef_ID_seq'::TEXT) NOT NULL,
	iduri varchar(500) NOT NULL,
	home varchar(500),
	createReplica boolean DEFAULT false NOT NULL,
	fk_auditableevent bigint NOT NULL
);




CREATE TABLE RegistryObject ( 
	ID integer DEFAULT NEXTVAL('RegistryObject_ID_seq'::TEXT) NOT NULL,
	type varchar(50) NOT NULL,
	iduri varchar(500) NOT NULL,
	home varchar(500),
	liduri varchar(500),
	objectType varchar(150),
	status varchar(50)
);





CREATE TABLE RegistryPackage ( 
	ID integer DEFAULT NEXTVAL('RegistryPackage_ID_seq'::TEXT) NOT NULL,
	fk_registryobject integer NOT NULL
);




CREATE TABLE Slot ( 
	ID integer DEFAULT NEXTVAL('Slot_ID_seq'::TEXT) NOT NULL,
	name varchar(150) NOT NULL,
	slotType varchar(250)
);




CREATE TABLE SlotValues ( 
	ID integer DEFAULT NEXTVAL('SlotValues_ID_seq'::TEXT) NOT NULL,
	stringvalue varchar(256),
	geomvalue geometry,
	fk_slot integer NOT NULL
);




CREATE TABLE VersionInfo ( 
	ID integer DEFAULT NEXTVAL('VersionInfo_ID_seq'::TEXT) NOT NULL,
	versionName varchar(16),
	comment varchar(500),
	fk_registryobject integer NOT NULL
);


SELECT AddGeometryColumn('', 'slotvalues','geometry', 4326,'POLYGON',2);
ALTER TABLE slotvalues ADD CONSTRAINT slotvalues_geom_check CHECK ( isvalid ( geometry ) );
CREATE INDEX slotvalues_spatial_idx ON slotvalues USING GIST ( geometry GIST_GEOMETRY_OPS );
VACUUM ANALYZE slotvalues;

CREATE SEQUENCE Association_ID_seq INCREMENT 1 START 1;
CREATE SEQUENCE AuditableEvent_ID_seq INCREMENT 1 START 1;
CREATE SEQUENCE Classification_ID_seq INCREMENT 1 START 1;
CREATE SEQUENCE ClassificationNode_ID_seq INCREMENT 1 START 1;
CREATE SEQUENCE ClassificationScheme_ID_seq INCREMENT 1 START 1;
CREATE SEQUENCE Description_ID_seq INCREMENT 1 START 1;
CREATE SEQUENCE ExternalIdentifier_ID_seq INCREMENT 1 START 1;
CREATE SEQUENCE ExtrinsicObject_ID_seq INCREMENT 1 START 1;
CREATE SEQUENCE LINK_RegObj_RegObj_ID_seq INCREMENT 1 START 1;
CREATE SEQUENCE LocalizedString_ID_seq INCREMENT 1 START 1;
CREATE SEQUENCE Name_ID_seq INCREMENT 1 START 1;
CREATE SEQUENCE ObjectRef_ID_seq INCREMENT 1 START 1;
CREATE SEQUENCE RegistryObject_ID_seq INCREMENT 1 START 1;
CREATE SEQUENCE RegistryPackage_ID_seq INCREMENT 1 START 1;
CREATE SEQUENCE Slot_ID_seq INCREMENT 1 START 1;
CREATE SEQUENCE SlotValues_ID_seq INCREMENT 1 START 1;
CREATE SEQUENCE VersionInfo_ID_seq INCREMENT 1 START 1;



ALTER TABLE Association ADD CONSTRAINT PK_Association 
	PRIMARY KEY (ID);



ALTER TABLE AuditableEvent ADD CONSTRAINT PK_AuditableEvent 
	PRIMARY KEY (ID);



ALTER TABLE Classification ADD CONSTRAINT PK_Classification 
	PRIMARY KEY (ID);



ALTER TABLE ClassificationNode ADD CONSTRAINT PK_ClassificationNode 
	PRIMARY KEY (ID);



ALTER TABLE ClassificationScheme ADD CONSTRAINT PK_ClassificationScheme 
	PRIMARY KEY (ID);



ALTER TABLE Description ADD CONSTRAINT PK_JT_NAME_LOCSTRING 
	PRIMARY KEY (ID);



ALTER TABLE ExternalIdentifier ADD CONSTRAINT PK_ExternalIdentifier 
	PRIMARY KEY (ID);



ALTER TABLE ExtrinsicObject ADD CONSTRAINT PK_ExtrinsicObject 
	PRIMARY KEY (ID);



ALTER TABLE LINK_RegObj_RegObj ADD CONSTRAINT PK_JT_RegObj_RegObj 
	PRIMARY KEY (ID);



ALTER TABLE LocalizedString ADD CONSTRAINT PK_LocalizedString 
	PRIMARY KEY (ID);



ALTER TABLE Name ADD CONSTRAINT PK_Name 
	PRIMARY KEY (ID);



ALTER TABLE ObjectRef ADD CONSTRAINT PK_ObjectRef 
	PRIMARY KEY (ID);



ALTER TABLE RegistryObject ADD CONSTRAINT PK_RegistryObject 
	PRIMARY KEY (ID);



ALTER TABLE RegistryPackage ADD CONSTRAINT PK_RegistryPackage 
	PRIMARY KEY (ID);



ALTER TABLE Slot ADD CONSTRAINT PK_Slot 
	PRIMARY KEY (ID);



ALTER TABLE SlotValues ADD CONSTRAINT PK_SlotValues 
	PRIMARY KEY (ID);



ALTER TABLE VersionInfo ADD CONSTRAINT PK_VersionInfo 
	PRIMARY KEY (ID);




ALTER TABLE RegistryObject
	ADD CONSTRAINT UQ_RegistryObject_iduri UNIQUE (iduri);

/*
* ALTER TABLE RegistryObject
*	ADD CONSTRAINT UQ_RegistryObject_liduri UNIQUE (liduri);
*/


