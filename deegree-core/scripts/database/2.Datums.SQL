CREATE TABLE vertical_datum ( 
	id INTEGER NOT NULL,
	anchor_point DOUBLE
)
;

CREATE TABLE prime_meridian ( 
	id INTEGER NOT NULL,
	unit VARCHAR(50) NOT NULL,
	longitude DOUBLE
)
;

CREATE TABLE geodetic_datum ( 
	id INTEGER NOT NULL,
	ellipsoid_id INTEGER NOT NULL,
	prime_meridian_id INTEGER,
	helmert_id INTEGER
)
;

CREATE TABLE ellipsoid ( 
	id INTEGER NOT NULL,
	semi_major_axis DOUBLE NOT NULL,
	eccentricity DOUBLE,
	inverse_flattening DOUBLE,
	semi_minor_axis DOUBLE,
	unit VARCHAR(20) NOT NULL
)
;



ALTER TABLE vertical_datum ADD CONSTRAINT PK_vertical_datum 
	PRIMARY KEY (id)
;


ALTER TABLE prime_meridian ADD CONSTRAINT PK_prime_meridian 
	PRIMARY KEY (id)
;


ALTER TABLE geodetic_datum ADD CONSTRAINT PK_geodetic_datum 
	PRIMARY KEY (id)
;


ALTER TABLE ellipsoid ADD CONSTRAINT PK_ellipsoid 
	PRIMARY KEY (id)
;



ALTER TABLE geodetic_datum ADD CONSTRAINT FK_geodetic_datum_ellipsoid 
	FOREIGN KEY (ellipsoid_id) REFERENCES ellipsoid (id)
;

ALTER TABLE geodetic_datum ADD CONSTRAINT FK_geodetic_datum_helmert_transformation 
	FOREIGN KEY (helmert_id) REFERENCES helmert_transformation (id)
;

ALTER TABLE geodetic_datum ADD CONSTRAINT FK_geodetic_datum_transformation_lookup 
	FOREIGN KEY (helmert_id) REFERENCES transformation_lookup (id)
;

ALTER TABLE geodetic_datum ADD CONSTRAINT FK_geodetic_datum_prime_meridian 
	FOREIGN KEY (prime_meridian_id) REFERENCES prime_meridian (id)
;
