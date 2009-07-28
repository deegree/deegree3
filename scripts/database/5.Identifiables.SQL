CREATE TABLE area ( 
	ref_id INTEGER NOT NULL,
	area_of_use VARCHAR(50) NOT NULL
)
;

CREATE TABLE description ( 
	ref_id INTEGER NOT NULL,
	description VARCHAR(255) NOT NULL
)
;

CREATE TABLE name ( 
	ref_id INTEGER NOT NULL,
	name VARCHAR(255) NOT NULL
)
;

CREATE TABLE version ( 
	ref_id INTEGER NOT NULL,
	version VARCHAR(50) NOT NULL
)
;

CREATE TABLE code ( 
	ref_id INTEGER NOT NULL,
	code VARCHAR(50) NOT NULL,
	codespace VARCHAR(50),
	codeversion VARCHAR(50),
	original VARCHAR(50)
)
;

CREATE VIEW identifiable_lookup (id, table_name) AS SELECT id, table_name FROM crs_lookup UNION
									SELECT id, table_name FROM transformation_lookup UNION
									SELECT id, table_name FROM projection_lookup UNION
									SELECT id, 'axis' as table_name FROM axis UNION
									SELECT id, 'ellipsoid' as table_name FROM ellipsoid UNION
									SELECT id, 'geodetic_datum' as table_name FROM geodetic_datum UNION
									SELECT id, 'prime_meridian' as table_name FROM prime_meridian;
