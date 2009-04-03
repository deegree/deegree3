CREATE TABLE transformation_lookup ( 
	id INTEGER NOT NULL,
	table_name VARCHAR(50)
)
;

CREATE TABLE least_square_transformation ( 
	id INTEGER NOT NULL,
	source_crs_id INTEGER,
	target_crs_id INTEGER,
	polynomial_order SMALLINT,
	x_parameters VARCHAR(50),
	y_parameter VARCHAR(50),
	scale_x DOUBLE NOT NULL,
	scale_y DOUBLE NOT NULL,
	class VARCHAR(50)
)
;

CREATE TABLE polynomial_transformation ( 
	id INTEGER NOT NULL,
	source_crs_id INTEGER,
	target_crs_id INTEGER,
	polynomial_order SMALLINT,
	x_parameters VARCHAR(50),
	y_parameters VARCHAR(50),
	class VARCHAR(50)
)
;

CREATE TABLE helmert_transformation ( 
	id INTEGER NOT NULL,
	source_crs_id INTEGER NOT NULL,
	target_crs_id INTEGER NOT NULL,
	x_axis_translation DOUBLE,
	y_axis_translation DOUBLE,
	z_axis_translation DOUBLE,
	x_axis_rotation DOUBLE,
	y_axis_rotation DOUBLE,
	z_axis_rotation DOUBLE,
	scale_difference DOUBLE
)
;

ALTER TABLE transformation_lookup ADD CONSTRAINT PK_transformation_lookup 
	PRIMARY KEY (id)
;



ALTER TABLE least_square_transformation ADD CONSTRAINT PK_least_square_transformation 
	PRIMARY KEY (id)
;


ALTER TABLE polynomial_transformation ADD CONSTRAINT PK_polynomial_transformation 
	PRIMARY KEY (id)
;


ALTER TABLE helmert_transformation ADD CONSTRAINT PK_wgs84transformation 
	PRIMARY KEY (id)
;




ALTER TABLE least_square_transformation ADD CONSTRAINT FK_least_square_transformation_transformation_lookup 
	FOREIGN KEY (id) REFERENCES transformation_lookup (id)
;

ALTER TABLE polynomial_transformation ADD CONSTRAINT FK_polynomial_transformation_transformation_lookup 
	FOREIGN KEY (id) REFERENCES transformation_lookup (id)
;

ALTER TABLE helmert_transformation ADD CONSTRAINT FK_helmert_transformation_transformation_lookup 
	FOREIGN KEY (id) REFERENCES transformation_lookup (id)
;

