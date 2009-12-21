SELECT GeomFromText(AsText('POINT(1.0 1.0)'))
SELECT GeomFromText(AsText('POINT(4.0 8.0)'))
SELECT GeomFromText(AsText('POINT(4.0 9.0)'))


SELECT GeomFromText(AsText('LINESTRING(4.0 4.0, 4.0 4.0, 5.0 5.0)'))

SELECT GeomFromText(AsText('POLYGON((0.0 0.0, 2.0 0.0, 2.0 2.0, 0.0 2.0, 0.0 0.0))'))
				    
SELECT GeomFromText(AsText('POLYGON((0.0 0.0, 2.0 0.0, 2.0 2.0, 0.0 2.0, 0.0 0.0),
				    (0.5 0.5, 1.5 0.5, 1.5 1.5, 0.5 1.5, 0.5 0.5))'))
