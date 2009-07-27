CREATE TABLE vertical_crs ( 
	id INTEGER NOT NULL,
	axis_id INTEGER,
	vertical_datum_id INTEGER
)
;

CREATE TABLE crs_lookup ( 
	id INTEGER NOT NULL,
	table_name VARCHAR(50) NOT NULL
)
;

CREATE TABLE axis ( 
	id INTEGER NOT NULL,
	name VARCHAR(50) NOT NULL,
	units VARCHAR(50) NOT NULL,
	axis_orientation VARCHAR(50) NOT NULL
)
;

CREATE TABLE compound_crs ( 
	id INTEGER NOT NULL,
	base_crs INTEGER NOT NULL,
	height_axis_id INTEGER,
	default_height DOUBLE DEFAULT 0
)
;

CREATE TABLE geocentric_crs ( 
	id INTEGER NOT NULL,
	axis1_id INTEGER,
	axis2_id INTEGER,
	axis3_id INTEGER,
	datum_id INTEGER NOT NULL
)
;


CREATE TABLE projected_crs ( 
	id INTEGER NOT NULL,
	axis1_id INTEGER,
	axis2_id INTEGER,
	projection_id INTEGER
)
;


ALTER TABLE vertical_crs ADD CONSTRAINT PK_vertical_crs 
	PRIMARY KEY (id)
;


ALTER TABLE crs_lookup ADD CONSTRAINT PK_crs_lookup 
	PRIMARY KEY (id)
;


ALTER TABLE axis ADD CONSTRAINT PK_axis 
	PRIMARY KEY (id)
;


ALTER TABLE compound_crs ADD CONSTRAINT PK_compound_crs 
	PRIMARY KEY (id)
;


ALTER TABLE geocentric_crs ADD CONSTRAINT PK_geocentric_crs 
	PRIMARY KEY (id)
;



ALTER TABLE projected_crs ADD CONSTRAINT PK_projected_crs 
	PRIMARY KEY (id)
;




ALTER TABLE vertical_crs ADD CONSTRAINT FK_vertical_crs_axis 
	FOREIGN KEY (axis_id) REFERENCES axis (id)
;

ALTER TABLE vertical_crs ADD CONSTRAINT FK_vertical_crs_crs_lookup 
	FOREIGN KEY (id) REFERENCES crs_lookup (id)
;

ALTER TABLE vertical_crs ADD CONSTRAINT FK_vertical_crs_vertical_datum 
	FOREIGN KEY (vertical_datum_id) REFERENCES vertical_datum (id)
;

ALTER TABLE compound_crs ADD CONSTRAINT FK_compound_crs_crs_lookup1 
	FOREIGN KEY (id) REFERENCES crs_lookup (id)
;

ALTER TABLE compound_crs ADD CONSTRAINT FK_compound_crs_crs_lookup2 
	FOREIGN KEY (base_crs) REFERENCES crs_lookup (id)
;


ALTER TABLE geocentric_crs ADD CONSTRAINT FK_geocentric_crs_axis1 
	FOREIGN KEY (axis1_id) REFERENCES axis (id)
;

ALTER TABLE geocentric_crs ADD CONSTRAINT FK_geocentric_crs_axis2 
	FOREIGN KEY (axis2_id) REFERENCES axis (id)
;

ALTER TABLE geocentric_crs ADD CONSTRAINT FK_geocentric_crs_axis3 
	FOREIGN KEY (axis3_id) REFERENCES axis (id)
;


ALTER TABLE geocentric_crs ADD CONSTRAINT FK_geocentric_crs_crs_lookup 
	FOREIGN KEY (id) REFERENCES crs_lookup (id)
;

ALTER TABLE geocentric_crs ADD CONSTRAINT FK_geocentric_crs_geodetic_datum 
	FOREIGN KEY (datum_id) REFERENCES geodetic_datum (id)
;

ALTER TABLE geographic_crs ADD CONSTRAINT FK_geographic_crs_axis1 
	FOREIGN KEY (axis1_id) REFERENCES axis (id)
;

ALTER TABLE geographic_crs ADD CONSTRAINT FK_geographic_crs_axis2 
	FOREIGN KEY (axis2_id) REFERENCES axis (id)
;

ALTER TABLE geographic_crs ADD CONSTRAINT FK_geographic_crs_crs_lookup 
	FOREIGN KEY (id) REFERENCES crs_lookup (id)
;

ALTER TABLE geographic_crs ADD CONSTRAINT FK_geographic_crs_geodetic_datum 
	FOREIGN KEY (datum_id) REFERENCES geodetic_datum (id)
;

ALTER TABLE projected_crs ADD CONSTRAINT FK_projected_crs_axis1 
	FOREIGN KEY (axis1_id) REFERENCES axis (id)
;

ALTER TABLE projected_crs ADD CONSTRAINT FK_projected_crs_axis2 
	FOREIGN KEY (axis2_id) REFERENCES axis (id)
;

ALTER TABLE projected_crs ADD CONSTRAINT FK_projected_crs_crs_lookup 
	FOREIGN KEY (id) REFERENCES crs_lookup (id)
;

ALTER TABLE projected_crs ADD CONSTRAINT FK_projected_crs_projection_lookup 
	FOREIGN KEY (projection_id) REFERENCES projection_lookup (id)
;
