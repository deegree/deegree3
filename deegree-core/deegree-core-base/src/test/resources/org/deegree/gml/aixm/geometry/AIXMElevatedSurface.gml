<?xml version="1.0" encoding="UTF-8"?>
<ElevatedSurface gml:id="S1" xmlns="http://www.aixm.aero/schema/5.1" xmlns:gml="http://www.opengis.net/gml/3.2"
  xmlns:xlink="http://www.w3.org/1999/xlink" srsName="EPSG:4326" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://www.aixm.aero/schema/5.1 ../../../../gml/schema/aixm/AIXM_Features.xsd">
  <gml:metaDataProperty>
    <gml:GenericMetaData>Example for metadata: Ce surface ne pas une GML surface, c'est une AIXM surface.</gml:GenericMetaData>
  </gml:metaDataProperty>
  <gml:description>This is just for testing the parsing of standard GML properties.</gml:description>
  <gml:descriptionReference xlink:href="http://www.aixm.org/whatever" />
  <gml:identifier codeSpace="urn:blabla:bla">XYZ</gml:identifier>
  <gml:name>Surface S1</gml:name>
  <gml:name>S1</gml:name>
  <gml:patches>
    <gml:PolygonPatch>
      <gml:exterior>
        <gml:LinearRing>
          <gml:posList>2 0 0 2 -2 0 -4 2 -6 0 0 10 2 0</gml:posList>
        </gml:LinearRing>
      </gml:exterior>
      <gml:interior>
        <gml:LinearRing>
          <gml:posList>2 0 0 2 -2 0 -4 2 -6 0 0 10 2 0</gml:posList>
        </gml:LinearRing>
      </gml:interior>
      <gml:interior>
        <gml:LinearRing>
          <gml:posList>2 0 0 2 -2 0 -4 2 -6 0 0 10 2 0</gml:posList>
        </gml:LinearRing>
      </gml:interior>
    </gml:PolygonPatch>
    <gml:PolygonPatch>
      <gml:exterior>
        <gml:LinearRing>
          <gml:posList>2 0 0 2 -2 0 -4 2 -6 0 0 10 2 0</gml:posList>
        </gml:LinearRing>
      </gml:exterior>
      <gml:interior>
        <gml:LinearRing>
          <gml:posList>2 0 0 2 -2 0 -4 2 -6 0 0 10 2 0</gml:posList>
        </gml:LinearRing>
      </gml:interior>
      <gml:interior>
        <gml:LinearRing>
          <gml:posList>2 0 0 2 -2 0 -4 2 -6 0 0 10 2 0</gml:posList>
        </gml:LinearRing>
      </gml:interior>
    </gml:PolygonPatch>
  </gml:patches>
  <horizontalAccuracy uom="M">1.0</horizontalAccuracy>
  <annotation xsi:nil="true" nilReason="inapplicable" />
  <elevation uom="M">47.11</elevation>
  <geoidUndulation uom="M">3.22</geoidUndulation>
  <verticalDatum>NAVD88</verticalDatum>
  <verticalAccuracy uom="M">2.0</verticalAccuracy>  
</ElevatedSurface>
