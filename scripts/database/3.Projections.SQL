CREATE TABLE projection_lookup ( 
	id INTEGER NOT NULL,
	table_name VARCHAR(50) NOT NULL
)
;

CREATE TABLE lambert_conformal_conic ( 
	id INTEGER NOT NULL,
	natural_origin_lat DOUBLE NOT NULL,
	natural_origin_long DOUBLE NOT NULL,
	scale DOUBLE,
	false_easting DOUBLE NOT NULL,
	false_northing DOUBLE NOT NULL,
	firstParallelLatitude DOUBLE,
	secondParallelLatitude DOUBLE,
	units VARCHAR(50) NOT NULL,
	geographic_crs_id INTEGER NOT NULL
)
;

CREATE TABLE stereographic_azimuthal ( 
	id INTEGER NOT NULL,
	natural_origin_lat DOUBLE NOT NULL,
	natural_origin_long DOUBLE NOT NULL,
	scale DOUBLE,
	false_easting DOUBLE NOT NULL,
	false_northing DOUBLE NOT NULL,
	true_scale_latitude DOUBLE,
	geographic_crs_id INTEGER,
	units VARCHAR(50) NOT NULL
)
;

CREATE TABLE transverse_mercator ( 
	id INTEGER NOT NULL,
	natural_origin_lat DOUBLE,
	natural_origin_long DOUBLE,
	scale DOUBLE,
	false_easting DOUBLE,
	false_northing DOUBLE,
	northern_hemisphere SMALLINT,
	geographic_crs_id INTEGER NOT NULL,
	units VARCHAR(50) NOT NULL
)
;

CREATE TABLE mercator (
  id INTEGER NOT NULL,
	natural_origin_lat DOUBLE,
	natural_origin_long DOUBLE,
	scale DOUBLE,
	false_easting DOUBLE,
	false_northing DOUBLE,
	geographic_crs_id INTEGER NOT NULL,
  units VARCHAR(50) NOT NULL
)
;

CREATE TABLE stereographic_alternative ( 
	id INTEGER NOT NULL,
	natural_origin_lat DOUBLE NOT NULL,
	natural_origin_long DOUBLE NOT NULL,
	scale DOUBLE NOT NULL,
	false_easting DOUBLE NOT NULL,
	false_northing DOUBLE NOT NULL,
	geographic_crs_id INTEGER NOT NULL,
	units VARCHAR(50) NOT NULL
)
;

CREATE TABLE lambert_azimuthal_equal_area ( 
	id INTEGER NOT NULL,
	natural_origin_lat DOUBLE NOT NULL,
	natural_origin_long DOUBLE NOT NULL,
	scale DOUBLE,
	false_easting DOUBLE NOT NULL,
	false_northing DOUBLE NOT NULL,
	geographic_crs_id INTEGER NOT NULL,
	units VARCHAR(50) NOT NULL
)
;

CREATE TABLE custom_projection ( 
	id INTEGER NOT NULL,
	natural_origin_lat DOUBLE NOT NULL,
	natural_origin_long DOUBLE NOT NULL,
	scale DOUBLE,
	false_easting DOUBLE NOT NULL,
	false_northing DOUBLE NOT NULL,
	geographic_crs_id INTEGER NOT NULL,
	units VARCHAR(50) NOT NULL,
	class VARCHAR(100) NOT NULL
)
;

ALTER TABLE custom_projection ADD CONSTRAINT PK_custom_projection
	PRIMARY KEY (id);


CREATE TABLE geographic_crs ( 
	id INTEGER NOT NULL,
	axis1_id INTEGER,
	axis2_id INTEGER,
	datum_id INTEGER NOT NULL
)
;

ALTER TABLE geographic_crs ADD CONSTRAINT PK_geographic_crs 
	PRIMARY KEY (id)
;



ALTER TABLE projection_lookup ADD CONSTRAINT PK_projection_lookup 
	PRIMARY KEY (id)
;


ALTER TABLE lambert_conformal_conic ADD CONSTRAINT PK_lambert_conformal_conic 
	PRIMARY KEY (id)
;


ALTER TABLE stereographic_azimuthal ADD CONSTRAINT PK_stereographic_azimuthal 
	PRIMARY KEY (id)
;


ALTER TABLE transverse_mercator ADD CONSTRAINT PK_transverse_mercator 
	PRIMARY KEY (id)
;


ALTER TABLE mercator ADD CONSTRAINT PK_mercator 
	PRIMARY KEY (id)
;

ALTER TABLE stereographic_alternative ADD CONSTRAINT PK_stereographic_alternative 
	PRIMARY KEY (id)
;


ALTER TABLE lambert_azimuthal_equal_area ADD CONSTRAINT PK_lambert_azimuthal_equal_area 
	PRIMARY KEY (id)
;



ALTER TABLE lambert_conformal_conic ADD CONSTRAINT FK_lambert_conformal_conic_geographic_crs 
	FOREIGN KEY (geographic_crs_id) REFERENCES geographic_crs (id)
;

ALTER TABLE lambert_conformal_conic ADD CONSTRAINT FK_lambert_conformal_conic_projection_lookup 
	FOREIGN KEY (id) REFERENCES projection_lookup (id)
;

ALTER TABLE stereographic_azimuthal ADD CONSTRAINT FK_stereographic_azimuthal_geographic_crs 
	FOREIGN KEY (geographic_crs_id) REFERENCES geographic_crs (id)
;

ALTER TABLE stereographic_azimuthal ADD CONSTRAINT FK_stereographic_azimuthal_projection_lookup 
	FOREIGN KEY (id) REFERENCES projection_lookup (id)
;

ALTER TABLE transverse_mercator ADD CONSTRAINT FK_transverse_mercator_geographic_crs 
	FOREIGN KEY (geographic_crs_id) REFERENCES geographic_crs (id)
;

ALTER TABLE transverse_mercator ADD CONSTRAINT FK_transverse_mercator_projection_lookup 
	FOREIGN KEY (id) REFERENCES projection_lookup (id)
;

ALTER TABLE mercator ADD CONSTRAINT FK_mercator_geographic_crs 
	FOREIGN KEY (geographic_crs_id) REFERENCES geographic_crs (id)
;

ALTER TABLE mercator ADD CONSTRAINT FK_mercator_projection_lookup 
	FOREIGN KEY (id) REFERENCES projection_lookup (id)
;

ALTER TABLE stereographic_alternative ADD CONSTRAINT FK_stereographic_alternative_geographic_crs 
	FOREIGN KEY (geographic_crs_id) REFERENCES geographic_crs (id)
;

ALTER TABLE stereographic_alternative ADD CONSTRAINT FK_stereographic_alternative_projection_lookup 
	FOREIGN KEY (id) REFERENCES projection_lookup (id)
;

ALTER TABLE lambert_azimuthal_equal_area ADD CONSTRAINT FK_lambert_azimuthal_equal_area_geographic_crs 
	FOREIGN KEY (geographic_crs_id) REFERENCES geographic_crs (id)
;

ALTER TABLE lambert_azimuthal_equal_area ADD CONSTRAINT FK_lambert_azimuthal_equal_area_projection_lookup 
	FOREIGN KEY (id) REFERENCES projection_lookup (id)
;

ALTER TABLE custom_projection ADD CONSTRAINT FK_custom_projection_projection_lookup
	FOREIGN KEY (id) REFERENCES projection_lookup (id);
