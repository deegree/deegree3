-- This file is part of deegree, for copyright/license information, please visit http://www.deegree.org/license.
-- load data of Counties from import table
-- With this step double entrys will be removed and geometries will be merged

insert into counties (geographicIdentifier) select name from imp_counties group by name order by name;

-- merge the geometries 
update counties set geographicextent = (select multi(geomunion(the_geom)) from imp_counties where counties.geographicidentifier = imp_counties.name);
--  calculate cetroid for position and lat lon boundingboxes 
update counties 
set position = setSRID(PointOnSurface((geographicextent)), 26912),
westboundlongitude = xmin(transform((geographicextent), 4326)),
eastboundlongitude = xmax(transform((geographicextent), 4326)),
southboundlatitude = ymin(transform((geographicextent), 4326)),
northboundlatitude = ymax(transform((geographicextent), 4326));