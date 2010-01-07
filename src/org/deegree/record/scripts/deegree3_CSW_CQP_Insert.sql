DROP TABLE geometrytest;
CREATE TABLE geometrytest(
ID integer NOT NULL);
SELECT AddGeometryColumn('public','geometrytest','circularstring','-1','GEOMETRY','2');

INSERT INTO geometrytest VALUES (1,GeomFromEWKT(
'POLYGON((-180.0 -90.0, 180 -90, 0 80.0, -180 90, -180 -90))'));

-----
--some definitions additional to the database schema
--source
ALTER TABLE datasets ADD COLUMN source character varying(250);
ALTER TABLE datasets ALTER COLUMN source SET STORAGE EXTENDED;
COMMENT ON COLUMN datasets.source IS 'common queryable property in DC, but is not supported in ISO AP';

--creator
--ALTER TABLE datasets ADD COLUMN creator character varying(50);
--ALTER TABLE datasets ALTER COLUMN creator SET STORAGE EXTENDED;
--COMMENT ON COLUMN datasets.creator IS 'common queryable property in DC, but is not supported in ISO AP';

--publisher
--ALTER TABLE datasets ADD COLUMN publisher character varying(50);
--ALTER TABLE datasets ALTER COLUMN publisher SET STORAGE EXTENDED;
--COMMENT ON COLUMN datasets.publisher IS 'common queryable property in DC, but is not supported in ISO AP';

--contributor
--ALTER TABLE datasets ADD COLUMN contributor character varying(50);
--ALTER TABLE datasets ALTER COLUMN contributor SET STORAGE EXTENDED;
--COMMENT ON COLUMN datasets.contributor IS 'common queryable property in DC, but is not supported in ISO AP';

--association
ALTER TABLE datasets ADD COLUMN association character varying(50);
ALTER TABLE datasets ALTER COLUMN association SET STORAGE EXTENDED;
COMMENT ON COLUMN datasets.association IS 'common queryable property in DC, but is not supported in ISO AP';


--abstract
ALTER TABLE isoqp_abstract DROP COLUMN Abstract;
ALTER TABLE isoqp_abstract ADD COLUMN Abstract text;

--polygon
SELECT AddGeometryColumn('public','isoqp_boundingbox','bbox','4326','POLYGON','2');

--for dropping the geometrycolumn
--SELECT DropGeometryColumn ('public','isoqp_boundingbox','bbox');


--rights angelegt in deegree3_CSW.sql

-----

--is needed because of FK Constraint, isn't it??
INSERT INTO userdefinedqueryableproperties VALUES (1);
INSERT INTO userdefinedqueryableproperties VALUES (2);
INSERT INTO userdefinedqueryableproperties VALUES (3);
INSERT INTO userdefinedqueryableproperties VALUES (4);
INSERT INTO userdefinedqueryableproperties VALUES (5);
INSERT INTO userdefinedqueryableproperties VALUES (6);




--basic table 
--INSERT INTO datasets VALUES (1,null,null,'','e4076086-cfe6-42ae-a6f7-b4d9ead9cbf5',null,FALSE,'eng','','The linework of the map is obtained by --delineating drainage basin boundaries from an hydrologically corrected digital elevation model with a resolution of 1 * 1 km.', null, null, null, --null);

--INSERT INTO datasets VALUES (2,null,null,'','a5b42291-8425-40c5-a09c-26152fe2af22',null,FALSE,null,'',null, null, null,null, null);

--INSERT INTO datasets VALUES (3,null,null,'','ddc55084-d67f-45a9-8665-0b63f8aaf1b0',null,FALSE,'en','','None.', null,null, null, null);

--INSERT INTO datasets VALUES (4,null,null,'','6387e01d-bb6d-4876-96d5-f5f0e07dc345','2005-03-31T19:13:30'::timestamp,FALSE,'en','','Compiled from published vegetation maps of the 8 circumpolar countries.', 'GeoNetwork test user', 'GeoNetwork test centre', 'Unknown', 'Unknown');

