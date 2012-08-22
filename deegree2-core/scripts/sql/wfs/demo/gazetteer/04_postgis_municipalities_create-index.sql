-- This file is part of deegree, for copyright/license information, please visit http://www.deegree.org/license.
-- create indexes
create index idx_municipalities_geographicidentifier on municipalities (geographicidentifier);
create index idx_municipalities_parent on municipalities (parent);
create index idx_municipalities_northboundlatitude on municipalities (northboundlatitude);
create index idx_municipalities_southboundlatitude on municipalities (southboundlatitude);
create index idx_municipalities_eastboundlongitude on municipalities (eastboundlongitude);
create index idx_municipalities_westboundlongitude on municipalities (westboundlongitude);


-- create spatial indexes
create index sdx_municipalities_pos on municipalities using GIST (position GIST_GEOMETRY_OPS);
create index sdx_municipalities_ext on municipalities using GIST (geographicextent GIST_GEOMETRY_OPS);

vacuum analyse municipalities;
