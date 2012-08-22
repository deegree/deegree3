-- This file is part of deegree, for copyright/license information, please visit http://www.deegree.org/license.
-- load data of Counties from import table
-- With this step double entrys will be removed and geometries will be merged


--   adm1
insert into municipalities (geographicIdentifier, parent) select name, county from imp_municipalities group by name, county order by name;

-- merge the geometries
update municipalities set geographicextent = (select multi(geomunion(the_geom)) from imp_municipalities where municipalities.geographicidentifier = imp_municipalities.name);
--  calculate cetroid for position and lat lon boundingboxes 
update municipalities 
set position = setSRID(PointOnSurface((geographicextent)), 26912),
westboundlongitude = xmin(transform((geographicextent), 4326)),
eastboundlongitude = xmax(transform((geographicextent), 4326)),
southboundlatitude = ymin(transform((geographicextent), 4326)),
northboundlatitude = ymax(transform((geographicextent), 4326));