--INSERT INTO datasets VALUES (5,null,null,'','','20011220'::timestamp,FALSE,null,'',null, 'GeoNetwork test user','GeoNetwork test centre', null, null);

--INSERT INTO datasets VALUES (6,null,null,'','9b41ce80-1cfe-403e-8514-a072a8c8394e','2009-10-06T09:44:50'::timestamp,FALSE,'german','','should be the source', 'Thomas','any man', 'company', null);



INSERT INTO datasets VALUES (1,null,null,'','e4076086-cfe6-42ae-a6f7-b4d9ead9cbf5',null,FALSE,'eng','', 'The linework of the map is obtained by --delineating drainage basin boundaries from an hydrologically corrected digital elevation model with a resolution of 1 * 1 km.', null);

INSERT INTO datasets VALUES (2,null,null,'','a5b42291-8425-40c5-a09c-26152fe2af22',null,FALSE,null,'', null, null);

INSERT INTO datasets VALUES (3,null,null,'','ddc55084-d67f-45a9-8665-0b63f8aaf1b0',null,FALSE,'en','', 'None.', null);

INSERT INTO datasets VALUES (4,null,null,'','6387e01d-bb6d-4876-96d5-f5f0e07dc345','2005-03-31T19:13:30'::timestamp,FALSE,'en','', 'Compiled from published vegetation maps of the 8 circumpolar countries.', 'Unknown');

INSERT INTO datasets VALUES (5,null,null,'','','20011220'::timestamp,FALSE,null,'', null, null);

INSERT INTO datasets VALUES (6,null,null,'','9b41ce80-1cfe-403e-8514-a072a8c8394e','2009-10-06T09:44:50'::timestamp,FALSE,'german','', 'should be the source', null);


--title
INSERT INTO isoqp_title VALUES (1,1,'Hydrological Basins in Africa (Sample record, please remove!)');
INSERT INTO isoqp_title VALUES (2,2,'Physiographic Map of North and Central Eurasia (Sample record, please remove!)');
INSERT INTO isoqp_title VALUES (3,3,'Hydrological basins in Europe');
INSERT INTO isoqp_title VALUES (4,4,'Natural polar ecosystems');
INSERT INTO isoqp_title VALUES (5,5,'Globally threatened species of the world');
INSERT INTO isoqp_title VALUES (6,6,'Template for Dublin Core');

--subject
--what's with keywords in this context??
INSERT INTO isoqp_topiccategory VALUES (1,1,'watersheds');
INSERT INTO isoqp_topiccategory VALUES (2,1,'river basins');
INSERT INTO isoqp_topiccategory VALUES (3,1,'water resources');
INSERT INTO isoqp_topiccategory VALUES (4,1,'hydrology');
INSERT INTO isoqp_topiccategory VALUES (5,1,'AQUASTAT');
INSERT INTO isoqp_topiccategory VALUES (6,1,'AWRD');
INSERT INTO isoqp_topiccategory VALUES (7,1,'Africa');

INSERT INTO isoqp_topiccategory VALUES (8,2,'physiography, soil');
INSERT INTO isoqp_topiccategory VALUES (9,2,'Eurasia');

INSERT INTO isoqp_topiccategory VALUES (10,3,'watersheds');
INSERT INTO isoqp_topiccategory VALUES (11,3,'Europe');

INSERT INTO isoqp_topiccategory VALUES (12,4,'Antarctic ecosystem');
INSERT INTO isoqp_topiccategory VALUES (13,4,'Arctic ecosystem');
INSERT INTO isoqp_topiccategory VALUES (14,4,'polar ecosystem');

INSERT INTO isoqp_topiccategory VALUES (15,4,'GEMET 2000 biodiversity');-- endangered animal species endangered plant species');
INSERT INTO isoqp_topiccategory VALUES (16,4,'None Biology');
INSERT INTO isoqp_topiccategory VALUES (17,4,'None Global');

