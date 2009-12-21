SELECT asText(GeomFromtext('MULTICURVE((0 0, 5 5),CIRCULARSTRING(4 0, 4 4, 8 4))'));
 
SELECT ST_GeomFromEWKT('CURVEPOLYGON(CIRCULARSTRING(0 0,2 0,
 2 1, 2 3, 0 0),(4 3, 4 5, 1 4, 4 3))');

SELECT AsText(GeomFromEWKT('CIRCULARSTRING(0 0,1 1,1 0, 2 2, 9 9)
'));

SELECT AsText(ST_GeomFromEWKT('COMPOUNDCURVE(CIRCULARSTRING(0 0,4 0,4 4,0 4,1 1),(1 1,3 3,3 1,0 0))
'));

SELECT AsText(ST_GeomFromEWKT(SetSRID('BOX3D(54 80,52.0 78)'::box3d,4326)));
  

--not possible in 1.3; but possible in 1.4
SELECT ST_GeomFromEWKT('CURVEPOLYGON(
				COMPOUNDCURVE(
					CIRCULARSTRING(0 0,2 0, 2 1, 2 3, 4 3),(4 3, 4 5, 1 4, 0 0)
				), 
					CIRCULARSTRING(1.7 1, 1.4 0.4, 1.6 0.4, 1.6 0.5, 1.7 1) 
			) ');




DROP TABLE geometrytest;
CREATE TABLE geometrytest(
ID integer NOT NULL);
SELECT AddGeometryColumn('public','geometrytest','circularstring','-1','GEOMETRY','2');


INSERT INTO geometrytest VALUES (1,GeomFromText(
'MULTICURVE((0 0, 5 5),CIRCULARSTRING(4 0, 4 4, 8 4))'));

select  *, astext(circularstring) 
from geometrytest

INSERT INTO geometrytest VALUES (1,GeomFromEWKT(
'POLYGON((-180.0 -90.0, 180 -90, 0 80.0, -180 90, -180 -90))'));

INSERT INTO geometrytest VALUES (1,GeomFromWKB(
asBinary('MULTICURVE((0 0, 5 5),CIRCULARSTRING(4 0, 4 4, 8 4))'),-1));

INSERT INTO geometrytest VALUES (1,GeomFromEWKB(
asBinary('POLYGON((-180.0 -90.0, 180 -90, 0 80.0, -180 90, -180 -90))'),-1));
