-- This file is part of deegree, for copyright/license information, please visit http://www.deegree.org/license.
-- This script creates a database table for the digitizer.
select DropGeometryTable ('digitizefeatures');

create table digitizefeatures (
	gid serial PRIMARY KEY,
	geographicIdentifier varchar,
        comment varchar);
select AddGeometryColumn('digitizefeatures', 'geometry', 26912, 'POLYGON', 2);

ALTER TABLE digitizefeatures OWNER TO deegreetest;