-- This file is part of deegree, for copyright/license information, please visit http://www.deegree.org/license.
select DropGeometryTable ('counties');

create table counties (
	gid serial PRIMARY KEY,
	geographicIdentifier varchar(255),
	locationtype int4 DEFAULT 2,
	westBoundLongitude double precision,
	eastBoundLongitude double precision,
	southBoundLatitude double precision,
	northBoundLatitude double precision
);
select AddGeometryColumn('counties', 'geographicextent', 26912, 'MULTIPOLYGON', 2);
select AddGeometryColumn('counties', 'position', 26912, 'POINT', 2);