INSERT INTO isoqp_topiccategory VALUES (18,6,'DC');

--format
INSERT INTO isoqp_format VALUES (1,1,'ShapeFile');
INSERT INTO isoqp_format VALUES (2,4,'Web page');
INSERT INTO isoqp_format VALUES (3,6,'anything of the format');

--abstract
INSERT INTO isoqp_abstract VALUES (1,1,'Major hydrological basins and their sub-basins. This dataset divides the African continent according to its hydrological characteristics.
The dataset consists of the following information:- numerical code and name of the major basin (MAJ_BAS and MAJ_NAME); - area of the major basin in square km (MAJ_AREA); - numerical code and name of the sub-basin (SUB_BAS and SUB_NAME); - area of the sub-basin in square km (SUB_AREA); - numerical code of the sub-basin towards which the sub-basin flows (TO_SUBBAS) (the codes -888 and -999 have been assigned respectively to internal sub-basins and to sub-basins draining into the sea)');

INSERT INTO isoqp_abstract VALUES (2,2,'Physiographic maps for the CIS and Baltic States (CIS_BS), Mongolia, China and Taiwan Province of China. Between the three regions (China, Mongolia, and CIS_BS countries) DCW boundaries were introduced. There are no DCW boundaries between Russian Federation and the rest of the new countries of the CIS_BS. The original physiographic map of China includes the Chinese border between India and China, which extends beyond the Indian border line, and the South China Sea islands (no physiographic information is present for islands in the South China Sea). The use of these country boundaries does not imply the expression of any opinion whatsoever on the part of FAO concerning the legal or constitutional states of any country, territory, or sea area, or concerning delimitation of frontiers. The Maps visualize the items LANDF, HYPSO, SLOPE that correspond to Landform, Hypsometry and Slope.');

INSERT INTO isoqp_abstract VALUES (3,3,'Major hydrological basins and their sub-basins. This dataset divides the Europe in zones.');

INSERT INTO isoqp_abstract VALUES (4,4,'A harmonised database of natural ecosystems in the Circumpolar Arctic, based on published vegetation maps.');

INSERT INTO isoqp_abstract VALUES (5,5,'Contains information on animals and plants threatened at the global level.');

INSERT INTO isoqp_abstract VALUES (6,6,'should be the description');


--Type
INSERT INTO isoqp_type VALUES (1,4,'Maps and graphics');
INSERT INTO isoqp_type VALUES (2,6,'service');

--rights
--INSERT INTO dcqp_rights VALUES (1,2,'copyright');
--INSERT INTO dcqp_rights VALUES (2,5,'None');
--INSERT INTO dcqp_rights VALUES (3,5,'No restrictions');
--INSERT INTO dcqp_rights VALUES (4,6,'GPL');

--BBox
--speciality!!! something with CRS 
-- Envelope --> lon lat, lon lat
INSERT INTO isoqp_boundingbox VALUES (1,1,SetSRID('BOX3D(51.1 -34.6,-17.3 38.2)'::box3d,4326));
INSERT INTO isoqp_boundingbox VALUES (2,2,SetSRID('BOX3D(156 -3,-37 83)'::box3d,4326));
INSERT INTO isoqp_boundingbox VALUES (3,3,SetSRID('BOX3D(51.1 -34.6,-17.3 38.2)'::box3d,4326));
INSERT INTO isoqp_boundingbox VALUES (4,4,SetSRID('BOX3D(-180 -90,180 90)'::box3d,4326));
INSERT INTO isoqp_boundingbox VALUES (5,5,SetSRID('BOX3D(180.0 -90,-180.0 90)'::box3d,4326));
INSERT INTO isoqp_boundingbox VALUES (6,6,SetSRID('BOX3D(-180.0 -90.0,180 90.0)'::box3d,4326));


--INSERT INTO isoqp_boundingbox VALUES (6,6,GeometryFromText('BOX3D(-180.0 -90.0,180 90.0)'::box3d,4326));


--recordBrief

INSERT INTO recordbrief VALUES (1,1,1,'<csw:BriefRecord xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" xmlns:ows="http://www.opengis.net/ows" xmlns:geonet="http://www.fao.org/geonetwork" xmlns:dc="http://purl.org/dc/elements/1.1/"><dc:identifier>e4076086-cfe6-42ae-a6f7-b4d9ead9cbf5</dc:identifier><dc:title>Hydrological Basins in Africa (Sample record, please remove!)</dc:title><ows:BoundingBox crs="::WGS 1984"><ows:LowerCorner>51.1 -34.6</ows:LowerCorner><ows:UpperCorner>-17.3 38.2</ows:UpperCorner></ows:BoundingBox></csw:BriefRecord>');

INSERT INTO recordbrief VALUES (2,2,1,'<csw:BriefRecord xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" xmlns:ows="http://www.opengis.net/ows" xmlns:geonet="http://www.fao.org/geonetwork" xmlns:dc="http://purl.org/dc/elements/1.1/"><dc:identifier>a5b42291-8425-40c5-a09c-26152fe2af22</dc:identifier><dc:title>Physiographic Map of North and Central Eurasia (Sample record, please remove!)</dc:title><ows:BoundingBox crs="::Lambert Azimuthal Projection"><ows:LowerCorner>156 -3</ows:LowerCorner><ows:UpperCorner>37 83</ows:UpperCorner></ows:BoundingBox></csw:BriefRecord>');

INSERT INTO recordbrief VALUES (3,3,1,'<csw:BriefRecord xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" xmlns:ows="http://www.opengis.net/ows" xmlns:geonet="http://www.fao.org/geonetwork" xmlns:dc="http://purl.org/dc/elements/1.1/"><dc:identifier>ddc55084-d67f-45a9-8665-0b63f8aaf1b0</dc:identifier><dc:title>Hydrological basins in Europe</dc:title><ows:BoundingBox crs="::"><ows:LowerCorner>51.1 -34.6</ows:LowerCorner><ows:UpperCorner>-17.3 38.2</ows:UpperCorner></ows:BoundingBox></csw:BriefRecord>');

INSERT INTO recordbrief VALUES (4,4,1,'<csw:BriefRecord xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:geonet="http://www.fao.org/geonetwork"><dc:identifier>6387e01d-bb6d-4876-96d5-f5f0e07dc345</dc:identifier><dc:title>Natural polar ecosystems</dc:title><dc:type>Maps and graphics</dc:type></csw:BriefRecord>');

INSERT INTO recordbrief VALUES (5,5,1,'<csw:BriefRecord xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dct="http://purl.org/dc/terms/" xmlns:geonet="http://www.fao.org/geonetwork"><dc:title>Globally threatened species of the world</dc:title></csw:BriefRecord>');

INSERT INTO recordbrief VALUES (6,6,1,'<csw:BriefRecord xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:geonet="http://www.fao.org/geonetwork"><dc:identifier>9b41ce80-1cfe-403e-8514-a072a8c8394e</dc:identifier><dc:title>Template for Dublin Core</dc:title><dc:type>service</dc:type></csw:BriefRecord>');





--recordSummary

INSERT INTO recordsummary VALUES (1,1,1,'<csw:SummaryRecord xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" xmlns:geonet="http://www.fao.org/geonetwork" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dct="http://purl.org/dc/terms/"><dc:identifier>e4076086-cfe6-42ae-a6f7-b4d9ead9cbf5</dc:identifier><dc:title>Hydrological Basins in Africa (Sample record, please remove!)</dc:title><dc:subject>watersheds</dc:subject><dc:subject>river basins</dc:subject><dc:subject>water resources</dc:subject><dc:subject>hydrology</dc:subject><dc:subject>AQUASTAT</dc:subject><dc:subject>AWRD</dc:subject><dc:subject>Africa</dc:subject><dc:subject>inlandWaters</dc:subject><dc:format>ShapeFile</dc:format><dct:abstract>Major hydrological basins and their sub-basins. This dataset divides the African continent according to its hydrological characteristics.
The dataset consists of the following information:- numerical code and name of the major basin (MAJ_BAS and MAJ_NAME); - area of the major basin in square km (MAJ_AREA); - numerical code and name of the sub-basin (SUB_BAS and SUB_NAME); - area of the sub-basin in square km (SUB_AREA); - numerical code of the sub-basin towards which the sub-basin flows (TO_SUBBAS) (the codes -888 and -999 have been assigned respectively to internal sub-basins and to sub-basins draining into the sea)</dct:abstract></csw:SummaryRecord>');

INSERT INTO recordsummary VALUES (2,2,1,'<csw:SummaryRecord xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" xmlns:geonet="http://www.fao.org/geonetwork" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dct="http://purl.org/dc/terms/"><dc:identifier>a5b42291-8425-40c5-a09c-26152fe2af22</dc:identifier><dc:title>Physiographic Map of North and Central Eurasia (Sample record, please remove!)</dc:title><dc:subject>physiography, soil</dc:subject><dc:subject>Eurasia</dc:subject><dc:subject>geoscientificInformation</dc:subject><dct:abstract>Physiographic maps for the CIS and Baltic States (CIS_BS), Mongolia, China and Taiwan Province of China. Between the three regions (China, Mongolia, and CIS_BS countries) DCW boundaries were introduced. There are no DCW boundaries between Russian Federation and the rest of the new countries of the CIS_BS. The original physiographic map of China includes the Chinese border between India and China, which extends beyond the Indian border line, and the South China Sea islands (no physiographic information is present for islands in the South China Sea). The use of these country boundaries does not imply the expression of any opinion whatsoever on the part of FAO concerning the legal or constitutional states of any country, territory, or sea area, or concerning delimitation of frontiers. The Maps visualize the items LANDF, HYPSO, SLOPE that correspond to Landform, Hypsometry and Slope.</dct:abstract></csw:SummaryRecord>');

INSERT INTO recordsummary VALUES (3,3,1,'<csw:SummaryRecord xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" xmlns:geonet="http://www.fao.org/geonetwork" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dct="http://purl.org/dc/terms/"><dc:identifier>ddc55084-d67f-45a9-8665-0b63f8aaf1b0</dc:identifier><dc:title>Hydrological basins in Europe</dc:title><dc:subject>watersheds</dc:subject><dc:subject>Europe</dc:subject><dc:subject>geoscientificInformation</dc:subject><dct:abstract>Major hydrological basins and their sub-basins. This dataset divides the Europe in zones.</dct:abstract></csw:SummaryRecord>');

INSERT INTO recordsummary VALUES (4,4,1,'<csw:SummaryRecord xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dct="http://purl.org/dc/terms/" xmlns:geonet="http://www.fao.org/geonetwork"><dc:identifier>6387e01d-bb6d-4876-96d5-f5f0e07dc345</dc:identifier><dc:title>Natural polar ecosystems</dc:title><dc:type>Maps and graphics</dc:type><dc:subject>Antarctic ecosystem</dc:subject><dc:subject>Arctic ecosystem</dc:subject><dc:subject>polar ecosystem</dc:subject><dc:format>Web page</dc:format><dc:relation>Unknown</dc:relation><dct:modified>2005-03-31</dct:modified><dc:creator>GeoNetwork test user</dc:creator><dc:contributor>Unknown</dc:contributor><dc:publisher>GeoNetwork test centre</dc:publisher><dc:source>Compiled from published vegetation maps of the 8 circumpolar countries.</dc:source><dc:language>en</dc:language><dc:rights>Access constraints: None.  Usage constraints: No restrictions</dc:rights></csw:SummaryRecord>');

INSERT INTO recordsummary VALUES (5,5,1,'<csw:SummaryRecord xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dct="http://purl.org/dc/terms/" xmlns:geonet="http://www.fao.org/geonetwork"><dc:title>Globally threatened species of the world</dc:title><dc:subject>GEMET 2000 biodiversity endangered animal species endangered plant species</dc:subject><dc:subject>None Biology</dc:subject><dc:subject>None Global</dc:subject><dct:abstract>Contains information on animals and plants threatened at the global level.</dct:abstract><dct:modified>20011220</dct:modified></csw:SummaryRecord>');

INSERT INTO recordsummary VALUES (6,6,1,'<csw:SummaryRecord xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dct="http://purl.org/dc/terms/" xmlns:geonet="http://www.fao.org/geonetwork" xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"><dc:identifier>9b41ce80-1cfe-403e-8514-a072a8c8394e</dc:identifier><dc:title>Template for Dublin Core</dc:title><dc:type>service</dc:type><dc:subject>DC</dc:subject><dc:format>anything of the format</dc:format><dc:relation>localhost</dc:relation><dct:modified>2009-10-06</dct:modified><dc:creator>Thomas</dc:creator><dc:contributor>company</dc:contributor><dc:publisher>any man</dc:publisher><dc:source>should be the source</dc:source><dc:language>german</dc:language><dc:rights>GPL</dc:rights></csw:SummaryRecord>');


--recordFull

INSERT INTO recordfull VALUES (1,1,1,'<csw:Record xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" xmlns:geonet="http://www.fao.org/geonetwork" xmlns:ows="http://www.opengis.net/ows" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dct="http://purl.org/dc/terms/"><dc:identifier>e4076086-cfe6-42ae-a6f7-b4d9ead9cbf5</dc:identifier><dc:title>Hydrological Basins in Africa (Sample record, please remove!)</dc:title><dc:subject>watersheds</dc:subject><dc:subject>river basins</dc:subject><dc:subject>water resources</dc:subject><dc:subject>hydrology</dc:subject><dc:subject>AQUASTAT</dc:subject><dc:subject>AWRD</dc:subject><dc:subject>Africa</dc:subject><dc:format>ShapeFile</dc:format><dct:abstract>Major hydrological basins and their sub-basins. This dataset divides the African continent according to its hydrological characteristics. The dataset consists of the following information:- numerical code and name of the major basin (MAJ_BAS and MAJ_NAME); - area of the major basin in square km (MAJ_AREA); - numerical code and name of the sub-basin (SUB_BAS and SUB_NAME); - area of the sub-basin in square km (SUB_AREA); - numerical code of the sub-basin towards which the sub-basin flows (TO_SUBBAS) (the codes -888 and -999 have been assigned respectively to internal sub-basins and to sub-basins draining into the sea)</dct:abstract><dc:language>eng</dc:language><dc:source>The linework of the map is obtained by delineating drainage basin boundaries from an hydrologically corrected digital elevation model with a resolution of 1 * 1 km.</dc:source><dc:format>ShapeFile</dc:format><ows:BoundingBox crs="urn:ogc:def:crs:::WGS 1984"><ows:LowerCorner>51.1 -34.6</ows:LowerCorner><ows:UpperCorner>-17.3 38.2</ows:UpperCorner></ows:BoundingBox></csw:Record>');

INSERT INTO recordfull VALUES (2,2,1,'<csw:Record xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" xmlns:geonet="http://www.fao.org/geonetwork" xmlns:ows="http://www.opengis.net/ows" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dct="http://purl.org/dc/terms/"><dc:identifier>a5b42291-8425-40c5-a09c-26152fe2af22</dc:identifier><dc:title>Physiographic Map of North and Central Eurasia (Sample record, please remove!)</dc:title><dc:subject>physiography, soil</dc:subject><dc:subject>Eurasia</dc:subject><dct:abstract>Physiographic maps for the CIS and Baltic States (CIS_BS), Mongolia, China and Taiwan Province of China. Between the three regions (China, Mongolia, and CIS_BS countries) DCW boundaries were introduced. There are no DCW boundaries between Russian Federation and the rest of the new countries of the CIS_BS. The original physiographic map of China includes the Chinese border between India and China, which extends beyond the Indian border line, and the South China Sea islands (no physiographic information is present for islands in the South China Sea). The use of these country boundaries does not imply the expression of any opinion whatsoever on the part of FAO concerning the legal or constitutional states of any country, territory, or sea area, or concerning delimitation of frontiers. The Maps visualize the items LANDF, HYPSO, SLOPE that correspond to Landform, Hypsometry and Slope.</dct:abstract><dc:rights>copyright</dc:rights><dc:rights>copyright</dc:rights><dc:language /><dc:source /><ows:BoundingBox crs="urn:ogc:def:crs:::Lambert Azimuthal Projection"><ows:LowerCorner>156 -3</ows:LowerCorner><ows:UpperCorner>37 83</ows:UpperCorner></ows:BoundingBox></csw:Record>');

INSERT INTO recordfull VALUES (3,3,1,'<csw:Record xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" xmlns:geonet="http://www.fao.org/geonetwork" xmlns:ows="http://www.opengis.net/ows" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dct="http://purl.org/dc/terms/"><dc:title xmlns:dct="http://purl.org/dc/terms/">Natural polar ecosystems</dc:title><dc:creator xmlns:dct="http://purl.org/dc/terms/">GeoNetwork test user</dc:creator><dc:subject xmlns:dct="http://purl.org/dc/terms/">Antarctic ecosystem</dc:subject><dc:subject xmlns:dct="http://purl.org/dc/terms/">Arctic ecosystem</dc:subject><dc:subject xmlns:dct="http://purl.org/dc/terms/">polar ecosystem</dc:subject><dc:description xmlns:dct="http://purl.org/dc/terms/">A harmonised database of natural ecosystems in the Circumpolar Arctic, based on published vegetation maps.</dc:description><dc:publisher xmlns:dct="http://purl.org/dc/terms/">GeoNetwork test centre</dc:publisher><dc:contributor xmlns:dct="http://purl.org/dc/terms/">Unknown</dc:contributor><dc:date xmlns:dct="http://purl.org/dc/terms/">2000</dc:date><dc:type xmlns:dct="http://purl.org/dc/terms/">Maps and graphics</dc:type><dc:format xmlns:dct="http://purl.org/dc/terms/">Web page</dc:format><dc:identifier xmlns:dct="http://purl.org/dc/terms/">6387e01d-bb6d-4876-96d5-f5f0e07dc345</dc:identifier><dc:source xmlns:dct="http://purl.org/dc/terms/">Compiled from published vegetation maps of the 8 circumpolar countries.</dc:source><dc:language xmlns:dct="http://purl.org/dc/terms/">en</dc:language><dc:relation xmlns:dct="http://purl.org/dc/terms/">Unknown</dc:relation><dc:coverage xmlns:dct="http://purl.org/dc/terms/">North 90, South -90, East 180, West -180.  (Global)</dc:coverage><dc:rights xmlns:dct="http://purl.org/dc/terms/">Access constraints: None.  Usage constraints: No restrictions</dc:rights><dct:modified xmlns:dct="http://purl.org/dc/terms/">2005-03-31T19:13:30</dct:modified><ows:BoundingBox crs="urn:x-ogc:def:crs:EPSG:6.11:4326"><ows:LowerCorner>-90 -180</ows:LowerCorner><ows:UpperCorner>90 180</ows:UpperCorner></ows:BoundingBox></csw:Record>');

INSERT INTO recordfull VALUES (4,4,1,'<csw:Record xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" xmlns:dct="http://purl.org/dc/terms/" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:ows="http://www.opengis.net/ows" xmlns:geonet="http://www.fao.org/geonetwork"><dc:identifier>ddc55084-d67f-45a9-8665-0b63f8aaf1b0</dc:identifier><dc:title>Hydrological basins in Europe</dc:title><dc:subject>watersheds</dc:subject><dc:subject>Europe</dc:subject><dct:abstract>Major hydrological basins and their sub-basins. This dataset divides the Europe in zones.</dct:abstract><dc:language>en</dc:language><dc:source>None.</dc:source><ows:BoundingBox crs="urn:ogc:def:crs:EPSG:6.6:4326"><ows:LowerCorner>51.1 -34.6</ows:LowerCorner><ows:UpperCorner>-17.3 38.2</ows:UpperCorner></ows:BoundingBox></csw:Record>');

INSERT INTO recordfull VALUES (5,5,1,'<csw:Record xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dct="http://purl.org/dc/terms/" xmlns:ows="http://www.opengis.net/ows" xmlns:geonet="http://www.fao.org/geonetwork"><dc:title>Globally threatened species of the world</dc:title><dc:subject>GEMET 2000 biodiversity endangered animal species endangered plant species</dc:subject><dc:subject>None Biology</dc:subject><dc:subject>None Global</dc:subject><dct:abstract>Contains information on animals and plants threatened at the global level.</dct:abstract><dct:modified>20011220</dct:modified><dc:creator>GeoNetwork test user</dc:creator><dc:publisher>GeoNetwork test centre</dc:publisher><dc:rights>None</dc:rights><dc:rights>No restrictions</dc:rights><ows:BoundingBox crs=""><ows:LowerCorner>180.0 -90.0</ows:LowerCorner><ows:UpperCorner>-180.0 90.0</ows:UpperCorner></ows:BoundingBox></csw:Record>');

INSERT INTO recordfull VALUES (6,6,1,'<csw:Record xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:ows="http://www.opengis.net/ows" xmlns:geonet="http://www.fao.org/geonetwork"><dc:title xmlns:dct="http://purl.org/dc/terms/">Template for Dublin Core</dc:title><dc:creator xmlns:dct="http://purl.org/dc/terms/">Thomas</dc:creator><dc:subject xmlns:dct="http://purl.org/dc/terms/">DC</dc:subject><dc:description xmlns:dct="http://purl.org/dc/terms/">should be the description</dc:description><dc:publisher xmlns:dct="http://purl.org/dc/terms/">any man</dc:publisher><dc:contributor xmlns:dct="http://purl.org/dc/terms/">company</dc:contributor><dc:date xmlns:dct="http://purl.org/dc/terms/">6.10.2009</dc:date><dc:type xmlns:dct="http://purl.org/dc/terms/">service</dc:type><dc:format xmlns:dct="http://purl.org/dc/terms/">anything of the format</dc:format><dc:identifier xmlns:dct="http://purl.org/dc/terms/">9b41ce80-1cfe-403e-8514-a072a8c8394e</dc:identifier><dc:source xmlns:dct="http://purl.org/dc/terms/">should be the source</dc:source><dc:language xmlns:dct="http://purl.org/dc/terms/">german</dc:language><dc:relation xmlns:dct="http://purl.org/dc/terms/">localhost</dc:relation><dc:coverage xmlns:dct="http://purl.org/dc/terms/">North 90, South -90, East 180, West -180. Global</dc:coverage><dc:rights xmlns:dct="http://purl.org/dc/terms/">GPL</dc:rights><dct:modified xmlns:dct="http://purl.org/dc/terms/">2009-10-06T09:44:50</dct:modified><ows:BoundingBox crs="urn:x-ogc:def:crs:EPSG:6.11:4326"><ows:LowerCorner>-90 -180</ows:LowerCorner><ows:UpperCorner>90 180</ows:UpperCorner></ows:BoundingBox></csw:Record>');


