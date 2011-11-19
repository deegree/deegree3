CREATE TABLE IDXTB_MAIN ( 
	id integer NOT NULL,
	version integer,   
	status numeric(1),
	title varchar(500) NOT NULL, -- can occure multiple times, considering multi languages
	alternatetitles varchar(500),
	abstract nvarchar(max) NOT NULL, -- can occure multiple times, considering multi languages
	anytext nvarchar(max),
	fileidentifier varchar(150) NOT NULL,
	modified datetime NOT NULL,
	type varchar(25),
	topicCategories varchar(1000), 
	revisiondate datetime,
	creationdate datetime,
	publicationdate datetime,
	OrganisationName varchar(250),
	HasSecurityConstraints bit,
	Language char(3),
	ResourceId varchar(150),
	ParentId varchar(150),
	ResourceLanguage char(3),
	GeographicDescriptionCode varchar(500),
	Denominator integer,
	DistanceValue decimal(10,2),
	DistanceUOM varchar(10),
	TempExtent_begin datetime,
	TempExtent_end datetime,
	ServiceType varchar(150),
	ServiceTypeVersion varchar(1000),
	CouplingType varchar(10),
	formats varchar(2000),
	Operations varchar(2000),
	degree bit,
	lineage nvarchar(max),
	RespPartyRole varchar(25),
	SpecDate datetime,
	SpecDateType varchar(15),
	SpecTitle varchar(500),
	bbox geometry,
	recordfull varbinary(max) NOT NULL
);
CREATE TABLE IDXTB_Constraint ( 
	id integer NOT NULL,
	fk_main integer NOT NULL,
	ConditionAppToAcc nvarchar(max),
	AccessConstraints varchar(30),
	OtherConstraints nvarchar(max),
	Classification varchar(20)
);
CREATE TABLE IDXTB_CRS ( 
	id integer NOT NULL,
	fk_main integer NOT NULL,
	authority varchar(500),
	crsid varchar(500) NOT NULL,
	version varchar(50)
);
CREATE TABLE IDXTB_KEYWORD ( 
	id integer NOT NULL,
	fk_main integer NOT NULL,
	keywords nvarchar(max) NOT NULL,
	keywordtype varchar(250)
);
CREATE TABLE IDXTB_OperatesOnData ( 
	id integer NOT NULL,
	fk_main integer NOT NULL,
	OperatesOn varchar(150),
	OperatesOnId varchar(150) NOT NULL,
	OperatesOnName varchar(150) NOT NULL
);

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
--CREATE INDEX abstract_idx ON IDXTB_MAIN (abstract);
--CREATE INDEX anytext_idx ON IDXTB_MAIN (anytext);
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
--CREATE INDEX lineage_idx ON IDXTB_MAIN (lineage);
CREATE INDEX RespPartyRole_idx ON IDXTB_MAIN (RespPartyRole);
CREATE INDEX SpecDate_idx ON IDXTB_MAIN (SpecDate);
CREATE INDEX SpecDateType_idx ON IDXTB_MAIN (SpecDateType);
CREATE INDEX SpecTitle_idx ON IDXTB_MAIN (SpecTitle);
CREATE INDEX formats_idx ON IDXTB_MAIN (formats);
CREATE INDEX Operations_idx ON IDXTB_MAIN (Operations);
-- IDXTB_Constraint
CREATE INDEX fk_main_constraint_idx ON IDXTB_Constraint (fk_main);
--CREATE INDEX ConditionAppToAcc_idx ON IDXTB_Constraint (ConditionAppToAcc);
CREATE INDEX AccessConstraints_idx ON IDXTB_Constraint (AccessConstraints);
--CREATE INDEX OtherConstraints_idx ON IDXTB_Constraint (OtherConstraints);
CREATE INDEX Classification_idx ON IDXTB_Constraint (Classification);
-- IDXTB_CRS
CREATE INDEX fk_main_crs_idx ON IDXTB_CRS (fk_main);
CREATE INDEX crsid_idx ON IDXTB_CRS (crsid);
CREATE INDEX authority_idx ON IDXTB_CRS (authority);
CREATE INDEX version_idx ON IDXTB_CRS (version);
-- IDXTB_KEYWORD
CREATE INDEX fk_main_keyword_idx ON IDXTB_KEYWORD (fk_main);
--CREATE INDEX keywords_idx ON IDXTB_KEYWORD (keywords);
CREATE INDEX keywordtype_idx ON IDXTB_KEYWORD (keywordtype);
-- IDXTB_OperatesOnData
CREATE INDEX fk_main_OperatesOnData_idx ON IDXTB_OperatesOnData (fk_main);
CREATE INDEX OperatesOnId_idx ON IDXTB_OperatesOnData (OperatesOnId);
CREATE INDEX OperatesOnName_idx ON IDXTB_OperatesOnData (OperatesOnName);
CREATE INDEX OperatesOn_idx ON IDXTB_OperatesOnData (OperatesOn);
CREATE SPATIAL INDEX [bbox_spx] ON [dbo].[IDXTB_MAIN] ([bbox] )USING  GEOMETRY_GRID WITH (BOUNDING_BOX =(-180, -90, 180, 90), GRIDS =(LEVEL_1 = MEDIUM,LEVEL_2 = MEDIUM,LEVEL_3 = MEDIUM,LEVEL_4 = MEDIUM), CELLS_PER_OBJECT = 16, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON);
