drop table $TABLE$_pyr;
drop table $TABLE$;
drop SEQUENCE $TABLE$_ID_seq ;
drop SEQUENCE $TABLE$_pyr_ID_seq ;

create table $TABLE$_pyr (
	ID integer DEFAULT NEXTVAL('$TABLE$_pyr_ID_seq'::TEXT),
	level integer,
	minscale float,
	maxscale float
);


create table $TABLE$ (
	ID integer DEFAULT NEXTVAL('$TABLE$_ID_seq'::TEXT),
	level integer,
	dir varchar(256),
	file varchar(256)
);
SELECT AddGeometryColumn('', '$TABLE$','bbox',-1,'POLYGON',2);
CREATE SEQUENCE $TABLE$_pyr_ID_seq ;
CREATE SEQUENCE $TABLE$_ID_seq ;